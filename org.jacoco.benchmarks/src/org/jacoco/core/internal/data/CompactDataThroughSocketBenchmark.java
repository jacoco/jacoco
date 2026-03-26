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
 * Benchmark for {@link CompactDataOutput} and {@link CompactDataInput} with
 * {@link Socket} connection. {@link BenchmarkSide#SENDER} and
 * {@link BenchmarkSide#RECEIVER} respectively.
 *
 * Demonstrates benefits of using {@link #bufferedSender}
 * ({@link BufferedInputStream} over raw {@link Socket#getInputStream()}) and
 * {@link #bufferedReceiver} ({@link BufferedOutputStream} over raw
 * {@link Socket#getOutputStream()}).
 *
 * Use of buffering only on receiver side is less beneficial.
 *
 * Use of buffering only on sender side is neither beneficial nor harmful.
 *
 * This asymmetry when only one side buffered is due to difference between
 * {@link Socket#getSendBufferSize()} and {@link Socket#getReceiveBufferSize()}.
 *
 * @see CompactDataOutputBenchmark
 */
@State(Scope.Thread)
public class CompactDataThroughSocketBenchmark {

	public enum BenchmarkSide {
		SENDER, RECEIVER
	}

	@Param({ "SENDER", "RECEIVER" })
	private BenchmarkSide benchmarkSide;

	@Param({ "true", "false" })
	private boolean bufferedReceiver;

	@Param({ "true", "false" })
	private boolean bufferedSender;

	public enum DataType {
		STRING, BOOLEAN_ARRAY
	}

	@Param({ "STRING", "BOOLEAN_ARRAY" })
	public DataType dataType;

	@Param({ "32", "128", "1024" })
	private int dataSize;

	private boolean[] booleanArray;
	private String string;

	interface Operation {
		void execute() throws Exception;
	}

	private Operation operation;
	private Socket socket;

	@Setup
	public void setup() throws Exception {
		booleanArray = new boolean[dataSize];
		string = new String(new char[dataSize]);
		final ServerSocket serverSocket = new ServerSocket(0, 1);
		new Thread() {
			@Override
			public void run() {
				try {
					final Socket socket = serverSocket.accept();
					serverSocket.close();
					final Operation operation = setup(socket,
							benchmarkSide == BenchmarkSide.RECEIVER);
					while (true) {
						operation.execute();
					}
				} catch (final Exception e) {
					// ignore
				}
			}
		}.start();
		socket = new Socket(InetAddress.getByName(null),
				serverSocket.getLocalPort());
		operation = setup(socket, benchmarkSide == BenchmarkSide.SENDER);
	}

	private Operation setup(final Socket socket, final boolean sender)
			throws Exception {
		if (sender) {
			OutputStream outputStream = socket.getOutputStream();
			if (bufferedReceiver) {
				outputStream = new BufferedOutputStream(outputStream);
			}
			final CompactDataOutput output = new CompactDataOutput(
					outputStream);
			return new Operation() {
				public void execute() throws Exception {
					sendTo(output);
				}
			};
		} else {
			InputStream inputStream = socket.getInputStream();
			if (bufferedSender) {
				inputStream = new BufferedInputStream(inputStream);
			}
			final CompactDataInput input = new CompactDataInput(inputStream);
			return new Operation() {

				public void execute() throws Exception {
					receiveFrom(input);
				}

			};
		}
	}

	private void sendTo(final CompactDataOutput output) throws Exception {
		switch (dataType) {
		case STRING:
			output.writeUTF(string);
			break;
		case BOOLEAN_ARRAY:
			output.writeBooleanArray(booleanArray);
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void receiveFrom(final CompactDataInput input) throws Exception {
		switch (dataType) {
		case STRING:
			input.readUTF();
			break;
		case BOOLEAN_ARRAY:
			input.readBooleanArray();
			break;
		default:
			throw new IllegalStateException();
		}
	}

	@Benchmark
	public void benchmark() throws Exception {
		operation.execute();
	}

	@TearDown
	public void tearDown() throws Exception {
		socket.close();
	}

	public static void main(String[] args) throws Exception {
		new Runner(new OptionsBuilder() //
				.include(CompactDataThroughSocketBenchmark.class.getName()) //
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
