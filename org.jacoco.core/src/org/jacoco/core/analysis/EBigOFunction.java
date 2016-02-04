/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis;

/**
 * An immutable object that holds the results of the optional Empirical Big-O
 * analysis for use by general reporting tools.
 */
public class EBigOFunction implements Comparable<EBigOFunction> {
	/**
	 * The function types supported
	 */
	@SuppressWarnings("javadoc")
	public static enum Type {
		Undefined, Logarithmic, Linear, PowerLaw, Exponential;
	}

	/**
	 * Represent an missing or undefined result from empirical Big-O analysis.
	 */
	public static final EBigOFunction UNDEFINED = new EBigOFunction(
			EBigOFunction.Type.Undefined, 0, 0);

	private final EBigOFunction.Type type;
	private final double slope;
	private final double intercept;

	/**
	 * Construct an immutable instance.
	 * 
	 * @param type
	 *            the function type: Logarithmic, Linear, PowerLaw, Exponential,
	 *            or Undefined
	 * @param slope
	 *            the regression slope which becomes the coefficient for
	 *            Logarithmic and Linear, and the power for PowerLaw and
	 *            Exponential.
	 * @param intercept
	 *            the regression intercept which becomes the constant for
	 *            Logarithmic and Linear, and the coefficient for PowerLaw and
	 *            Exponential.
	 */
	public EBigOFunction(final EBigOFunction.Type type, final double slope,
			final double intercept) {
		if (type == null) {
			throw new IllegalArgumentException("type is null");
		}
		this.type = type;
		this.slope = slope;
		this.intercept = intercept;
	}

	/**
	 * Return the function type: Logarithmic, Linear, PowerLaw, Exponential, or
	 * Undefined.
	 * 
	 * @return the function type.
	 */
	public EBigOFunction.Type getType() {
		return type;
	}

	/**
	 * Returns the regression slope which becomes the coefficient for
	 * Logarithmic and Linear, and the power for PowerLaw and Exponential.
	 * 
	 * @return the regression slope.
	 */
	public double getSlope() {
		return slope;
	}

	/**
	 * Returns the regression intercept which becomes the constant for
	 * Logarithmic and Linear, and the coefficient for PowerLaw and Exponential.
	 * 
	 * @return the regression intercept
	 */
	public double getIntercept() {
		return intercept;
	}

	/**
	 * Returns a String representation of the order of magnitude of the function
	 * described by this object. For example: if the function is
	 * <code>10 + 3x</code>, the order of magnitude is <code>n</code>
	 * 
	 * @return the order of magnitude of the function described by this object
	 */
	public String getOrderOfMagnitude() {
		switch (type) {
		case Logarithmic:
			return 0 == slope ? "1" : "log(n)";
		case Linear:
			return 0 == slope ? "1" : "n";
		case PowerLaw:
			if (0 == intercept || 0 == slope) {
				return "1";
			}
			if (1 == slope) {
				return "n";
			}
			return String.format("n^%-5.2f", new Double(slope));
		case Exponential:
			if (0 == intercept || 0 == slope) {
				return "1";
			}
			return "2^n";
		default:
			return "";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(intercept);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(slope);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + type.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final EBigOFunction other = (EBigOFunction) obj;
		if (Double.doubleToLongBits(intercept) != Double
				.doubleToLongBits(other.intercept)) {
			return false;
		}
		if (Double.doubleToLongBits(slope) != Double
				.doubleToLongBits(other.slope)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	public int compareTo(final EBigOFunction o) {
		if (o == null) {
			return -1;
		}
		final int compare = type.compareTo(o.type);
		if (compare != 0) {
			return compare;
		}
		switch (type) {
		case Exponential:
		case PowerLaw:
			return (int) (slope - o.slope);
		}
		return 0;
	}

	@Override
	public String toString() {
		return "EBigOFunction [type=" + type + ", slope=" + slope
				+ ", intercept=" + intercept + "]";
	}
}