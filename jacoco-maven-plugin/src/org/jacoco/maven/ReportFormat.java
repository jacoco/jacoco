/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

/**
 * Configurable output formats for the report goals.
 */
public enum ReportFormat {

	/**
	 * Multi-page html report.
	 */
	HTML,

	/**
	 * Single-file XML report.
	 */
	XML,

	/**
	 * Single-file CSV report.
	 */
	CSV

}
