/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.data;

/**
 * Interface for data output of the internal structure of a single class. This
 * interface is meant to be implemented by parties that want to retrieve data
 * from the instrumentation process.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface IClassStructureVisitor {

	/**
	 * Called once to report the class name, signature, superclass name and
	 * names of implemented/extended interfaces.
	 * 
	 * @param name
	 *            VM name of the class
	 * @param signature
	 *            VM signature of the class
	 * @param superName
	 *            VM name of the super class
	 * @param interfaces
	 *            VM names of extended/implemented interfaces
	 */
	public void visit(String name, String signature, String superName,
			String[] interfaces);

	/**
	 * The source file name might be reported through this method call.
	 * 
	 * @param name
	 *            name of the corresponding source file
	 */
	public void visitSourceFile(String name);

	/**
	 * Called for every instrumented method.
	 * 
	 * @param name
	 *            name of the method
	 * @param desc
	 *            parameter and return value description
	 * @param signature
	 *            generic signature or <code>null</code>
	 * @return call-back for structure details about the method
	 */
	public IMethodStructureVisitor visitMethodStructure(String name,
			String desc, String signature);

	/**
	 * Signals the end of this class structure.
	 */
	public void visitEnd();

}
