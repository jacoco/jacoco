/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.build.tools.ant;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * Custom Ant type that renames a collection of class files. Not only the file
 * names are renamed, also the class definitions are adjusted.
 */
public class RenamedClassFileSet implements ResourceCollection {

	/**
	 * Type for the nested mapping element.
	 */
	public static class Mapping {

		private Pattern from;

		private String to;

		/**
		 * @param from
		 *            the pattern that may apply
		 */
		public void setFrom(final String from) {
			this.from = Pattern.compile(from);
		}

		/**
		 * @param to
		 *            replacement text
		 */
		public void setTo(final String to) {
			this.to = to;
		}

		String apply(final String name) {
			return from.matcher(name).replaceAll(to);
		}

	}

	private static final String CLASSEXTENSION = ".class";

	private final List<ResourceCollection> delegates = new ArrayList<ResourceCollection>();

	private Collection<Resource> renamedResources;

	private final List<Mapping> mappings = new ArrayList<Mapping>();

	// === ResourceCollection ===

	public boolean isFilesystemOnly() {
		return false;
	}

	public Iterator<?> iterator() {
		return getRenamedResources().iterator();
	}

	public int size() {
		return getRenamedResources().size();
	}

	// === Configuration API ===

	/**
	 * Adds the given resource collection to the collection of classes where
	 * dependent class files are extracted from.
	 * 
	 * @param collection
	 *            collection to add
	 */
	public void add(final ResourceCollection collection) {
		delegates.add(collection);
	}

	/**
	 * Adds a mapping element.
	 * 
	 * @param mapping
	 *            element to add
	 */
	public void addMapping(final Mapping mapping) {
		mappings.add(mapping);
	}

	// === Internal renaming implementation ===

	private Collection<Resource> getRenamedResources() {
		if (renamedResources != null) {
			return renamedResources;
		}
		renamedResources = new HashSet<Resource>();
		for (final ResourceCollection c : delegates) {
			final Iterator<?> i = c.iterator();
			while (i.hasNext()) {
				final Resource resource = (Resource) i.next();
				if (resource.isExists()) {
					renamedResources.add(rename(resource));
				}
			}
		}
		return renamedResources;
	}

	private String rename(String className) {
		for (final Mapping m : mappings) {
			className = m.apply(className);
		}
		return className;
	}

	private Resource rename(final Resource res) {
		// On Windows we get back slashes:
		final String name = rename(res.getName().replace('\\', '/'));
		return new Resource(name, res.isExists(), res.getLastModified(),
				res.isDirectory(), res.getSize()) {
			@Override
			public InputStream getInputStream() throws IOException {
				final InputStream stream = res.getInputStream();
				if (getName().endsWith(CLASSEXTENSION)) {
					return rename(stream);
				}
				return stream;
			}
		};
	}

	private InputStream rename(final InputStream stream) throws IOException {
		final Remapper remapper = new Remapper() {
			@Override
			public String map(final String typeName) {
				return rename(typeName);
			}
		};
		final ClassReader reader = new ClassReader(stream);
		stream.close();
		// Don't re-use constant pool as we're renaming all classes:
		final ClassWriter writer = new ClassWriter(0);
		reader.accept(new RemappingClassAdapter(writer, remapper),
				ClassReader.EXPAND_FRAMES);
		return new ByteArrayInputStream(writer.toByteArray());
	}

}
