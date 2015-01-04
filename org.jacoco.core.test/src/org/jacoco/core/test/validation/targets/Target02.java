/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation.targets;

import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.i1;
import static org.jacoco.core.test.validation.targets.Stubs.i2;
import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.jacoco.core.test.validation.targets.Stubs.t;

/**
 * This target exercises boolean expressions.
 */
public class Target02 implements Runnable {

	public void run() {

		// 1. Boolean comparison result (one case)
		nop(i2() > 3); // $line-booleancmp1$

		// 2. Boolean comparison result (both cases)
		for (int i = 0; i < 2; i++) {
			nop(i < 1); // $line-booleancmp2$
		}

		// 3. And
		if (f() & f()) { // $line-andFF$
			nop();
		}
		if (f() & t()) { // $line-andFT$
			nop();
		}
		if (t() & f()) { // $line-andTF$
			nop();
		}
		if (t() & t()) { // $line-andTT$
			nop();
		}

		// 4. Conditional And
		if (f() && f()) { // $line-conditionalandFF$
			nop();
		}
		if (f() && t()) { // $line-conditionalandFT$
			nop();
		}
		if (t() && f()) { // $line-conditionalandTF$
			nop();
		}
		if (t() && t()) { // $line-conditionalandTT$
			nop();
		}

		// 5. Or
		if (f() | f()) { // $line-orFF$
			nop();
		}
		if (f() | t()) { // $line-orFT$
			nop();
		}
		if (t() | f()) { // $line-orTF$
			nop();
		}
		if (t() | t()) { // $line-orTT$
			nop();
		}

		// 6. Conditional Or
		if (f() || f()) { // $line-conditionalorFF$
			nop();
		}
		if (f() || t()) { // $line-conditionalorFT$
			nop();
		}
		if (t() || f()) { // $line-conditionalorTF$
			nop();
		}
		if (t() || t()) { // $line-conditionalorTT$
			nop();
		}

		// 7. Exclusive Or
		if (f() ^ f()) { // $line-xorFF$
			nop();
		}
		if (f() ^ t()) { // $line-xorFT$
			nop();
		}
		if (t() ^ f()) { // $line-xorTF$
			nop();
		}
		if (t() ^ t()) { // $line-xorTT$
			nop();
		}

		// 8. Conditional Operator
		nop(t() ? i1() : i2()); // $line-condT$
		nop(f() ? i1() : i2()); // $line-condF$

		// 9. Not (one case)
		nop(!t()); // $line-notT$
		nop(!f()); // $line-notF$

		// 10. Not (both cases)
		for (boolean b : new boolean[] { true, false }) {
			nop(!b); // $line-notTF$
		}

	}

	public static void main(String[] args) {
		new Target02().run();
	}

}
