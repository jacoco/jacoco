/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    Kyle Lieber - implementation of CheckMojo
 *    Marc Hoffmann - redesign using report APIs
 *    
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.check.IViolationsOutput;
import org.jacoco.report.check.Limit;
import org.jacoco.report.check.Rule;
import org.jacoco.report.check.RulesChecker;

/**
 * Checks that the code coverage metrics are being met.
 * 
 * @goal check
 * @phase verify
 * @requiresProject true
 * @threadSafe
 * @since 0.6.1
 */
public class CheckMojo extends AbstractJacocoMojo implements IViolationsOutput {

	private static final String MSG_SKIPPING = "Skipping JaCoCo execution due to missing execution data file:";
	private static final String CHECK_SUCCESS = "All coverage checks have been met.";
	private static final String CHECK_FAILED = "Coverage checks have not been met. See log for details.";

	/**
	 * <p>
	 * Check configuration used to specify rules on element types (BUNDLE,
	 * PACKAGE, CLASS, SOURCEFILE or METHOD) with a list of limits. Each limit
	 * applies to a certain counter (INSTRUCTION, LINE, BRANCH, COMPLEXITY,
	 * METHOD, CLASS) and defines a minimum or maximum for the corresponding
	 * value (TOTALCOUNT, COVEREDCOUNT, MISSEDCOUNT, COVEREDRATIO, MISSEDRATIO).
	 * If a limit refers to a ratio the range is from 0.0 to 1.0 where the
	 * number of decimal places will also determine the precision in error
	 * messages.
	 * 
	 * Note that you <b>must</b> use <tt>implementation</tt> hints for
	 * <tt>rule</tt> and <tt>limit</tt> when using Maven 2, with Maven 3 you do
	 * not need to specify the attributes.
	 * </p>
	 * 
	 * <p>
	 * This example requires an overall instruction coverage of 80% and no class
	 * must be missed:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * <rules>
	 *   <rule implementation="org.jacoco.maven.RuleConfiguration">
	 *     <element>BUNDLE</element>
	 *     <limits>
	 *       <limit implementation="org.jacoco.report.check.Limit">
	 *         <counter>INSTRUCTION</counter>
	 *         <value>COVEREDRATIO</value>
	 *         <minimum>0.80</minimum>
	 *       </limit>
	 *       <limit implementation="org.jacoco.report.check.Limit">
	 *         <counter>CLASS</counter>
	 *         <value>MISSEDCOUNT</value>
	 *         <maximum>0</maximum>
	 *       </limit>
	 *     </limits>
	 *   </rule>
	 * </rules>}
	 * </pre>
	 * 
	 * <p>
	 * This example requires a line coverage minimum of 50% for every class
	 * except test classes:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * <rules>
	 *   <rule>
	 *     <element>CLASS</element>
	 *     <excludes>
	 *       <exclude>*Test</exclude>
	 *     </excludes>
	 *     <limits>
	 *       <limit>
	 *         <counter>LINE</counter>
	 *         <value>COVEREDRATIO</value>
	 *         <minimum>0.50</minimum>
	 *       </limit>
	 *     </limits>
	 *   </rule>
	 * </rules>}
	 * </pre>
	 * 
	 * @parameter
	 * @required
	 */
	private List<RuleConfiguration> rules;

	/**
	 * Halt the build if any of the checks fail.
	 * 
	 * @parameter property="jacoco.haltOnFailure" default-value="true"
	 * @required
	 */
	private boolean haltOnFailure;

	/**
	 * File with execution data.
	 * 
	 * @parameter default-value="${project.build.directory}/jacoco.exec"
	 */
	private File dataFile;

	private boolean violations;

	private boolean canCheckCoverage() {
		if (!dataFile.exists()) {
			getLog().info(MSG_SKIPPING + dataFile);
			return false;
		}
		final File classesDirectory = new File(getProject().getBuild()
				.getOutputDirectory());
		if (!classesDirectory.exists()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing classes directory:"
							+ classesDirectory);
			return false;
		}
		return true;
	}

	@Override
	public void executeMojo() throws MojoExecutionException,
			MojoExecutionException {
		if (!canCheckCoverage()) {
			return;
		}
		executeCheck();
	}

	private void executeCheck() throws MojoExecutionException {
		final IBundleCoverage bundle = loadBundle();
		violations = false;

		final RulesChecker checker = new RulesChecker();
		final List<Rule> checkerrules = new ArrayList<Rule>();
		for (final RuleConfiguration r : rules) {
			checkerrules.add(r.rule);
		}
		checker.setRules(checkerrules);

		final IReportVisitor visitor = checker.createVisitor(this);
		try {
			visitor.visitBundle(bundle, null);
		} catch (final IOException e) {
			throw new MojoExecutionException(
					"Error while checking code coverage: " + e.getMessage(), e);
		}
		if (violations) {
			if (this.haltOnFailure) {
				throw new MojoExecutionException(CHECK_FAILED);
			} else {
				this.getLog().warn(CHECK_FAILED);
			}
		} else {
			this.getLog().info(CHECK_SUCCESS);
		}
	}

	private IBundleCoverage loadBundle() throws MojoExecutionException {
		final FileFilter fileFilter = new FileFilter(this.getIncludes(),
				this.getExcludes());
		final BundleCreator creator = new BundleCreator(getProject(),
				fileFilter, getLog());
		try {
			final ExecutionDataStore executionData = loadExecutionData();
			return creator.createBundle(executionData);
		} catch (final IOException e) {
			throw new MojoExecutionException(
					"Error while reading code coverage: " + e.getMessage(), e);
		}
	}

	private ExecutionDataStore loadExecutionData() throws IOException {
		final ExecFileLoader loader = new ExecFileLoader();
		loader.load(dataFile);
		return loader.getExecutionDataStore();
	}

	public void onViolation(final ICoverageNode node, final Rule rule,
			final Limit limit, final String message) {
		this.getLog().warn(message);
		violations = true;
	}

}
