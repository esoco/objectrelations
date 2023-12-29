//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.lib.expression;

import de.esoco.lib.expression.function.AbstractBinaryFunction;
import de.esoco.lib.expression.function.AbstractFunction;

/**
 * Contains factory methods for mathematical functions.
 *
 * @author eso
 */
public class MathFunctions {

	private static final Function<String, Integer> PARSE_INTEGER =
		new AbstractFunction<String, Integer>("ParseInteger") {
			@Override
			public Integer evaluate(String value) {
				return Integer.valueOf(value);
			}
		};

	/**
	 * Private, only static use.
	 */
	private MathFunctions() {
	}

	/**
	 * Returns a binary function that will add an integer value to the input
	 * value.
	 *
	 * @param rightValue The integer value to add
	 * @return A new binary function instance
	 */
	@SuppressWarnings("boxing")
	public static BinaryFunction<Integer, Integer, Integer> add(
		final int rightValue) {
		return new AbstractBinaryFunction<Integer, Integer, Integer>(rightValue,
			"Add") {
			@Override
			public Integer evaluate(Integer leftValue, Integer rightValue) {
				return leftValue.intValue() + rightValue.intValue();
			}
		};
	}

	/**
	 * Returns a binary function that will divide the input value by an integer
	 * value.
	 *
	 * @param rightValue The integer value to divide by
	 * @return A new binary function instance
	 */
	@SuppressWarnings("boxing")
	public static BinaryFunction<Integer, Integer, Integer> divide(
		final int rightValue) {
		return new AbstractBinaryFunction<Integer, Integer, Integer>(rightValue,
			"Divide") {
			@Override
			public Integer evaluate(Integer leftValue, Integer rightValue) {
				return leftValue.intValue() / rightValue.intValue();
			}
		};
	}

	/**
	 * Returns a binary function that returns the remainder of dividing the
	 * input value by an integer value.
	 *
	 * @param rightValue The integer value to divide by
	 * @return A new binary function instance
	 */
	@SuppressWarnings("boxing")
	public static BinaryFunction<Integer, Integer, Integer> modulo(
		final int rightValue) {
		return new AbstractBinaryFunction<Integer, Integer, Integer>(rightValue,
			"Modulo") {
			@Override
			public Integer evaluate(Integer leftValue, Integer rightValue) {
				return leftValue.intValue() % rightValue.intValue();
			}
		};
	}

	/**
	 * Returns a binary function that will multiply the input value with an
	 * integer value.
	 *
	 * @param rightValue The integer value to multiply with
	 * @return A new binary function instance
	 */
	@SuppressWarnings("boxing")
	public static BinaryFunction<Integer, Integer, Integer> multiply(
		final int rightValue) {
		return new AbstractBinaryFunction<Integer, Integer, Integer>(rightValue,
			"Multiply") {
			@Override
			public Integer evaluate(Integer leftValue, Integer rightValue) {
				return leftValue.intValue() * rightValue.intValue();
			}
		};
	}

	/**
	 * Returns a function constant that invokes
	 * {@link Integer#valueOf(String)}.
	 *
	 * @return The function constant
	 */
	public static Function<String, Integer> parseInteger() {
		return PARSE_INTEGER;
	}

	/**
	 * Returns a binary function that will subtract an integer value from the
	 * input value.
	 *
	 * @param rightValue The integer value to subtract
	 * @return A new binary function instance
	 */
	@SuppressWarnings("boxing")
	public static BinaryFunction<Integer, Integer, Integer> subtract(
		final int rightValue) {
		return new AbstractBinaryFunction<Integer, Integer, Integer>(rightValue,
			"Subtract") {
			@Override
			public Integer evaluate(Integer leftValue, Integer rightValue) {
				return leftValue.intValue() - rightValue.intValue();
			}
		};
	}
}
