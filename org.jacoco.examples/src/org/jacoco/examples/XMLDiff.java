/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.examples;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This example reads two JaCoCo XML reports and displays difference between
 * them.
 */
public final class XMLDiff {

	private XMLDiff() {
	}

	/**
	 * Executes this example.
	 *
	 * @param args
	 *            arguments
	 * @throws Exception
	 *             in case of errors
	 */
	public static void main(final String[] args) throws Exception {
		compare(System.out, read(new FileInputStream(args[0])),
				read(new FileInputStream(args[1])));
	}

	/**
	 * Compares two reports.
	 *
	 * @param out
	 *            stream for output
	 * @param left
	 *            first report
	 * @param right
	 *            second report
	 */
	static void compare(final PrintStream out,
			final Map<String, Map<Integer, LineInfo>> left,
			final Map<String, Map<Integer, LineInfo>> right) {
		compare(left, right, new Callback<String, Map<Integer, LineInfo>>() {
			public void inLeft(final String sourceName,
					final Map<Integer, LineInfo> value) {
				out.println("-" + sourceName);
			}

			public void inRight(final String sourceName,
					final Map<Integer, LineInfo> value) {
				out.println("+" + sourceName);
			}

			public void inBoth(final String sourceName,
					final Map<Integer, LineInfo> left,
					final Map<Integer, LineInfo> right) {
				compare(left, right, new Callback<Integer, LineInfo>() {
					public void inLeft(final Integer line,
							final LineInfo value) {
						out.print(" " + sourceName + ":" + line);
						out.print(" mi: -" + value.mi);
						out.print(" ci: -" + value.ci);
						out.print(" mb: -" + value.mb);
						out.print(" cb: -" + value.cb);
						out.println();
					}

					public void inRight(final Integer line,
							final LineInfo value) {
						out.print(" " + sourceName + ":" + line);
						out.print(" mi: +" + value.mi);
						out.print(" ci: +" + value.ci);
						out.print(" mb: +" + value.mb);
						out.print(" cb: +" + value.cb);
						out.println();
					}

					public void inBoth(final Integer line, final LineInfo left,
							final LineInfo right) {
						final StringBuilder sb = new StringBuilder();
						if (left.mi != right.mi) {
							sb.append(" mi: -").append(left.mi).append(" +")
									.append(right.mi);
						}
						if (left.ci != right.ci) {
							sb.append(" ci: -").append(left.ci).append(" +")
									.append(right.ci);
						}
						if (left.mb != right.mb) {
							sb.append(" mb: -").append(left.mb).append(" +")
									.append(right.mb);
						}
						if (left.cb != right.cb) {
							sb.append(" cb: -").append(left.cb).append(" +")
									.append(right.cb);
						}
						if (sb.length() != 0) {
							out.println(" " + sourceName + ":" + line
									+ sb.toString());
						}
					}
				});
			}
		});
	}

	private interface Callback<K, V> {

		void inLeft(K key, V value);

		void inRight(K key, V value);

		void inBoth(K key, V left, V right);

	}

	private static <K, V> void compare(final Map<K, V> left,
			final Map<K, V> right, final Callback<K, V> callback) {
		final Set<K> keys = new HashSet<K>();
		keys.addAll(left.keySet());
		keys.addAll(right.keySet());
		for (K key : keys) {
			final V leftValue = left.get(key);
			final V rightValue = right.get(key);
			if (leftValue == null) {
				callback.inRight(key, rightValue);
			} else if (rightValue == null) {
				callback.inLeft(key, leftValue);
			} else {
				callback.inBoth(key, leftValue, rightValue);
			}
		}
	}

	/**
	 * Reads JaCoCo XML report.
	 *
	 * @param in
	 *            stream to read XML report from
	 * @return parsed report
	 * @throws Exception
	 *             in case of errors
	 */
	static Map<String, Map<Integer, LineInfo>> read(final InputStream in)
			throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();
		final ReportHandler handler = new ReportHandler();
		parser.parse(in, handler);
		return handler.r;
	}

	static final class LineInfo {
		final int mi;
		final int ci;
		final int mb;
		final int cb;

		LineInfo(final int mi, final int ci, final int mb, final int cb) {
			this.mi = mi;
			this.ci = ci;
			this.mb = mb;
			this.cb = cb;
		}
	}

	private static class ReportHandler extends DefaultHandler {
		final Map<String, Map<Integer, LineInfo>> r = new HashMap<String, Map<Integer, LineInfo>>();

		private String packageName;
		private Map<Integer, LineInfo> s;

		@Override
		public void startElement(final String uri, final String localName,
				final String qName, final Attributes attributes) {
			if ("package".equals(qName)) {
				packageName = attributes.getValue("name");

			} else if ("sourcefile".equals(qName)) {
				final String fileName = attributes.getValue("name");
				final String name = packageName + "/" + fileName;
				s = new HashMap<Integer, LineInfo>();
				if (r.put(name, Collections.unmodifiableMap(s)) != null) {
					throw new IllegalStateException("duplicate " + name);
				}

			} else if ("line".equals(qName)) {
				final int lineNumber = Integer
						.parseInt(attributes.getValue("nr"));
				final int mi = parseAsOptionalInt(attributes, "mi");
				final int ci = parseAsOptionalInt(attributes, "ci");
				final int mb = parseAsOptionalInt(attributes, "mb");
				final int cb = parseAsOptionalInt(attributes, "cb");
				s.put(lineNumber, new LineInfo(mi, ci, mb, cb));
			}
		}

		private static int parseAsOptionalInt(final Attributes attributes,
				final String name) {
			final String value = attributes.getValue(name);
			if (value == null) {
				return 0;
			}
			return Integer.parseInt(value);
		}

		@Override
		public InputSource resolveEntity(final String publicId,
				final String systemId) {
			if (publicId.startsWith("-//JACOCO//DTD Report")) {
				return new InputSource(new StringReader(""));
			}
			return null;
		}
	}

}
