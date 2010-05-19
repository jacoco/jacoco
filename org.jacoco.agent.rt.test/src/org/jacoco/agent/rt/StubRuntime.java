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

import org.jacoco.core.runtime.AbstractRuntime;
import org.objectweb.asm.MethodVisitor;

public class StubRuntime extends AbstractRuntime {

	public int generateDataAccessor(long classid, String classname,
			int probecount, MethodVisitor mv) {
		return 0;
	}

	public void startup() {
	}

	public void shutdown() {
	}
}