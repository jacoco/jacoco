/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mads Mohr Christensen - implementation of MergeMojo
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.jacoco.core.data.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Mojo for merging a set of execution data files (*.exec) into a single file
 *
 * @phase generate-resources
 * @goal merge
 * @requiresProject true
 * @threadSafe
 */
public class MergeMojo extends AbstractJacocoMojo {

    private static final String MSG_SKIPPING = "Skipping JaCoCo merge execution due to missing execution data files";

    /**
     * Path to the output file for execution data.
     *
     * @parameter expression="${jacoco.destFile}"
     * default-value="${project.build.directory}/jacoco.exec"
     */
    private File destFile;

    /**
     * This mojo accepts any number of execution data file sets.
     *
     * @parameter expression="${jacoco.fileSets}"
     * @required
     */
    private List<FileSet> fileSets;

    @Override
    protected void executeMojo() throws MojoExecutionException, MojoFailureException {
        if (!canMergeReports()) {
            return;
        }
        executeMerge();
    }

    private boolean canMergeReports() {
        if (fileSets == null || fileSets.isEmpty()) {
            getLog().info(MSG_SKIPPING);
            return false;
        }
        return true;
    }

    private void executeMerge() throws MojoExecutionException {
        final ExecFileLoader loader = new ExecFileLoader();

        load(loader);
        save(loader);
    }

    private void load(final ExecFileLoader loader) throws MojoExecutionException {
        final FileSetManager fileSetManager = new FileSetManager(getLog());
        for (FileSet fileSet : fileSets) {
            for (String includedFilename : fileSetManager.getIncludedFiles(fileSet)) {
                final File inputFile = new File(fileSet.getDirectory(), includedFilename);
                if (inputFile.isDirectory()) {
                    continue;
                }
                try {
                    getLog().info("Loading execution data file " + inputFile.getAbsolutePath());
                    loader.load(inputFile);
                } catch (IOException e) {
                    throw new MojoExecutionException("Unable to read " + inputFile.getAbsolutePath(), e);
                }
            }
        }
    }

    private void save(final ExecFileLoader loader) throws MojoExecutionException {
        if (loader.getExecutionDataStore().getContents().isEmpty()) {
            getLog().info(MSG_SKIPPING);
            return;
        }
        getLog().info("Writing merged execution data to " + destFile.getAbsolutePath());
        try {
            File parent = destFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            destFile.createNewFile();
            loader.save(destFile, false);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write merged file " + destFile.getAbsolutePath(), e);
        }
    }

}
