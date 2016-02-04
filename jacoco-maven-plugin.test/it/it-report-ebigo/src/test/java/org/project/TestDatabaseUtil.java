/*******************************************************************************
 *  Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.project;

import java.io.IOException;

import org.jacoco.ebigo.tools.EBigOWorkloadMarker;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDatabaseUtil {
	/** The connection to the remote Jacoco Agent */
	private static EBigOWorkloadMarker marker;

	@BeforeClass
	public static void setupRun() throws Exception {
		marker = EBigOWorkloadMarker.getInstance();
	}

	public void doStuff(int iterations) throws IOException {
		marker.beginWorkload();

		// Do workload
		for (int i = 0; i < iterations; i++) {
			new DatabaseUtil().doStuff();
		}
		
		marker.endWorkload(iterations);
	}

	@Test
	public void testSample() throws Exception {
		for(int sampleNo = 1; sampleNo <= 5; sampleNo++) {
			doStuff(sampleNo);
		}
	}
}