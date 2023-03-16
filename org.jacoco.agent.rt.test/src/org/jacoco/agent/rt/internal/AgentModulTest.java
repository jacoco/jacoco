/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.test.validation.JavaVersion;
import org.junit.AssumptionViolatedException;
import org.junit.Test;

/**
 * Unit tests for {@link AgentModule}.
 */
public class AgentModulTest {

	@Test
	public void isSupported_should_return_false_before_Java9() {
		if (JavaVersion.current().isAtLeast("9")) {
			throw new AssumptionViolatedException(
					"Modules available after Java 9");
		}
		assertFalse(AgentModule.isSupported());
	}

	@Test
	public void isSupported_should_return_true_for_Java9_or_greater() {
		if (JavaVersion.current().isBefore("9")) {
			throw new AssumptionViolatedException(
					"Modules only available after Java 9");
		}
		assertTrue(AgentModule.isSupported());
	}

	@Test
	public void should_only_load_classes_in_scope() throws Exception {
		if (JavaVersion.current().isBefore("9")) {
			throw new AssumptionViolatedException(
					"Modules only available after Java 9");
		}

		AgentModule am = new AgentModule();
		Class<? extends Target> targetclass = am
				.loadClassInModule(TargetImpl.class);
		Target t = targetclass.getDeclaredConstructor().newInstance();

		assertNotSame(this.getClass().getClassLoader(),
				t.getClass().getClassLoader());
		assertNotSame(this.getClass().getClassLoader(),
				t.getInnerClassInstance().getClass().getClassLoader());
		assertSame(this.getClass().getClassLoader(),
				t.getOtherClassInstance().getClass().getClassLoader());
	}

	public interface Target {

		Object getInnerClassInstance();

		Object getOtherClassInstance();

	}

	public static class TargetImpl implements Target {

		static class Inner {
		}

		public Object getInnerClassInstance() {
			return new Inner();
		}

		public Object getOtherClassInstance() {
			return new Other();
		}
	}

	public static class Other {
	}

}
