/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.badge;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.internal.badge.SVGBadge;
import org.jacoco.report.internal.badge.TotalVisitor;

/**
 * Report formatter that will create a single SVG badge with the total coverage
 * percentage.
 */
public class BadgeFormatter {

	/**
	 * Creates a new visitor to write a badge to the given stream.
	 * 
	 * @param output
	 *            output stream to write the badge to
	 * @return visitor to emit the report data to
	 */
	public IReportVisitor createVisitor(final OutputStream output) {
		return new BadgeVisitor() {
			@Override
			protected void handleEnd() throws IOException {
				final double coverage = total.getInstructionCounter()
						.getCoveredRatio();
				final String percentage = ((int) (100 * coverage)) + "%";
				new SVGBadge(60, "coverage", 40, percentage).render(output);
			}
		};
	}

	private static class BadgeVisitor extends TotalVisitor
			implements IReportVisitor {

		public BadgeVisitor() {
			super("total");
		}

		public void visitInfo(final List<SessionInfo> sessionInfos,
				final Collection<ExecutionData> executionData)
				throws IOException {
			// nothing to do here
		}

	}

}
