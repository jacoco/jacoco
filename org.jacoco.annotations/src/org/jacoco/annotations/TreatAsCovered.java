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

package org.jacoco.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Treat the method as having all branches covered even if it is
  * never executed.
  */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TreatAsCovered {
}
