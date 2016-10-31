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
package org.jacoco.core.data;

import static java.lang.String.format;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.jacoco.core.internal.data.CompactDataInput;

/**
 * Deserialization of execution data from binary streams.
 */
public class ExecutionDataReader {

	/** Underlying data input */
	protected final CompactDataInput in;

	private ISessionInfoVisitor sessionInfoVisitor = null;

	private IExecutionDataVisitor executionDataVisitor = null;

	private IClientInfoVisitor clientInfoVisitor = null;

	public IClientInfoVisitor getClientInfoVisitor() {
		return clientInfoVisitor;
	}

	public void setClientInfoVisitor(IClientInfoVisitor clientInfoVisitor) {
		this.clientInfoVisitor = clientInfoVisitor;
	}

	private boolean firstBlock = true;
	
	/**
	 * 是否是新的client
	 */
	private boolean isNewClient = false;
	
	public boolean isNewClient() {
        return isNewClient;
    }

    public void setNewClient(boolean isNewClient) {
        this.isNewClient = isNewClient;
    }

    /**
	 * Creates a new reader based on the given input stream input. Depending on
	 * the nature of the underlying stream input should be buffered as most data
	 * is read in single bytes.
	 * 
	 * @param input
	 *            input stream to read execution data from
	 */
	public ExecutionDataReader(final InputStream input) {
		this.in = new CompactDataInput(input);
	}

	/**
	 * Sets an listener for session information.
	 * 
	 * @param visitor
	 *            visitor to retrieve session info events
	 */
	public void setSessionInfoVisitor(final ISessionInfoVisitor visitor) {
		this.sessionInfoVisitor = visitor;
	}

	/**
	 * Sets an listener for execution data.
	 * 
	 * @param visitor
	 *            visitor to retrieve execution data events
	 */
	public void setExecutionDataVisitor(final IExecutionDataVisitor visitor) {
		this.executionDataVisitor = visitor;
	}

	/**
	 * Reads all data and reports it to the corresponding visitors. The stream
	 * is read until its end or a command confirmation has been sent.
	 * 
	 * @return <code>true</code> if additional data can be expected after a
	 *         command has been executed. <code>false</code> if the end of the
	 *         stream has been reached.
	 * @throws IOException
	 *             might be thrown by the underlying input stream
	 * @throws IncompatibleExecDataVersionException
	 *             incompatible data version from different JaCoCo release
	 */
	public boolean read() throws IOException,
			IncompatibleExecDataVersionException {
		try {
			byte type;
			do {
				
				int i = in.read();
	            if (i == -1) {
	                return false; // EOF
	            }
	            type = (byte) i;
				
				if (type == ExecutionDataWriter.BLOCK_FIRSTHAND) {
					System.out.println("ExecutionDataWriter.BLOCK_FIRSTHAND");
					continue;
				}
				
				//解决心跳文件，服务端可以发送心跳消息来检测客户端是否存在了
				if (type == ExecutionDataWriter.BLOCK_HEART) {
                    System.out.println("ExecutionDataWriter.BLOCK_HEART");
                    continue;
                }
				
				//如果不是初次建立的client，那么将直接读取数据，而不判断第一个字符是否是ExecutionDataWriter.BLOCK_HEADER
				if (!isNewClient) {
				    
				    if (firstBlock && type != ExecutionDataWriter.BLOCK_HEADER) {
	                    throw new IOException("Invalid execution data file.");
	                }
	                firstBlock = false;
				}
				
			} while (readBlock(type));
			return true;
		} catch (final EOFException e) {
			return false;
		}
	}

	/**
	 * Reads a block of data identified by the given id. Subclasses may
	 * overwrite this method to support additional block types.
	 * 
	 * @param blocktype
	 *            block type
	 * @return <code>true</code> if there are more blocks to read
	 * @throws IOException
	 *             might be thrown by the underlying input stream
	 */
	protected boolean readBlock(final byte blocktype) throws IOException {
		switch (blocktype) {
		case ExecutionDataWriter.BLOCK_HEADER:
			readHeader();
			return true;
		case ExecutionDataWriter.BLOCK_SESSIONINFO:
			readSessionInfo();
			return true;
		case ExecutionDataWriter.BLOCK_EXECUTIONDATA:
			readExecutionData();
			return true;
		case ExecutionDataWriter.BLOCK_FIRSTHAND:
			//读取id实例后停止此次操作，后续的数据读取待后面完成
			readClientId();
			return false;
		case ExecutionDataWriter.BLOCK_HEART:
            //心跳要持续读取
            readHeart();
            return true;
		default:
			throw new IOException(format("Unknown block type %x.",
					Byte.valueOf(blocktype)));
		}
	}

	private void readHeader() throws IOException {
		if (in.readChar() != ExecutionDataWriter.MAGIC_NUMBER) {
			throw new IOException("Invalid execution data file.");
		}
		final char version = in.readChar();
		if (version != ExecutionDataWriter.FORMAT_VERSION) {
			throw new IncompatibleExecDataVersionException(version);
		}
	}

	private void readSessionInfo() throws IOException {
		if (sessionInfoVisitor == null) {
			throw new IOException("No session info visitor.");
		}
		final String id = in.readUTF();
		final long start = in.readLong();
		final long dump = in.readLong();
		sessionInfoVisitor.visitSessionInfo(new SessionInfo(id, start, dump));
	}

	
	private void readClientId() throws IOException {
		
		long id = in.readLong();
		int type = in.readInt();
		System.out.println("in.readLong(): " + id);
		clientInfoVisitor.visitClientInfo(new ClientInfo(id, type));
	}
	
	/**
	 * 心跳，为了检测当前的client是否存在了
	 * @throws IOException
	 */
	private void readHeart() throws IOException {
        
        long id = in.readLong();
        System.out.println("in.readLong(): " + id);
    }
	
	private void readExecutionData() throws IOException {
		if (executionDataVisitor == null) {
			throw new IOException("No execution data visitor.");
		}
		final long id = in.readLong();
		final String name = in.readUTF();
		final boolean[] probes = in.readBooleanArray();
		executionDataVisitor.visitClassExecution(new ExecutionData(id, name,
				probes));
	}

	public void setClientId() {
		
	}

}
