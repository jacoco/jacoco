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
package org.jacoco.core.internal.instr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 * Support class to filter entries from JARs related to signatures.
 */
public class SignatureRemover {

	private static final Pattern SIGNATURE_FILES = Pattern
			.compile("META-INF/[^/]*\\.SF|" //
					+ "META-INF/[^/]*\\.DSA|" //
					+ "META-INF/[^/]*\\.RSA|" //
					+ "META-INF/SIG-[^/]*");

	private static final String MANIFEST_MF = "META-INF/MANIFEST.MF";

	private static final String DIGEST_SUFFIX = "-Digest";

	private boolean active;

	/**
	 * Creates a new remover which is active.
	 */
	public SignatureRemover() {
		active = true;
	}

	/**
	 * Defines whether this remover should be active. If it is not active it
	 * will not remove any entries.
	 *
	 * @param active
	 *            <code>true</code> if it should remove signature related
	 *            entries.
	 */
	public void setActive(final boolean active) {
		this.active = active;
	}

	/**
	 * Checks whether a entry with the provided name should be ignored at all.
	 *
	 * @param name
	 *            path name of the entry in question
	 * @return true is the entry should be ignored
	 */
	public boolean removeEntry(final String name) {
		return active && SIGNATURE_FILES.matcher(name).matches();
	}

	/**
	 * Filters the content of the entry with the provided name if necessary.
	 *
	 * @param name
	 *            path name of the entry in question
	 * @param in
	 *            source for the element to filter
	 * @param out
	 *            output for the filtered contents
	 * @return <code>true</code> if the content was filtered
	 * @throws IOException
	 *             if the content can't be read or written
	 */
	public boolean filterEntry(final String name, final InputStream in,
			final OutputStream out) throws IOException {
		if (!active || !MANIFEST_MF.equals(name)) {
			return false;
		}
		final Manifest mf = new Manifest(in);
		filterManifestEntry(mf.getEntries().values());
		mf.write(out);
		return true;
	}

	private void filterManifestEntry(final Collection<Attributes> entry) {
		for (final Iterator<Attributes> i = entry.iterator(); i.hasNext();) {
			final Attributes attributes = i.next();
			filterManifestEntryAttributes(attributes);
			if (attributes.isEmpty()) {
				i.remove();
			}
		}
	}

	private void filterManifestEntryAttributes(final Attributes attrs) {
		for (final Iterator<Object> i = attrs.keySet().iterator(); i
				.hasNext();) {
			if (String.valueOf(i.next()).endsWith(DIGEST_SUFFIX)) {
				i.remove();
			}
		}
	}

}
