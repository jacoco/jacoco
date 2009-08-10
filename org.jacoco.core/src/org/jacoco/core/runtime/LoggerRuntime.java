/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.instr.GeneratorConstants;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * This {@link IRuntime} implementation uses the Java logging API to report
 * coverage data. The advantage is, that the instrumented classes do not get
 * dependencies to other classes than the JRE library itself.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class LoggerRuntime implements IRuntime {

	private static final String CHANNEL = "jacoco-runtime";

	private final String key;

	private final Logger logger;

	private final Handler handler;

	final Map<Long, boolean[][]> dataMap;

	/**
	 * Creates a new runtime.
	 */
	public LoggerRuntime() {
		this.key = Integer.toHexString(hashCode());
		this.logger = configureLogger();
		this.handler = new RuntimeHandler();
		dataMap = Collections.synchronizedMap(new HashMap<Long, boolean[][]>());
	}

	private Logger configureLogger() {
		final Logger l = Logger.getLogger(CHANNEL);
		l.setUseParentHandlers(false);
		l.setLevel(Level.ALL);
		return l;
	}

	public void generateRegistration(final long classId,
			final GeneratorAdapter gen) {

		// boolean[][] data = pop()
		final int data = gen.newLocal(GeneratorConstants.DATAFIELD_TYPE);
		gen.storeLocal(data);

		// stack := Logger.getLogger(CHANNEL)
		gen.push(CHANNEL);
		gen.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/logging/Logger",
				"getLogger", "(Ljava/lang/String;)Ljava/util/logging/Logger;");

		// stack = Level.INFO;
		gen.getStatic(Type.getObjectType("java/util/logging/Level"), "INFO",
				Type.getObjectType("java/util/logging/Level"));

		// stack := key
		gen.push(key);

		// stack := new Object[2]
		gen.push(2);
		gen.newArray(Type.getObjectType("java/lang/Object"));

		// stack[0] = Long.valueOf(classId)
		gen.dup();
		gen.push(0);
		gen.push(classId);
		gen.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
				"(J)Ljava/lang/Long;");
		gen.arrayStore(Type.getObjectType("java/lang/Object"));

		// stack[1] = data
		gen.dup();
		gen.push(1);
		gen.loadLocal(data);
		gen.arrayStore(Type.getObjectType("java/lang/Object"));

		// stack.log(stack, stack, stack)
		gen
				.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
						"java/util/logging/Logger", "log",
						"(Ljava/util/logging/Level;Ljava/lang/String;[Ljava/lang/Object;)V");
	}

	public void startup() {
		this.logger.addHandler(handler);
	}

	public void collect(final IExecutionDataVisitor visitor, final boolean reset) {
		synchronized (dataMap) {
			for (final Map.Entry<Long, boolean[][]> entry : dataMap.entrySet()) {
				final long classId = entry.getKey().longValue();
				final boolean[][] blockData = entry.getValue();
				visitor.visitClassExecution(classId, blockData);
			}
			if (reset) {
				reset();
			}
		}
	}

	public void reset() {
		synchronized (dataMap) {
			for (final boolean[][] data : dataMap.values()) {
				for (final boolean[] arr : data) {
					Arrays.fill(arr, false);
				}
			}
		}
	}

	public void shutdown() {
		this.logger.removeHandler(handler);
	}

	private class RuntimeHandler extends Handler {

		@Override
		public void publish(final LogRecord record) {
			if (key.equals(record.getMessage())) {
				final Object[] params = record.getParameters();
				dataMap.put((Long) params[0], (boolean[][]) params[1]);
			}
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}
	}

}
