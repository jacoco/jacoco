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
import org.jacoco.core.test.TargetLoader;
import org.junit.Test;

/**
 * Unit test for {@link Companions}.
 */
public class CompanionsTest {

	@Test
	public void test() throws IOException, ClassNotFoundException {
		final Loader classLoader = new Loader();
		final CodeSource codeSource = new CodeSource(null,
				new Certificate[] { new CertificateMock() });
		final ProtectionDomain protectionDomain = new ProtectionDomain(
				codeSource, null);
		classLoader.define(Target.class, protectionDomain);

		final RuntimeData runtimeData = new RuntimeData();
		final Companions companions = new Companions(runtimeData);

		assertFalse(classLoader.hasClass(Companions.COMPANION_NAME));
		companions.instrument(classLoader, Target.class.getName(), TargetLoader.getClassDataAsBytes(Target.class));
		assertTrue(classLoader.hasClass(Companions.COMPANION_NAME));
	}

	private static class Loader extends ClassLoader {
		public void define(final Class<?> source,
				final ProtectionDomain protectionDomain) throws IOException {
			final byte[] bytes = TargetLoader.getClassDataAsBytes(source);
			defineClass(source.getName(), bytes, 0, bytes.length,
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

	private static class Target {
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
