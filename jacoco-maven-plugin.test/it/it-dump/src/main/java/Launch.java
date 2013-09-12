/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Chas Honton - initial implementation
 *
 *******************************************************************************/
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Class to launch a java process
 */
public class Launch {

	/**
	 * Launch Example in another process. Do not wait for Process to exit
	 * 
	 * @param arguments
	 *            A single argument - the vm javaagent argument
	 * @throws Exception
	 */
	public static void main(final String[] arguments) throws Exception {
		final File javaHome = new File(System.getProperty("java.home"));
		final File javaBin = new File(new File(javaHome, "bin"), "java");

		final String javaPath = javaBin.getCanonicalPath();
		final String javaAgent = arguments[0];
		final String classpath = System.getProperty("java.class.path");

		System.err.println("launching: " + javaPath + ' ' + javaAgent + ' '
				+ "-cp" + ' ' + classpath + ' ' + "Example");
		final ProcessBuilder builder = new ProcessBuilder(javaPath, javaAgent,
				"-cp", classpath, "Example");

		builder.redirectErrorStream(true);
		final Process process = builder.start();
		process.getOutputStream().close();
		final InputStream is = process.getInputStream();
		final Thread streamPump = new Thread() {
			@Override
			public void run() {
				copy(is, System.out);
			}
		};
		streamPump.setDaemon(true);
		streamPump.start();
	}

	private static void copy(final InputStream is, final PrintStream out) {
		try {
			for (;;) {
				final int c = is.read();
				if (c < 0) {
					break;
				}
				out.append((char) c);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
