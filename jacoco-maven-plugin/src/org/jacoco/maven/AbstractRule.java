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

import org.jacoco.core.analysis.ICoverageNode;

import java.util.HashMap;
import java.util.Map;

public class AbstractRule {
    private static final double DEFAULT_RATIO = 0;

    private final Map<ICoverageNode.CounterEntity, Double> configuration;


    /**
     * Construct a new CheckConfiguration instance.
     */
    public AbstractRule() {
        this.configuration = new HashMap<ICoverageNode.CounterEntity, Double>();
    }

    /**
     * Set the minimum allowed code coverage for instructions.
     *
     * @param ratio
     *            percent of instructions covered
     */
    public void setInstructionRatio(final Double ratio) {
        this.configuration.put(ICoverageNode.CounterEntity.INSTRUCTION, ratio);
    }

    /**
     * Set the minimum allowed code coverage for branches.
     *
     * @param ratio
     *            percent of branches covered
     */
    public void setBranchRatio(final Double ratio) {
        this.configuration.put(ICoverageNode.CounterEntity.BRANCH, ratio);
    }

    /**
     * Set the minimum allowed code coverage for lines.
     *
     * @param ratio
     *            percent of lines covered
     */
    public void setLineRatio(final Double ratio) {
        this.configuration.put(ICoverageNode.CounterEntity.LINE, ratio);
    }

    /**
     * Set the minimum allowed code coverage for complexity.
     *
     * @param ratio
     *            percent of complexities covered
     */
    public void setComplexityRatio(final Double ratio) {
        this.configuration.put(ICoverageNode.CounterEntity.COMPLEXITY, ratio);
    }

    /**
     * Set the minimum allowed code coverage for methods.
     *
     * @param ratio
     *            percent of methods covered
     */
    public void setMethodRatio(final Double ratio) {
        this.configuration.put(ICoverageNode.CounterEntity.METHOD, ratio);
    }

    /**
     * Set the minimum allowed code coverage for classes.
     *
     * @param ratio
     *            percent of classes covered
     */
    public void setClassRatio(final Double ratio) {
        this.configuration.put(ICoverageNode.CounterEntity.CLASS, ratio);
    }

    /**
     * Get the ratio for the given CounterEntity
     *
     * @param entity
     *            the counter type
     * @return minimum percent covered for given CounterEntity
     */
    public double getRatio(final ICoverageNode.CounterEntity entity) {
        final Double ratio = this.configuration.get(entity);
        return ratio == null ? DEFAULT_RATIO : ratio.doubleValue();
    }



}
