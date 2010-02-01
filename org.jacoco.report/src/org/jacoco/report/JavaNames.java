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

	private String getClassName(final String vmname) {
		final int pos = vmname.lastIndexOf('/');
		final String name = pos == -1 ? vmname : vmname.substring(pos + 1);
		return name.replace('$', '.');
	}

	private boolean isAnonymous(final String vmname) {
		// assume non-identifier start character for anonymous classes
		final char start = vmname.charAt(vmname.lastIndexOf('$') + 1);
		return start > 0 && !Character.isJavaIdentifierStart(start);
	}

	public String getClassName(final String vmname, final String vmsignature,
			final String vmsuperclass, final String[] vminterfaces) {
		if (isAnonymous(vmname)) {
			final String vmsupertype;
			if (vminterfaces != null && vminterfaces.length > 0) {
				vmsupertype = vminterfaces[0];
			} else if (vmsuperclass != null) {
				vmsupertype = vmsuperclass;
			} else {
				vmsupertype = null;
			}
			// Append Eclipse style label, e.g. "Foo.1: new Bar() {...}"
			if (vmsupertype != null) {
				final StringBuilder builder = new StringBuilder();
				final String vmenclosing = vmname.substring(0, vmname
						.lastIndexOf('$'));
				builder.append(getClassName(vmenclosing)).append(".new ")
						.append(getClassName(vmsupertype)).append("() {...}");
				return builder.toString();
			}
		}
		return getClassName(vmname);
	}

	public String getMethodName(final String vmclassname,
			final String vmmethodname, final String vmdesc,
			final String vmsignature) {
		if (vmmethodname.equals("<clinit>")) {
			return "static {...}";
		}
		final StringBuilder result = new StringBuilder();
		if (vmmethodname.equals("<init>")) {
			if (isAnonymous(vmclassname)) {
				return "{...}";
			} else {
				result.append(getClassName(vmclassname));
			}
		} else {
			result.append(vmmethodname);
		}
		result.append('(');
		final Type[] arguments = Type.getArgumentTypes(vmdesc);
		boolean comma = false;
		for (final Type arg : arguments) {
			if (comma) {
				result.append(", ");
			} else {
				comma = true;
			}
			result.append(getShortTypeName(arg));
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
