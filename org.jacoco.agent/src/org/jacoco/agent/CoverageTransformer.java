package org.jacoco.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;

public class CoverageTransformer implements ClassFileTransformer {

	private final Instrumenter instrumenter;

	public CoverageTransformer(IRuntime runtime) {
		this.instrumenter = new Instrumenter(runtime);
	}

	public byte[] transform(ClassLoader loader, String classname,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		if (loader == null) {
			// don't instrument classes of the bootstrap loader
			return null;
		}

		if (loader.getClass().getName().equals(
				"sun.reflect.DelegatingClassLoader")) {
			// don't instrument classes generated for reflection
			return null;
		}

		try {
			return instrumenter.instrument(classfileBuffer);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
}
