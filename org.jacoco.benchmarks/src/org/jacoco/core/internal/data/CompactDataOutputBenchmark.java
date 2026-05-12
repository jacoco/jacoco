/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.data;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Benchmark for {@link CompactDataOutput}. Demonstrates benefits of using
 * {@link #toBufferedFileStream()} over {@link #toRawFileStream()}.
 *
 * <p>
 * Note that {@link CompactDataOutput#writeUTF(String)} uses
 * {@link java.io.OutputStream#write(byte[], int, int)}. So
 * {@link #toRawFileStream()} gets close to {@link #toBufferedFileStream()} when
 * {@link #size} of {@link #string} gets close to size of buffer in
 * {@link BufferedOutputStream} (default is 8192 since
 * <a href="https://bugs.openjdk.org/browse/JDK-4953311">JDK-4953311</a> in JDK
 * versions from 5 up to latest as of writing JDK 27). However it is used by
 * {@link org.jacoco.core.data.ExecutionDataWriter} to write class names that
 * are usually short - for example average length in `java.base` of JDK 25 is
 * about 42.
 * </p>
 */
@State(Scope.Thread)
public class CompactDataOutputBenchmark {

	public enum Data {
		STRING, BOOLEAN_ARRAY,
	}

	@Param({ "STRING", "BOOLEAN_ARRAY" })
	private Data data;

	@Param({ "32", "128", "1024" })
	private int size;

	private FileOutputStream fileOutputStream;
	private CompactDataOutput rawFileOutput;
	private CompactDataOutput bufferedFileOutput;
	private ByteArrayOutputStream byteArrayOutputStream;
	private CompactDataOutput byteArrayOutput;

	private boolean[] booleanArray;
	private String string;

	@Setup
	public void setup() throws Exception {
		final File file = File.createTempFile(
				CompactDataOutputBenchmark.class.getName(), "out");
		fileOutputStream = new FileOutputStream(file);
		rawFileOutput = new CompactDataOutput(fileOutputStream);
		bufferedFileOutput = new CompactDataOutput(
				new BufferedOutputStream(rawFileOutput));
		byteArrayOutputStream = new ByteArrayOutputStream(size);
		byteArrayOutput = new CompactDataOutput(byteArrayOutputStream);
		booleanArray = new boolean[size];
		string = new String(new char[size]);
	}

	@Benchmark
	public void toBufferedFileStream() throws Exception {
		fileOutputStream.getChannel().position(0);
		write(bufferedFileOutput);
		fileOutputStream.flush();
	}

	@Benchmark
	public void toRawFileStream() throws Exception {
		fileOutputStream.getChannel().position(0);
		write(rawFileOutput);
		fileOutputStream.flush();
	}

	@Benchmark
	public void toByteArrayStream() throws Exception {
		byteArrayOutputStream.reset();
		write(byteArrayOutput);
	}

	private void write(final CompactDataOutput output) throws Exception {
		switch (data) {
		case BOOLEAN_ARRAY:
			output.writeBooleanArray(booleanArray);
			break;
		case STRING:
			output.writeUTF(string);
			break;
		default:
			throw new IllegalStateException();
		}
	}

	public static void main(String[] args) throws Exception {
		new Runner(new OptionsBuilder()
				.include(CompactDataOutputBenchmark.class.getName()) //
				.mode(Mode.AverageTime) //
				.timeUnit(TimeUnit.MICROSECONDS) //
				.warmupIterations(5) //
				.warmupTime(TimeValue.seconds(1)) //
				.measurementIterations(10) //
				.measurementTime(TimeValue.seconds(1)) //
				.forks(2) //
				.build()).run();
	}

}
