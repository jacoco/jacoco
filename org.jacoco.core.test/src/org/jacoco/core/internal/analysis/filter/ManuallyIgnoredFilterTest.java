/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ManuallyIgnoredFilter}.
 */
public class ManuallyIgnoredFilterTest extends FilterTestBase {

  public static final String SOME_METHOD_NAME = "someMethodName";
  private static final String ANNOTATION_VALUE = "L" + ManuallyIgnoredFilter.ANNOTATION_VALUE + ";";
  private final IFilter filter = new ManuallyIgnoredFilter();

  @Test
  public void should_filter_methods_annotated_with_runtime_invisible_annotation_JacocoIgnored() {
    final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
      SOME_METHOD_NAME, "()I", null, null);
    m.visitAnnotation(ANNOTATION_VALUE, true);

    m.visitInsn(Opcodes.ICONST_0);
    m.visitInsn(Opcodes.IRETURN);

    filter.filter(m, context, output);

    assertMethodIgnored(m);
  }

  @Test
  public void should_filter_classes_annotated_with_runtime_visible_annotation_JacocoIgnored() {
    final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
      "readExternal", "()V", null, null);

    m.visitInsn(Opcodes.NOP);
    context.classAnnotations.add(ANNOTATION_VALUE);

    filter.filter(m, context, output);

    assertMethodIgnored(m);
  }


  @Test
  public void should_not_filter_when_no_annotations() {
    final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
      "hashCode", "()I", null, null);

    m.visitInsn(Opcodes.ICONST_0);
    m.visitInsn(Opcodes.IRETURN);

    filter.filter(m, context, output);

    assertIgnored();
  }

  @Test
  public void should_not_filter_when_other_annotations() {
    final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
      "hashCode", "()I", null, null);
    m.visitAnnotation("LOtherAnnotation;", true);

    m.visitInsn(Opcodes.ICONST_0);
    m.visitInsn(Opcodes.IRETURN);

    context.classAnnotations.add("LOtherAnnotation;");

    filter.filter(m, context, output);

    assertIgnored();
  }

}
