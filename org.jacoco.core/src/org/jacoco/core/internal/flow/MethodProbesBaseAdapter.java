package org.jacoco.core.internal.flow;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Base adapter class which exposes a {@link #visitProbe()} method
 */
public abstract class MethodProbesBaseAdapter extends MethodVisitor {

	/**
	 * New visitor instance that delegates to the given visitor.
	 * 
	 * @param mv
	 *            optional next visitor in chain
	 */
	public MethodProbesBaseAdapter(final MethodVisitor mv) {
		super(Opcodes.ASM4, mv);
	}

	/**
	 * Generate a new probe
	 */
	public abstract void visitProbe();
}
