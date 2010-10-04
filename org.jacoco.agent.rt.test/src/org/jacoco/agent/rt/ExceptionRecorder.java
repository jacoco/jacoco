/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link IExceptionLogger} implementation for testing purposes.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class ExceptionRecorder implements IExceptionLogger {

	private final List<Exception> exceptions = new ArrayList<Exception>();

	public void logExeption(Exception ex) {
		exceptions.add(ex);
	}

	public void clear() {
		exceptions.clear();
	}

	public void assertEmpty() {
		assertEquals(Collections.emptyList(), getTypes());
	}

	public void assertException(final Class<? extends Throwable> type,
			final String message) {
		assertEquals(Collections.singletonList(type), getTypes());
		assertEquals(Collections.singletonList(message), getMessages());
	}

	private List<Class<?>> getTypes() {
		final List<Class<?>> types = new ArrayList<Class<?>>();
		for (Exception e : exceptions) {
			types.add(e.getClass());
		}
		return types;
	}

	private List<String> getMessages() {
		final List<String> messages = new ArrayList<String>();
		for (Exception e : exceptions) {
			messages.add(e.getMessage());
		}
		return messages;
	}

}
