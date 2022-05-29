package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.tree.MethodNode;

/**
 * Filters toString.
 */
public final class toStringFilter implements IFilter {

    public void filter(final MethodNode methodNode,
                       final IFilterContext context, final IFilterOutput output) {

        if (istoString(methodNode, context)) {
            output.ignore(methodNode.instructions.getFirst(),
                    methodNode.instructions.getLast());
        }
    }

    private boolean istoString(final MethodNode methodNode,final IFilterContext context) {
        return methodNode.name.equals("toString");
    }

}