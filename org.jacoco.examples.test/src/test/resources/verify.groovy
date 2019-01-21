/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mirko Friedenhagen - initial API and implementation
 *
 *******************************************************************************/
File realBaseDir = new File(basedir, "../../../target/it/build");
assert new File(realBaseDir, "target/site/jacoco/index.html").exists();
assert !new File(realBaseDir, "target/site/jacoco-it/index.html").exists();
return true;
