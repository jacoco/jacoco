/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/

/**
 * <p>
 * Empirical-Big-O calculation and analysis. The empirical Big-O information is calculated
 * with an {@link org.jacoco.ebigo.analysis.EmpiricalBigOAnalyzer} instance from class files
 * (target) and
 * {@link org.jacoco.ebigo.analysis.IEmpiricalBigOVisitor execution and big-O data}
 * (actual).
 * </p>
 *
 * <p>
 * The {@link org.jacoco.ebigo.analysis.IEmpiricalBigOVisitor} ends up with the X-axis values
 * detected for the whole sample, and a {@link org.jacoco.ebigo.analysis.IClassEmpiricalBigO} 
 * for each class analyzed that contains the empirical big-O data for the class, and each 
 * method and line in the class. 
 * </p>
 */
package org.jacoco.ebigo.analysis;