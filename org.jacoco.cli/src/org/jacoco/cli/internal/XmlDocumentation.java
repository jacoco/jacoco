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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.jacoco.cli.internal.commands.AllCommands;
import org.jacoco.report.internal.xml.XMLElement;
import org.kohsuke.args4j.spi.OptionHandler;

/**
 * Internal utility to dump all command descriptions as XML.
 */
public final class XmlDocumentation {

	private XmlDocumentation() {
	}

	private static void writeCommand(final Command command,
			final XMLElement parent) throws IOException {
		final CommandParser parser = new CommandParser(command);
		final XMLElement element = parent.element("command");
		element.attr("name", command.name());
		element.element("usage").text(command.usage(parser));
		element.element("description").text(command.description());
		writeOptions(element, parser.getArguments());
		writeOptions(element, parser.getOptions());
	}

	private static void writeOptions(final XMLElement parent,
			@SuppressWarnings("rawtypes") final List<OptionHandler> list)
			throws IOException {
		for (final OptionHandler<?> o : list) {
			final XMLElement optionNode = parent.element("option");
			optionNode.attr("required", String.valueOf(o.option.required()));
			optionNode.attr("multiple",
					String.valueOf(o.setter.isMultiValued()));
			optionNode.element("usage").text(o.getNameAndMeta(null));
			optionNode.element("description").text(o.option.usage());
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

		for (final Command c : AllCommands.get()) {
			writeCommand(c, root);
		}

		root.close();
	}

}
