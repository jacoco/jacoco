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
 * $Id: $
 *******************************************************************************/
package org.jacoco.agent.rt;

import static junit.framework.Assert.assertFalse;

import java.util.Arrays;

import org.jacoco.core.runtime.AbstractRuntime;
import org.jacoco.core.runtime.IRuntime;
import org.objectweb.asm.MethodVisitor;

/**
 * Stub {@link IRuntime} implementation for unit testing only.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class StubRuntime extends AbstractRuntime {

	public StubRuntime() {
		setSessionId("stubid");
		store.get(Long.valueOf(0x12345678), "Foo", 2);
	}

	public int generateDataAccessor(long classid, String classname,
			int probecount, MethodVisitor mv) {
		return 0;
	}

	public void startup() {
	}

	public void shutdown() {
	}

	public void fillProbes() {
		final boolean[] data = store.get(0x12345678).getData();
		Arrays.fill(data, true);
	}

	public void assertNoProbes() {
		final boolean[] data = store.get(0x12345678).getData();
		assertFalse(data[0]);
		assertFalse(data[1]);
	}

}