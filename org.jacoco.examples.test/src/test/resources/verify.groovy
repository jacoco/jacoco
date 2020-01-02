/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Mirko Friedenhagen - initial API and implementation
 *
 *******************************************************************************/
File realBaseDir = new File(basedir, "../../../target/it/build");
assert new File(realBaseDir, "target/site/jacoco/index.html").exists();
assert !new File(realBaseDir, "target/site/jacoco-it/index.html").exists();
return true;
