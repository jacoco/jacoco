/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.runtime;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.data.ExecutionDataStore;

/**
 * Utility class that wraps read-access to an {@link ExecutionDataStore} in a
 * {@link Map} interface. The map interface can then safely be referenced by
 * instrumented code.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
class MapAdapter implements Map<Long, boolean[]> {

	private final ExecutionDataStore store;

	MapAdapter(final ExecutionDataStore store) {
		this.store = store;
	}

	public boolean[] get(final Object key) {
		final Long id = (Long) key;
		synchronized (store) {
			final boolean[] data = store.getData(id);
			if (data == null) {
				throw new IllegalStateException(String.format(
						"Unknown class id %x.", id));
			}
			return data;
		}
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(final Object key) {
		throw new UnsupportedOperationException();
	}

	public boolean containsValue(final Object value) {
		throw new UnsupportedOperationException();
	}

	public Set<Entry<Long, boolean[]>> entrySet() {
		throw new UnsupportedOperationException();
	}

	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	public Set<Long> keySet() {
		throw new UnsupportedOperationException();
	}

	public boolean[] put(final Long key, final boolean[] value) {
		throw new UnsupportedOperationException();
	}

	public void putAll(final Map<? extends Long, ? extends boolean[]> t) {
		throw new UnsupportedOperationException();
	}

	public boolean[] remove(final Object key) {
		throw new UnsupportedOperationException();
	}

	public Collection<boolean[]> values() {
		throw new UnsupportedOperationException();
	}

	public int size() {
		throw new UnsupportedOperationException();
	}

}
