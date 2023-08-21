/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lars Grefer - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * Helper methods for working with AspectJ.
 */
public class AspectJUtil {

	private static final Pattern ASPECTJ_AROUND_BODY_PATTERN = Pattern
			.compile(".*_aroundBody\\d+");

	/**
	 * Checks if the class has an attribute indicating it has been touched by
	 * ajc.
	 */
	public static boolean isAspectJClass(IFilterContext context) {

		for (String classAttribute : context.getClassAttributes()) {
			if (classAttribute.startsWith("org.aspectj.weaver.")) {
				return true;
			}
		}

		return false;
	}

	public static boolean isAspectJAroundBody(MethodNode methodNode) {
		return ASPECTJ_AROUND_BODY_PATTERN.matcher(methodNode.name).matches()
				&& methodNode.desc.contains("Lorg/aspectj/lang/JoinPoint");
	}

	private static Field contentField;

	/**
	 * Gets the line number of the method declaration from AJC's
	 * {@code org.aspectj.weaver.MethodDeclarationLineNumber} attribute.
	 */
	public static int getMethodDeclarationLineNumber(MethodNode methodNode) {

		if (methodNode.attrs == null) {
			return -1;
		}

		for (Attribute attr : methodNode.attrs) {
			if ("org.aspectj.weaver.MethodDeclarationLineNumber"
					.equals(attr.type)) {

				byte[] content;

				try {
					if (contentField == null) {
						contentField = Attribute.class
								.getDeclaredField("content");
						contentField.setAccessible(true);
					}
					content = (byte[]) contentField.get(attr);
				} catch (NoSuchFieldException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				return intFromByteArray(content);
			}
		}

		return -1;
	}

	static int intFromByteArray(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
				| ((bytes[2] & 0xFF) << 8) | ((bytes[3] & 0xFF) << 0);
	}

}
