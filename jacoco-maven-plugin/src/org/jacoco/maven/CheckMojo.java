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
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
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
     * Example requiring overall minimum 75% coverage, instruction, method, branch
     * complexity and line. In additional with a rule requiring min 75% line coverage for all classes.
     * </p>
     *
     * <pre>
     * {@code
     * <check>
     *      <name>Min Project Coverage Rule</name>
     *      <classRatio>100</classRatio>
     *      <instructionRatio>75</instructionRatio>
     *      <methodRatio>75</methodRatio>
     *      <branchRatio>75</branchRatio>
     *      <complexityRatio>75</complexityRatio>
     *      <lineRatio>75</lineRatio>
     *      <rules>
     *          <rule>
     *              <name>Min Line Coverage For All Classes</name>
     *              <element>class</element>
     *          	<counterEntity>line</counterEntity>
     *          	<counterProperty>coveredRatio</counterProperty>
     *          	<minimum>75</minimum>
     *          </rule>
     *      </rules>
     * </check>}
     * </pre>
     *
     * @parameter
     * @required
     */
    private CheckConfiguration check=new CheckConfiguration();

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
		int totalFailureCount = checkBundleFailures(check, bundle);
        logIfFailed("Bundle Coverage Checks", totalFailureCount);
        List<Rule> rules = check.getRules();
        boolean rulesAreValid= checkRuleDefinitionIsValid(rules);
        if (rulesAreValid) {
            for (Rule rule : rules) {
                totalFailureCount += checkRuleFailures(rule, bundle);
            }
            return totalFailureCount==0;
        }
		return false;
	}

    public boolean checkRuleDefinitionIsValid(Collection<Rule> rules)
    {
        int errorMessageCount=0;
        for (Rule rule : rules) {
            final List<String> ruleErrorMessages = rule.getRuleErrorMessages();
            int ruleErrorCount = ruleErrorMessages.size();
            if (ruleErrorCount>0) {
                logRuleDefinitionErrors(rule.getName(), ruleErrorMessages);
            }
            errorMessageCount += ruleErrorMessages.size();
        }
        return errorMessageCount==0;
    }

    private void logRuleDefinitionErrors(String ruleName, List<String> ruleErrorMessages) {
        getLog().error(String.format("Rule '%s' is invalid, has the following problems:",ruleName));
        for (String ruleErrorMessage : ruleErrorMessages) {
            getLog().error(ruleErrorMessage);
        }
    }

    private int checkBundleFailures(final CheckConfiguration check, final IBundleCoverage coverageNode) {
        int failureCount=0;
        for (final CounterEntity entity : CounterEntity.values()) {
            boolean passed = this.checkCounter(coverageNode,entity, coverageNode.getCounter(entity),
                    check.getRatio(entity));
            failureCount=passed?failureCount:failureCount+1;
        }
        return failureCount;
    }

    private int checkRuleFailures(final Rule rule, final IBundleCoverage bundle) {
        final Collection<IPackageCoverage> packages = bundle.getPackages();
        int ruleFailureCount = checkRuleFailures(rule, packages);
        for (IPackageCoverage packageCoverage : packages) {
            ruleFailureCount += checkRuleFailures(rule, packageCoverage.getClasses());
        }
        logIfFailed(rule.getName(), ruleFailureCount);
        return ruleFailureCount;
    }

    private void logIfFailed(final String name,final int ruleFailureCount) {
        if (ruleFailureCount>0) {
            final String message = String.format("Failed rule '%s', %s failure(s)", name, ruleFailureCount);
            if (haltOnFailure) {
                getLog().error(message);
            }
            else {
                getLog().warn(message);
            }
        }
    }


    private int checkRuleFailures(Rule rule, final Collection<? extends ICoverageNode> coverageNodes) {
        int failureCount=0;
        for (ICoverageNode coverageNode : coverageNodes) {
            boolean ruleAppliesToNode = rule.ruleApplies(coverageNode.getElementType());
            if (ruleAppliesToNode && !checkRuleFailure(rule, coverageNode)) {
                    ++failureCount;
            }
        }
        return failureCount;
    }

    private boolean checkRuleFailure(final Rule rule, final ICoverageNode coverageNode) {
        final CounterEntity entity = rule.getCounterEntity();
        return this.checkCounter(coverageNode,entity, coverageNode.getCounter(entity),
                    rule.getMinimum());
    }


    private boolean checkCounter(final ICoverageNode coverageNode, final CounterEntity entity,
			final ICounter counter, final double checkValue) {
		boolean passed = true;

		final double ratio = counter.getCoveredRatio() * 100;

        if (ratio < checkValue) {
            String message = String.format(INSUFFICIENT_COVERAGE, coverageNode.getElementType(), coverageNode.getName().replace("/","."), entity.name(),
                            truncate(ratio), truncate(checkValue));
            getLog().warn(message);
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
