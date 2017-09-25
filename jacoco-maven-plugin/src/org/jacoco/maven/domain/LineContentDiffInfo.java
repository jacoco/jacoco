package org.jacoco.maven.domain;

/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Julian Gamble - initial API and implementation
 *
 *******************************************************************************/
/**
 * Retain information from cobertura about covered and uncovered lines we care about
 * Created by juliangamble on 16/7/17.
 */
public class LineContentDiffInfo extends LineDiffInfo {
    private String content;

    private boolean isCovered;

    private boolean isCoverageApplicable;

    public LineContentDiffInfo(Integer lineNumber, String filePackagePathName, String contentArg, boolean isCoveredArg, boolean isCoverageApplicableArg) {
        super(lineNumber, filePackagePathName);
        content = contentArg;
        isCovered = isCoveredArg;
        isCoverageApplicable = isCoverageApplicableArg;
    }

    public String getContent() {
        return content;
    }

    public boolean isCovered() {
        return isCovered;
    }

    public boolean isCoverageApplicable() {
        return isCoverageApplicable;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "LineContentDiffInfo{" +
                "lineNumber=" + lineNumber +
                ", filePackagePathName='" + filePackagePathName + '\'' +
                ", content='" + content + '\'' +
                ", isCovered=" + isCovered +
                ", isCoverageApplicable=" + isCoverageApplicable +
                '}';
    }

    public String toLineDifferenceString() {
        return filePackagePathName + ":" + lineNumber + " " + content;
    }
}
