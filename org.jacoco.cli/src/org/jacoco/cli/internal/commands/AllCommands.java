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

import java.util.Arrays;
import java.util.List;

import org.jacoco.cli.internal.Command;

/**
 * List of all available commands.
 */
public final class AllCommands {

	private AllCommands() {
	}

	/**
	 * @return list of new instances of all available commands
	 */
	public static List<Command> get() {
		return Arrays.asList(new Dump(), new Instrument(), new Merge(),
				new Report(), new ClassInfo(), new ExecInfo(), new Version());
	}

	/**
	 * @return String containing all available command names
	 */
	public static String names() {
		final StringBuilder sb = new StringBuilder();
		for (final Command c : get()) {
			if (sb.length() > 0) {
				sb.append('|');
			}
			sb.append(c.name());
		}
		return sb.toString();
	}

}
