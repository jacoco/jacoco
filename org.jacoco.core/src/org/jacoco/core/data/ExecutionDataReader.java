/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.data;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jacoco.core.internal.data.CompactDataInput;
import org.jacoco.core.internal.data.CompactDataOutput;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * Deserialization of execution data from binary streams.
 */
public class ExecutionDataReader {

	/** Underlying data input */
	protected final CompactDataInput in;

	private ISessionInfoVisitor sessionInfoVisitor = null;

	private IExecutionDataVisitor executionDataVisitor = null;

	private boolean firstBlock = true;

	private CompactDataOutput out;

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

	public ExecutionDataReader(final InputStream input,
							   final OutputStream outputStream) {
		this.in = new CompactDataInput(input);
		this.out = new CompactDataOutput(outputStream);
	}

	/**
	 * Sets a listener for session information.
	 *
	 * @param visitor
	 *            visitor to retrieve session info events
	 */
	public void setSessionInfoVisitor(final ISessionInfoVisitor visitor) {
		this.sessionInfoVisitor = visitor;
	}

	/**
	 * Sets a listener for execution data.
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
	public boolean read()
			throws IOException, IncompatibleExecDataVersionException {
		byte type;
		do {
			int i = in.read();
			if (i == -1) {
				return false; // EOF
			}
			type = (byte) i;
			// 增加下载报表系统的bbzxjar reportviewjar websitejar
			if (type == RemoteControlWriter.BLOCK_DOWNBBZX
					|| type == RemoteControlWriter.BLOCK_DOWNREPORTVIEW
					|| type == RemoteControlWriter.BLOCK_DOWNWEBSITE) {
				try {
					downJar(type);
				} catch (IOException e) {
					System.out
							.println(type + "jar包下载失败，失败原因:" + e.getMessage());
				} finally {
					return false;
				}
			}
			if (firstBlock && type != ExecutionDataWriter.BLOCK_HEADER) {
				throw new IOException("Invalid execution data file.");
			}
			firstBlock = false;
			// 藏得很深，具体解析在 readBlock 这个方法
		} while (readBlock(type));
		return true;
	}


	public boolean downJar(byte type) throws IOException {
		String jarFile;
		switch (type) {
			case RemoteControlWriter.BLOCK_DOWNBBZX:
				jarFile = getJarFilepath("com.yss.ams.bbzx");
				downLoadJar(jarFile);
				return true;
			case RemoteControlWriter.BLOCK_DOWNREPORTVIEW:
				jarFile = getJarFilepath("com.yss.ams.ReportViewer");
				downLoadJar(jarFile);
				return true;
			case RemoteControlWriter.BLOCK_DOWNWEBSITE:
				jarFile = getJarFilepath("com.yss.ams.website");
				downLoadJar(jarFile);
				return true;
			default:
				return false;
		}
	}

	private void downLoadJar(String filename) throws IOException {
		if (filename != null) {
			FileInputStream fis = new FileInputStream(filename);
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = fis.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		}
	}

	public String getJarFilepath(String jarName) {
		String sofaHome = System.getenv("SOFA_HOME");
		System.out.println("从环境变量读取到的sofa_home=" + sofaHome);
		String dir = sofaHome + File.separator + "soft" + File.separator
				+ "tomcat" + File.separator + "webapps" + File.separator
				+ "sofa" + File.separator + "WEB-INF" + File.separator
				+ "sofa-container" + File.separator + "repository"
				+ File.separator + "sofa";
		File f = new File(dir);
		if (!f.exists() || !f.isDirectory()) {
			return null;
		}
		for (File file : new File(dir).listFiles()) {
			if (file.getName().startsWith(jarName)) {
				return file.getAbsolutePath();
			}
		}
		return null;
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
			// exec 文件开头会有个头字节码校验，文件头部信息，标识文件格式、版本等
			readHeader();
			return true;
		case ExecutionDataWriter.BLOCK_SESSIONINFO:
			// 会话信息块：记录本次测试运行的会话信息，比如运行时间、JVM 信息等
			readSessionInfo();
			return true;
		case ExecutionDataWriter.BLOCK_EXECUTIONDATA:
			// 入口在这里
			readExecutionData();
			return true;
		default:
			throw new IOException(
					format("Unknown block type %x.", Byte.valueOf(blocktype)));
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
		// 当前会话id，机器名+随机字符串
		final String id = in.readUTF();
		// 测试时间
		final long start = in.readLong();
		// dump 时间
		final long dump = in.readLong();
		sessionInfoVisitor.visitSessionInfo(new SessionInfo(id, start, dump));
	}

	private void readExecutionData() throws IOException {
		if (executionDataVisitor == null) {
			throw new IOException("No execution data visitor.");
		}
		final long id = in.readLong();
		final String name = in.readUTF();
		// 从流中获取探针信息
		final boolean[] probes = in.readBooleanArray();
		executionDataVisitor
				.visitClassExecution(new ExecutionData(id, name, probes));
	}

}
