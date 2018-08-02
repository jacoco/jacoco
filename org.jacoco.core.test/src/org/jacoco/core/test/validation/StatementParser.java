/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * foo("BAR", 42);
 * </pre>
 * 
 * Method parameters can only be String or int literals.
 */
public class StatementParser {

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
		tokenizer.whitespaceChars(' ', ' ');
		tokenizer.whitespaceChars('\t', '\t');
		tokenizer.ordinaryChar('(');
		tokenizer.ordinaryChar(')');
		tokenizer.ordinaryChar(',');
		tokenizer.ordinaryChar(';');
		tokenizer.wordChars('a', 'z');
		tokenizer.wordChars('A', 'Z');
		tokenizer.quoteChar('"');
	}

	private void parse() throws IOException {
		while (!accept(StreamTokenizer.TT_EOF)) {
			invocation();
		}
	}

	private void invocation() throws IOException {
		final String name = expectWord();
		final List<Object> args = new ArrayList<Object>();
		expect('(');
		boolean more = false;
		while (!accept(')')) {
			if (more) {
				expect(',');
			}
			arg(args);
			more = true;
		}
		expect(';');
		visitor.visitInvocation(ctx, name, args.toArray());
	}

	private void arg(List<Object> result) throws IOException {
		if (accept(StreamTokenizer.TT_NUMBER)) {
			result.add(Integer.valueOf((int) tokenizer.nval));
			return;
		}
		if (accept('"')) {
			result.add(tokenizer.sval);
			return;
		}
		syntaxError();
	}

	private boolean accept(final int type) throws IOException {
		if (tokenizer.nextToken() == type) {
			return true;
		} else {
			tokenizer.pushBack();
			return false;
		}
	}

	private String expectWord() throws IOException {
		expect(StreamTokenizer.TT_WORD);
		return tokenizer.sval;
	}

	private void expect(final int type) throws IOException {
		if (tokenizer.nextToken() != type) {
			syntaxError();
		}
	}

	private void syntaxError() throws IOException {
		throw new IOException("Invalid syntax in " + ctx);
	}

}
