/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 * <p>
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.analysis;

import org.jacoco.core.tools.ExecFileLoader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link CoverageBundleMethodFilterScalaImpl}.
 */
public class CoverageBundleMethodFilterScalaImplTest {

	private File getProjectDirectory(String projectName) {
		return new File(new File(".").getAbsolutePath(),
				"src/test/resources/" + projectName);
	}

	private IBundleCoverage prepareSourceBundle(final File projectDirectory) {
		File executionDataFile = new File(projectDirectory, "jacoco.exec");
		File classesDirectory = new File(projectDirectory, "bin");

		ExecFileLoader execFileLoader = new ExecFileLoader();
		try {
			execFileLoader.load(executionDataFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(
				execFileLoader.getExecutionDataStore(), coverageBuilder);

		try {
			analyzer.analyzeAll(classesDirectory);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return coverageBuilder.getBundle("test");
	}

	private static <T extends ICoverageNode> T findByName(Collection<T> nodes,
			String name) {
		return findByName(nodes, name, false);
	}

	private static <T extends ICoverageNode> T findByName(Collection<T> nodes,
			String name, boolean returnNull) {
		for (T node : nodes) {
			if (name.equals(node.getName())) {
				return node;
			}
		}

		if (returnNull)
			return null;
		else
			throw new AssertionError("Node not found: " + name);
	}

	private static IMethodCoverage findMethodByDesc(
			Collection<IMethodCoverage> nodes, String desc) {
		for (IMethodCoverage node : nodes) {
			if (desc.equals(node.getDesc())) {
				return node;
			}
		}

		return null;
	}

	@Test
	public void testClassInObject() {
		File projectDirectory = getProjectDirectory("01-class-in-object");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"org/apache/commons/configuration");
		assertEquals(34,
				packageCoverage.getInstructionCounter().getMissedCount());
		assertEquals(6, packageCoverage.getLineCounter().getMissedCount());
		assertEquals(6,
				packageCoverage.getComplexityCounter().getMissedCount());
		assertEquals(6, packageCoverage.getMethodCounter().getMissedCount());
		assertEquals(5, packageCoverage.getClassCounter().getMissedCount());
		assertEquals(0, packageCoverage.getBranchCounter().getMissedCount());

		assertEquals(5, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"org/apache/commons/configuration/SubsetConfigurationMethods");
		assertEquals(0, classCoverage.getMethods().size());

		// #2
		classCoverage = findByName(packageCoverage.getClasses(),
				"org/apache/commons/configuration/SubsetConfigurationMethods$SubsetConfigurationOps$");
		assertEquals(0, classCoverage.getMethods().size());

		// #3
		classCoverage = findByName(packageCoverage.getClasses(),
				"org/apache/commons/configuration/SubsetConfigurationMethods$");
		assertEquals(7, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(2, classCoverage.getLineCounter().getMissedCount());
		assertEquals(2, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(2, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(2, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(),
				"SubsetConfigurationOps", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #4
		classCoverage = findByName(packageCoverage.getClasses(),
				"org/apache/commons/configuration/SubsetConfigurationMethods$SubsetConfigurationOps");
		assertEquals(14,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(2, classCoverage.getLineCounter().getMissedCount());
		assertEquals(3, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(3, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(3, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "conf", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "getParentKey", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #5
		classCoverage = findByName(packageCoverage.getClasses(),
				"org/apache/commons/configuration/SubsetConfigurationMethods$SubsetConfigurationOps$$anonfun$getParentKey$extension$1");
		assertEquals(13,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "apply", true));
	}

	// @Test
	// public void testSoloClass() {
	// // no file with only class found - for now
	//
	// }

	@Test
	public void testSoloCObject() {
		File projectDirectory = getProjectDirectory("03-solo-object");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(), "za/co/absa/commons/io");
		assertEquals(41,
				packageCoverage.getInstructionCounter().getMissedCount());
		assertEquals(7, packageCoverage.getLineCounter().getMissedCount());
		assertEquals(5,
				packageCoverage.getComplexityCounter().getMissedCount());
		assertEquals(4, packageCoverage.getMethodCounter().getMissedCount());
		assertEquals(2, packageCoverage.getClassCounter().getMissedCount());
		assertEquals(2, packageCoverage.getBranchCounter().getMissedCount());

		assertEquals(2, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/io/LocalFileSystemUtils");
		assertEquals(0, classCoverage.getMethods().size());

		// #2
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/io/LocalFileSystemUtils$");
		assertEquals(41,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(7, classCoverage.getLineCounter().getMissedCount());
		assertEquals(5, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(4, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(2, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(4, classCoverage.getMethods().size());
		assertNotNull(
				findByName(classCoverage.getMethods(), "localExists", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "readLocalFile", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "replaceHome", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
	}

	@Test
	public void testTrait() {
		File projectDirectory = getProjectDirectory("04-trait");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/lang");
		assertEquals(29,
				packageCoverage.getInstructionCounter().getMissedCount());
		assertEquals(8, packageCoverage.getLineCounter().getMissedCount());
		assertEquals(5,
				packageCoverage.getComplexityCounter().getMissedCount());
		assertEquals(5, packageCoverage.getMethodCounter().getMissedCount());
		assertEquals(3, packageCoverage.getClassCounter().getMissedCount());
		assertEquals(0, packageCoverage.getBranchCounter().getMissedCount());

		assertEquals(5, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/lang/Converter");
		assertEquals(0, classCoverage.getMethods().size());

		// #2
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/lang/CachingConverter");
		assertEquals(0, classCoverage.getMethods().size());

		// #3
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/lang/Converter$class");
		assertEquals(4, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(2, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		// two diff signatures of apply
		assertNotNull(findByName(classCoverage.getMethods(), "apply", true));

		// #4
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/lang/CachingConverter$class");
		assertEquals(19,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(6, classCoverage.getLineCounter().getMissedCount());
		assertEquals(3, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(3, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(3, classCoverage.getMethods().size());
		// two diff signatures of apply
		assertNotNull(findByName(classCoverage.getMethods(), "values", true));
		assertNotNull(findByName(classCoverage.getMethods(), "keyOf", true));
		assertNotNull(findByName(classCoverage.getMethods(), "convert", true));

		// #5
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/lang/CachingConverter$$anonfun$convert$1");
		assertEquals(6, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		// two diff signatures of apply
		assertNotNull(findByName(classCoverage.getMethods(), "apply", true));
	}

	@Test
	public void testClassConstructorAttributes() {
		File projectDirectory = getProjectDirectory(
				"05-class-constructor-attributes");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/error");
		assertEquals(35,
				packageCoverage.getInstructionCounter().getMissedCount());
		assertEquals(6, packageCoverage.getLineCounter().getMissedCount());
		assertEquals(4,
				packageCoverage.getComplexityCounter().getMissedCount());
		assertEquals(4, packageCoverage.getMethodCounter().getMissedCount());
		assertEquals(2, packageCoverage.getClassCounter().getMissedCount());
		assertEquals(0, packageCoverage.getBranchCounter().getMissedCount());

		assertEquals(2, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/error/ErrorRef");
		assertEquals(14,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(4, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #2
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/error/ErrorRef$");
		assertEquals(21,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(3, classCoverage.getLineCounter().getMissedCount());
		assertEquals(3, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(3, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(3, classCoverage.getMethods().size());
		// two diff signatures of apply
		assertNotNull(findMethodByDesc(classCoverage.getMethods(),
				"(Ljava/lang/Throwable;Ljava/lang/String;)Lza/co/absa/commons/error/ErrorRef;"));
		assertNotNull(findMethodByDesc(classCoverage.getMethods(),
				"(Ljava/util/UUID;JLscala/Option;)Lza/co/absa/commons/error/ErrorRef;"));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
	}

	@Test
	public void testExceptionAnonfunAndTypecreator() {
		File projectDirectory = getProjectDirectory(
				"06-exception-anonfun-and-typecreator");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/config");
		assertEquals(84, packageCoverage.getClasses().size());

		// #1 1./3 typecreators
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/config/ConfigurationImplicits$$typecreator1$1");
		assertEquals(37,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "apply", true));

		// #2 2./3 typecreators
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/config/ConfigurationImplicits$$typecreator2$1");
		assertEquals(41,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		// two diff signatures of apply
		assertNotNull(findByName(classCoverage.getMethods(), "apply", true));

		// #3 3./3 typecreators
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/config/ConfigurationImplicits$$typecreator3$1");
		assertEquals(63,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		// two diff signatures of apply
		assertNotNull(findByName(classCoverage.getMethods(), "apply", true));

		// #4 anonfun example - short named
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/config/ConfigurationImplicits$$anonfun$11");
		assertEquals(9, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		// two diff signatures of apply
		assertNotNull(findByName(classCoverage.getMethods(), "apply", true));

		// #5 anonfun example - long named aka deep in object hierarchy
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/config/ConfigurationImplicits$ConfigurationOptionalWrapper$$anonfun$getOptionalDouble$extension$2");
		assertEquals(8, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		// two diff signatures of apply
		assertNotNull(findByName(classCoverage.getMethods(), "apply", true));
	}

	@Test
	public void testCompanionObjects() {
		File projectDirectory = getProjectDirectory("08-companion-objects");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(), "za/co/absa/commons/io");
		assertEquals(48,
				packageCoverage.getInstructionCounter().getMissedCount());
		assertEquals(10, packageCoverage.getLineCounter().getMissedCount());
		assertEquals(8,
				packageCoverage.getComplexityCounter().getMissedCount());
		assertEquals(7, packageCoverage.getMethodCounter().getMissedCount());
		assertEquals(2, packageCoverage.getClassCounter().getMissedCount());
		assertEquals(2, packageCoverage.getBranchCounter().getMissedCount());

		assertEquals(2, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/io/TempFile");
		assertEquals(36,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(7, classCoverage.getLineCounter().getMissedCount());
		assertEquals(6, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(5, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(2, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(5, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "path", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "deleteOnExit", true));
		assertNotNull(findByName(classCoverage.getMethods(), "toURI", true));
		assertNotNull(findByName(classCoverage.getMethods(), "asString", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #2
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/io/TempFile$");
		assertEquals(12,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(3, classCoverage.getLineCounter().getMissedCount());
		assertEquals(2, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(2, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(2, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "apply", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
	}

	@Test
	public void testLazyAndFinalVals() {
		File projectDirectory = getProjectDirectory("07-lazy-and-final_vals");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/buildinfo");
		assertEquals(94,
				packageCoverage.getInstructionCounter().getMissedCount());
		assertEquals(21, packageCoverage.getLineCounter().getMissedCount());
		assertEquals(16,
				packageCoverage.getComplexityCounter().getMissedCount());
		assertEquals(13, packageCoverage.getMethodCounter().getMissedCount());
		assertEquals(8, packageCoverage.getClassCounter().getMissedCount());
		assertEquals(6, packageCoverage.getBranchCounter().getMissedCount());

		assertEquals(9, packageCoverage.getClasses().size());

		// #1 - check lazy methods are not present class filtered methods
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/buildinfo/BuildInfo");
		assertEquals(48,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(13, classCoverage.getLineCounter().getMissedCount());
		assertEquals(7, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(4, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(6, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(4, classCoverage.getMethods().size());
		assertNotNull(
				findByName(classCoverage.getMethods(), "BuildProps", true));
		assertNotNull(findByName(classCoverage.getMethods(), "Version", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "Timestamp", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
	}

	@Test
	public void testComplexObjectSubTrait() {
		File projectDirectory = getProjectDirectory(
				"09-complex-object-sub-trait");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/graph");
		assertEquals(131,
				packageCoverage.getInstructionCounter().getMissedCount());
		assertEquals(30, packageCoverage.getLineCounter().getMissedCount());
		assertEquals(18,
				packageCoverage.getComplexityCounter().getMissedCount());
		assertEquals(18, packageCoverage.getMethodCounter().getMissedCount());
		assertEquals(14, packageCoverage.getClassCounter().getMissedCount());
		assertEquals(0, packageCoverage.getBranchCounter().getMissedCount());

		assertEquals(15, packageCoverage.getClasses().size());

		// #1 - class GraphImplicits
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits");
		assertEquals(0, classCoverage.getMethods().size());

		// #2 - object GraphImplicits$
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$");
		assertEquals(7, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(2, classCoverage.getLineCounter().getMissedCount());
		assertEquals(2, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(2, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(2, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(),
				"DAGNodeTraversableOps", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #3 - sub trait - DAGNodeIdMapping
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeIdMapping");
		assertEquals(0, classCoverage.getMethods().size());

		// #4 - sub object - IdOrdering
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$IdOrdering$");
		assertEquals(9, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(2, classCoverage.getLineCounter().getMissedCount());
		assertEquals(2, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(2, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(2, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "none", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #5 - sub object - IdOrdering's anon
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$IdOrdering$$anon$1");
		assertEquals(9, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(2, classCoverage.getLineCounter().getMissedCount());
		assertEquals(2, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(2, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(2, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "compare", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #6 - sub class - DAGNodeTraversableOps
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeTraversableOps");
		assertEquals(28,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(7, classCoverage.getLineCounter().getMissedCount());
		assertEquals(4, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(4, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(4, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "xs", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"sortedTopologically", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"sortedTopologicallyBy", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #7 - sub class - DAGNodeTraversableOps$
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeTraversableOps$");
		assertEquals(0, classCoverage.getMethods().size());

		// #8, 9, 10, 11, 12 - sub class - DAGNodeTraversableOps's anonfuns
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeTraversableOps$$anonfun$1");
		assertEquals(1, classCoverage.getMethods().size());
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeTraversableOps$$anonfun$2");
		assertEquals(1, classCoverage.getMethods().size());
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeTraversableOps$$anonfun$3");
		assertEquals(1, classCoverage.getMethods().size());
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeTraversableOps$$anonfun$4");
		assertEquals(1, classCoverage.getMethods().size());
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeTraversableOps$$anonfun$5");
		assertEquals(1, classCoverage.getMethods().size());

		// #13, 14, 15 - sub class - DAGNodeTraversableOps's rest of anonfuns
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeTraversableOps$$anonfun$4$$anonfun$apply$1");
		assertEquals(1, classCoverage.getMethods().size());
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeTraversableOps$$anonfun$sortedTopologically$extension$1");
		assertEquals(1, classCoverage.getMethods().size());
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/graph/GraphImplicits$DAGNodeTraversableOps$$anonfun$sortedTopologically$extension$2");
		assertEquals(1, classCoverage.getMethods().size());
	}

	@Test
	public void testTraitAsParent() {
		File projectDirectory = getProjectDirectory("10-trait-as-parent");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/json");
		assertEquals(12,
				packageCoverage.getInstructionCounter().getMissedCount());
		assertEquals(3, packageCoverage.getLineCounter().getMissedCount());
		assertEquals(2,
				packageCoverage.getComplexityCounter().getMissedCount());
		assertEquals(2, packageCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, packageCoverage.getClassCounter().getMissedCount());
		assertEquals(0, packageCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, packageCoverage.getClasses().size());

		// #1 - class GraphImplicits
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/json/AbstractJsonSerDe$class");
		assertEquals(12,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(3, classCoverage.getLineCounter().getMissedCount());
		assertEquals(2, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(2, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(2, classCoverage.getMethods().size());
		assertNotNull(
				findByName(classCoverage.getMethods(), "EntityToJson", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "JsonToEntity", true));
	}

	@Test
	public void testPrivateLazyVal() {
		File projectDirectory = getProjectDirectory("11-private-lazy-val");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/json");

		assertEquals(6, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/json/AbstractJsonSerDe$");
		assertEquals(44,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(7, classCoverage.getLineCounter().getMissedCount());
		assertEquals(7, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(4, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(6, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(4, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "jsonMethods", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"za$co$absa$commons$json$AbstractJsonSerDe$$parse_json4s_32",
				true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"za$co$absa$commons$json$AbstractJsonSerDe$$parse_json4s_33",
				true));
	}

	@Test
	public void testPrivateDef() {
		File projectDirectory = getProjectDirectory("11-private-lazy-val");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/json");

		assertEquals(6, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/json/AbstractJsonSerDe$JsonToEntity");
		assertEquals(133,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(12, classCoverage.getLineCounter().getMissedCount());
		assertEquals(6, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(4, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(4, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(4, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "parse", true));
		assertNotNull(findByName(classCoverage.getMethods(), "fromJson", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "asPrettyJson", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
	}

	@Test
	public void testTraitImplicitDefs() {
		File projectDirectory = getProjectDirectory("12-trait-implicit-defs");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/lang");
		assertEquals(13,
				packageCoverage.getInstructionCounter().getMissedCount());
		assertEquals(6, packageCoverage.getLineCounter().getMissedCount());
		assertEquals(4,
				packageCoverage.getComplexityCounter().getMissedCount());
		assertEquals(4, packageCoverage.getMethodCounter().getMissedCount());
		assertEquals(3, packageCoverage.getClassCounter().getMissedCount());
		assertEquals(0, packageCoverage.getBranchCounter().getMissedCount());

		assertEquals(5, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/lang/TypeConstraints");
		assertEquals(0, classCoverage.getMethods().size());

		// #2
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/lang/TypeConstraints$");
		assertEquals(7, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(2, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #3
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/lang/TypeNegationConstraint");
		assertEquals(0, classCoverage.getMethods().size());

		// #4
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/lang/TypeNegationConstraint$class");
		assertEquals(6, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(4, classCoverage.getLineCounter().getMissedCount());
		assertEquals(3, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(3, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(3, classCoverage.getMethods().size());
		assertNotNull(
				findByName(classCoverage.getMethods(), "passingProbe", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "failingProbe1", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "failingProbe2", true));

		// #5
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/lang/TypeNegationConstraint$$bang$less$colon");
		assertEquals(0, classCoverage.getMethods().size());
	}

	@Test
	public void testTreeCreator() {
		File projectDirectory = getProjectDirectory("13-treecreator");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/reflect");

		assertEquals(13, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/reflect/EnumerationMacros$$treecreator1$1");
		assertEquals(21,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "apply", true));
	}

	@Test
	public void testAnonfunInMethodName() {
		File projectDirectory = getProjectDirectory("14-anonfunInMethodName");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/config");

		assertEquals(12, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/config/ConfigurationImplicits$");
		assertEquals(96,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(309,
				classCoverage.getInstructionCounter().getCoveredCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(21, classCoverage.getLineCounter().getCoveredCount());
		assertEquals(15, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(20,
				classCoverage.getComplexityCounter().getCoveredCount());
		assertEquals(9, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(15, classCoverage.getMethodCounter().getCoveredCount());
		assertEquals(0, classCoverage.getClassCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getCoveredCount());
		assertEquals(6, classCoverage.getBranchCounter().getMissedCount());
		assertEquals(16, classCoverage.getBranchCounter().getCoveredCount());

		assertEquals(24, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "$anonfun$toMap$1",
				true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"$anonfun$toMap$12", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"$anonfun$toMap$2$adapted", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"$anonfun$toMap$9$adapted", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"za$co$absa$commons$config$ConfigurationImplicits$$toMap",
				true));
	}

	@Test
	public void testTraitScala212Methods() {
		File projectDirectory = getProjectDirectory("15-traitScala212Methods");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/version/impl");

		assertEquals(8, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/version/impl/SemVer20Impl");
		assertEquals(0, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(194,
				classCoverage.getInstructionCounter().getCoveredCount());
		assertEquals(0, classCoverage.getLineCounter().getMissedCount());
		assertEquals(14, classCoverage.getLineCounter().getCoveredCount());
		assertEquals(2, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(7, classCoverage.getComplexityCounter().getCoveredCount());
		assertEquals(0, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(6, classCoverage.getMethodCounter().getCoveredCount());
		assertEquals(0, classCoverage.getClassCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getCoveredCount());
		assertEquals(2, classCoverage.getBranchCounter().getMissedCount());
		assertEquals(4, classCoverage.getBranchCounter().getCoveredCount());

		assertEquals(6, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "asSemVer", true));
		assertNotNull(findByName(classCoverage.getMethods(), "parseIdentifiers",
				true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"$anonfun$asSemVer$1", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"$anonfun$asSemVer$2", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"$anonfun$asSemVer$3", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"$anonfun$parseIdentifiers$1", true));
	}

	@Test
	public void testPrivateDefInScala212() {
		File projectDirectory = getProjectDirectory("16-privateDefInScala212");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/commons/config");

		assertEquals(12, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/commons/config/ConfigurationImplicits$ConfigurationOptionalWrapper");
		assertEquals(71,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(0,
				classCoverage.getInstructionCounter().getCoveredCount());
		assertEquals(13, classCoverage.getLineCounter().getMissedCount());
		assertEquals(0, classCoverage.getLineCounter().getCoveredCount());
		assertEquals(14, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(0, classCoverage.getComplexityCounter().getCoveredCount());
		assertEquals(14, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(0, classCoverage.getMethodCounter().getCoveredCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getClassCounter().getCoveredCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getCoveredCount());

		assertEquals(14, classCoverage.getMethods().size());
		assertNotNull(
				findByName(classCoverage.getMethods(), "getOptional", true));
	}

	@Test
	public void testDefInDef() {
		// test data from Enceladus/utils
		File projectDirectory = getProjectDirectory("17-defInDef");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/enceladus/utils/schema");

		assertEquals(6, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/utils/schema/SchemaUtils$");
		assertEquals(59,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(8, classCoverage.getLineCounter().getMissedCount());
		assertEquals(6, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(4, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(4, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(4, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(),
				"getRenamesInSchema", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"za$co$absa$enceladus$utils$schema$SchemaUtils$$getRenamesRecursively$1",
				true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"za$co$absa$enceladus$utils$schema$SchemaUtils$$getStructInArray$1",
				true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
	}

	@Test
	public void testParentClassClear() {
		// detect that no relict of previous class object kept in logic. Was
		// observed in complex scala files.

		// test data from Enceladus/utils
		File projectDirectory = getProjectDirectory("18-parentClassClear");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/enceladus/dao");

		assertEquals(22, packageCoverage.getClasses().size());

		// #1
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/dao/OptionallyRetryableException$");
		assertEquals(152,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(13, classCoverage.getLineCounter().getMissedCount());
		assertEquals(5, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(3, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(4, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(3, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(),
				"mapIntToOptionallyRetryableException", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"getOptionallyRetryableException", true));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
	}

	@Test
	public void testPackageScopeOnObject() {
		// detect that no relict of previous class object kept in logic. Was
		// observed in complex scala files.

		// test data from Enceladus/utils
		File projectDirectory = getProjectDirectory("19-packageScopeOnObject");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/enceladus/model/properties/essentiality");

		assertEquals(10, packageCoverage.getClasses().size());

		// #1 - parent package
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/model/properties/essentiality/package$");
		assertEquals(1, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #2 - trait Essentiality
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/model/properties/essentiality/package$Essentiality");
		assertEquals(0, classCoverage.getMethods().size());

		// #3 - class Optional
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/model/properties/essentiality/package$Optional");
		assertEquals(5, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #4 - class Recommended
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/model/properties/essentiality/package$Recommended");
		assertEquals(5, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #5 - class Mandatory
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/model/properties/essentiality/package$Mandatory");
		assertEquals(8, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(1, classCoverage.getLineCounter().getMissedCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(1, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));

		// #6 - object Essentiality
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/model/properties/essentiality/package$Essentiality$");
		assertEquals(36,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(5, classCoverage.getLineCounter().getMissedCount());
		assertEquals(5, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(5, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(5, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "Optional", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "Recommended", true));
		assertNotNull(findMethodByDesc(classCoverage.getMethods(),
				"(Z)Lza/co/absa/enceladus/model/properties/essentiality/package$Mandatory;"));
		assertNotNull(findMethodByDesc(classCoverage.getMethods(),
				"()Lza/co/absa/enceladus/model/properties/essentiality/package$Mandatory;"));
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
	}

	@Test
	public void testPackageScopeOnObjectWithVal() {
		// test data from Enceladus/utils
		File projectDirectory = getProjectDirectory(
				"20-packageScopeOnObjectWithVal");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/enceladus/model");

		assertEquals(2, packageCoverage.getClasses().size());

		// #1 - package object
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/model/package$");
		assertEquals(3, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "ModelVersion", true));
		assertNotNull(findByName(classCoverage.getMethods(), "CollectionSuffix",
				true));

		// #2 - companion class
		classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/model/package");
		assertEquals(0, classCoverage.getMethods().size());

	}

	@Test
	public void testDefSignNames() {
		// test data from Enceladus/utils
		File projectDirectory = getProjectDirectory("21-defSignNames");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/enceladus/model/dataFrameFilter");

		assertEquals(28, packageCoverage.getClasses().size());

		// #1 - package object
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/model/dataFrameFilter/package$DataFrameFilter$class");
		assertEquals(54,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(212,
				classCoverage.getInstructionCounter().getCoveredCount());
		assertEquals(2, classCoverage.getLineCounter().getMissedCount());
		assertEquals(12, classCoverage.getLineCounter().getCoveredCount());
		assertEquals(14, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(7, classCoverage.getComplexityCounter().getCoveredCount());
		assertEquals(2, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(3, classCoverage.getMethodCounter().getCoveredCount());
		assertEquals(0, classCoverage.getClassCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getCoveredCount());
		assertEquals(12, classCoverage.getBranchCounter().getMissedCount());
		assertEquals(20, classCoverage.getBranchCounter().getCoveredCount());

		assertEquals(5, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "or", true));
		assertNotNull(findByName(classCoverage.getMethods(), "and", true));
		assertNotNull(findByName(classCoverage.getMethods(), "$plus", true));
		assertNotNull(findByName(classCoverage.getMethods(), "$times", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "unary_$bang", true));
	}

	@Test
	public void testPrivateDefInTrait() {
		// test data from Enceladus/utils
		File projectDirectory = getProjectDirectory("22-privateDefInTrait");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/enceladus/migrations/framework/migration");

		assertEquals(5, packageCoverage.getClasses().size());

		// #1 - package object
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/migrations/framework/migration/JsonMigration$class");
		assertEquals(170,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(241,
				classCoverage.getInstructionCounter().getCoveredCount());
		assertEquals(7, classCoverage.getLineCounter().getMissedCount());
		assertEquals(27, classCoverage.getLineCounter().getCoveredCount());
		assertEquals(5, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(8, classCoverage.getComplexityCounter().getCoveredCount());
		assertEquals(1, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(7, classCoverage.getMethodCounter().getCoveredCount());
		assertEquals(0, classCoverage.getClassCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getCoveredCount());
		assertEquals(6, classCoverage.getBranchCounter().getMissedCount());
		assertEquals(4, classCoverage.getBranchCounter().getCoveredCount());

		assertEquals(8, classCoverage.getMethods().size());
		assertNotNull(
				findByName(classCoverage.getMethods(), "transformJSON", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "getTransformer", true));
		assertNotNull(findByName(classCoverage.getMethods(), "execute", true));
		assertNotNull(findByName(classCoverage.getMethods(), "validate", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"validateMigration", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"logMigrationStatistics", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"ensureCollectionEmpty", true));
		assertNotNull(findByName(classCoverage.getMethods(),
				"za$co$absa$enceladus$migrations$framework$migration$JsonMigration$$applyTransformers",
				true));
	}

	@Test
	public void testVarDefined() {
		// test data from Enceladus/utils
		File projectDirectory = getProjectDirectory("23-varDefined");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/enceladus/migrationscli/cmd");

		assertEquals(7, packageCoverage.getClasses().size());

		// #1 - package object
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/migrationscli/cmd/ContinuousMigratorCmdConfig$CmdParser");
		assertEquals(88,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(12, classCoverage.getLineCounter().getMissedCount());
		assertEquals(2, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(2, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getMissedCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());

		assertEquals(2, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(), "<init>", true));
		assertNotNull(
				findByName(classCoverage.getMethods(), "rawFormat", true));
	}

	@Test
	public void testCurlyBracesInString() {
		// covered example: @Value("${enceladus.rest.auth.ad.domain:}")

		// test data from Enceladus/utils
		File projectDirectory = getProjectDirectory("24-curlyBracesInString");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/enceladus/rest_api/auth/kerberos");

		assertEquals(4, packageCoverage.getClasses().size());

		// #1 - package object
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/rest_api/auth/kerberos/RestApiKerberosAuthentication");
		assertEquals(219,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(46,
				classCoverage.getInstructionCounter().getCoveredCount());
		assertEquals(44, classCoverage.getLineCounter().getMissedCount());
		assertEquals(13, classCoverage.getLineCounter().getCoveredCount());
		assertEquals(12, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(2, classCoverage.getComplexityCounter().getCoveredCount());
		assertEquals(9, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(2, classCoverage.getMethodCounter().getCoveredCount());
		assertEquals(0, classCoverage.getClassCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getCoveredCount());
		assertEquals(5, classCoverage.getBranchCounter().getMissedCount());
		assertEquals(1, classCoverage.getBranchCounter().getCoveredCount());

		assertEquals(11, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(),
				"za$co$absa$enceladus$rest_api$auth$kerberos$RestApiKerberosAuthentication$$validateParam",
				true));
	}

	@Test
	public void testDefInPrivateDef() {
		// test data from Enceladus/utils
		File projectDirectory = getProjectDirectory("25-defInPrivateDef");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/enceladus/rest_api/controllers/v3");

		assertEquals(5, packageCoverage.getClasses().size());

		// #1 - package object
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/rest_api/controllers/v3/ControllerPagination$");
		assertEquals(5, classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(172,
				classCoverage.getInstructionCounter().getCoveredCount());
		assertEquals(2, classCoverage.getLineCounter().getMissedCount());
		assertEquals(15, classCoverage.getLineCounter().getCoveredCount());
		assertEquals(1, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(12,
				classCoverage.getComplexityCounter().getCoveredCount());
		assertEquals(0, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(9, classCoverage.getMethodCounter().getCoveredCount());
		assertEquals(0, classCoverage.getClassCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getCoveredCount());
		assertEquals(1, classCoverage.getBranchCounter().getMissedCount());
		assertEquals(7, classCoverage.getBranchCounter().getCoveredCount());

		assertEquals(9, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(),
				"za$co$absa$enceladus$rest_api$controllers$v3$ControllerPagination$$tryToInt$1",
				true));
	}

	@Test
	public void testDefInPrivateInTrait() {
		// test data from Enceladus/utils
		File projectDirectory = getProjectDirectory("26-defInPrivateInTrait");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/enceladus/rest_api/services");

		assertEquals(57, packageCoverage.getClasses().size());

		// #1 - package object
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/enceladus/rest_api/services/VersionedModelService$class");
		assertEquals(48,
				classCoverage.getInstructionCounter().getMissedCount());
		assertEquals(484,
				classCoverage.getInstructionCounter().getCoveredCount());
		assertEquals(4, classCoverage.getLineCounter().getMissedCount());
		assertEquals(47, classCoverage.getLineCounter().getCoveredCount());
		assertEquals(4, classCoverage.getComplexityCounter().getMissedCount());
		assertEquals(29,
				classCoverage.getComplexityCounter().getCoveredCount());
		assertEquals(4, classCoverage.getMethodCounter().getMissedCount());
		assertEquals(24, classCoverage.getMethodCounter().getCoveredCount());
		assertEquals(0, classCoverage.getClassCounter().getMissedCount());
		assertEquals(1, classCoverage.getClassCounter().getCoveredCount());
		assertEquals(0, classCoverage.getBranchCounter().getMissedCount());
		assertEquals(10, classCoverage.getBranchCounter().getCoveredCount());

		assertEquals(28, classCoverage.getMethods().size());
		assertNotNull(findByName(classCoverage.getMethods(),
				"exportVersionErrorMessage$1", true));
	}

	@Test
	public void testComplexClassNaming() {
		File projectDirectory = getProjectDirectory("27-complexClassNaming");
		File sourceDirectory = new File(projectDirectory, "src");
		final IBundleCoverage bundleCoverage = prepareSourceBundle(
				projectDirectory);

		final IBundleCoverage bundleCoverageFiltered = new CoverageBundleMethodFilterScalaImpl()
				.filterMethods(bundleCoverage, sourceDirectory);

		assertEquals(1, bundleCoverageFiltered.getPackages().size());

		IPackageCoverage packageCoverage = findByName(
				bundleCoverageFiltered.getPackages(),
				"za/co/absa/atum/utils/controlmeasure");

		assertEquals(18, packageCoverage.getClasses().size());

		// #1 - package object
		IClassCoverage classCoverage = findByName(packageCoverage.getClasses(),
				"za/co/absa/atum/utils/controlmeasure/ControlMeasureBuilder$ControlMeasureBuilderImpl");
		assertEquals(21, classCoverage.getMethods().size());
	}

}
