/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import java.io.IOException;
import java.net.Socket;

import org.jacoco.agent.rt.internal.IExceptionLogger;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;

/**
 * Output that connects to a TCP port. This controller uses the following
 * agent options:
 * <ul>
 * <li>address</li>
 * <li>port</li>
 * </ul>
 */
public class TcpClientOutput implements IAgentOutput {

	private final IExceptionLogger logger;

	private TcpConnection connection;

	private Thread worker;
	
	private RuntimeData data;
	
	private AgentOptions options;
	
	static final long HEARTTIME= 5000l;
	
	static final int MAXCOUNT = 10;

	/**
	 * New controller instance.
	 * 
	 * @param logger
	 *            logger to use in case of exceptions is spawned threads
	 */
	public TcpClientOutput(final IExceptionLogger logger) {
		this.logger = logger;
	}

	public void startup(final AgentOptions options, final RuntimeData data)
			throws IOException {
	    this.options = options;
	    this.data = data;
	    
		final Socket socket = createSocket(options);
		
		System.out.println("TcpClientOutput: " + options.getId(AgentOptions.DEFAULT_ID, 0));
		if (options != null && options.getId(AgentOptions.DEFAULT_ID, 0) > 0) {
			connection = new TcpConnection(options, socket, data);
		}
		else {
		    connection = new TcpConnection(socket, data);
		}
		
		connection.init();
		worker = new Thread(new Runnable() {
			public void run() {
				try {
					connection.run();
				} catch (final IOException e) {
				    
				    if (options != null && options.getId(AgentOptions.DEFAULT_ID, 0) > 0) {
				        System.out.println("ThreadingTest disconnected ,begin reconnect....");
				        reconnection();
				    }
				    e.printStackTrace();
					logger.logExeption(e);
				}
			}
		});
		worker.setName(getClass().getName());
		worker.setDaemon(true);
		worker.start();
	}
	
	private void reconnection() {
	    for (int count = 0; count < MAXCOUNT; count++) {
	        try {
                Thread.sleep(HEARTTIME);
//                shutdown();
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
	        
	        System.out.println("ThreadingTest Reconnect to " + options.getAddress() + " count: " + (count + 1) );
	        try {
	            startup(this.options, this.data);
	            //如果没有异常，退出，否则进行重试
	            break;
	        }
	        catch (Exception e) {
	            e.printStackTrace();
            }
	    }
	}

	public void shutdown() throws Exception {
		connection.close();
		worker.join();
	}

	public void writeExecutionData(final boolean reset) throws IOException {
		connection.writeExecutionData(reset);
	}

	/**
	 * Open a socket based on the given configuration.
	 * 
	 * @param options
	 *            address and port configuration
	 * @return opened socket
	 * @throws IOException
	 */
	protected Socket createSocket(final AgentOptions options)
			throws IOException {
		return new Socket(options.getAddress(), options.getPort());
	}

}
