/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.agent.rt.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jacoco.core.internal.data.CRC64;

/**
 * Internal dumper for class files.
 */
class ClassFileDumper {

	private final File location;

	/**
	 * Create a new dumper for the given location.
	 *
	 * @param location
	 *            relative path to dump directory. <code>null</code> if no dumps
	 *            should be written
	 */
	ClassFileDumper(final String location) {
		if (location == null) {
			this.location = null;
		} else {
			this.location = new File(location);
		}
	}

	/**
	 * Dumps the given binary content under the given name if a non-
	 * <code>null</code> location has been specified.
	 *
	 * @param name
	 *            qualified class name in VM notation
	 * @param contents
	 *            binary contents
	 * @throws IOException
	 *             in case of problems while dumping the file
	 */
	void dump(final String name, final byte[] contents) throws IOException {
		if (location != null) {
			final File outputdir;
			final String localname;
			final int pkgpos = name.lastIndexOf('/');
			if (pkgpos != -1) {
				outputdir = new File(location, name.substring(0, pkgpos));
				localname = name.substring(pkgpos + 1);
			} else {
				outputdir = location;
				localname = name;
			}
			outputdir.mkdirs();
			final Long id = Long.valueOf(CRC64.classId(contents));
			final File file = new File(outputdir,
					String.format("%s.%016x.class", localname, id));
			final OutputStream out = new FileOutputStream(file);
			out.write(contents);
			out.close();
		}
	}

}
