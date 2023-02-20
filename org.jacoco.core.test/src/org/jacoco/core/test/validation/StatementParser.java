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
package org.jacoco.core.test.validation;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple parser for Java like method invocation statements. For example:
 *
 * <pre>
 * foo("BAR", 42)
 * </pre>
 *
 * Method parameters can only be String or int literals.
 */
class StatementParser {

	/**
	 * Call-back interface for parsed statements.
	 */
	public interface IStatementVisitor {

		/**
		 * Called when a method invocation has been paresed.
		 *
		 * @param ctx
		 *            context information for error messages
		 * @param name
		 *            local method name
		 * @param args
		 *            argument values
		 */
		void visitInvocation(String ctx, String name, Object... args);

	}

	/**
	 * Parses the given source.
	 *
	 * @param source
	 *            source string to parse
	 * @param visitor
	 *            visitor to emit parsed statements
	 * @param ctx
	 *            context information to include in error messages
	 */
	public static void parse(String source, IStatementVisitor visitor,
			String ctx) throws IOException {
		new StatementParser(source, visitor, ctx).parse();
	}

	private final IStatementVisitor visitor;
	private final StreamTokenizer tokenizer;
	private final String ctx;

	private StatementParser(String source, IStatementVisitor visitor,
			String ctx) {
		this.visitor = visitor;
		this.ctx = ctx;
		tokenizer = new StreamTokenizer(new StringReader(source));
		tokenizer.resetSyntax();
		tokenizer.whitespaceChars(' ', ' ');
		tokenizer.whitespaceChars('\t', '\t');
		tokenizer.wordChars('a', 'z');
		tokenizer.wordChars('A', 'Z');
		tokenizer.quoteChar('"');
		tokenizer.parseNumbers();
	}

	private void parse() throws IOException {
		while (!accept(StreamTokenizer.TT_EOF)) {
			invocation();
		}
	}

	private void invocation() throws IOException {
		final String name = expect(StreamTokenizer.TT_WORD).sval;
		final List<Object> args = new ArrayList<Object>();
		expect('(');
		if (!accept(')')) {
			args.add(argument());
			while (!accept(')')) {
				expect(',');
				args.add(argument());
			}
		}
		visitor.visitInvocation(ctx, name, args.toArray());
	}

	private Object argument() throws IOException {
		if (accept(StreamTokenizer.TT_NUMBER)) {
			return Integer.valueOf((int) tokenizer.nval);
		}
		if (accept('"')) {
			return tokenizer.sval;
		}
		throw syntaxError();
	}

	private boolean accept(final int type) throws IOException {
		final boolean match = tokenizer.nextToken() == type;
		if (!match) {
			tokenizer.pushBack();
		}
		return match;
	}

	private StreamTokenizer expect(final int type) throws IOException {
		if (tokenizer.nextToken() != type) {
			throw syntaxError();
		}
		return tokenizer;
	}

	private IOException syntaxError() {
		return new IOException("Invalid syntax (" + ctx + ")");
	}

}
