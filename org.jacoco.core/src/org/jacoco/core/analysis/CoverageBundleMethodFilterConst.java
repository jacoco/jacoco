/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Miroslav Pojer - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface CoverageBundleMethodFilterConst {

	int SCOPE_PUBLIC = 0;
	int SCOPE_PROTECTED = 1;
	int SCOPE_PRIVATE = 2;
	int SCOPE_PACKAGE = 3;

	int GROUP_MEMBER = 1;
	int GROUP_OBJECT = 2;

	int TYPE_CLASS = 1;
	int TYPE_OBJECT = 2;
	int TYPE_TRAIT = 3;

	String STYPE_CLASS = "class";
	String STYPE_OBJECT = "object";
	String STYPE_TRAIT = "trait";
	String STYPE_DEV = "def";
	String STYPE_VAL = "val";
	String STYPE_VAR = "var";
	String STYPE_ANON = "anon";
	String STYPE_ENUM = "enum";

	List<String> supportedExtensions = new ArrayList<>(
			Collections.singletonList("scala")
	);

	List<String> validExceptions = new ArrayList<>(
			// '<init>' .. class constructor
			Collections.singletonList("<init>"));
}
