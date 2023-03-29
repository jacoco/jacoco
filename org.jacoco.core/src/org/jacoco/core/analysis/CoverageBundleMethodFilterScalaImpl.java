/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 * <p>
 * Contributors:
 *    Miroslav Pojer - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.analysis;

import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoverageBundleMethodFilterScalaImpl
		implements ICoverageBundleMethodFilter {

	private final static Map<String, ClassCondensate> loadedClassFiles = new HashMap<String, ClassCondensate>();

	/**
	 * Perform smart filtering of classes and methods from input data bundle.
	 * <p>
	 * Known places to keep in reports - constructors -- object:
	 * LocalFileSystemUtils.() | TempDirectory.() -- class:
	 * TempDirectory(String, String, boolean) -- trait: CachingConverter -
	 * anonfun (-s) -- keep them (by direct 'anonfun' string detection in class
	 * name - exception) -- keep them (by direct 'anonfun' string detection in
	 * method name - exception)
	 * <p>
	 * Known places to remove from reports - Class constructor attributes --
	 * when not used val keyword -- Possible|Not-implemented to remove
	 * attributes when used 'val' new code needed - defaults values --
	 * LocalFileSystemUtils.readLocalFile$default$2() - lazy compute --
	 * TempDirectory:hook$lzycompute() - all methods not present in their class
	 * - all classes without source file
	 *
	 * @param bundleCoverage
	 *            Input bundle to apply filtering.
	 * @param classesRootDir
	 *            Root path for searching class source files.
	 * @return Reduced data bundle.
	 */
	public IBundleCoverage filterMethods(IBundleCoverage bundleCoverage,
			File classesRootDir) {
		System.out.printf(String.format(
				"INFO: Removing by scala src files analysis. Scala classes root dir path '%s'",
				classesRootDir));

		if (!Files.isDirectory(classesRootDir.toPath())) {
			throw new IllegalArgumentException("Path must be a directory!");
		}

		List<String> classFilePaths = scanClassFiles(classesRootDir);

		removeByClassExistence(bundleCoverage, classFilePaths);
		removeByMethodExistence(bundleCoverage, classFilePaths);

		return bundleCoverage;
	}

	/**
	 * Remove entire classes from data bundle when no class file found among
	 * provided class paths.
	 *
	 * @param bundleCoverage
	 *            Input bundle to apply filtering by classes.
	 * @param classFilePaths
	 *            List of detected class file paths.
	 */
	private static void removeByClassExistence(IBundleCoverage bundleCoverage,
			List<String> classFilePaths) {
		for (IPackageCoverage packageCoverage : bundleCoverage.getPackages()) {

			List<IClassCoverage> classesToRemoveFromBundle = new ArrayList<IClassCoverage>();
			for (IClassCoverage classCoverage : packageCoverage.getClasses()) {

				// remove entire class if no source file found
				if (!existClassSourceFile(packageCoverage.getName(),
						classCoverage.getSourceFileName(), classFilePaths)) {
					classesToRemoveFromBundle.add(classCoverage);

					((PackageCoverageImpl) packageCoverage)
							.decrement(classCoverage);
					((CoverageNodeImpl) bundleCoverage)
							.decrement(classCoverage);
				}
			}

			packageCoverage.getClasses().removeAll(classesToRemoveFromBundle);
		}
	}

	/**
	 * Remove method from data bundle when it is not found in its class file.
	 *
	 * @param bundleCoverage
	 *            Input bundle to apply filtering by methods.
	 * @param classFilePaths
	 *            List of detected class file paths.
	 */
	private static void removeByMethodExistence(IBundleCoverage bundleCoverage,
			List<String> classFilePaths) {
		for (IPackageCoverage packageCoverage : bundleCoverage.getPackages()) {
			for (IClassCoverage classCoverage : packageCoverage.getClasses()) {

				List<IMethodCoverage> methodsToRemoveFromBundle = new ArrayList<IMethodCoverage>();
				for (IMethodCoverage methodCoverage : classCoverage
						.getMethods()) {
					// check if method exist in class file ==> find not existing
					// ones to removed them from bundle
					if (!existInSourceFileAsClassMember(
							classCoverage.getSourceFileName(), classFilePaths,
							packageCoverage.getName(), classCoverage.getName(),
							methodCoverage.getName(),
							methodCoverage.getDesc())) {
						// add method for remove to list for later removing
						// process
						if (!methodsToRemoveFromBundle
								.contains(methodCoverage)) {
							methodsToRemoveFromBundle.add(methodCoverage);

							((ClassCoverageImpl) classCoverage)
									.decrement(methodCoverage);
							((PackageCoverageImpl) packageCoverage)
									.decrement(methodCoverage);
							((CoverageNodeImpl) bundleCoverage)
									.decrement(methodCoverage);
						} else {
							System.out.printf(String.format(
									"WARNING: Not expected duplicate method coverage '%s. %s'",
									methodCoverage.getName(), methodCoverage));
						}
					}
					// else ==> method kept in report data set
				}

				classCoverage.getMethods().removeAll(methodsToRemoveFromBundle);
			}
		}
	}

	/**
	 * If source class does not exist then remove from coverage, otherwise do
	 * method analysis.
	 *
	 * @param packageName
	 *            Value of class package.
	 * @param classSourceFileName
	 *            Value of class source file name.
	 * @param classesFilePaths
	 *            Set of existing class files to check.
	 * @return True if method is not implemented in provided classes, otherwise
	 *         false.
	 */
	private static boolean existClassSourceFile(String packageName,
			String classSourceFileName, List<String> classesFilePaths) {
		String partialClassName = Paths.get(packageName, classSourceFileName)
				.toString();
		return findByContains(partialClassName, classesFilePaths) != null;
	}

	/**
	 * Decide if method exist in its class.
	 *
	 * @return True if method found in class source code, otherwise return
	 *         false.
	 */
	private static boolean existInSourceFileAsClassMember(
			String classSourceFileName, List<String> classesFilePaths,
			String packageName, String className, String methodName,
			String methodDesc) {
		// get file content and parse it
		String partialClassPath = Paths.get(packageName, classSourceFileName)
				.toString();
		ClassCondensate classCondensate = getClassFileSourceCondensate(
				findByContains(partialClassPath, classesFilePaths));

		// do not remove these methods
		// find "Object-type" with correct name
		for (int i = 0; i < classCondensate.size(); i++) {
			int cCurlyIndent = classCondensate.getCurlyIndent(i);
			int cObjectGroup = classCondensate.getGroupType(i);
			String cClassName = classCondensate.getElementName(i);

			// do not remove these
			if (className.contains("$anonfun$")
					|| className.contains("$typecreator")
					|| className.contains("$treecreator")) {
				if (methodName.equals("<init>")) {
					System.out.printf(String.format(
							"REMOVED_BY_SKIP_CLASS_NAME_INIT_METHOD: package: %s, className: %s, methodName: %s, desc: %s\n",
							packageName, className, methodName, methodDesc));
					return false;
				}

				System.out.printf(String.format(
						"KEPT_BY_SKIP_CLASS_NAME: package: %s, className: %s, methodName: %s, desc: %s\n",
						packageName, className, methodName, methodDesc));
				return true;
			}

			// Note: '$anonfun$' can be visible when scala with 2.12, not
			// observed in 2.11
			if (methodName.contains("$anonfun$")) {
				System.out.printf(String.format(
						"KEPT_BY_SKIP_METHOD_NAME: package: %s, className: %s, methodName: %s, desc: %s\n",
						packageName, className, methodName, methodDesc));
				return true;
			}

			// check correct class
			if (className.equals(cClassName)
					&& (cObjectGroup == CoverageBundleMethodFilterConst.GROUP_OBJECT)) {
				// check if method with provided name exist
				for (int j = i + 1; j < classCondensate.size(); j++) {
					if (classCondensate.getGroupType(
							j) != CoverageBundleMethodFilterConst.GROUP_MEMBER)
						continue;

					int mCurlyIndent = classCondensate.getCurlyIndent(j);

					// check if we are still inside correct object condensate
					if (cCurlyIndent == mCurlyIndent) {
						// classCondensate can contain more valid objects
						// not found in current class - when init j => i + 1
						break;
					}

					// check simple method names
					if ((mCurlyIndent > cCurlyIndent)) {
						if (methodName
								.equals(classCondensate.getElementName(j))) {
							System.out.printf(String.format(
									"KEPT_BY_EXISTING: package: %s, className: %s, methodName: %s, desc: %s\n",
									packageName, className, methodName,
									methodDesc));
							return true;
						}
					}
				}

				// check if we found exception method name - cannot be decided
				// ==> by name
				if (CoverageBundleMethodFilterConst.validExceptions
						.contains(methodName)) {
					System.out.printf(String.format(
							"KEPT_BY_EXCEPTION: package: %s, className: %s, methodName: %s, desc: %s\n",
							packageName, className, methodName, methodDesc));
					return true;
				}

				// not found in class-type object
				System.out.printf(String.format(
						"REMOVED - not found in class file. package: %s, className: %s, methodName: %s, desc: %s\n",
						packageName, className, methodName, methodDesc));
				return false;
			}
		}

		System.out.printf(String.format(
				"REMOVED - not found in classCondensate. package: %s, className: %s, methodName: %s, desc: %s\n",
				packageName, className, methodName, methodDesc));
		return false;
	}

	private static List<String> scanClassFiles(File classesRootDir) {
		try (Stream<Path> walk = Files.walk(classesRootDir.toPath(), 999)) {
			return walk.filter(p -> !Files.isDirectory(p))
					// convert path to string
					.map(Path::toString)
					.filter(CoverageBundleMethodFilterScalaImpl::isEndWith)
					.collect(Collectors.toList());
		} catch (IOException e) {
			System.out.print("Failed scan of project source files.");
			throw new RuntimeException(e);
		}
	}

	private static boolean isEndWith(String file) {
		for (String fileExtension : CoverageBundleMethodFilterConst.supportedExtensions) {
			if (file.endsWith(fileExtension))
				return true;
		}
		return false;
	}

	private static String findByContains(String sequence, List<String> list) {
		for (String item : list) {
			if (item.contains(sequence))
				return item;
		}
		return null;
	}

	/**
	 * Provide "JaCoCo" condensate for class.
	 *
	 * @param filePath
	 *            Path to class file.
	 * @return List of class elements in "JaCoCo" condensate form.
	 */
	private static ClassCondensate getClassFileSourceCondensate(
			String filePath) {
		if (!loadedClassFiles.containsKey(filePath))
			loadedClassFiles.put(filePath,
					new ClassCondensate().load(filePath));

		return loadedClassFiles.get(filePath);
	}
}
