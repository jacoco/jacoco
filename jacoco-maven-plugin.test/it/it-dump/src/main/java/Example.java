/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Chas Honton - initial implementation
 *
 *******************************************************************************/
public class Example {

	private static final long TEN_MINUTES = 10 * 1000 * 60;

	public static void main(final String[] args) {
		System.out.println(System.currentTimeMillis()+": Hello ...");
		try {
			Thread.sleep(TEN_MINUTES);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis()+": ... world");
	}
}
