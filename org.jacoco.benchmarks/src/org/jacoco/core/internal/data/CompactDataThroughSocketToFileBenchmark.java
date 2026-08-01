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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Benchmark for {@link CompactDataOutput} and {@link CompactDataInput}
 * connected through {@link Socket} to {@link FileOutputStream}.
 *
 * Demonstrates benefits of using {@link #bufferedFileWriter}
 * ({@link BufferedOutputStream} over raw {@link FileOutputStream}),
 * {@link #bufferedSocketReceiver} ({@link BufferedInputStream} over raw
 * {@link Socket#getInputStream()}) and {@link #bufferedSocketSender}
 * ({@link BufferedOutputStream} over raw {@link Socket#getOutputStream()})
 * together.
 *
 * @see CompactDataOutputBenchmark
 * @see CompactDataThroughSocketBenchmark
 */
@State(Scope.Thread)
public class CompactDataThroughSocketToFileBenchmark {

	@Param({ "32", "128", "1024" })
	private int dataSize;

	private boolean[] booleanArray;

	private Socket socket;

	@Param({ "true", "false" })
	private boolean bufferedSocketSender;

	@Param({ "true", "false" })
	private boolean bufferedSocketReceiver;

	@Param({ "true", "false" })
	private boolean bufferedFileWriter;

	private CompactDataInput input;
	private CompactDataOutput output;
	private FileOutputStream fileOutputStream;

	@Setup
	public void setup() throws Exception {
		booleanArray = new boolean[dataSize];
		final ServerSocket serverSocket = new ServerSocket(0, 1);
		new Thread() {
			@Override
			public void run() {
				try {
					final Socket socket = serverSocket.accept();
					serverSocket.close();
					OutputStream outputStream = socket.getOutputStream();
					if (bufferedSocketSender) {
						outputStream = new BufferedOutputStream(outputStream);
					}
					final CompactDataOutput output = new CompactDataOutput(
							outputStream);
					while (true) {
						output.writeBooleanArray(booleanArray);
					}
				} catch (final Exception e) {
					// ignore
				}
			}
		}.start();
		socket = new Socket(InetAddress.getByName(null),
				serverSocket.getLocalPort());

		InputStream inputStream = socket.getInputStream();
		if (bufferedSocketReceiver) {
			inputStream = new BufferedInputStream(inputStream);
		}
		input = new CompactDataInput(inputStream);

		final File file = File.createTempFile(
				CompactDataThroughSocketToFileBenchmark.class.getName(), "out");
		fileOutputStream = new FileOutputStream(file);
		output = new CompactDataOutput(
				bufferedFileWriter ? new BufferedOutputStream(fileOutputStream)
						: fileOutputStream);
	}

	@Benchmark
	public void benchmark() throws Exception {
		fileOutputStream.getChannel().position(0);
		output.writeBooleanArray(input.readBooleanArray());
		output.flush();
	}

	@TearDown
	public void tearDown() throws Exception {
		socket.close();
	}

	public static void main(String[] args) throws Exception {
		new Runner(new OptionsBuilder() //
				.include(
						CompactDataThroughSocketToFileBenchmark.class.getName()) //
				.mode(Mode.AverageTime) //
				.timeUnit(TimeUnit.MICROSECONDS) //
				.warmupIterations(5) //
				.warmupTime(TimeValue.seconds(1)) //
				.measurementIterations(10) //
				.measurementTime(TimeValue.seconds(1)) //
				.forks(1) //
				.build()).run();
	}
}
