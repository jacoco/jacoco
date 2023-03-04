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
package org.jacoco.ant;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;

/**
 * Utility class to create exec files required for some Ant tests.
 */
public class CreateExecFiles {

	private static final String BASE_LOCATION = "./src/org/jacoco/ant/data/";

	public static void main(String[] args) throws IOException {
		OutputStream out;

		out = new FileOutputStream(BASE_LOCATION + "sample1.exec");
		new ExecutionDataWriter(out);
		out.close();

		out = new FileOutputStream(BASE_LOCATION + "sample2.exec");
		new ExecutionDataWriter(out);
		out.close();

		out = new FileOutputStream(BASE_LOCATION + "nomatch.exec");
		ExecutionDataWriter writer = new ExecutionDataWriter(out);
		writer.visitClassExecution(new ExecutionData(0,
				"org/jacoco/ant/TestTarget", new boolean[0]));
		out.close();
	}

}
