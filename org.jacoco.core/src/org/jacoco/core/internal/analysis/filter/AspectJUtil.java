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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;

public class AspectJUtil {

	public static boolean isAspectJClass(IFilterContext context) {

		for (String classAttribute : context.getClassAttributes()) {
			if (classAttribute.startsWith("org.aspectj.weaver.")) {
				return true;
			}
		}

		return false;
	}

	public static int getMethodDeclarationLineNumber(MethodNode methodNode) {

		if (methodNode.attrs == null) {
			return -1;
		}

		for (Attribute attr : methodNode.attrs) {
			if (attr.type
					.equals("org.aspectj.weaver.MethodDeclarationLineNumber")) {

				byte[] content;
				try {
					Field contentField = Attribute.class
							.getDeclaredField("content");
					contentField.setAccessible(true);
					content = (byte[]) contentField.get(attr);
					return fromByteArray(content);
				} catch (NoSuchFieldException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return -1;
	}

	static int fromByteArray(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
				| ((bytes[2] & 0xFF) << 8) | ((bytes[3] & 0xFF) << 0);
	}

}
