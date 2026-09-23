/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.jacoco.report.internal.xml.XMLElement;

import picocli.CommandLine;

/**
 * Internal utility to dump all command descriptions as XML.
 */
public final class XmlDocumentation {

	private XmlDocumentation() {
	}

	private static void writeCommand(final CommandLine.Help command,
			final XMLElement parent) throws IOException {
		final XMLElement element = parent.element("command");
		element.attr("name", command.commandSpec().name());
		element.element("usage").text(command.synopsis(0));
		element.element("description").text(command.description());
		writeOptions(element, command.commandSpec().positionalParameters());
		writeOptions(element, command.commandSpec().options());
	}

	private static void writeOptions(final XMLElement parent,
			final List<? extends CommandLine.Model.ArgSpec> args)
			throws IOException {
		for (final CommandLine.Model.ArgSpec arg : args) {
			final XMLElement optionNode = parent.element("option");
			optionNode.attr("required", String.valueOf(arg.required()));
			optionNode.attr("multiple", String.valueOf(arg.isMultiValue()));
			if (arg.isPositional()) {
				optionNode.element("usage").text(arg.paramLabel());
			} else {
				final CommandLine.Model.OptionSpec optionSpec = (CommandLine.Model.OptionSpec) arg;
				optionNode.element("usage")
						.text(optionSpec.longestName()
								+ (arg.typeInfo().isBoolean() ? ""
										: " " + arg.paramLabel()));
			}
			optionNode.element("description").text(arg.description()[0]);
		}
	}

	/**
	 * Called during the build process.
	 *
	 * @param args
	 *            exactly one argument expected with the target location
	 * @throws IOException
	 *             if XML document cannot be written
	 */
	public static void main(final String... args) throws IOException {
		final File file = new File(args[0]);
		file.getParentFile().mkdirs();

		final XMLElement root = new XMLElement("documentation", null, null,
				true, "UTF-8", new FileOutputStream(file));

		final CommandLine commandLine = Main.commandLine(
				CommandLine.Help.Ansi.OFF, //
				new PrintWriter(System.out, true),
				new PrintWriter(System.err, true));
		commandLine.setUsageHelpWidth(2048);
		for (final CommandLine.Help command : commandLine.getHelp()
				.subcommands().values()) {
			writeCommand(command, root);
		}

		root.close();
	}

}
