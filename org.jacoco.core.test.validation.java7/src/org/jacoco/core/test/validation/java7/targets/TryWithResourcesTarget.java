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
package org.jacoco.core.test.validation.java7.targets;

import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

import java.io.Closeable;
import java.io.IOException;

/**
 * This test target is a try-with-resources statement.
 */
public class TryWithResourcesTarget {

	private static class Resource implements Closeable {
		@Override
		public void close() {
		}
	}

	/**
	 * Closing performed using {@link org.objectweb.asm.Opcodes#INVOKEVIRTUAL}
	 * or {@link org.objectweb.asm.Opcodes#INVOKEINTERFACE} depending on a class
	 * of resource.
	 */
	private static Object test() throws Exception {
		nop(); // assertFullyCovered()
		try ( // assertTry()
				Resource r1 = new Resource(); // assertFullyCovered()
				Closeable r2 = new Resource(); // assertFullyCovered()
				AutoCloseable r3 = new Resource() // assertFullyCovered()
		) {
			return read(r1, r2, r3); // assertFullyCovered()
			/* without filter next line has branches: */
		} // assertEmpty()
		catch (Exception e) {
			nop(); // assertNotCovered()
			throw e;
		} finally {
			nop(); // assertFullyCovered()
		}
	}

	private static void test2() throws Exception {
		nop(); // assertFullyCovered()
		try ( // assertTry()
				Resource r1 = new Resource(); // assertFullyCovered()
				Closeable r2 = new Resource(); // assertFullyCovered()
				AutoCloseable r3 = new Resource() // assertFullyCovered()
		) {
			read(r1, r2, r3); // assertFullyCovered()
			/* without filter next line has branches: */
		} // assertEmpty()
		catch (Exception e) {
			nop(); // assertNotCovered()
		} finally {
			nop(); // assertFullyCovered()
		}
		nop(); // assertFullyCovered()
	}

	private static Object returnInBody() throws IOException {
		try ( // assertTry()
				Closeable r = new Resource() // assertFullyCovered()
		) {
			return read(r); // assertFullyCovered()
		} // assertReturnInBodyClose()
	}

	private static void nested() {
		try ( // assertTry()
				Resource r1 = new Resource() // assertFullyCovered()
		) {

			try ( // assertTry()
					Resource r2 = new Resource() // assertFullyCovered()
			) {
				nop(r1.toString() + r2.toString()); // assertFullyCovered()
			} // assertEmpty()
			catch (Exception e) {
				nop(); // assertNotCovered()
			} finally {
				nop(); // assertFullyCovered()
			}

		} // assertEmpty()
		catch (Exception e) {
			nop(); // assertNotCovered()
		} finally {

			try ( // assertTry()
					Resource r2 = new Resource() // assertFullyCovered()
			) {
				nop(r2); // assertFullyCovered()
			} // assertEmpty()
			catch (Exception e) {
				nop(); // assertNotCovered()
			} finally {
				nop(); // assertFullyCovered()
			}

		}
	}

	/**
	 * In this case bytecode will contain 3 copies of <code>finally</code>
	 * block, each containing 2 branches, resulting in 6 branches in total. One
	 * could think that this is artifact of try-with-resources, but the same
	 * happens without it.
	 */
	private static Object returnInCatch() {
		try ( // assertTry()
				Resource r = new Resource() // assertFullyCovered()
		) {
			read(r);
			/* without filter next line has branches: */
		} // assertEmpty()
		catch (Exception e) {
			return null;
		} finally {
			nop(!f()); // assertPartlyCovered(1, 1)
		}

		try { // assertEmpty()
			read(new Resource());
		} catch (Exception e) {
			return null;
		} finally {
			nop(!f()); // assertPartlyCovered(1, 1)
		}

		return null;
	}

	private static Object read(Object r1, Object r2, Object r3) {
		return r1.toString() + r2.toString() + r3.toString();
	}

	private static Object read(Object r1) {
		return r1.toString();
	}

	/*
	 * Corner cases
	 */

	private static void empty() throws Exception {
		try ( // assertTry()
				Closeable r = new Resource() // assertFullyCovered()
		) {
		} // assertEmptyClose()
	}

	private static void handwritten() throws IOException {
		Closeable r = new Resource();
		Throwable primaryExc = null;
		try {
			nop(r);
		} catch (Throwable t) {
			primaryExc = t;
			throw t;
		} finally {
			if (r != null) { // assertHandwritten()
				if (primaryExc != null) {
					try {
						r.close();
					} catch (Throwable suppressedExc) {
						primaryExc.addSuppressed(suppressedExc);
					}
				} else {
					r.close();
				}
			}
		}
	}

	private static void throwInBody() throws IOException {
		try ( // assertNotCovered()
				Closeable r = new Resource()) {
			nop(r);
			throw new RuntimeException();
		} // assertThrowInBodyClose()
	}

	public static void main(String[] args) throws Exception {
		test();
		test2();
		returnInBody();
		nested();

		returnInCatch();

		empty();
		handwritten();
	}

}
