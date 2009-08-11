/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
package org.jacoco.report;

import org.objectweb.asm.Type;

/**
 * Names for the Java language.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class JavaNames implements ILanguageNames {

	public String getPackageName(final String vmname) {
		if (vmname.length() == 0) {
			return "default";
		}
		return vmname.replace('/', '.');
	}

	public String getClassName(final String vmname) {
		final int pos = vmname.lastIndexOf('/');
		final String name = pos == -1 ? vmname : vmname.substring(pos + 1);
		return name.replace('$', '.');
	}

	public String getMethodName(final String vmclassname,
			final String vmmethodname, final String vmdesc) {
		if (vmmethodname.equals("<cinit>")) {
			return "static {...}";
		}
		final Type[] arguments = Type.getArgumentTypes(vmdesc);
		final StringBuilder result = new StringBuilder();
		if (vmmethodname.equals("<init>")) {
			result.append(getClassName(vmclassname));
		} else {
			result.append(vmmethodname);
		}
		result.append('(');
		boolean colon = false;
		for (final Type arg : arguments) {
			if (colon) {
				result.append(", ");
			}
			result.append(getShortTypeName(arg));
			colon = true;
		}
		result.append(')');
		return result.toString();
	}

	private String getShortTypeName(final Type type) {
		final String name = type.getClassName();
		final int pos = name.lastIndexOf('.');
		final String shortName = pos == -1 ? name : name.substring(pos + 1);
		return shortName.replace('$', '.');
	}

}
