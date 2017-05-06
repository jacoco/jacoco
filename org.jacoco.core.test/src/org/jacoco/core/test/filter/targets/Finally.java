/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.filter.targets;

import static org.jacoco.core.test.validation.targets.Stubs.ex;
import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.nop;
import static org.jacoco.core.test.validation.targets.Stubs.t;

import java.util.Collections;

import org.jacoco.core.test.validation.targets.Stubs.StubException;

/**
 * This test target is a finally block.
 */
public class Finally {

	/**
	 * Note that ECJ places duplicate of finally handler for non exceptional
	 * path after handler, while javac not. So that identical bytecode sequence
	 * after handler should be merged with handler if it really represents
	 * duplicate of a finally block on non exceptional path and not just an
	 * identical bytecode sequence.
	 */
	private static void test() {
		try {
			nop(); // $line-test.tryBlock$
		} finally {
			nop(); // $line-test.finallyBlock$
		}
		nop(); // $line-test.after$
	}

	/**
	 * Note that ECJ generates <code>goto</code> instruction at the end of catch
	 * handler that refers to a line of previous instruction. Currently we
	 * ignore this instruction, because otherwise it will cause partial coverage
	 * of last line of finally handler, when catch handler not executed. However
	 * because of this {@link #breakStatement() break statements are also
	 * ignored}.
	 */
	private static void catchNotExecuted() {
		try {
			nop(); // $line-catchNotExecuted.tryBlock$
		} catch (Exception e) { // $line-catchNotExecuted.catch$
			nop(); // $line-catchNotExecuted.catchBlock$
		} finally {
			nop(); // $line-catchNotExecuted.finallyBlock$
		}
		nop(); // $line-catchNotExecuted.after$
	}

	/**
	 * Note that javac generates <code>goto</code> instruction at the end of
	 * <code>while</code> statement that refers to a line of previous
	 * instruction. And so causes partial coverage of last line of finally
	 * handler, when loop executed only once. <code>do-while</code> and
	 * <code>for</code> statements not affected by this.
	 *
	 * @see #insideDoWhile()
	 * @see #insideFor()
	 * @see #insideForEach()
	 */
	private static void insideWhile() {
		while (t()) {
			try {
				ex();
			} finally {
				nop(); // $line-insideWhile.finally$
				nop(); // $line-insideWhile.finallyLastLine$
			}
		}
	}

	private static void insideDoWhile() {
		do {
			try {
				nop();
			} finally {
				nop(); // $line-insideDoWhile.finally$
			}
		} while (f());
	}

	private static void insideFor() {
		for (int i = 0; i < 1; i++) {
			try {
				nop();
			} finally {
				nop(); // $line-insideFor.finally$
			}
		}
	}

	private static void insideForEach() {
		for (Object o : Collections.singleton(new Object())) {
			try {
				nop(o);
			} finally {
				nop(); // $line-insideForEach.finally$
			}
		}
	}

	/**
	 * Note that in this case javac does not assign line number to
	 * <code>goto</code> instruction generated for <code>break</code> statement.
	 * And currently in case of ECJ we ignore this instruction to cover case of
	 * {@link #catchNotExecuted()}. This is misleading especially in case of
	 * partial coverage of <code>if</code> statement.
	 */
	private static void breakStatement() {
		for (int i = 0; i < 1; i++) {
			try {
				if (f()) {
					break; // $line-breakStatement.break$
				}
				nop();
			} finally {
				nop(); // $line-breakStatement.finally$
			}
		}
	}

	private static void branches(boolean t) {
		int i = 0;
		try {
			ex(t);
			i++;
		} catch (Exception e) {
			nop();
		} finally {
			nop(i == 0); // $line-conditionalJump$

			switch (i) { // $line-tableswitch$
			case 0:
			case 2:
				break;
			case 1:
			default:
				break;
			}

			switch (i) { // $line-lookupswitch$
			case 0:
				break;
			default:
				break;
			}
		}
	}

	private static Object returnInBody() {
		try {
			return null;
		} finally {
			nop(); // $line-returnInBody$
		}
	}

	/**
	 * Note that in this case javac generates bytecode that uses different slots
	 * in different duplicates of same finally block for variable that stores
	 * exception.
	 */
	private static void nested() {
		try {
			nop();
		} finally {
			try {
				nop();
			} finally {
				nop(); // $line-nested$
			}
		}
	}

	/**
	 * Note that in this case ECJ places duplicate of finally handler as last
	 * sequence of instructions.
	 */
	private static void noInstructionsAfterDuplicate() {
		while (true) {
			try {
				ex();
			} finally {
				synchronized (Finally.class) {
					nop(); // $line-noInstructionsAfterDuplicate.finally$
				}
			}
		}
	}

	private static void alwaysCompletesAbruptly() {
		try {
			nop(); // $line-alwaysCompletesAbruptly.tryBlock$
		} finally { // $line-alwaysCompletesAbruptly.finally$
			return; // $line-alwaysCompletesAbruptly.finallyBlock$
		}
	}

	public static void main(String[] args) {
		test();

		catchNotExecuted();

		try {
			insideWhile();
		} catch (StubException ignore) {
		}
		insideDoWhile();
		insideFor();
		insideForEach();

		breakStatement();

		branches(false);
		branches(true);

		returnInBody();

		nested();

		try {
			noInstructionsAfterDuplicate();
		} catch (StubException ignore) {
		}

		alwaysCompletesAbruptly();
	}

}
