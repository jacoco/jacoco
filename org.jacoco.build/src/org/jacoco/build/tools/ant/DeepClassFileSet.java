/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.build.tools.ant;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Union;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * Custom Ant type that extracts all class files that are referenced from a
 * given set of root classes. Unlike the original Ant type
 * <code>ClassFileSet</code> this task works recursively.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class DeepClassFileSet implements ResourceCollection {

	private static final String CLASSEXTENSION = ".class";

	private final List<ResourceCollection> delegates = new ArrayList<ResourceCollection>();

	private final Union seed = new Union();

	private Collection<Resource> resourceSet;

	// === ResourceCollection ===

	public boolean isFilesystemOnly() {
		return false;
	}

	public Iterator<?> iterator() {
		return getCalculateResources().iterator();
	}

	public int size() {
		return getCalculateResources().size();
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
	 * Sets the seed classes where dependency traversal starts.
	 * 
	 * @return container for seed classes
	 */
	public Union createSeed() {
		return seed;
	}

	// === Internal dependency calculation ===

	private Collection<Resource> getCalculateResources() {
		if (resourceSet != null) {
			return resourceSet;
		}
		final Map<String, Resource> allClasses = createAllClassesMap();
		final Map<String, Resource> selectedClasses = new HashMap<String, Resource>();
		for (final Iterator<?> i = seed.iterator(); i.hasNext();) {
			final Resource resource = (Resource) i.next();
			if (resource.getName().endsWith(CLASSEXTENSION)) {
				for (final String dependency : getDependencies(resource)) {
					addClass(dependency, selectedClasses, allClasses);
				}
			}
		}
		return resourceSet = selectedClasses.values();
	}

	private void addClass(final String name,
			final Map<String, Resource> selectedClasses,
			final Map<String, Resource> allClasses) {
		if (selectedClasses.containsKey(name)) {
			// we already have this class:
			return;
		}
		final Resource resource = allClasses.get(name);
		if (resource == null) {
			// this class is out of scope:
			return;
		}
		selectedClasses.put(name, resource);
		for (final String dependency : getDependencies(resource)) {
			addClass(dependency, selectedClasses, allClasses);
		}
	}

	/**
	 * Reads and indexes all class of the underlying collections
	 * 
	 * @return VM class name to resource mapping
	 */
	private Map<String, Resource> createAllClassesMap() {
		final Map<String, Resource> map = new HashMap<String, Resource>();
		for (final ResourceCollection c : delegates) {
			for (final Iterator<?> i = c.iterator(); i.hasNext();) {
				final Resource resource = (Resource) i.next();
				final String classname = getClassName(resource);
				if (classname != null) {
					map.put(classname, resource);
				}
			}
		}
		return map;
	}

	private String getClassName(final Resource resource) {
		final String name = resource.getName();
		if (!resource.isExists() || !name.endsWith(CLASSEXTENSION)) {
			return null;
		}
		final String classname = name.substring(0, name.length()
				- CLASSEXTENSION.length());
		// On Windows we get back slashes:
		return classname.replace('\\', '/');
	}

	/**
	 * Finds all dependencies of the given class file.
	 * 
	 * @param resource
	 *            class file resource
	 * @return VM names of all dependent classes
	 */
	private Collection<String> getDependencies(final Resource resource) {
		final Collection<String> dependencies = new HashSet<String>();
		final Remapper remapper = new Remapper() {
			@Override
			public String map(final String typeName) {
				dependencies.add(typeName);
				return typeName;
			}
		};
		try {
			final InputStream stream = resource.getInputStream();
			final ClassReader reader = new ClassReader(stream);
			stream.close();
			reader.accept(new RemappingClassAdapter(new EmptyVisitor(),
					remapper), 0);
		} catch (final IOException e) {
			throw new BuildException(e);
		}
		return dependencies;
	}

}
