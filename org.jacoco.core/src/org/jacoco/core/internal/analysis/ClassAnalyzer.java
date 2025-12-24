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
package org.jacoco.core.internal.analysis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jacoco.core.internal.analysis.filter.Filters;
import org.jacoco.core.internal.analysis.filter.IFilter;
import org.jacoco.core.internal.analysis.filter.IFilterContext;
import org.jacoco.core.internal.analysis.filter.KotlinSMAP;
import org.jacoco.core.internal.diff.ChangeLine;
import org.jacoco.core.internal.diff.ClassInfoDto;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.flow.ClassProbesVisitor;
import org.jacoco.core.internal.flow.IProbeIdGenerator;
import org.jacoco.core.internal.flow.MethodProbesAdapter;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.tools.ExecFileLoader;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Analyzes the structure of a class.
 */
public class ClassAnalyzer extends ClassProbesVisitor
		implements IFilterContext {

	private final ClassCoverageImpl coverage;
	private final boolean[] probes;
	private final StringPool stringPool;

	private final Set<String> classAnnotations = new HashSet<String>();

	private final Set<String> classAttributes = new HashSet<String>();

	private String sourceDebugExtension;

	// 只收集方法中的指令的覆盖率，在收集到指令后退出后面的分析流程
	private boolean onlyAnaly = false;

	/**
	 * 变更类信息
	 */
	private List<ClassInfoDto> classInfos;

	private KotlinSMAP smap;
	private final HashMap<String, SourceNodeImpl> fragments = new HashMap<String, SourceNodeImpl>();

	private final IFilter filter;

	/**
	 * Creates a new analyzer that builds coverage data for a class.
	 *
	 * @param coverage
	 *            coverage node for the analyzed class data
	 * @param probes
	 *            execution data for this class or <code>null</code>
	 * @param stringPool
	 *            shared pool to minimize the number of {@link String} instances
	 */
	public ClassAnalyzer(final ClassCoverageImpl coverage,
			final boolean[] probes, final StringPool stringPool) {
		this.coverage = coverage;
		this.probes = probes;
		this.stringPool = stringPool;
		this.filter = Filters.all();
	}

	public ClassAnalyzer(final ClassCoverageImpl coverage,
						 final boolean[] probes, final StringPool stringPool,List<ClassInfoDto> classInfos, boolean onlyAnaly) {
		this.coverage = coverage;
		this.probes = probes;
		this.stringPool = stringPool;
		this.filter = Filters.all();
		this.classInfos = classInfos;
		this.onlyAnaly = onlyAnaly;
	}


	public List<ClassInfoDto> getClassInfos() {
		return classInfos;
	}

	public void setClassInfos(List<ClassInfoDto> classInfos) {
		this.classInfos = classInfos;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		coverage.setSignature(stringPool.get(signature));
		coverage.setSuperName(stringPool.get(superName));
		coverage.setInterfaces(stringPool.get(interfaces));
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		classAnnotations.add(desc);
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public void visitAttribute(final Attribute attribute) {
		classAttributes.add(attribute.type);
	}

	@Override
	public void visitSource(final String source, final String debug) {
		coverage.setSourceFileName(stringPool.get(source));
		sourceDebugExtension = debug;
	}

	@Override
	public MethodProbesVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {

		InstrSupport.assertNotInstrumented(name, coverage.getName());

		final InstructionsBuilder builder = new InstructionsBuilder(probes);

		// 对方法解析完毕后的一个钩子方法，从visitMethod的mv对象调用过来
		return new MethodAnalyzer(builder) {

			@Override
			public void accept(final MethodNode methodNode,
					final MethodVisitor methodVisitor) {
				// 统计method的方法体的指令级别覆盖率，指令级别需要关注的是braches和coverbraches，line代码合并不需要关注，染色用
				String methodSign = access + name + desc + signature;

				super.accept(methodNode, methodVisitor);
				// 合并多版本覆盖率的时候不要走后面addMethodCoverage的流程，只获取到指令覆盖率就行
				if (exceptions != null) {
					for (String s : exceptions) {
						methodSign += s;
					}
				}
				Map<String, Map<String, Map<String, Instruction>>> instrunctions = ExecFileLoader.instrunctionsThreadLocal.get();
				Map<String, boolean[]> probesMap = ExecFileLoader.probesMap.get();
				if (onlyAnaly) {
					Map<String, Map<String, Instruction>> methodInstructions = new HashMap<>();
					Map<String, Instruction> instructionMap = new HashMap<>();
					Map<AbstractInsnNode, Instruction> builderInstructions = builder.getInstructions();
					for (Instruction instruction : builderInstructions.values()) {
						instructionMap.put(instruction.getSign(), instruction);
					}
					methodInstructions.put(methodSign, instructionMap);
					if (instrunctions == null) {
						instrunctions = new HashMap<>();
						instrunctions.put(coverage.getName(), methodInstructions);
						ExecFileLoader.instrunctionsThreadLocal.set(instrunctions);
					} else {
						if (instrunctions.containsKey(coverage.getName())) {
							instrunctions.get(coverage.getName()).put(methodSign, instructionMap);
						} else {
							instrunctions.put(coverage.getName(), methodInstructions);
						}
					}
					if (probesMap == null) {
						probesMap = new HashMap<>();
						probesMap.put(coverage.getName(), probes);
						ExecFileLoader.probesMap.set(probesMap);
					} else {
						probesMap.put(coverage.getName(), probes);
					}
					return;
				}
				// 如果存在已有的覆盖率数据，则合并method的指令覆盖率
				if (instrunctions != null && instrunctions.containsKey(coverage.getName())) {
					// 合并method的指令数据
					Map<String, Instruction> mergeInstructionMap = instrunctions.get(coverage.getName()).get(methodSign);
					// 通过指令判断是否为同一个方法，所有指令签名一样的情况下判断是一样的
					Map<AbstractInsnNode, Instruction> nowInstructions = builder.getInstructionsNotWireJumps();
					if (mergeInstructionMap != null && mergeInstructionMap.size() == nowInstructions.size()) {
						boolean isSameMethod = true;
						for (final Instruction instruction : mergeInstructionMap.values()) {
							Optional<Instruction> optionalInstruction = nowInstructions.values().stream().filter(i -> i.getSign().equals(instruction.getSign())).findAny();
							if (!optionalInstruction.isPresent()) {
								isSameMethod = false;
								break;
							}
						}
						// 同一个方法
						if (isSameMethod) {
							//合并exec新方案--直接合并两个probes对应的探针
							Map<String, boolean[]> mergeProbesMap = ExecFileLoader.probesMap.get();
							Optional<Instruction> instructionOptional = nowInstructions.values().stream().filter(i -> i.getProbeIndex() >= 0).min(Comparator.comparingInt(Instruction::getProbeIndex));
							if (probes != null && instructionOptional.isPresent()) {
								int probeStrart = instructionOptional.get().getProbeIndex();
								int probeEnd = nowInstructions.values().stream().filter(i -> i.getProbeIndex() >= 0).max(Comparator.comparingInt(Instruction::getProbeIndex)).get().getProbeIndex();
								int mergeProbeStart = mergeInstructionMap.values().stream().filter(i -> i.getProbeIndex() >= 0).min(Comparator.comparingInt(Instruction::getProbeIndex)).get().getProbeIndex();
								int mergeProbeEnd = mergeInstructionMap.values().stream().filter(i -> i.getProbeIndex() >= 0).max(Comparator.comparingInt(Instruction::getProbeIndex)).get().getProbeIndex();
								//jacoco是以方法级别进行插桩的，所以理论上同个方法的探针的长度是一样的
								assert probeEnd - probeStrart == mergeProbeEnd - mergeProbeStart;
								if (mergeProbesMap != null && mergeProbesMap.containsKey(coverage.getName())) {
									boolean[] mergeProbes = mergeProbesMap.get(coverage.getName());
									int currentIndex = mergeProbeStart;
									for (int k = probeStrart; k < probeEnd+1; k++) {
										if (mergeProbes[currentIndex]) {
											probes[k] = true;
										}
										currentIndex++;
									}
								}
							}
							//合并指令
							for (AbstractInsnNode key : nowInstructions.keySet()) {
								Instruction instruction = nowInstructions.get(key);
								//合并指令
								Instruction other = mergeInstructionMap.get(instruction.getSign());
								Instruction merge = instruction.mergeNew(other);
								nowInstructions.put(key, merge);
							}
						}
					}
				}
				List<ChangeLine> changeLineList = new ArrayList<>();
				if (methodVisitor instanceof MethodProbesAdapter) {
					MethodProbesAdapter methodProbesAdapter = (MethodProbesAdapter) methodVisitor;
					// 使用转换后的对象
					IProbeIdGenerator idGenerator = methodProbesAdapter.getIdGenerator();
					ClassProbesAdapter classProbesAdapter =(ClassProbesAdapter)idGenerator;
					String classNameInner = extractClassProbesAdapterName(classProbesAdapter);
					changeLineList = getChangeList(classNameInner);
				}
				addMethodCoverage(stringPool.get(name), stringPool.get(desc), stringPool.get(signature), builder, methodNode, changeLineList);
			}
		};
	}

	public String extractClassProbesAdapterName(ClassProbesAdapter adapter) {
		try {
			Field nameField = ClassProbesAdapter.class.getDeclaredField("name");
			nameField.setAccessible(true);
			return (String) nameField.get(adapter);
		} catch (Exception e) {
			throw new RuntimeException("Failed to extract ClassProbesAdapter name", e);
		}
	}
	/**
	 * 尝试获取changeList
	 */
	private List<ChangeLine> getChangeList(String classNameInner) {
		// 在此处提取一次changelist,用来判断，最好还是找到上一层的类级来计算changelist
		List<ChangeLine> changeLineList = null;
		if (ExecFileLoader.classInfoDto.get() != null) {
			List<ClassInfoDto> dtoList = ExecFileLoader.classInfoDto.get();
//            final String classNameInner = location.replace(".class", "");
			Optional<ClassInfoDto> classInfoDto = dtoList.stream().filter(i -> i.getClassFile().equals(classNameInner)).findAny();
			if (classInfoDto.isPresent()) {
				changeLineList = classInfoDto.get().getLines();
			}
		}
		return changeLineList;
	}

	private void addMethodCoverage(final String name, final String desc,
			final String signature, final InstructionsBuilder icc,
			final MethodNode methodNode, List<ChangeLine> changeLineList) {

		final Map<AbstractInsnNode, Instruction> instructions = icc
				.getInstructions();
		calculateFragments(instructions);

		final MethodCoverageCalculator mcc = new MethodCoverageCalculator(
				instructions);
		filter.filter(methodNode, this, mcc);

		final MethodCoverageImpl mc = new MethodCoverageImpl(name, desc,
				signature);
		// method的级别级别的计算，指令级别是最小的行覆盖率，这是整个覆盖率计算的基础
		mcc.calculate(mc, changeLineList);

		if (mc.containsCode()) {
			// class级别的覆盖率统计
			// Only consider methods that actually contain code
			coverage.addMethod(mc);
		}

	}

	private void calculateFragments(
			final Map<AbstractInsnNode, Instruction> instructions) {
		if (sourceDebugExtension == null || !Filters.isKotlinClass(this)) {
			return;
		}
		if (smap == null) {
			// Note that visitSource is invoked before visitAnnotation,
			// that's why parsing is done here
			smap = new KotlinSMAP(getSourceFileName(), sourceDebugExtension);
		}
		for (final KotlinSMAP.Mapping mapping : smap.mappings()) {
			if (coverage.getName().equals(mapping.inputClassName())
					&& mapping.inputStartLine() == mapping.outputStartLine()) {
				continue;
			}
			SourceNodeImpl fragment = fragments.get(mapping.inputClassName());
			if (fragment == null) {
				fragment = new SourceNodeImpl(null, mapping.inputClassName());
				fragments.put(mapping.inputClassName(), fragment);
			}
			final int mappingOutputEndLine = mapping.outputStartLine()
					+ mapping.repeatCount() - 1;
			for (Instruction instruction : instructions.values()) {
				if (mapping.outputStartLine() <= instruction.getLine()
						&& instruction.getLine() <= mappingOutputEndLine) {
					final int originalLine = mapping.inputStartLine()
							+ instruction.getLine() - mapping.outputStartLine();
					fragment.increment(instruction.getInstructionCounter(),
							CounterImpl.COUNTER_0_0, originalLine);
				}
			}
		}
	}

	@Override
	public FieldVisitor visitField(final int access, final String name,
			final String desc, final String signature, final Object value) {
		InstrSupport.assertNotInstrumented(name, coverage.getName());
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public void visitTotalProbeCount(final int count) {
		// nothing to do
	}

	@Override
	public void visitEnd() {
		if (!fragments.isEmpty()) {
			coverage.setFragments(Arrays
					.asList(fragments.values().toArray(new SourceNodeImpl[0])));
		}
	}

	// IFilterContext implementation

	public String getClassName() {
		return coverage.getName();
	}

	public String getSuperClassName() {
		return coverage.getSuperName();
	}

	public Set<String> getClassAnnotations() {
		return classAnnotations;
	}

	public Set<String> getClassAttributes() {
		return classAttributes;
	}

	public String getSourceFileName() {
		return coverage.getSourceFileName();
	}

	public String getSourceDebugExtension() {
		return sourceDebugExtension;
	}

}
