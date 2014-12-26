/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Håvard Nesvold - Initial implementation.
 *
 *******************************************************************************/
package org.jacoco.report.check;

import org.jacoco.core.analysis.ICoverageNode;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object containing the results from a single coverage check.
 */
public final class CheckResult {

    public static enum Result {
        OK, TOO_LOW, TOO_HIGH
    }

    private final ICoverageNode node;
    private final String elementName;
    private final Limit limit;
    private final Result result;

    private CheckResult(final ICoverageNode node,
                       final Limit limit,
                       final String elementName,
                       final Result result) {

        this.node = node;
        this.limit = limit;
        this.elementName = elementName;
        this.result = result;
    }

    public static CheckResult tooLow(final ICoverageNode node, final Limit limit, final String elementName) {
        return new CheckResult(node, limit, elementName, Result.TOO_LOW);
    }

    public static CheckResult tooHigh(final ICoverageNode node, final Limit limit, final String elementName) {
        return new CheckResult(node, limit, elementName, Result.TOO_HIGH);
    }

    public static CheckResult ok(final ICoverageNode node, final Limit limit, final String elementName) {
        return new CheckResult(node, limit, elementName, Result.OK);
    }

    public String createMessage() {
        final BigDecimal value = BigDecimal.valueOf(node.getCounter(limit.getEntity()).getValue(limit.getValue()));
        String message;
        if (result == Result.OK) {
            message = String.format("Rule conforms for %s %s: %s %s is %s",
                    node.getElementType(),
                    elementName,
                    limit.getEntityName(),
                    limit.getValueName(),
                    value.toPlainString());
        } else {
            String minimumOrMaximum;
            BigDecimal ref;
            RoundingMode roundingMode;
            if (result == Result.TOO_LOW) {
                ref = new BigDecimal(limit.getMinimum());
                roundingMode = RoundingMode.FLOOR;
                minimumOrMaximum = "minimum";
            } else if (result == Result.TOO_HIGH) {
                roundingMode = RoundingMode.CEILING;
                ref = new BigDecimal(limit.getMaximum());
                minimumOrMaximum = "maximum";
            } else {
                throw new IllegalStateException("Unsupported result: " + result);
            }
            BigDecimal rounded = value.setScale(ref.scale(), roundingMode);
            message = String.format("Rule violated for %s %s: %s %s is %s, but expected %s is %s",
                    node.getElementType(),
                    elementName,
                    limit.getEntityName(),
                    limit.getValueName(),
                    rounded.toPlainString(),
                    minimumOrMaximum,
                    ref.toPlainString());
        }
        return message;
    }
    
    public Result getResult() {
        return result;
    }

}
