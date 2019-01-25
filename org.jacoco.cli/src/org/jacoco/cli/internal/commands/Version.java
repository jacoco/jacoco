/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
