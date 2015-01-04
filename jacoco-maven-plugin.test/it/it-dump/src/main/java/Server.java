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

import java.io.File;

/**
 * Simple server which runs as long a termination file is created.
 */
public class Server {

	public static void main(String[] args) throws Exception {
		System.out.println("Test server started");

		// Wait for termination file to turn up
		// This option puts the target in a pseudo 'server' mode
		if (args.length == 1) {
			final File termFile = new File(args[0]);

			while (!termFile.exists()) {
				Thread.sleep(100);
			}
		}

		System.out.println("Test server stopped");
	}

}
