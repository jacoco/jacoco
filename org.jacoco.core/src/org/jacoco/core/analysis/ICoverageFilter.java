package org.jacoco.core.analysis;

import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Coverage Filter base class
 */
public interface ICoverageFilter {

	/**
	 * @param className
	 * @return True if className should be included
	 */
	public boolean includeClass(String className);

	/**
	 * @param delegate
	 * @return A {@link MethodVisitor} that wraps the provided delegate or
	 *         simply returns the provided {@link MethodProbesVisitor} instance
	 *         if no extra processing is required.
	 */
	public MethodProbesVisitor getVisitor(MethodProbesVisitor delegate);

	/**
	 * @return True if coverage is enabled
	 */
	public boolean enabled();
}
