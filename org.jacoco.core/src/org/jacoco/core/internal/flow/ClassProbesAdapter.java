/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.internal.flow;

import org.jacoco.core.internal.analysis.ClassAnalyzer;
import org.jacoco.core.internal.diff.ClassInfoDto;
import org.jacoco.core.internal.diff.CodeDiffUtil;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AnalyzerAdapter;

import java.util.List;

/**
 * A {@link org.objectweb.asm.ClassVisitor} that calculates probes for every
 * method.
 */
public class ClassProbesAdapter extends ClassVisitor
		implements IProbeIdGenerator {

	private static final MethodProbesVisitor EMPTY_METHOD_PROBES_VISITOR = new MethodProbesVisitor() {
	};

	private final ClassProbesVisitor cv;

	private final boolean trackFrames;

	private int counter = 0;

	private String name;

	/**
	 * Creates a new adapter that delegates to the given visitor.
	 *
	 * @param cv
	 *            instance to delegate to
	 * @param trackFrames
	 *            if <code>true</code> stackmap frames are tracked and provided
	 */
	public ClassProbesAdapter(final ClassProbesVisitor cv,
			final boolean trackFrames) {
		super(InstrSupport.ASM_API_VERSION, cv);
		this.cv = cv;
		this.trackFrames = trackFrames;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		// name 为当前正在处理的类，version 为字节码的JDK版本号（52表示JDK8）superName为 name 对应的类的父类，interfaces 为 name 对应的类实现的接口
		this.name = name;
		// 会跳到 ClassAnalyzer的visit方法
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public final MethodVisitor visitMethod(final int access, final String name,
										   final String desc, final String signature,
										   final String[] exceptions) {
		final MethodProbesVisitor methodProbes;
		// 在插桩逻辑中cv=ClassInstrumente，cv.vistMethod返回的是MethodInstrumenter，这个类是具体的方法插桩类
		final MethodProbesVisitor mv = cv.visitMethod(access, name, desc,
				signature, exceptions);
		if (null != mv) {
			List<ClassInfoDto> classInfos = null;
			if(cv instanceof ClassAnalyzer){
				classInfos = ((ClassAnalyzer) cv).getClassInfos();
			}
			// 增量代码，有点绕，由于参数定义成final,无法第二次指定,代码无法简化
			if (null != classInfos
					&& !classInfos.isEmpty()) {
				if (CodeDiffUtil.checkMethodIn(this.name, name, desc,classInfos)) {
					methodProbes = mv;
				} else {
					methodProbes = EMPTY_METHOD_PROBES_VISITOR;
				}
			} else {
				methodProbes = mv;
			}
		} else {
			methodProbes = EMPTY_METHOD_PROBES_VISITOR;
		}

		return new MethodSanitizer(null, access, name, desc, signature,
				exceptions) {

			// ClassInstrumente结束后会进入钩子方法，visitEnd()
			@Override
			public void visitEnd() {
				super.visitEnd();
				LabelFlowAnalyzer.markLabels(this);
				final MethodProbesAdapter probesAdapter = new MethodProbesAdapter(
						methodProbes, ClassProbesAdapter.this);
				// 插桩采用的是(version & 0xFFFF) >= Opcodes.V1_6，大于1.6的JDK
				if (trackFrames) {
					// 插桩采用的是(version & 0xFFFF) >= Opcodes.V1_6，大于1.6的JDK
					final AnalyzerAdapter analyzer = new AnalyzerAdapter(
							ClassProbesAdapter.this.name, access, name, desc,
							probesAdapter);
					probesAdapter.setAnalyzer(analyzer);
					// analyzer也是MethodVisitor，它的mv为probesAdapter，调用的是MethodProbesAdapter的适配类，probesAdapter适配中的mv
					// 又是methodProbes，最终调用的还是MethodInstrumenter的方法，MethodProbesAdapter中overwrite的重写方法中visitLabel，visint等方法，
					// probid自增，然后根据判断调用probeInserter实现插桩
					// 传入到MethodInstrumenter，实现插桩顺序连续性
					// MethodInstrumenter中ProbeInserter实现最终的插桩
					methodProbes.accept(this, analyzer);
				} else {
					// 这里调用的就是mv的钩子方法，,report的方法走的分支，从顶层的classProbesAdapter适配类过来
					methodProbes.accept(this, probesAdapter);
				}
			}

		};
	}

	@Override
	public void visitEnd() {
		cv.visitTotalProbeCount(counter);
		super.visitEnd();
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return counter++;
	}

	public int getId() {
		return counter;
	}

}
