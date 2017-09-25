package org.jacoco.maven.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    James Moger - initial API and implementation
 *
 *******************************************************************************/
/**
 * Collection of static methods for retrieving information from a repository.
 *
 * @author James Moger
 *
 */
public class JGitUtils {

    static final Logger LOGGER = LoggerFactory.getLogger(JGitUtils.class);

    /**
     * Returns a list of commits since the minimum date starting from the
     * specified object id.
     *
     * @param repository
     * @param objectId
     *            if unspecified, HEAD is assumed.
     * @param minimumDate
     * @return list of commits
     */
    public static List<RevCommit> getRevLog(Repository repository, String objectId, Date minimumDate) {
        List<RevCommit> list = new ArrayList<RevCommit>();
        if (!hasCommits(repository)) {
            return list;
        }
        try {
            // resolve branch
            ObjectId branchObject;
            if (StringUtils.isEmpty(objectId)) {
                branchObject = getDefaultBranch(repository);
            } else {
                branchObject = repository.resolve(objectId);
            }

            RevWalk rw = new RevWalk(repository);
            rw.markStart(rw.parseCommit(branchObject));
            rw.setRevFilter(CommitTimeRevFilter.after(minimumDate));
            Iterable<RevCommit> revlog = rw;
            for (RevCommit rev : revlog) {
                list.add(rev);
            }
            rw.dispose();
        } catch (Throwable t) {
            error(t, repository, "{0} failed to get {1} revlog for minimum date {2}", objectId,
                    minimumDate);
        }
        return list;
    }

    /**
     * Determine if a repository has any commits. This is determined by checking
     * the for loose and packed objects.
     *
     * @param repository
     * @return true if the repository has commits
     */
    public static boolean hasCommits(Repository repository) {
        if (repository != null && repository.getDirectory().exists()) {
            return (new File(repository.getDirectory(), "objects").list().length > 2)
                    || (new File(repository.getDirectory(), "objects/pack").list().length > 0);
        }
        return false;
    }

    /**
     * Returns the default branch to use for a repository. Normally returns
     * whatever branch HEAD points to, but if HEAD points to nothing it returns
     * the most recently updated branch.
     *
     * @param repository
     * @return the objectid of a branch
     * @throws Exception
     */
    public static ObjectId getDefaultBranch(Repository repository) throws Exception {
        ObjectId object = repository.resolve(Constants.HEAD);
        if (object == null) {
            // no HEAD
            // perhaps non-standard repository, try local branches
            List<RefModel> branchModels = getLocalBranches(repository, true, -1);
            if (branchModels.size() > 0) {
                // use most recently updated branch
                RefModel branch = null;
                Date lastDate = new Date(0);
                for (RefModel branchModel : branchModels) {
                    if (branchModel.getDate().after(lastDate)) {
                        branch = branchModel;
                        lastDate = branch.getDate();
                    }
                }
                object = branch.getReferencedObjectId();
            }
        }
        return object;
    }

    /**
     * Returns the list of local branches in the repository. If repository does
     * not exist or is empty, an empty list is returned.
     *
     * @param repository
     * @param fullName
     *            if true, /refs/heads/yadayadayada is returned. If false,
     *            yadayadayada is returned.
     * @param maxCount
     *            if < 0, all local branches are returned
     * @return list of local branches
     */
    public static List<RefModel> getLocalBranches(Repository repository, boolean fullName,
                                                  int maxCount) {
        return getRefs(repository, Constants.R_HEADS, fullName, maxCount);
    }

    /**
     * Retrieves a Java Date from a Git commit.
     *
     * @param commit
     * @return date of the commit or Date(0) if the commit is null
     */
    public static Date getAuthorDate(RevCommit commit) {
        if (commit == null) {
            return new Date(0);
        }
        return commit.getAuthorIdent().getWhen();
    }

    /**
     * Returns a list of references in the repository matching "refs". If the
     * repository is null or empty, an empty list is returned.
     *
     * @param repository
     * @param refs
     *            if unspecified, all refs are returned
     * @param fullName
     *            if true, /refs/something/yadayadayada is returned. If false,
     *            yadayadayada is returned.
     * @param maxCount
     *            if < 0, all references are returned
     * @return list of references
     */
    private static List<RefModel> getRefs(Repository repository, String refs, boolean fullName,
                                          int maxCount) {
        List<RefModel> list = new ArrayList<RefModel>();
        if (maxCount == 0) {
            return list;
        }
        if (!hasCommits(repository)) {
            return list;
        }
        try {
            Map<String, Ref> map = repository.getRefDatabase().getRefs(refs);
            RevWalk rw = new RevWalk(repository);
            for (Map.Entry<String, Ref> entry : map.entrySet()) {
                Ref ref = entry.getValue();
                RevObject object = rw.parseAny(ref.getObjectId());
                String name = entry.getKey();
                if (fullName && !StringUtils.isEmpty(refs)) {
                    name = refs + name;
                }
                list.add(new RefModel(name, ref, object));
            }
            rw.dispose();
            Collections.sort(list);
            Collections.reverse(list);
            if (maxCount > 0 && list.size() > maxCount) {
                list = new ArrayList<RefModel>(list.subList(0, maxCount));
            }
        } catch (IOException e) {
            error(e, repository, "{0} failed to retrieve {1}", refs);
        }
        return list;
    }

    /**
     * Log an error message and exception.
     *
     * @param t
     * @param repository
     *            if repository is not null it MUST be the {0} parameter in the
     *            pattern.
     * @param pattern
     * @param objects
     */
    private static void error(Throwable t, Repository repository, String pattern, Object... objects) {
        List<Object> parameters = new ArrayList<Object>();
        if (objects != null && objects.length > 0) {
            for (Object o : objects) {
                parameters.add(o);
            }
        }
        if (repository != null) {
            parameters.add(0, repository.getDirectory().getAbsolutePath());
        }
        LOGGER.error(MessageFormat.format(pattern, parameters.toArray()), t);
    }

}
