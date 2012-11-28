/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Hare Robertson - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filters;

import org.jacoco.core.internal.analysis.filters.ICoverageFilterStatus.ICoverageFilter;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Turn coverage off for implicit Enum methods values() and valueOf()
 */
public class ImplicitEnumMethodsCoverageFilter implements ICoverageFilter {

	private boolean enabled = true;
	private boolean isEnum = false;
	private String className;

	public boolean enabled() {
		return enabled;
	}

	public boolean includeClass(final String className) {
		this.className = className;
		return true;
	}

	public ClassVisitor visitClass(final ClassVisitor delegate) {
		return new EnumClassVisitor(delegate);
	}

	private class EnumClassVisitor extends ClassVisitor {
		private EnumClassVisitor(final ClassVisitor delegate) {
			super(Opcodes.ASM4, delegate);
		}

		@Override
		public void visit(final int version, final int access,
				final String name, final String signature,
				final String superName, final String[] interfaces) {
			isEnum = ((access | Opcodes.ACC_ENUM) != 0);
			super.visit(version, access, name, signature, superName, interfaces);
		}
	}

	public MethodVisitor preVisitMethod(final String name, final String desc,
			final MethodVisitor delegate) {
		return delegate;
	}

	public MethodProbesVisitor visitMethod(final String name,
			final String desc, final MethodProbesVisitor delegate) {
		if (isEnum) {
			if ("values".equals(name)) {
				if (("()[L" + className + ";").equals(desc)) {
					enabled = false;
				}
			} else if ("valueOf".equals(name)) {
				if (("(Ljava/lang/String;)L" + className + ";").equals(desc)) {
					enabled = false;
				}
			} else {
				enabled = true;
			}
		} else {
			enabled = true;
		}
		return delegate;
	}

}
