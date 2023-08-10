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
package org.jacoco.cli.internal.commands;

import java.io.IOException;
import java.io.PrintWriter;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.JaCoCo;

/**
 * The <code>version</code> command.
 */
public class Version extends Command {

	@Override
	public String description() {
		return "Print JaCoCo version information.";
	}

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws IOException {
		out.println(JaCoCo.VERSION);
		return 0;
	}

}
