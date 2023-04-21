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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.jacoco.core.test.validation.JavaVersion;
import org.junit.Test;

/**
 * Unit tests for {@link AgentModule}.
 */
public class AgentModuleTest {

	@Test
	public void isSupported_should_return_false_before_Java9() {
		Boolean expected = Boolean
				.valueOf(!JavaVersion.current().isBefore("9"));
		Boolean supported = Boolean.valueOf(AgentModule.isSupported());
		assertEquals(expected, supported);
	}

	@Test
	public void should_only_load_classes_in_scope() throws Exception {
		AgentModule am = new AgentModule();
		Class<? extends Target> targetclass = am
				.loadClassInModule(TargetImpl.class);
		Target t = targetclass.getDeclaredConstructor().newInstance();

		assertNotSame(this.getClass().getClassLoader(),
				t.getClass().getClassLoader());
		assertSame(AgentModule.class.getProtectionDomain(),
				t.getClass().getProtectionDomain());
		assertSame(t.getClass().getClassLoader(),
				t.getInnerClassInstance().getClass().getClassLoader());
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
