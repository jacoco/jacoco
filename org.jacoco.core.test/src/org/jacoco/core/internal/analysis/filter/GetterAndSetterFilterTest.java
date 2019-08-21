/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: donhui - add GetterAndSetterFilterTest
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;

/**
 * Unit tests for {@link GetterAndSetterFilter}.
 */
public class GetterAndSetterFilterTest extends FilterTestBase {

    private final GetterAndSetterFilter filter = new GetterAndSetterFilter();

    private void should_filter_common(MethodNode m) {
        m.visitInsn(Opcodes.NOP);
        context.classFields = new HashSet<FieldNode>();
        context.classFields.add(new FieldNode(0, "commonStr", null, null, null));
        context.classFields.add(new FieldNode(0, "flag", "Z", null, null));

        filter.filter(m, context, output);

        assertIgnored(new Range(m.instructions.getFirst(), m.instructions.getLast()));
    }

    @Test
    public void should_filter_getter() {
        final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0, "getCommonStr", null, null, null);
        should_filter_common(m);
    }

    @Test
    public void should_filter_getter_for_boolean() {
        final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0, "isFlag", "()Z", null, null);
        should_filter_common(m);
    }

    @Test
    public void should_filter_setter() {
        final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0, "setCommonStr", null, null, null);
        should_filter_common(m);
    }

    @Test
    public void should_not_filter() {
        final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0, "getCommonStr", null, null, null);
        m.visitInsn(Opcodes.NOP);

        filter.filter(m, context, output);

        assertIgnored();
    }

}
