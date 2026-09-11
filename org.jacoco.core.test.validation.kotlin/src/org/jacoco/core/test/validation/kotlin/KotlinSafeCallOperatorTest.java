/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.kotlin.targets.KotlinSafeCallOperatorTarget;
import org.junit.Test;

/**
 * Test of code coverage in {@link KotlinSafeCallOperatorTarget}.
 */
public class KotlinSafeCallOperatorTest extends ValidationTestBase {

	public KotlinSafeCallOperatorTest() {
		super(KotlinSafeCallOperatorTarget.class);
	}

	@Test
	public void bytecodeSnapshots() throws Exception {
		assertSnapshot(KotlinSafeCallOperatorTarget.class,
				"safeCall$fullCoverage", "safe_call.txt");
		assertSnapshot(KotlinSafeCallOperatorTarget.class,
				"safeCallChain$fullCoverage", "safe_call_chain.txt");
		assertSnapshot(KotlinSafeCallOperatorTarget.class,
				"safeCallChainMultiline$fullCoverage",
				"safe_call_chain_multiline.txt");
		assertSnapshot(KotlinSafeCallOperatorTarget.class,
				"safeCallChainMultiline2$fullCoverage",
				"safe_call_chain_multiline2.txt");
		assertSnapshot(KotlinSafeCallOperatorTarget.class,
				"safeCallFollowedByElvis$fullCoverage",
				"safe_call_followed_by_elvis.txt");
		assertSnapshot(KotlinSafeCallOperatorTarget.class,
				"safeCallFollowedByElvisMultiline$fullCoverage",
				"safe_call_followed_by_elvis_multiline.txt");
		assertSnapshot(KotlinSafeCallOperatorTarget.class,
				"safeCallChainFollowedByElvis$fullCoverage",
				"safe_call_chain_followed_by_elvis.txt");
		assertSnapshot(KotlinSafeCallOperatorTarget.class,
				"safeCallChainFollowedByElvisMultiline$fullCoverage",
				"safe_call_chain_followed_by_elvis_multiline.txt");
	}

}
