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
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.i1;
import static org.jacoco.core.test.validation.targets.Stubs.i2;
import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.jacoco.core.test.validation.targets.Stubs.t;

/**
 * This target exercises boolean expressions.
 */
public class BooleanExpressionsTarget {

	public static void main(String[] args) {

		/* 1. Boolean comparison result (one case) */
		nop(i2() > 3); // assertPartlyCovered(1, 1)

		/* 2. Boolean comparison result (both cases) */
		for (int i = 0; i < 2; i++) {
			nop(i < 1); // assertFullyCovered(0, 2)
		}

		/* 3. And */
		if (f() & f()) { // assertFullyCovered(1, 1)
			nop();
		}
		if (f() & t()) { // assertFullyCovered(1, 1)
			nop();
		}
		if (t() & f()) { // assertFullyCovered(1, 1)
			nop();
		}
		if (t() & t()) { // assertFullyCovered(1, 1)
			nop();
		}

		/* 4. Conditional And */
		if (f() && f()) { // assertPartlyCovered(3, 1)
			nop();
		}
		if (f() && t()) { // assertPartlyCovered(3, 1)
			nop();
		}
		if (t() && f()) { // assertFullyCovered(2, 2)
			nop();
		}
		if (t() && t()) { // assertFullyCovered(2, 2)
			nop();
		}

		/* 5. Or */
		if (f() | f()) { // assertFullyCovered(1, 1)
			nop();
		}
		if (f() | t()) { // assertFullyCovered(1, 1)
			nop();
		}
		if (t() | f()) { // assertFullyCovered(1, 1)
			nop();
		}
		if (t() | t()) { // assertFullyCovered(1, 1)
			nop();
		}

		/* 6. Conditional Or */
		if (f() || f()) { // assertFullyCovered(2, 2)
			nop();
		}
		if (f() || t()) { // assertFullyCovered(2, 2)
			nop();
		}
		if (t() || f()) { // assertPartlyCovered(3, 1)
			nop();
		}
		if (t() || t()) { // assertPartlyCovered(3, 1)
			nop();
		}

		/* 7. Exclusive Or */
		if (f() ^ f()) { // assertFullyCovered(1, 1)
			nop();
		}
		if (f() ^ t()) { // assertFullyCovered(1, 1)
			nop();
		}
		if (t() ^ f()) { // assertFullyCovered(1, 1)
			nop();
		}
		if (t() ^ t()) { // assertFullyCovered(1, 1)
			nop();
		}

		/* 8. Conditional Operator */
		nop(t() ? i1() : i2()); // assertPartlyCovered(1, 1)
		nop(f() ? i1() : i2()); // assertPartlyCovered(1, 1)

		/* 9. Not (one case) */
		nop(!t()); // assertPartlyCovered(1, 1)
		nop(!f()); // assertPartlyCovered(1, 1)

		/* 10. Not (both cases) */
		for (boolean b : new boolean[] { true, false }) {
			nop(!b); // assertFullyCovered(0, 2)
		}

	}

}
