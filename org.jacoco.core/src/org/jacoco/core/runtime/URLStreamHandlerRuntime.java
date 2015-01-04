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
package org.jacoco.core.runtime;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This {@link IRuntime} implementation registers a special
 * {@link URLStreamHandler} to process coverage data. The handler is not
 * actually used for opening a URL, but to get access to the runtime object.
 */
public class URLStreamHandlerRuntime extends AbstractRuntime {

	private static final String PROTOCOLPREFIX = "jacoco-";

	private final String protocol;

	private Map<String, URLStreamHandler> handlers;

	/**
	 * Creates a new runtime.
	 */
	public URLStreamHandlerRuntime() {
		super();
		protocol = PROTOCOLPREFIX + Integer.toHexString(hashCode());
	}

	@Override
	public void startup(final RuntimeData data) throws Exception {
		super.startup(data);
		handlers = getHandlersReference();
		handlers.put(protocol, handler);
	}

	private Map<String, URLStreamHandler> getHandlersReference()
			throws Exception {
		final Field field = URL.class.getDeclaredField("handlers");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		final Map<String, URLStreamHandler> map = (Map<String, URLStreamHandler>) field
				.get(null);
		return map;
	}

	public void shutdown() {
		handlers.remove(protocol);
	}

	public int generateDataAccessor(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {

		// The data accessor performs the following steps:
		//
		// final URL url = new URL(protocol, null, "");
		// final URLConnection connection = url.openConnection();
		// final Object[] args = new Object[3];
		// args[0] = Long.valueOf(classid);
		// args[1] = classname;
		// args[2] = Integer.valueOf(probecount);
		// connection.equals(args);
		// final byte[] probedata = (byte[]) args[0];

		RuntimeData.generateArgumentArray(classid, classname, probecount, mv);
		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: [Ljava/lang/Object;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitTypeInsn(Opcodes.NEW, "java/net/URL");
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn(protocol);
		mv.visitInsn(Opcodes.ACONST_NULL);
		mv.visitLdcInsn("");
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/net/URL", "<init>",
				"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
				false);

		// Stack[2]: [Ljava/net/URL;
		// Stack[1]: [Ljava/lang/Object;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/URL",
				"openConnection", "()Ljava/net/URLConnection;", false);

		// Stack[2]: [Ljava/net/URLConnection;
		// Stack[1]: [Ljava/lang/Object;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitInsn(Opcodes.SWAP);

		// Stack[2]: [Ljava/lang/Object;
		// Stack[1]: [Ljava/net/URLConnection;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals",
				"(Ljava/lang/Object;)Z", false);

		// Stack[1]: Z;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitInsn(Opcodes.POP);

		// Stack[0]: [Ljava/lang/Object;

		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitInsn(Opcodes.AALOAD);
		mv.visitTypeInsn(Opcodes.CHECKCAST, InstrSupport.DATAFIELD_DESC);

		return 7;
	}

	private final URLStreamHandler handler = new URLStreamHandler() {
		@Override
		protected URLConnection openConnection(final URL u) throws IOException {
			return connection;
		}
	};

	private final URLConnection connection = new URLConnection(null) {
		@Override
		public void connect() throws IOException {
			throw new AssertionError();
		}

		@Override
		public boolean equals(final Object obj) {
			return data.equals(obj);
		}
	};

}
