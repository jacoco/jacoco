/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.agent.rt.controller;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TcpServerListener implements Runnable {
	private boolean shutdownRequested;
	private ServerSocketChannel ssc;
	private final String address;
	private final int port;

	public TcpServerListener(String address, int port) {
		this.address = address;
		this.port = port;
	}

	public void requestShutdown() {
		shutdownRequested = true;
	}

	public void run() {
		try {
			shutdownRequested = false;
			ssc = bindServerSocket();

			while (!shutdownRequested) {
				SocketChannel clientSocket = null;
				try {
					clientSocket = ssc.accept();

					// Command command = null;
					// ByteBuffer buffer = ByteBuffer.allocate(2);
					// do {
					// clientSocket.read(buffer);
					// char commandId = buffer.getChar();
					// Command.fromId(commandId);
					//
					// } while (command != Command.DISCONNECT);

				} catch (ClosedByInterruptException e) {
					// This is an expected case if no one has attempted a
					// connection
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					safeClose(clientSocket);
					clientSocket = null;
				}
			}

		} catch (IOException e) {
			// TODO need a better way to track this
			e.printStackTrace();
		} finally {
			safeClose(ssc);
			shutdownRequested = false;
		}

	}

	private ServerSocketChannel bindServerSocket() throws IOException {
		InetSocketAddress socketAddress = new InetSocketAddress(address, port);
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ServerSocket serverSocket = ssc.socket();
		serverSocket.bind(socketAddress, 1);

		return ssc;
	}

	private void safeClose(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {
		}
	}

}