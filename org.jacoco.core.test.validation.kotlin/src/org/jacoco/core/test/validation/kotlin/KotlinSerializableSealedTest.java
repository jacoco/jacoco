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
import org.jacoco.core.test.validation.kotlin.targets.KotlinSerializableSealedTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinSerializableSealedTarget}.
 */
public class KotlinSerializableSealedTest extends ValidationTestBase {

	public KotlinSerializableSealedTest() {
		super(KotlinSerializableSealedTarget.class);
	}

	@Override
	protected Collection<String> additionalClassesForAnalysis() {
		return Collections.singletonList(
				"org.jacoco.core.test.validation.kotlin.targets.KotlinSerializableSealedTarget$Sealed$A$$serializer");
	}

	@Test
	public void test_method_count() {
		assertMethodCount(
				/* main + static initializer + constructor + getter */4);
	}

}
