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
package org.jacoco.cli.internal;

import java.util.AbstractList;

import org.jacoco.cli.internal.commands.AllCommands;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Messages;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * {@link OptionHandler} which uses {@link CommandParser} internally to provide
 * help context also for sub-commands.
 */
public class CommandHandler extends OptionHandler<Command> {

	/**
	 * This constructor is required by the args4j framework.
	 *
	 * @param parser
	 * @param option
	 * @param setter
	 */
	public CommandHandler(final CmdLineParser parser, final OptionDef option,
			final Setter<Object> setter) {
		super(parser,
				new OptionDef(AllCommands.names(), "<command>",
						option.required(), option.help(), option.hidden(),
						CommandHandler.class, option.isMultiValued()) {
				}, setter);
	}

	@Override
	public int parseArguments(final Parameters params) throws CmdLineException {
		final String subCmd = params.getParameter(0);

		for (final Command c : AllCommands.get()) {
			if (c.name().equals(subCmd)) {
				parseSubArguments(c, params);
				setter.addValue(c);
				return params.size(); // consume all the remaining tokens
			}
		}

		throw new CmdLineException(owner,
				Messages.ILLEGAL_OPERAND.format(option.toString(), subCmd));
	}

	private void parseSubArguments(final Command c, final Parameters params)
			throws CmdLineException {
		final CmdLineParser p = new CommandParser(c);
		p.parseArgument(new AbstractList<String>() {
			@Override
			public String get(final int index) {
				try {
					return params.getParameter(index + 1);
				} catch (final CmdLineException e) {
					// invalid index was accessed.
					throw new IndexOutOfBoundsException();
				}
			}

			@Override
			public int size() {
				return params.size() - 1;
			}
		});
	}

	@Override
	public String getDefaultMetaVariable() {
		return "<command>";
	}

}
