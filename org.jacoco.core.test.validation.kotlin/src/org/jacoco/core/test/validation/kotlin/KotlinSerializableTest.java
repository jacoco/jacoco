/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.kotlin;

import java.util.Collection;
import java.util.Collections;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinSerializableTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinSerializableTarget}.
 */
public class KotlinSerializableTest extends ValidationTestBase {

	public KotlinSerializableTest() {
		super(KotlinSerializableTarget.class);
	}

	@Override
	protected Collection<String> additionalClassesForAnalysis() {
		return Collections.singletonList(
				"org.jacoco.core.test.validation.kotlin.targets.KotlinSerializableTarget$Example$$serializer");
	}

	@Test
	public void test_method_count() {
		assertMethodCount(
				/* main + 3 constructors + 3 getters + 1 method in companion */8);
	}

}
