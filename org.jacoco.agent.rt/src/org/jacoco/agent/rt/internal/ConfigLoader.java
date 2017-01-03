/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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

	private static final Pattern SUBST_PATTERN = Pattern
			.compile("\\$\\{([^\\}]+)\\}");

	static Properties load(final String resource, final Properties system) {
		final Properties result = new Properties();
		loadResource(resource, result);
		loadSystemProperties(system, result);
		substSystemProperties(result, system);
		return result;
	}

	private static void loadResource(final String resource,
			final Properties result) {
		final InputStream file = Offline.class.getResourceAsStream(resource);
		if (file != null) {
			try {
				result.load(file);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static void loadSystemProperties(final Properties system,
			final Properties result) {
		for (final Map.Entry<Object, Object> entry : system.entrySet()) {
			final String keystr = entry.getKey().toString();
			if (keystr.startsWith(SYS_PREFIX)) {
				result.put(keystr.substring(SYS_PREFIX.length()),
						entry.getValue());
			}
		}
	}

	private static void substSystemProperties(final Properties result,
			final Properties system) {
		for (final Map.Entry<Object, Object> entry : result.entrySet()) {
			final String oldValue = (String) entry.getValue();
			final StringBuilder newValue = new StringBuilder();
			final Matcher m = SUBST_PATTERN.matcher(oldValue);
			int pos = 0;
			while (m.find()) {
				newValue.append(oldValue.substring(pos, m.start()));
				final String sub = system.getProperty(m.group(1));
				newValue.append(sub == null ? m.group(0) : sub);
				pos = m.end();
			}
			newValue.append(oldValue.substring(pos));
			entry.setValue(newValue.toString());
		}
	}

	private ConfigLoader() {
	}

}
