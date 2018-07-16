/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
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

import static org.jacoco.core.test.validation.targets.Stubs.f;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

import java.io.Closeable;
import java.io.IOException;

/**
 * This test target is a try-with-resources statement.
 */
public class TryWithResources {

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
		nop(); // $line-test.before$
		try ( // $line-test.try$
				Resource r1 = new Resource(); // $line-test.open1$
				Closeable r2 = new Resource(); // $line-test.open2$
				AutoCloseable r3 = new Resource() // $line-test.open3$
		) {
			return read(r1, r2, r3); // $line-test.body$
		} // $line-test.close$
		catch (Exception e) {
			nop(); // $line-test.catch$
			throw e;
		} finally {
			nop(); // $line-test.finally$
		}
	}

	private static void test2() throws Exception {
		nop(); // $line-test2.before$
		try ( // $line-test2.try$
				Resource r1 = new Resource(); // $line-test2.open1$
				Closeable r2 = new Resource(); // $line-test2.open2$
				AutoCloseable r3 = new Resource() // $line-test2.open3$
		) {
			read(r1, r2, r3); // $line-test2.body$
		} // $line-test2.close$
		catch (Exception e) {
			nop(); // $line-test2.catch$
		} finally {
			nop(); // $line-test2.finally$
		}
		nop(); // $line-test2.after$
	}

	private static Object returnInBody() throws IOException {
		try ( // $line-returnInBody.try$
				Closeable r = new Resource() // $line-returnInBody.open$
		) {
			return read(r); // $line-returnInBody.return$
		} // $line-returnInBody.close$
	}

	private static void nested() {
		try ( // $line-nested.try1$
				Resource r1 = new Resource() // $line-nested.open1$
		) {

			try ( // $line-nested.try2$
					Resource r2 = new Resource() // $line-nested.open2$
			) {
				nop(r1.toString() + r2.toString()); // $line-nested.body$
			} // $line-nested.close2$
			catch (Exception e) {
				nop(); // $line-nested.catch2$
			} finally {
				nop(); // $line-nested.finally2$
			}

		} // $line-nested.close1$
		catch (Exception e) {
			nop(); // $line-nested.catch1$
		} finally {

			try ( // $line-nested.try3$
					Resource r2 = new Resource() // $line-nested.open3$
			) {
				nop(r2); // $line-nested.body3$
			} // $line-nested.close3$
			catch (Exception e) {
				nop(); // $line-nested.catch3$
			} finally {
				nop(); // $line-nested.finally3$
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
		try ( // $line-returnInCatch.try1$
				Resource r = new Resource() // $line-returnInCatch.open$
		) {
			read(r);
		} // $line-returnInCatch.close$
		catch (Exception e) {
			return null;
		} finally {
			nop(!f()); // $line-returnInCatch.finally1$
		}

		try { // $line-returnInCatch.try2$
			read(new Resource());
		} catch (Exception e) {
			return null;
		} finally {
			nop(!f()); // $line-returnInCatch.finally2$
		}

		return null;
	}

	private static Object read(Object r1, Object r2, Object r3) {
		return r1.toString() + r2.toString() + r3.toString();
	}

	private static Object read(Object r1) {
		return r1.toString();
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

	/*
	 * Corner cases
	 */

	private static void empty() throws Exception {
		try ( // $line-empty.try$
				Closeable r = new Resource() // $line-empty.open$
		) {
		} // $line-empty.close$
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
			if (r != null) { // $line-handwritten$
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
		try ( // $line-throwInBody.try$
				Closeable r = new Resource()) {
			nop(r);
			throw new RuntimeException();
		} // $line-throwInBody.close$
	}

}
