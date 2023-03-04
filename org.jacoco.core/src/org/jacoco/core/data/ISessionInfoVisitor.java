/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.data;

/**
 * Interface for data output of collected session information. This interface is
 * meant to be implemented by parties that want to retrieve data from the
 * coverage runtime.
 */
public interface ISessionInfoVisitor {

	/**
	 * Provides session information for the subsequent execution data calls. In
	 * case of merged sessions this method might be called multiple times.
	 *
	 * @param info
	 *            session information
	 */
	void visitSessionInfo(SessionInfo info);

}
