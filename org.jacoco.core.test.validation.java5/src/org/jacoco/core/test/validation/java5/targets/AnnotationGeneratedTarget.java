/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotationGeneratedTarget {

	private static class RetentionPolicyRuntime {

		@Retention(RetentionPolicy.RUNTIME)
		@interface Generated {
		}

		@RetentionPolicyRuntime.Generated
		static void annotatedMethod() {
			nop(); // assertEmpty()
		}

		@RetentionPolicyRuntime.Generated
		static class AnnotatedClass {
			AnnotatedClass() {
				nop(); // assertEmpty()
			}
		}

	}

	private static class RetentionPolicyClass {

		@Retention(RetentionPolicy.CLASS)
		@interface Generated {
		}

		@RetentionPolicyClass.Generated
		static void annotatedMethod() {
			nop(); // assertEmpty()
		}

		@RetentionPolicyClass.Generated
		static class AnnotatedClass {
			AnnotatedClass() {
				nop(); // assertEmpty()
			}
		}

	}

	private static class RetentionPolicySource {

		@Retention(RetentionPolicy.SOURCE)
		@interface Generated {
		}

		@RetentionPolicySource.Generated
		static void annotatedMethod() {
			nop(); // assertFullyCovered()
		}

		@RetentionPolicySource.Generated
		static class AnnotatedClass {
			AnnotatedClass() {
				nop(); // assertFullyCovered()
			}
		}

	}

	public static void main(String[] args) {
		RetentionPolicyRuntime.annotatedMethod();
		new RetentionPolicyRuntime.AnnotatedClass();

		RetentionPolicyClass.annotatedMethod();
		new RetentionPolicyClass.AnnotatedClass();

		RetentionPolicySource.annotatedMethod();
		new RetentionPolicySource.AnnotatedClass();
	}

}
