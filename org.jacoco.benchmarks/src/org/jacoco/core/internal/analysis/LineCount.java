/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LineCount {

	private final int instructionsLimit = 32;
	private final int branchesLimit = 16;
	private final int[][] counts = new int[instructionsLimit + 1][branchesLimit
			+ 1];
	private int total = 0;

	private void process(String path) throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(new ExecutionDataStore(),
				coverageBuilder) {
			@Override
			public int analyzeAll(final File file) throws IOException {
				if (file.getName().endsWith(".jmod")) {
					return analyzeJmod(new FileInputStream(file),
							file.getPath());
				}
				return super.analyzeAll(file);
			}

			private int analyzeJmod(final InputStream input,
					final String location) throws IOException {
				input.skip(4); // JM\003\004
				final ZipInputStream zip = new ZipInputStream(input);
				ZipEntry entry;
				int count = 0;
				while ((entry = zip.getNextEntry()) != null) {
					count += analyzeAll(zip, location + "@" + entry.getName());
				}
				return count;
			}

			@Override
			public void analyzeClass(final byte[] buffer, final String location)
					throws IOException {
				try {
					super.analyzeClass(buffer, location);
				} catch (final IOException e) {
					if (!e.getCause().getMessage().startsWith(
							"Can't add different class with same name:")) {
						throw e;
					}
				}
			}
		};
		analyzer.analyzeAll(new File(path));
		for (final IClassCoverage classCoverage : coverageBuilder
				.getClasses()) {
			for (final IMethodCoverage methodCoverage : classCoverage
					.getMethods()) {
				for (int line = methodCoverage
						.getFirstLine(); line <= methodCoverage
								.getLastLine(); line++) {
					final ILine methodLine = methodCoverage.getLine(line);
					if (methodLine.getInstructionCounter()
							.getTotalCount() == 0) {
						continue;
					}
					total++;
					int it = methodLine.getInstructionCounter().getTotalCount();
					int bt = methodCoverage.getBranchCounter().getTotalCount();
					if (it <= instructionsLimit && bt <= branchesLimit) {
						counts[it][bt]++;
					}
				}
			}
		}
	}

	private void print() {
		int current = 0;
		int all_8_8 = 0;
		int even_8_8 = 0;
		int all_16_4 = 0;
		int even_16_6 = 0;
		int all_16_6 = 0;
		int even_16_8 = 0;
		int all_16_8 = 0;
		int even_32_4 = 0;
		for (int i = 0; i <= instructionsLimit; i++) {
			for (int b = 0; b <= branchesLimit; b++) {
				int count = counts[i][b];
				System.out.printf("%8d,", count);

				if (i <= 8 && b <= 4) {
					current += count;
				}
				if (i <= 8 && b <= 8) {
					all_8_8 += count;
					if (b % 2 == 0) {
						even_8_8 += count;
					}
				}
				if (i <= 16 && b <= 4) {
					all_16_4 += count;
				}
				if (i <= 16 && b <= 6) {
					all_16_6 += count;
					if (b % 2 == 0) {
						even_16_6 += count;
					}
				}
				if (i <= 16 && b <= 8) {
					all_16_8 += count;
					if (b % 2 == 0) {
						even_16_8 += count;
					}
				}
				if (i <= 32 && b <= 4 && b % 2 == 0) {
					even_32_4 += count;
				}
			}
			System.out.println();
		}

		// ~ 68 KB
		System.out.println("Total lines in IMethodCoverage nodes: " + total);
		System.out.printf("Current guaranteed: %.2f%%\n", //
				100.0 * current / total);

		// 15_004 instances, 425_688 bytes
		System.out.printf("       Even ( 8,8): %.2f%%\n",
				100.0 * even_8_8 / total);
		System.out.printf("        All ( 8,8): %.2f%%\n",
				100.0 * all_8_8 / total);

		// 4_712 instances, 149_624 bytes
		System.out.printf("        All (16,4): %.2f%%\n",
				100.0 * all_16_4 / total);

		// 12_920 instances, 376_032 bytes
		System.out.printf("       Even (16,6): %.2f%%\n",
				100.0 * even_16_6 / total);
		System.out.printf("        All (16,6): %.2f%%\n",
				100.0 * all_16_6 / total);

		// 51_832 instances, 1_468_984 bytes
		System.out.printf("       Even (16,8): %.2f%%\n",
				100.0 * even_16_8 / total);
		// 2_206_152 bytes
		System.out.printf("        All (16,8): %.2f%%\n",
				100.0 * all_16_8 / total);

		// 11_760 instances, 367_312 bytes
		System.out.printf("       Even (32,4): %.2f%%\n",
				100.0 * even_32_4 / total);
	}

	private static void count(String... paths) throws IOException {
		System.out.println(Arrays.toString(paths));
		final LineCount count = new LineCount();
		for (final String path : paths) {
			count.process(path);
		}
		count.print();
	}

	private static String user(String path) {
		return System.getProperty("user.home") + "/" + path;
	}

	private static String maven(final String groupId, final String artifactId,
			final String version, final String classifier, final String type) {
		final String path = user(".m2/repository/" + groupId.replace('.', '/')
				+ "/" + artifactId + "/" + version + "/" + artifactId + "-"
				+ version + (classifier == null ? "" : "-" + classifier) + "."
				+ type);
		if (!new File(path).exists()) {
			throw new IllegalStateException("\n" + path + "\n" + //
					"mvn dependency:get" + //
					" -DgroupId=" + groupId.replace('/', '.') //
					+ " -DartifactId=" + artifactId //
					+ " -Dversion=" + version //
					+ (classifier == null ? "" : "-Dclassifier=" + classifier) //
					+ " -Dpackaging=" + type);
		}
		return path;
	}

	private static String maven(final String groupId, final String artifactId,
			final String version) {
		return maven(groupId, artifactId, version, null, "jar");
	}

	public static void main(String[] args) throws IOException {
		count(user(".java-select/versions/25/jmods/java.base.jmod"));
		count(user("eclipse/Eclipse.app/Contents"));
		count(user("Applications/IntelliJ IDEA.app/Contents"));
		count(maven("org/jetbrains/kotlin", "kotlin-compiler", "2.3.20"));
		count(maven("org/eclipse/jdt", "ecj", "3.44.0"));
		count(maven("org.eclipse.collections", "eclipse-collections",
				"13.0.0"));
		count(maven("com/google/guava", "guava", "33.6.0-jre"));
		count(maven("org/http4k", "http4k-core", "6.42.0.0"));
		count(maven("org/http4k", "http4k-core", "6.42.0.0"));
		count(maven("io/mockk", "mockk", "1.9.3"));
		count(maven("org/junit/jupiter", "junit-jupiter-engine", "6.0.3"));
		count(maven("junit", "junit", "4.13.2"));
		count(maven("org/ow2/asm", "asm", "9.9.1"));
		count("org.jacoco.core/target/classes");
	}

}
