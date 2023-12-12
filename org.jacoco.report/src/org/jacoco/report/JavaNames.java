/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report;

import org.objectweb.asm.Type;

/**
 * Names for the Java language.
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
		final int dollarPosition = vmname.lastIndexOf('$');
		if (dollarPosition == -1) {
			return false;
		}
		final int internalPosition = dollarPosition + 1;
		if (internalPosition == vmname.length()) {
			// shouldn't happen for classes compiled from Java source
			return false;
		}
		// assume non-identifier start character for anonymous classes
		final char start = vmname.charAt(internalPosition);
		return !Character.isJavaIdentifierStart(start);
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
			// append Eclipse style label, e.g. "Foo.new Bar() {...}"
			if (vmsupertype != null) {
				final StringBuilder builder = new StringBuilder();
				final String vmenclosing = vmname.substring(0,
						vmname.lastIndexOf('$'));
				builder.append(getClassName(vmenclosing)).append(".new ")
						.append(getClassName(vmsupertype)).append("() {...}");
				return builder.toString();
			}
		}
		return getClassName(vmname);
	}

	public String getQualifiedClassName(final String vmname) {
		return vmname.replace('/', '.').replace('$', '.');
	}

	public String getMethodName(final String vmclassname,
			final String vmmethodname, final String vmdesc,
			final String vmsignature) {
		return getMethodName(vmclassname, vmmethodname, vmdesc, false);
	}

	public String getQualifiedMethodName(final String vmclassname,
			final String vmmethodname, final String vmdesc,
			final String vmsignature) {
		return getQualifiedClassName(vmclassname) + "."
				+ getMethodName(vmclassname, vmmethodname, vmdesc, true);
	}

	private String getMethodName(final String vmclassname,
			final String vmmethodname, final String vmdesc,
			final boolean qualifiedParams) {
		if ("<clinit>".equals(vmmethodname)) {
			return "static {...}";
		}
		final StringBuilder result = new StringBuilder();
		if ("<init>".equals(vmmethodname)) {
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
			if (qualifiedParams) {
				result.append(getQualifiedClassName(arg.getClassName()));
			} else {
				result.append(getShortTypeName(arg));
			}
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
