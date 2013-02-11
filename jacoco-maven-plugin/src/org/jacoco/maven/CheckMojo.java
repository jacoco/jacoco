/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    Kyle Lieber - implementation of CheckMojo
 *    
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.data.ExecFileLoader;
import org.jacoco.core.data.ExecutionDataStore;

/**
 * Checks that the code coverage metrics are being met.
 * 
 * @goal check
 * @phase verify
 * @requiresProject true
 * @threadSafe
 */
public class CheckMojo extends AbstractJacocoMojo {

	private static final String MSG_SKIPPING = "Skipping JaCoCo execution due to missing execution data file";

	private static final String ERROR_UNABLE_TO_READ = "Unable to read execution data file %s: %s";
	private static final String ERROR_CHECKING_COVERAGE = "Error while checking coverage: %s";

	private static final String INSUFFICIENT_COVERAGE = "%s %s : Insufficient code coverage for %s: %4$.2f%% < %5$.2f%%";
	private static final String CHECK_FAILED = "Coverage checks have not been met. See report for details.";
	private static final String CHECK_SUCCESS = "All coverage checks have been met.";

    /**
     * <p>
     * Check configuration. Used to specify minimum coverage percentages that
     * must be met. Defaults to 0% if a percentage ratio is not specified.
     * </p>
     *
     * <p>
     * Example requiring minimum 75% coverage, instruction, method, branch
     * complexity and line overall, for each class minimum 60%
     * </p>
     *
     * <pre>
     * {@code
     * <check>
     *      <classRatio>100</classRatio>
     *      <instructionRatio>75</instructionRatio>
     *      <methodRatio>75</methodRatio>
     *      <branchRatio>75</branchRatio>
     *      <complexityRatio>75</complexityRatio>
     *      <lineRatio>75</lineRatio>
     *      <rules>
     *          <rule>
     *              <element>class</element>
     *              <name>Class Checks</name>
         *          <instructionRatio>60</instructionRatio>
         *          <methodRatio>60</methodRatio>
         *          <branchRatio>60</branchRatio>
         *          <complexityRatio>60</complexityRatio>
         *          <lineRatio>60</lineRatio>
     *          </rule>
     *      </rules>
     * </check>}
     * </pre>
     *
     * @parameter
     * @required
     */
    private Configuration check=new Configuration();

	/**
	 * Halt the build if any of the checks fail.
	 * 
	 * @parameter expression="${jacoco.haltOnFailure}" default-value="true"
	 * @required
	 */
	private boolean haltOnFailure;

	/**
	 * File with execution data.
	 * 
	 * @parameter default-value="${project.build.directory}/jacoco.exec"
	 */
	private File dataFile;

	private ExecutionDataStore executionDataStore;

	private boolean canCheckCoverage() {
		if (!dataFile.exists()) {
			getLog().info(MSG_SKIPPING);
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
		try {
			loadExecutionData();
		} catch (final IOException e) {
			throw new MojoExecutionException(String.format(
					ERROR_UNABLE_TO_READ, dataFile, e.getMessage()), e);
		}
		try {
			if (check()) {
				this.getLog().info(CHECK_SUCCESS);
			} else {
				this.handleFailure();
			}
		} catch (final IOException e) {
			throw new MojoExecutionException(String.format(
					ERROR_CHECKING_COVERAGE, e.getMessage()), e);
		}
	}

	private void loadExecutionData() throws IOException {
		final ExecFileLoader loader = new ExecFileLoader();
		loader.load(dataFile);
		executionDataStore = loader.getExecutionDataStore();
	}

	private boolean check() throws IOException {
		final FileFilter fileFilter = new FileFilter(this.getIncludes(),
				this.getExcludes());
		final BundleCreator creator = new BundleCreator(this.getProject(),
				fileFilter);
		final IBundleCoverage bundle = creator.createBundle(executionDataStore);
		int totalFailureCount = checkNodeCoverage("project", check, bundle);
        if (check.hasRules()){
            RuleList rules = check.getRules();
            for (Rule rule : rules) {
                totalFailureCount += checkRuleCompliance(rule,bundle);
            }

        }
		return totalFailureCount==0;
	}

    private int checkRuleCompliance(Rule rule,IBundleCoverage bundle) {
        Collection<IPackageCoverage> packages = bundle.getPackages();
        int ruleFailureCount = checkNodeCoverage("package",rule,packages);
        for (IPackageCoverage packageCoverage : packages) {
            ruleFailureCount += checkNodeCoverage("class", rule, packageCoverage.getClasses());
        }
        logRuleSummaryMessage(ruleFailureCount, rule.getName());
        return ruleFailureCount;
    }

    private void logRuleSummaryMessage(int ruleFailureCount, String name) {
        if (ruleFailureCount>0){
            getLog().info(String.format("%s coverage check failure(s) against [%s] ",ruleFailureCount, name));
        }
        else{
            getLog().info(String.format("Passed [%s] coverage check successfully ", name));
        }
    }


    private int checkNodeCoverage( String nodeType,Rule rule, Collection<? extends ICoverageNode> coverageNodes) {
        int failureCount=0;
        for (ICoverageNode coverageNode : coverageNodes) {
            if (rule.matches(nodeType,coverageNode.getName()))
            {
                failureCount+=checkNodeCoverage(nodeType, rule, coverageNode);
            }
        }
        return failureCount;
    }

    private int checkNodeCoverage(String nodeType, AbstractRule rule, ICoverageNode coverageNode) {
        int failureCount=0;
        for (final CounterEntity entity : CounterEntity.values()) {
            boolean passed = this.checkCounter(nodeType,coverageNode.getName().replace('/','.'),entity, coverageNode.getCounter(entity),
                    rule.getRatio(entity));
            failureCount=passed?failureCount:failureCount+1;
        }
        return failureCount;
    }

    private boolean checkCounter(final String type, final String nameOfType, final CounterEntity entity,
			final ICounter counter, final double checkRatio) {
		boolean passed = true;

		final double ratio = counter.getCoveredRatio() * 100;

        if (ratio < checkRatio) {
            String message = String.format(INSUFFICIENT_COVERAGE, type, nameOfType, entity.name(),
                            truncate(ratio), truncate(checkRatio));
            Log log = this.getLog();
            if (haltOnFailure){
                log.error(message);
            }
            else{
                log.warn(message);
            }
            passed = false;
        }
		return passed;
	}

	private BigDecimal truncate(final double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_FLOOR);
	}

	private void handleFailure() throws MojoExecutionException {
		if (this.haltOnFailure) {
			throw new MojoExecutionException(CHECK_FAILED);
		} else {
			this.getLog().warn(CHECK_FAILED);
		}
	}
}
