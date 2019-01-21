/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ant;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

/**
 * Unit tests for the locale conversion built into {@link ReportTask}.
 */
public class ReportTaskLocaleTest {

	@Test
	public void testNone() {
		Locale locale = ReportTask.parseLocale("");

		assertEquals("", locale.getLanguage());
		assertEquals("", locale.getCountry());
		assertEquals("", locale.getVariant());
	}

	@Test
	public void testLanguage() {
		Locale locale = ReportTask.parseLocale("fr");

		assertEquals("fr", locale.getLanguage());
		assertEquals("", locale.getCountry());
		assertEquals("", locale.getVariant());
	}

	@Test
	public void testLanguageCountry() {
		Locale locale = ReportTask.parseLocale("fr_FR");

		assertEquals("fr", locale.getLanguage());
		assertEquals("FR", locale.getCountry());
		assertEquals("", locale.getVariant());
	}

	@Test
	public void testLanguageCountryVariant() {
		Locale locale = ReportTask.parseLocale("de_CH_Matte");

		assertEquals("de", locale.getLanguage());
		assertEquals("CH", locale.getCountry());
		assertEquals("Matte", locale.getVariant());
	}

}
