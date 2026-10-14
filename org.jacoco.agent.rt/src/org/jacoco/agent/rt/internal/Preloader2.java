/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Preloader2 {

	public static void main(String[] args) {
		load();
	}

	private final Set<String> dependencies = new HashSet<String>();
	private final Queue<String> workList = new LinkedList<String>();

	public static void load() {
		final ClassLoader classLoader = Preloader.class.getClassLoader();
		final Preloader2 d = new Preloader2();
		d.calculate(classLoader);
		for (String name : d.dependencies) {
			final String className = name.replace('/', '.');
			loadClass(classLoader, className);
		}
	}

	private static void loadClass(ClassLoader classLoader, String className) {
		try {
			classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to load " + className, e);
		}
	}

	private void schedule(String className) {
		if (dependencies.add(className)) {
			workList.add(className);
		}
	}

	private void calculate(ClassLoader classLoader) {
		schedule(CoverageTransformer.class.getName().replace('.', '/'));
		while (!workList.isEmpty()) {
			final String name = workList.remove();
			InputStream inputStream = classLoader
					.getResourceAsStream(name + ".class");

			if (inputStream == null) {
				// TODO(Godin): JDK 7
				// java/util/function/IntConsumer$BaseSpliterator
				System.out.println("Can't find " + name);
				dependencies.remove(name);
				continue;
			}

			ClassRemapper v = new ClassRemapper(new DeepVisitor(),
					new Remapper() {
						@Override
						public String map(String typeName) {
							schedule(typeName);
							return typeName;
						}
					});

			try {
				new ClassReader(inputStream).accept(v,
						ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(
						"Unable to calculate dependencies " + name, e);
			}
		}
	}

    private static class DeepVisitor extends ClassVisitor {
		DeepVisitor() {
			super(Opcodes.ASM5);
		}

		private static final AnnotationVisitor av = new AnnotationVisitor(
				Opcodes.ASM5) {

			@Override
			public AnnotationVisitor visitAnnotation(String name, String desc) {
				return this;
			}

			@Override
			public AnnotationVisitor visitArray(String name) {
				return this;
			}
		};

		private static final MethodVisitor mv = new MethodVisitor(
				Opcodes.ASM5) {

			@Override
			public AnnotationVisitor visitAnnotationDefault() {
				return av;
			}

			@Override
			public AnnotationVisitor visitAnnotation(String desc,
					boolean visible) {
				return av;
			}

			@Override
			public AnnotationVisitor visitParameterAnnotation(int parameter,
					String desc, boolean visible) {
				return av;
			}
		};

		private static final FieldVisitor fieldVisitor = new FieldVisitor(
				Opcodes.ASM5) {
			@Override
			public AnnotationVisitor visitAnnotation(String desc,
					boolean visible) {
				return av;
			}
		};

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return av;
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc,
				String signature, Object value) {
			return fieldVisitor;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			return mv;
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef,
				TypePath typePath, String desc, boolean visible) {
			return av;
		}
	}

}
