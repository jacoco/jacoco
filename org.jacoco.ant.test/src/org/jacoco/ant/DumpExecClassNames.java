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

import java.io.File;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.tools.ExecFileLoader;

/**
 * Test utility to dump class names from exec file.
 */
public class DumpExecClassNames {

	public static void main(String[] args) throws Exception {
		final ExecFileLoader loader = new ExecFileLoader();
		for (String f : args) {
			loader.load(new File(f));
		}
		for (ExecutionData d : loader.getExecutionDataStore().getContents()) {
			System.out.println(d.getName());
		}
	}

}
