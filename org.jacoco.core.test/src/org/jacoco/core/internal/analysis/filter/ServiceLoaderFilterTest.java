/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergey Zhemzhitsky - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ServiceConfigurationError;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class ServiceLoaderFilterTest extends FilterTestBase {

	private File metaInf;
	private File services;
	private File filters;

	private boolean metaInfCreated;
	private boolean servicesCreated;

	private ServiceLoaderFilter filter;

	@Before
	public void setUp() throws Exception {
		final File cpRoot = new File(getClass().getResource("/").toURI());

		metaInf = new File(cpRoot, "META-INF");
		metaInfCreated = metaInf.mkdir();

		services = new File(metaInf, "services");
		servicesCreated = services.mkdirs();

		filters = new File(services, IFilter.class.getName());

		ServiceLoaderFilter1.invoked = false;
		ServiceLoaderFilter2.invoked = false;

		filter = new ServiceLoaderFilter();
	}

	@After
	public void tearDown() {
		if (filters.exists()) {
			Assert.assertTrue(filters.delete());
		}
		if (servicesCreated) {
			Assert.assertTrue(services.delete());
		}
		if (metaInfCreated) {
			Assert.assertTrue(metaInf.delete());
		}
		filter.reset();
	}

	@Test
	public void should_load_filters_by_service_loader() throws Exception {
		writeServiceLines(ServiceLoaderFilter1.class.getName(),
				ServiceLoaderFilter2.class.getName());

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		Assert.assertTrue(ServiceLoaderFilter1.class.getCanonicalName() +
				" should be invoked", ServiceLoaderFilter1.invoked);
		Assert.assertTrue(ServiceLoaderFilter2.class.getCanonicalName() +
				" should be invoked", ServiceLoaderFilter2.invoked);
	}

	@Test
	public void should_not_fail_when_no_service_file_found() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		Assert.assertFalse(ServiceLoaderFilter1.class.getCanonicalName() +
				" should not be invoked", ServiceLoaderFilter1.invoked);
		Assert.assertFalse(ServiceLoaderFilter2.class.getCanonicalName() +
				" should not be invoked", ServiceLoaderFilter2.invoked);
	}

	@Test
	public void should_not_fail_on_empty_service_file() throws Exception {
		writeServiceLines("\n");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		Assert.assertFalse(ServiceLoaderFilter1.class.getCanonicalName() +
				" should not be invoked", ServiceLoaderFilter1.invoked);
		Assert.assertFalse(ServiceLoaderFilter2.class.getCanonicalName() +
				" should not be invoked", ServiceLoaderFilter2.invoked);
	}

	@Test
	public void should_handle_comments_at_start() throws Exception {
		writeServiceLines("# " + ServiceLoaderFilter1.class.getName(),
				"# " + ServiceLoaderFilter2.class.getName());

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		Assert.assertFalse(ServiceLoaderFilter1.class.getCanonicalName() +
				" should not be invoked", ServiceLoaderFilter1.invoked);
		Assert.assertFalse(ServiceLoaderFilter2.class.getCanonicalName() +
				" should not be invoked", ServiceLoaderFilter2.invoked);
	}

	@Test
	public void should_handle_comments_at_eol() throws Exception {
		writeServiceLines(ServiceLoaderFilter1.class.getName() + " # comment",
				ServiceLoaderFilter2.class.getName() + " # comment");

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		Assert.assertTrue(ServiceLoaderFilter1.class.getCanonicalName() +
				" should be invoked", ServiceLoaderFilter1.invoked);
		Assert.assertTrue(ServiceLoaderFilter2.class.getCanonicalName() +
				" should be invoked", ServiceLoaderFilter2.invoked);
	}

	@Test(expected = ServiceConfigurationError.class)
	public void should_fail_on_invalid_first_char_in_name() throws Exception {
		writeServiceLines("1" + ServiceLoaderFilter1.class.getName());

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);
	}

	@Test(expected = ServiceConfigurationError.class)
	public void should_fail_on_invalid_char_in_name() throws Exception {
		writeServiceLines(
				ServiceLoaderFilter1.class.getName().replace('.', ','));

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);
	}

	@Test(expected = ServiceConfigurationError.class)
	public void should_fail_on_class_not_found() throws Exception {
		writeServiceLines(
				ServiceLoaderFilter1.class.getName().replace('1', '0'));

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);
	}

	@Test(expected = ServiceConfigurationError.class)
	public void should_fail_on_unassignable_class() throws Exception {
		writeServiceLines(String.class.getName());

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);
	}

	@Test(expected = ServiceConfigurationError.class)
	public void should_fail_on_no_default_constructor() throws Exception {
		writeServiceLines(ServiceLoaderFilter4.class.getName());

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);
	}

	@Test
	public void should_not_find_services_when_no_file_and_resetting()
			throws Exception {
		writeServiceLines(true, ServiceLoaderFilter1.class.getName(),
				ServiceLoaderFilter2.class.getName());

		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"foo", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		Assert.assertTrue(ServiceLoaderFilter1.class.getCanonicalName() +
				" should be invoked", ServiceLoaderFilter1.invoked);
		Assert.assertTrue(ServiceLoaderFilter2.class.getCanonicalName() +
				" should be invoked", ServiceLoaderFilter2.invoked);

		// reset services and theirs loader state
		ServiceLoaderFilter1.invoked = false;
		ServiceLoaderFilter2.invoked = false;

		writeServiceLines(false, "\n");
		filter.reset();

		filter.filter(m, context, output);

		Assert.assertFalse(ServiceLoaderFilter1.class.getCanonicalName() +
				" should not be invoked", ServiceLoaderFilter1.invoked);
		Assert.assertFalse(ServiceLoaderFilter2.class.getCanonicalName() +
				" should not be invoked", ServiceLoaderFilter2.invoked);
	}

	private void writeServiceLines(final boolean append, final String... lines)
			throws Exception {
		filters = new File(services, IFilter.class.getName());
		final FileOutputStream os = new FileOutputStream(filters, append);
		try {
			for (final String line : lines) {
				os.write(line.getBytes("UTF-8"));
				os.write('\n');
			}
		} finally {
			os.close();
		}
	}

	private void writeServiceLines(final String... lines)
			throws Exception {
		writeServiceLines(true, lines);
	}

	public static class ServiceLoaderFilter1 implements IFilter {
		private static boolean invoked;
		@Override
		public void filter(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			invoked = true;
		}
	}

	public static class ServiceLoaderFilter2 implements IFilter {
		private static boolean invoked;
		@Override
		public void filter(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			invoked = true;
		}
	}

	public static class ServiceLoaderFilter3 implements IFilter {
		@SuppressWarnings("unused")
		private static boolean invoked;
		private ServiceLoaderFilter3() {
		}
		@Override
		public void filter(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			invoked = true;
		}
	}

	public static class ServiceLoaderFilter4 implements IFilter {
		@SuppressWarnings("unused")
		private static boolean invoked;
		public ServiceLoaderFilter4(
				@SuppressWarnings("unused") String ignored) {
		}
		@Override
		public void filter(final MethodNode methodNode,
				final IFilterContext context, final IFilterOutput output) {
			invoked = true;
		}
	}

}
