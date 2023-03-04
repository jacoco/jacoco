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
package org.jacoco.core.test.validation;

import java.lang.reflect.InvocationTargetException;

/**
 * Executes statements against a given Java object instance.
 */
class StatementExecutor implements StatementParser.IStatementVisitor {

	private final Object target;
	private final Object[] prefixArgs;

	StatementExecutor(Object target, Object... prefixArgs) {
		this.target = target;
		this.prefixArgs = prefixArgs;
	}

	public void visitInvocation(String ctx, String name, Object... args) {
		args = concat(prefixArgs, args);
		try {
			target.getClass().getMethod(name, getTypes(args)).invoke(target,
					args);
		} catch (InvocationTargetException e) {
			Throwable te = e.getTargetException();
			if (te instanceof AssertionError) {
				throw (AssertionError) te;
			}
			throw new RuntimeException("Invocation error (" + ctx + ")", te);
		} catch (Exception e) {
			throw new RuntimeException("Invocation error (" + ctx + ")", e);
		}
	}

	private static Object[] concat(Object[] a, Object[] b) {
		final Object[] result = new Object[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	private static Class<?>[] getTypes(Object[] instances) {
		final Class<?>[] classes = new Class[instances.length];
		for (int i = 0; i < instances.length; i++) {
			Class<? extends Object> c = instances[i].getClass();
			if (c == Integer.class) {
				// We always use primitive int parameters:
				c = Integer.TYPE;
			}
			classes[i] = c;
		}
		return classes;
	}

}
