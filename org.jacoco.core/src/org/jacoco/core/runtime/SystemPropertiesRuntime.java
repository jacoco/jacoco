/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and others
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
package org.jacoco.core.runtime;

import java.util.Map;

import org.jacoco.core.instr.GeneratorConstants;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This {@link IRuntime} implementation makes the execution data available
 * through a special entry of the type {@link Map} in the
 * {@link System#getProperties()} hash table. The advantage is, that the
 * instrumented classes do not get dependencies to other classes than the JRE
 * library itself.
 * 
 * This runtime may cause problems in environments with security restrictions,
 * in applications that replace the system properties or in applications that
 * fail if non-String values are placed in the system properties.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class SystemPropertiesRuntime extends AbstractRuntime {

	private static final String KEYPREFIX = "jacoco-";

	private final String key;

	private final Map<Long, boolean[]> dataAccess = new MapAdapter(store);

	/**
	 * Creates a new runtime.
	 */
	public SystemPropertiesRuntime() {
		this.key = KEYPREFIX + hashCode();
	}

	public int generateDataAccessor(final long classid, final MethodVisitor mv) {

		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System",
				"getProperties", "()Ljava/util/Properties;");

		// Stack[0]: Ljava/util/Properties;

		mv.visitLdcInsn(key);

		// Stack[1]: Ljava/lang/String;
		// Stack[0]: Ljava/util/Properties;

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Properties",
				"get", "(Ljava/lang/Object;)Ljava/lang/Object;");

		// Stack[0]: Ljava/lang/Object;

		mv.visitTypeInsn(Opcodes.CHECKCAST, "java/util/Map");

		// Stack[0]: Ljava/util/Map;

		mv.visitLdcInsn(Long.valueOf(classid));

		// Stack[2]: J
		// Stack[1]: .
		// Stack[0]: Ljava/util/Map;

		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
				"(J)Ljava/lang/Long;");

		// Stack[1]: Ljava/lang/Long;
		// Stack[0]: Ljava/util/Map;

		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "get",
				"(Ljava/lang/Object;)Ljava/lang/Object;");

		// Stack[0]: Ljava/lang/Object;

		mv.visitTypeInsn(Opcodes.CHECKCAST, GeneratorConstants.PROBEDATA_TYPE
				.getInternalName());

		// Stack[0]: [Z

		return 3; // Maximum local stack size is 3
	}

	public void startup() {
		System.getProperties().put(key, dataAccess);
	}

	public void shutdown() {
		System.getProperties().remove(key);
	}

}
