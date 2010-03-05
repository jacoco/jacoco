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
package org.jacoco.core.instr;

import static java.lang.String.format;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.runtime.IRuntime;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Adapter that instruments a class for coverage tracing.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassInstrumenter extends BlockClassAdapter implements
		IExecutionDataAccessorGenerator {

	private final ClassVisitor delegate;

	private final long id;

	private final IRuntime runtime;

	private String className;

	// Inlining the coverage data init method is required for interfaces because
	// we can't add additional methods here.
	private boolean inlineInit;

	/**
	 * Emits a instrumented version of this class to the given class visitor.
	 * 
	 * @param id
	 *            unique identifier given to this class
	 * @param runtime
	 *            this runtime will be used for instrumentation
	 * @param cv
	 *            next delegate in the visitor chain will receive the
	 *            instrumented class
	 */
	public ClassInstrumenter(final long id, final IRuntime runtime,
			final ClassVisitor cv) {
		this.delegate = cv;
		this.id = id;
		this.runtime = runtime;
	}

	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		this.className = name;
		this.inlineInit = (access & Opcodes.ACC_INTERFACE) != 0;
		delegate.visit(version, access, name, signature, superName, interfaces);
	}

	public FieldVisitor visitField(final int access, final String name,
			final String desc, final String signature, final Object value) {
		assertNotInstrumented(name, GeneratorConstants.DATAFIELD_NAME);
		return delegate.visitField(access, name, desc, signature, value);
	}

	@Override
	protected IBlockMethodVisitor visitNonAbstractMethod(final int access,
			final String name, final String desc, final String signature,
			final String[] exceptions) {

		assertNotInstrumented(name, GeneratorConstants.INITMETHOD_NAME);

		final MethodVisitor mv = delegate.visitMethod(access, name, desc,
				signature, exceptions);

		if (mv == null) {
			return null;
		}
		final IExecutionDataAccessorGenerator gen = inlineInit ? runtime : this;
		return new MethodInstrumenter(mv, access, name, desc, gen, id);
	}

	@Override
	protected MethodVisitor visitAbstractMethod(final int access,
			final String name, final String desc, final String signature,
			final String[] exceptions) {
		return delegate.visitMethod(access, name, desc, signature, exceptions);
	}

	public void visitEnd() {
		if (!inlineInit) {
			createDataField();
			createInitMethod();
		}
		registerClass();
		delegate.visitEnd();
	}

	private void createDataField() {
		delegate.visitField(GeneratorConstants.DATAFIELD_ACC,
				GeneratorConstants.DATAFIELD_NAME,
				GeneratorConstants.PROBEDATA_TYPE.getDescriptor(), null, null);
	}

	private void createInitMethod() {
		final int access = GeneratorConstants.INITMETHOD_ACC;
		final MethodVisitor mv = delegate.visitMethod(access,
				GeneratorConstants.INITMETHOD_NAME,
				GeneratorConstants.INITMETHOD_DESC, null, null);

		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				GeneratorConstants.DATAFIELD_NAME,
				GeneratorConstants.PROBEDATA_TYPE.getDescriptor());
		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: [Z
		// Stack[0]: [Z

		// Skip initialization when we already have a data array:
		final Label alreadyInitialized = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, alreadyInitialized);

		// Stack[0]: [Z

		mv.visitInsn(Opcodes.POP);
		final int size = genInitializeDataField(mv);

		// Stack[0]: [Z

		// Return the method's block array:
		mv.visitLabel(alreadyInitialized);
		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(Math.max(size, 2), 1); // Maximum local stack size is 2
		mv.visitEnd();
	}

	/**
	 * Generates the byte code to initialize the static coverage data field
	 * within this class.
	 * 
	 * The code will push the [[Z data array on the operand stack.
	 * 
	 * @param mv
	 *            generator to emit code to
	 */
	private int genInitializeDataField(final MethodVisitor mv) {
		final int size = runtime.generateDataAccessor(id, mv);

		// Stack[0]: [Z

		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: [Z
		// Stack[0]: [Z

		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				GeneratorConstants.DATAFIELD_NAME,
				GeneratorConstants.PROBEDATA_TYPE.getDescriptor());

		// Stack[0]: [Z

		return Math.max(size, 2); // Maximum local stack size is 2
	}

	/**
	 * Ensures that the given member does not correspond to a internal member
	 * created by the instrumentation process. This would mean that the class
	 * has been instrumented twice.
	 * 
	 * @param member
	 *            name of the member to check
	 * @param instrMember
	 *            name of a instrumentation member
	 * @throws IllegalStateException
	 *             thrown if the member has the same name than the
	 *             instrumentation member
	 */
	private void assertNotInstrumented(final String member,
			final String instrMember) throws IllegalStateException {
		if (member.equals(instrMember)) {
			throw new IllegalStateException(format(
					"Class %s is already instrumented.", className));
		}
	}

	/**
	 * Create a execution data structure according to the structure of this
	 * class and registers it with the runtime.
	 */
	private void registerClass() {
		final boolean[] data = new boolean[getProbeCount()];
		runtime.registerClass(id, className, data);
	}

	// Methods we simply delegate:

	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		return delegate.visitAnnotation(desc, visible);
	}

	public void visitAttribute(final Attribute attr) {
		delegate.visitAttribute(attr);
	}

	public void visitInnerClass(final String name, final String outerName,
			final String innerName, final int access) {
		delegate.visitInnerClass(name, outerName, innerName, access);
	}

	public void visitOuterClass(final String owner, final String name,
			final String desc) {
		delegate.visitOuterClass(owner, name, desc);
	}

	public void visitSource(final String source, final String debug) {
		delegate.visitSource(source, debug);
	}

	// === IExecutionDataAccessorGenerator ===

	public int generateDataAccessor(final long classid, final MethodVisitor mv) {
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
				GeneratorConstants.INITMETHOD_NAME,
				GeneratorConstants.INITMETHOD_DESC);
		return 1;
	}

}
