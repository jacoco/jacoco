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
 * Retain information from JGit about lines of code that have changed.
 * Created by juliangamble on 16/7/17.
 */
public class LineDiffInfo {
    protected Integer lineNumber;
    protected String filePackagePathName;

    public LineDiffInfo(Integer lineNumber, String filePackagePathName) {
        this.lineNumber = lineNumber;
        this.filePackagePathName = filePackagePathName;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getFilePackagePathName() {
        return filePackagePathName;
    }

    @Override
    public String toString() {
        return "LineDiffInfo{" +
                "lineNumber=" + lineNumber +
                ", filePackagePathName='" + filePackagePathName + '\'' +
                '}';
    }

    public String toLineDifferenceString() {
        return filePackagePathName + ":" + lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineDiffInfo)) return false;

        LineDiffInfo that = (LineDiffInfo) o;

        if (!lineNumber.equals(that.lineNumber)) return false;
        return filePackagePathName.equals(that.filePackagePathName);
    }

    @Override
    public int hashCode() {
        int result = lineNumber.hashCode();
        result = 31 * result + filePackagePathName.hashCode();
        return result;
    }
}
