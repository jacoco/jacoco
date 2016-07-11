/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.CodeSource;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.ProtectionDomain;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

import org.jacoco.core.runtime.RuntimeData;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit test for {@link Companions}.
 */
public class CompanionsTest {

	private final Loader classLoader = new Loader();
	private final ProtectionDomain protectionDomain = new ProtectionDomain(
			new CodeSource(null, new Certificate[] { new CertificateMock() }),
			null);
	private final RuntimeData runtimeData = new RuntimeData();
	private final Companions companions = new Companions(runtimeData);

	@Test
	public void test() throws IOException, ClassNotFoundException {
		assertFalse(classLoader.hasClass(Companions.COMPANION_NAME + "0"));
		for (int i = 0; i < Companions.FIELDS_PER_CLASS; i++) {
			define(i);
		}
		assertTrue(classLoader.hasClass(Companions.COMPANION_NAME + "0"));
		assertFalse(classLoader.hasClass(Companions.COMPANION_NAME + "1"));
		define(Companions.FIELDS_PER_CLASS);
		assertTrue(classLoader.hasClass(Companions.COMPANION_NAME + "1"));
	}

	private void define(final int id) {
		final String className = "test/Target" + id;
		final byte[] bytes = companions.instrument(classLoader, className,
				create(className));
		classLoader.define(className, bytes, protectionDomain);
	}

	private static class Loader extends ClassLoader {
		public void define(final String className, final byte[] bytes,
				final ProtectionDomain protectionDomain) {
			defineClass(className.replace('/', '.'), bytes, 0, bytes.length,
					protectionDomain);
		}

		/**
		 * @return <code>true</code> if class with given name has been loaded by
		 *         this class loader
		 */
		public boolean hasClass(final String name) {
			return findLoadedClass(name) != null;
		}
	}

	private static byte[] create(final String className) {
		final ClassWriter cw = new ClassWriter(0);

		cw.visit(Opcodes.V1_1,
				Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
				className, null, "java/lang/Object", null);
		MethodVisitor mv = cw.visitMethod(0, "m", "()V", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.RETURN);
		mv.visitEnd();
		cw.visitEnd();

		return cw.toByteArray();
	}

	private static class CertificateMock extends Certificate {
		protected CertificateMock() {
			super("Mock");
		}

		@Override
		public byte[] getEncoded() throws CertificateEncodingException {
			return new byte[0];
		}

		@Override
		public void verify(final PublicKey key) throws CertificateException,
				NoSuchAlgorithmException, InvalidKeyException,
				NoSuchProviderException, SignatureException {
		}

		@Override
		public void verify(final PublicKey key, final String sigProvider)
				throws CertificateException, NoSuchAlgorithmException,
				InvalidKeyException, NoSuchProviderException,
				SignatureException {
		}

		@Override
		public String toString() {
			return null;
		}

		@Override
		public PublicKey getPublicKey() {
			return null;
		}
	}

}
