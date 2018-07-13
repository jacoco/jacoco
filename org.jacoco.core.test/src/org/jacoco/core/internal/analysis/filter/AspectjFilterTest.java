/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lars Grefer - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AspectjFilterTest implements IFilterOutput {

    private final AspectjFilter filter = new AspectjFilter();

    private final FilterContextMock context = new FilterContextMock();

    private AbstractInsnNode fromInclusive;
    private AbstractInsnNode toInclusive;

    @Test
    public void testAjSyntheticAttribute() {
        final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
                "aspectOf", "()LFoo", null, null);
        m.visitAttribute(new TestAttribute("org.aspectj.weaver.AjSynthetic"));
        m.visitInsn(Opcodes.NOP);
        m.visitInsn(Opcodes.NOP);
        m.visitInsn(Opcodes.NOP);

        filter.filter(m, context, this);

        assertEquals(m.instructions.getFirst(), fromInclusive);
        assertEquals(m.instructions.getLast(), toInclusive);
    }

    @Test
    public void testClinitOnlyPre() {
        final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
                "<clinit>", "()V", null, null);
        m.visitInsn(Opcodes.NOP);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo", "ajc$preClinit", "()V", false);
        m.visitInsn(Opcodes.NOP);
        m.visitInsn(Opcodes.RETURN);

        filter.filter(m, context, this);

        assertEquals(m.instructions.getFirst(), fromInclusive);
        assertEquals(m.instructions.getLast(), toInclusive);
    }

    @Test
    public void testClinitPreAndUserCode() {
        final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
                "<clinit>", "()V", null, null);
        m.visitInsn(Opcodes.NOP);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo", "ajc$preClinit", "()V", false);
        m.visitInsn(Opcodes.NOP);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo", "myCustomMethod", "()V", false);
        m.visitInsn(Opcodes.RETURN);

        filter.filter(m, context, this);

        assertEquals(m.instructions.getFirst(), fromInclusive);
        assertEquals(toInclusive.getOpcode(), Opcodes.INVOKESTATIC);
        assertEquals(((MethodInsnNode)toInclusive).name, "ajc$preClinit");
    }

    @Test
    public void testClinitOnlyPost() {
        final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
                "<clinit>", "()V", null, null);
        m.visitInsn(Opcodes.NOP);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo", "ajc$postClinit", "()V", false);
        m.visitInsn(Opcodes.NOP);
        m.visitInsn(Opcodes.RETURN);

        filter.filter(m, context, this);

        assertEquals(m.instructions.getFirst(), fromInclusive);
        assertEquals(m.instructions.getLast(), toInclusive);
    }

    @Test
    public void testClinitPostAndUserCode() {
        final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
                "<clinit>", "()V", null, null);
        m.visitInsn(Opcodes.NOP);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo", "myCustomMethod", "()V", false);
        m.visitInsn(Opcodes.NOP);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo", "ajc$postClinit", "()V", false);
        m.visitInsn(Opcodes.RETURN);

        filter.filter(m, context, this);

        assertEquals(fromInclusive.getOpcode(), Opcodes.INVOKESTATIC);
        assertEquals(((MethodInsnNode)fromInclusive).name, "ajc$postClinit");
        assertEquals(m.instructions.getLast(), toInclusive);
    }

    @Test
    public void testClinitOnlyPreAndPost() {
        final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
                "<clinit>", "()V", null, null);
        m.visitInsn(Opcodes.NOP);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo", "ajc$preClinit", "()V", false);
        m.visitInsn(Opcodes.NOP);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, "Foo", "ajc$postClinit", "()V", false);
        m.visitInsn(Opcodes.NOP);
        m.visitInsn(Opcodes.RETURN);

        filter.filter(m, context, this);

        assertEquals(m.instructions.getFirst(), fromInclusive);
        assertEquals(m.instructions.getLast(), toInclusive);
    }

    public void ignore(AbstractInsnNode fromInclusive,
                       AbstractInsnNode toInclusive) {
        this.fromInclusive = fromInclusive;
        this.toInclusive = toInclusive;
    }

    public void merge(final AbstractInsnNode i1, final AbstractInsnNode i2) {
        fail();
    }

    private static class TestAttribute extends Attribute {
        TestAttribute(String type) {
            super(type);
        }
    }
}
