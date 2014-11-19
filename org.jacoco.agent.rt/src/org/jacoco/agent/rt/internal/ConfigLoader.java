/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal utility to load runtime configuration from a classpath resource and
 * from system properties. System property keys are prefixed with
 * <code>jacoco.</code>. If the same property is defined twice the system
 * property takes precedence.
 */
final class ConfigLoader {

	private static final String SYS_PREFIX = "jacoco-agent.";

	static Properties load(final String resource, final Properties system) {
		final Properties result = new Properties();

		// 1. Try to load resource
		final InputStream file = Offline.class.getResourceAsStream(resource);
		if (file != null) {
			try {
				result.load(file);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		// 2. Override with system properties
		for (final Map.Entry<Object, Object> entry : system.entrySet()) {
			final String keystr = entry.getKey().toString();
			if (keystr.startsWith(SYS_PREFIX)) {
				result.put(keystr.substring(SYS_PREFIX.length()),
						entry.getValue());
			}
		}

		// 3. Perform environment variable replacement
		final Pattern replacementPattern = Pattern
				.compile("\\\\(.)|\\$\\{([A-Za-z.-_]+)\\}");
		for (final Map.Entry<Object, Object> entry : result.entrySet()) {
			final String value = (String) entry.getValue();
			final Matcher m = replacementPattern.matcher(value);
			if (m.find()) {
				final StringBuffer sb = new StringBuffer(value.length() * 2);
				int offset = 0;
				do {
					sb.append(value, offset, m.start());
					final String propertyName = m.group(2);
					if (propertyName != null) {
						sb.append(system.getProperty(propertyName, ""));
					} else {
						// append escaped character
						sb.append(m.group(1));
					}
					offset = m.end();
				} while (m.find());
				if (offset < value.length()) {
					sb.append(value, offset, value.length());
				}
				entry.setValue(sb.toString());
			}
		}

		return result;
	}

	private ConfigLoader() {
	}

}
