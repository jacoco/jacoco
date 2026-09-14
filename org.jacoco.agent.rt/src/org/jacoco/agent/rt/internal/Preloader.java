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
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Inspired by "org.objectweb.asm.depend.DependencyVisitor" in ASM examples.
 */
class Preloader {

	public static void main(String[] args) {
		load();
	}

	private final Queue<String> workList = new LinkedList<String>();
	private final Set<String> dependencies = new HashSet<String>();

	public static void load() {
		final ClassLoader classLoader = Preloader.class.getClassLoader();
		final Preloader d = new Preloader();
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

	private void calculate(ClassLoader classLoader) {
		workList.add(CoverageTransformer.class.getName().replace('.', '/'));
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
			try {
				new ClassReader(inputStream).accept(new DClassVisitor(), 0);
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(
						"Unable to calculate dependencies " + name, e);
			}
		}
	}

	class DClassVisitor extends ClassVisitor {
		public DClassVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			if (signature == null) {
				if (superName != null) {
					addType(Type.getObjectType(superName));
				}
				for (String i : interfaces) {
					addType(Type.getObjectType(i));
				}
			} else {
				addSignature(signature);
			}
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			addType(Type.getType(desc));
			return new DAnnotationVisitor();
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef,
				TypePath typePath, String desc, boolean visible) {
			addType(Type.getType(desc));
			return new DAnnotationVisitor();
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc,
				String signature, Object value) {
			if (signature == null) {
				addType(Type.getType(desc));
			} else {
				addTypeSignature(signature);
			}
			if (value instanceof Type) {
				addType((Type) value);
			}
			return new DFieldVisitor();
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			if (signature == null) {
				addType(Type.getMethodType(desc));
			} else {
				addSignature(signature);
			}
			if (exceptions != null) {
				for (String e : exceptions) {
					addType(Type.getObjectType(e));
				}
			}
			return new DMethodVisitor();
		}
	}

	class DFieldVisitor extends FieldVisitor {
		public DFieldVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			addType(Type.getType(desc));
			return new DAnnotationVisitor();
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef,
				TypePath typePath, String desc, boolean visible) {
			addType(Type.getType(desc));
			return new DAnnotationVisitor();
		}
	}

	class DAnnotationVisitor extends AnnotationVisitor {
		public DAnnotationVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public void visit(String name, Object value) {
			if (value instanceof Type) {
				addType((Type) value);
			}
		}

		@Override
		public void visitEnum(String name, String desc, String value) {
			addType(Type.getType(desc));
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String desc) {
			addType(Type.getType(desc));
			return this;
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			return this;
		}
	}

	class DMethodVisitor extends MethodVisitor {
		public DMethodVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			return new DAnnotationVisitor();
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			addType(Type.getType(desc));
			return new DAnnotationVisitor();
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef,
				TypePath typePath, String desc, boolean visible) {
			addType(Type.getType(desc));
			return new DAnnotationVisitor();
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter,
				String desc, boolean visible) {
			addType(Type.getType(desc));
			return new DAnnotationVisitor();
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
			addType(Type.getObjectType(type));
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name,
				String desc) {
			addType(Type.getObjectType(owner));
			addType(Type.getType(desc));
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name,
				String desc, boolean itf) {
			addType(Type.getObjectType(owner));
			addType(Type.getMethodType(desc));
		}

		@Override
		public void visitLdcInsn(Object cst) {
			if (cst instanceof Type) {
				addType((Type) cst);
			}
			// TODO Handle
		}

		@Override
		public void visitMultiANewArrayInsn(String desc, int dims) {
			addType(Type.getType(desc));
		}

		@Override
		public AnnotationVisitor visitInsnAnnotation(int typeRef,
				TypePath typePath, String desc, boolean visible) {
			addType(Type.getType(desc));
			return new DAnnotationVisitor();
		}

		@Override
		public void visitLocalVariable(String name, String desc,
				String signature, Label start, Label end, int index) {
			if (signature == null) {
				addType(Type.getType(desc));
			} else {
				addTypeSignature(signature);
			}
		}

		@Override
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
				TypePath typePath, Label[] start, Label[] end, int[] index,
				String desc, boolean visible) {
			addType(Type.getType(desc));
			return new DAnnotationVisitor();
		}

		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler,
				String type) {
			if (type != null) {
				addType(Type.getObjectType(type));
			}
		}

		@Override
		public AnnotationVisitor visitTryCatchAnnotation(int typeRef,
				TypePath typePath, String desc, boolean visible) {
			addType(Type.getType(desc));
			return new DAnnotationVisitor();
		}
	}

	class DSignatureVisitor extends SignatureVisitor {
		String signatureClassName;

		public DSignatureVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public void visitClassType(String name) {
			signatureClassName = name;
			addType(Type.getObjectType(signatureClassName));
		}

		@Override
		public void visitInnerClassType(String name) {
			signatureClassName = signatureClassName + "$" + name;
			addType(Type.getObjectType(signatureClassName));
		}
	}

	private void addSignature(String signature) {
		new SignatureReader(signature).accept(new DSignatureVisitor());
	}

	private void addTypeSignature(String signature) {
		new SignatureReader(signature).acceptType(new DSignatureVisitor());
	}

	private void addType(Type type) {
		switch (type.getSort()) {
		case Type.OBJECT:
			String dep = type.getInternalName();
			if (dependencies.add(dep)) {
				workList.add(dep);
			}
			break;
		case Type.ARRAY:
			addType(type.getElementType());
			break;
		case Type.METHOD:
			addType(Type.getReturnType(type.getDescriptor()));
			Type[] argumentTypes = Type.getArgumentTypes(type.getDescriptor());
			for (Type argumentType : argumentTypes) {
				addType(argumentType);
			}
			break;
		}
	}

}
