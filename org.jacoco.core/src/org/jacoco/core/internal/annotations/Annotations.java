/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc Pawlowsky - initial API and implementation
 *    
 *******************************************************************************/

package org.jacoco.core.internal.annotations;

import org.jacoco.annotations.TreatAsCovered;

/** Detect JaCoCo annotations. */
public class Annotations {
	public static final String treatAsCoveredId = "L"
			+ TreatAsCovered.class.getName().replaceAll("\\.", "/") + ";";

	/**
	 * Does the description match the definition of TreatAsCovered.
	 * 
	 * @param desc
	 *            Description of an annotation from a class file.
	 * @return true if the description matches TreatAsCovered.
	 */
	public static boolean isTreatAsCovered(final String desc) {
		final boolean matched = treatAsCoveredId.equals(desc);
		return matched;
	}
}
