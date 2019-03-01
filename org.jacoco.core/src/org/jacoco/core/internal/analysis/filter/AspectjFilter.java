/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.regex.Pattern;

/**
 * Filters synthetic methods created by the AspectJ-Compiler (ajc)
 * <p>
 * Every class containing an advice, will get the synthetic methods {@code aspectOf(..)},
 * {@code hasAspect(..)}. If the advice is a singleton, the class will also get the
 * {@code ajc$postClinit()} method which will be called from the static initializer
 * ({@code <clinit>()}).
 * This filter ignores these 3 method and the call.
 * <p>
 * Every class containing advised methods, where the advice takes a JoinPoint as parameter,
 * will contain the {@code ajc$preClinit()} method which will be called from the static
 * initializer ({@code <clinit>()}).
 * This filter ignores this method, and its call.
 * <p>
 * If the static initializer ({@code <clinit>()}) only contains the calls to
 * {@code ajc$preClinit()} and/or {@code ajc$postClinit()}, its completely ignored.
 * <p>
 * For every JoinPoint which is advised by an around advice, an {@code $AjcClosure}-class
 * is generated (unless the around advice is inlined).
 * This filter ignored all methods ins such classes.
 */
public class AspectjFilter implements IFilter {

    private static final String AJ_SYNTHETIC_ATTRIBUTE = "org.aspectj.weaver.AjSynthetic";
    private static final Pattern AJC_CLOSURE_PATTERN = Pattern.compile(".*\\$AjcClosure\\d+");

    public void filter(MethodNode methodNode, IFilterContext context, IFilterOutput output) {

        if (isAroundClosureClass(context)) {
            output.ignore(methodNode.instructions.getFirst(), methodNode.instructions.getLast());
            return;
        }

        if (isAjSynthetic(methodNode)) {
            output.ignore(methodNode.instructions.getFirst(), methodNode.instructions.getLast());
            return;
        }

        if (methodNode.name.equals("<clinit>")) {
            checkStaticInitializer(methodNode, output);
        }
    }

    private boolean isAroundClosureClass(IFilterContext context) {
        return context.getSuperClassName().equals("org/aspectj/runtime/internal/AroundClosure")
                && AJC_CLOSURE_PATTERN.matcher(context.getClassName()).matches();
    }

    /**
     * This method finds calls to ajc$preClinit() and ajc$postClinit() and ignores them.
     *
     * @param methodNode The {@literal <clinit>()}-Method
     * @param output
     */
    private void checkStaticInitializer(MethodNode methodNode, IFilterOutput output) {
        PreClinitMatcher preClinitMatcher = new PreClinitMatcher(methodNode);

        AbstractInsnNode match = preClinitMatcher.match();

        if (match != null) {
            output.ignore(methodNode.instructions.getFirst(), match);
        }

        for (TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
            PostClinitMatcher postClinitMatcher = new PostClinitMatcher(tryCatchBlock.start);

            AbstractInsnNode end = postClinitMatcher.match();

            if (end != null) {
                output.ignore(tryCatchBlock.start, end);
            }
        }

    }

    /**
     * Checks if the given method has the {@code org.aspectj.weaver.AjSynthetic} attribute.
     *
     * @return true, if the attribute is present.
     */
    private boolean isAjSynthetic(MethodNode methodNode) {
        if (methodNode.attrs != null) {
            for (Attribute attr : methodNode.attrs) {
                if (attr.type.equals(AJ_SYNTHETIC_ATTRIBUTE)) {
                    return true;
                }
            }
        }
        return false;
    }

    static class PreClinitMatcher extends AbstractMatcher {
        private MethodNode methodNode;

        PreClinitMatcher(MethodNode methodNode) {
            this.methodNode = methodNode;
        }

        public AbstractInsnNode match() {
            cursor = methodNode.instructions.getFirst();
            nextIs(Opcodes.INVOKESTATIC);
            if (cursor == null || !((MethodInsnNode) cursor).name.equals("ajc$preClinit")) {
                cursor = null;
                return null;
            }
            skipNonOpcodes();
            AbstractInsnNode end = cursor;

            nextIs(Opcodes.NOP);
            if (cursor != null) {
                return cursor;
            } else {
                cursor = end;
            }

            nextIs(Opcodes.RETURN);
            if (cursor != null) {
                return cursor;
            } else {
                return end;
            }
        }
    }


    static class PostClinitMatcher extends AbstractMatcher {

        private AbstractInsnNode start;

        PostClinitMatcher(AbstractInsnNode start) {
            this.start = start;
        }

        public AbstractInsnNode match() {
            cursor = start;
            nextIs(Opcodes.INVOKESTATIC);
            if (!((MethodInsnNode) cursor).name.equals("ajc$postClinit")) {
                cursor = null;
                return null;
            }
            nextIs(Opcodes.GOTO);
            LabelNode jumpTarget = ((JumpInsnNode) cursor).label;
            nextIs(Opcodes.ASTORE);
            nextIs(Opcodes.ALOAD);
            nextIs(Opcodes.PUTSTATIC);
            if (!((FieldInsnNode) cursor).name.startsWith("ajc$")) {
                cursor = null;
                return null;
            }
            if (cursor.getNext() != jumpTarget) {
                return cursor;
            } else {
                cursor = jumpTarget;
            }

            nextIs(Opcodes.RETURN);
            if (cursor == null) {
                return jumpTarget;
            } else {
                return cursor;
            }
        }
    }
}
