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

/**
 * Filters synthetic methods created by the AspectJ-Compiler (ajc)
 */
public class AspectjFilter implements IFilter {

    private static final String AJ_SYNTHETIC_ATTRIBUTE = "org.aspectj.weaver.AjSynthetic";

    public void filter(MethodNode methodNode, IFilterContext context, IFilterOutput output) {

        if (isAjSynthetic(methodNode)) {
            output.ignore(methodNode.instructions.getFirst(), methodNode.instructions.getLast());
            return;
        }

        if (methodNode.name.equals("<clinit>")) {
            checkStaticInitializer(methodNode, output);
        }

    }

    private void checkStaticInitializer(MethodNode methodNode, IFilterOutput output) {
        AbstractInsnNode preClinitNode = null;

        for (AbstractInsnNode node = methodNode.instructions.getFirst();
             node != null;
             node = node.getNext()) {

            if (node.getOpcode() == Opcodes.INVOKESTATIC) {
                String name = ((MethodInsnNode) node).name;

                if (name.equals("ajc$preClinit")) {
                    if (isEffectivelyLast(node.getNext())) {
                        output.ignore(methodNode.instructions.getFirst(), methodNode.instructions.getLast());
                        return;
                    } else {
                        preClinitNode = node;
                        output.ignore(methodNode.instructions.getFirst(), node);
                    }
                }

                if (name.equals("ajc$postClinit")) {
                    if (isEffectivelyFirst(node.getPrevious()) || (preClinitNode != null && node == getNextRealOp(preClinitNode)) ) {
                        output.ignore(methodNode.instructions.getFirst(), methodNode.instructions.getLast());
                        return;
                    } else {
                        output.ignore(node, methodNode.instructions.getLast());
                    }
                }
            }
        }
    }

    private AbstractInsnNode getNextRealOp(AbstractInsnNode preClinitNode) {
        AbstractInsnNode next = preClinitNode.getNext();

        if (next != null && next.getOpcode() <= 0) {
            return getNextRealOp(next);
        } else {
            return next;
        }
    }

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
        if (insnNode.getOpcode() == Opcodes.RETURN) {
            return true;
        } else if (insnNode.getOpcode() <= 0) {
            return isEffectivelyLast(insnNode.getNext());
        } else {
            return false;
        }
    }
}
