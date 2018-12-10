/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link IFilterContext} mock for unit tests.
 */
public class FilterContextMock implements IFilterContext {

	public String className = "Foo";
	public String superClassName = "java/lang/Object";
	public Set<String> classAnnotations = new HashSet<String>();
	public String sourceFileName = "Foo.java";
	public String sourceDebugExtension;

	public String getClassName() {
		return className;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public Set<String> getClassAnnotations() {
		return classAnnotations;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public String getSourceDebugExtension() {
		return sourceDebugExtension;
	}

}
