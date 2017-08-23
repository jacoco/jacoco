/*
 * Copyright 2011 gitblit.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jacoco.maven.util;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;

import java.io.Serializable;
import java.util.Date;

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
 * RefModel is a serializable model class that represents a tag or branch and
 * includes the referenced object.
 *
 * @author James Moger
 *
 */
public class RefModel implements Serializable, Comparable<RefModel> {

	private static final long serialVersionUID = 1L;
	public final String displayName;
	public final RevObject referencedObject;
	public transient Ref reference;

	public RefModel(String displayName, Ref ref, RevObject refObject) {
		this.displayName = displayName;
		this.reference = ref;
		this.referencedObject = refObject;
	}

	public Date getDate() {
		Date date = new Date(0);
		if (referencedObject != null) {
			if (referencedObject instanceof RevTag) {
				RevTag tag = (RevTag) referencedObject;
				PersonIdent tagger = tag.getTaggerIdent();
				if (tagger != null) {
					date = tagger.getWhen();
				}
			} else if (referencedObject instanceof RevCommit) {
				RevCommit commit = (RevCommit) referencedObject;
				date = JGitUtils.getAuthorDate(commit);
			}
		}
		return date;
	}

	public String getName() {
		if (reference == null) {
			return displayName;
		}
		return reference.getName();
	}

	public int getReferencedObjectType() {
		int type = referencedObject.getType();
		if (referencedObject instanceof RevTag) {
			type = ((RevTag) referencedObject).getObject().getType();
		}
		return type;
	}

	public ObjectId getReferencedObjectId() {
		if (referencedObject instanceof RevTag) {
			return ((RevTag) referencedObject).getObject().getId();
		}
		return referencedObject.getId();
	}

	public String getShortMessage() {
		String message = "";
		if (referencedObject instanceof RevTag) {
			message = ((RevTag) referencedObject).getShortMessage();
		} else if (referencedObject instanceof RevCommit) {
			message = ((RevCommit) referencedObject).getShortMessage();
		}
		return message;
	}

	public String getFullMessage() {
		String message = "";
		if (referencedObject instanceof RevTag) {
			message = ((RevTag) referencedObject).getFullMessage();
		} else if (referencedObject instanceof RevCommit) {
			message = ((RevCommit) referencedObject).getFullMessage();
		}
		return message;
	}

	public PersonIdent getAuthorIdent() {
		if (referencedObject instanceof RevTag) {
			return ((RevTag) referencedObject).getTaggerIdent();
		} else if (referencedObject instanceof RevCommit) {
			return ((RevCommit) referencedObject).getAuthorIdent();
		}
		return null;
	}

	public ObjectId getObjectId() {
		return reference.getObjectId();
	}

	public boolean isAnnotatedTag() {
		if (referencedObject instanceof RevTag) {
			return !getReferencedObjectId().equals(getObjectId());
		}
		return reference.getPeeledObjectId() != null;
	}

	@Override
	public int hashCode() {
		return getReferencedObjectId().hashCode() + getName().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof RefModel) {
			RefModel other = (RefModel) o;
			return getName().equals(other.getName());
		}
		return super.equals(o);
	}

	@Override
	public int compareTo(RefModel o) {
		return getDate().compareTo(o.getDate());
	}

	@Override
	public String toString() {
		return displayName;
	}
}