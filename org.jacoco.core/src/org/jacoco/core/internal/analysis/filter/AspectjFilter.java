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

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Filters synthetic methods created by the AspectJ-Compiler (ajc)
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

        VarInsnNode end = new JoinPointCreation().match(methodNode);
        if (end != null) {
            output.ignore(methodNode.instructions.getFirst(), end);
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
        MethodInsnNode preClinitNode = null;
        MethodInsnNode postClinitNode = null;

        for (AbstractInsnNode node = methodNode.instructions.getFirst();
             node != null;
             node = node.getNext()) {

            if (node.getOpcode() != Opcodes.INVOKESTATIC) {
                continue;
            }
            String name = ((MethodInsnNode) node).name;

            if (name.equals("ajc$preClinit")) {
                preClinitNode = (MethodInsnNode) node;
            }

            if (name.equals("ajc$postClinit")) {
                postClinitNode = (MethodInsnNode) node;
            }
        }

        if (preClinitNode != null) {
            ignorePreClinitCall(methodNode, output, preClinitNode);
        }

        if (postClinitNode != null) {
            ignorePostClinitCall(methodNode, output, postClinitNode);
        }

        if (preClinitNode != null && postClinitNode != null && getNextRealOp(preClinitNode) == postClinitNode) {
            output.ignore(preClinitNode, postClinitNode);
        }
    }

    private void ignorePreClinitCall(MethodNode methodNode, IFilterOutput output, MethodInsnNode preClinitNode) {
        AbstractInsnNode from = preClinitNode;
        AbstractInsnNode to = preClinitNode;

        if (from != methodNode.instructions.getFirst() && isEffectivelyFirst(preClinitNode.getPrevious())) {
            from = methodNode.instructions.getFirst();
        }
        if (isEffectivelyLast(preClinitNode.getNext())) {
            to = methodNode.instructions.getLast();
        }

        output.ignore(from, to);
    }

    private void ignorePostClinitCall(MethodNode methodNode, IFilterOutput output, MethodInsnNode postClinitNode) {
        AbstractInsnNode from = postClinitNode;
        AbstractInsnNode to = postClinitNode;

        if (postClinitNode.getNext().getOpcode() == Opcodes.GOTO) {
            JumpInsnNode next = (JumpInsnNode) postClinitNode.getNext();
            to = next.label;
        }

        if (isEffectivelyFirst(postClinitNode.getPrevious())) {
            from = methodNode.instructions.getFirst();
        }
        if (isEffectivelyLast(to.getNext())) {
            to = methodNode.instructions.getLast();
        }

        output.ignore(from, to);
    }

    private AbstractInsnNode getNextRealOp(AbstractInsnNode preClinitNode) {
        AbstractInsnNode next = preClinitNode.getNext();

        if (next != null && next.getOpcode() <= 0) {
            return getNextRealOp(next);
        } else {
            return next;
        }
    }

    /**
     * Checks if the given method has the {@literal org.aspectj.weaver.AjSynthetic} attribute.
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

    private boolean isEffectivelyFirst(AbstractInsnNode insnNode) {
        if (insnNode.getPrevious() == null) {
            return true;
        } else if (insnNode.getOpcode() <= 0) {
            return isEffectivelyFirst(insnNode.getPrevious());
        } else {
            return false;
        }
    }

    private boolean isEffectivelyLast(AbstractInsnNode insnNode) {
        if (insnNode.getNext() == null) {
            return true;
        } else if (insnNode.getOpcode() <= 0) {
            return isEffectivelyLast(insnNode.getNext());
        } else {
            return false;
        }
    }

    class JoinPointCreation extends AbstractMatcher {
        VarInsnNode match(MethodNode methodNode) {
            cursor = methodNode.instructions.getFirst();
            skipNonOpcodes();

            // 1. LOAD/STORE pair for each method parameter
            List<Integer> params = new LinkedList<Integer>();

            while (cursor != null && cursor.getOpcode() >= Opcodes.ILOAD && cursor.getOpcode() <= Opcodes.ALOAD) {
                switch (cursor.getOpcode()) {
                    case Opcodes.ILOAD:
                        nextIs(Opcodes.ISTORE);
                        params.add(0);
                        next();
                        break;
                    case Opcodes.LLOAD:
                        nextIs(Opcodes.LSTORE);
                        params.add(1);
                        next();
                        break;
                    case Opcodes.FLOAD:
                        nextIs(Opcodes.FSTORE);
                        params.add(2);
                        next();
                        break;
                    case Opcodes.DLOAD:
                        nextIs(Opcodes.DSTORE);
                        params.add(3);
                        next();
                        break;
                    case Opcodes.ALOAD:
                        nextIs(Opcodes.ASTORE);
                        params.add(4);
                        next();
                        break;
                    default:
                        return null;
                }
            }

            if (cursor == null) {
                return null;
            }

            // 2. Get Static for the joinpoint (first param for the makeJP call)
            if (cursor.getOpcode() == Opcodes.GETSTATIC) {
                FieldInsnNode getStatic = (FieldInsnNode) cursor;
                if (!getStatic.name.startsWith("ajc$") || !getStatic.desc.equals("Lorg/aspectj/lang/JoinPoint$StaticPart;")) {
                    return null;
                }
            }
            // 3. Second param for the makeJP call
            nextIs(Opcodes.ALOAD);
            // 4. Thrid param for the makeJP call
            nextIs(Opcodes.ALOAD);

            // 5. Optional fourth and fifth params
            boolean arrayCreation = params.size() >= 3;
            if (arrayCreation) {
                next(); // Array size is loaded (ICONST_x or SIPUSH)
                nextIs(Opcodes.ANEWARRAY);
                nextIs(Opcodes.ASTORE);
            }

            for (Integer param : params) {
                if (arrayCreation) {
                    nextIs(Opcodes.ALOAD); //Load array
                    next(); //Load array index
                }

                nextIs(Opcodes.ILOAD + param);

                if (param != 4) {
                    nextIs(Opcodes.INVOKESTATIC);
                    if (cursor == null || !((MethodInsnNode) cursor).owner.equals("org/aspectj/runtime/internal/Conversions")) {
                        return null;
                    }
                }
                if (arrayCreation) {
                    nextIs(Opcodes.AASTORE); // Store in Array
                }
            }

            if (arrayCreation) {
                nextIs(Opcodes.ALOAD);
            }
            nextIsInvokeStatic("org/aspectj/runtime/reflect/Factory", "makeJP");
            nextIs(Opcodes.ASTORE);
            return (VarInsnNode) cursor;
        }
    }
}
