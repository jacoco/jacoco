/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    Håvard Nesvold - refactored to also accommodate output from successful checks
 *    
 *******************************************************************************/
package org.jacoco.report.check;

/**
 * Call-back interface which is used to report rule checks to.
 * 
 */
public interface ICheckerOutput {

    /**
     * Called for every check;
     *
     * @param result
     *            the result of the checked rule.
     */
    void onResult(CheckResult result);

}
