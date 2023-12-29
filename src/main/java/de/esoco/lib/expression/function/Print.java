//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'ObjectRelations' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.expression.function;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A function that prints text to a {@link PrintWriter} in a user-defined
 * format.
 *
 * @author eso
 */
public class Print<I> extends AbstractBinaryFunction<I, PrintWriter, I> {

	private final String format;

	private final boolean withLinefeed;

	/**
	 * Creates a new instance that prints input values to System.out with a
	 * trailing linefeed.
	 *
	 * @param format The format string to be applied to input values with
	 *               {@link String#format(String, Object...)}
	 */
	public Print(String format) {
		this(System.out, format, true);
	}

	/**
	 * Creates a new instance that prints input values to a certain stream.
	 *
	 * @param out          The stream to print to
	 * @param format       The format string to be applied to input values with
	 *                     {@link String#format(String, Object...)}
	 * @param withLinefeed TRUE to print a linefeed after the text on each
	 *                     invocation
	 */
	public Print(OutputStream out, String format, boolean withLinefeed) {
		this(new PrintWriter(out, true), format, withLinefeed);
	}

	/**
	 * Creates a new instance that prints input values to a certain
	 * PrintWriter.
	 *
	 * @param writer       The writer to print to
	 * @param format       The format string to be applied to input values with
	 *                     {@link String#format(String, Object...)}
	 * @param withLinefeed TRUE to print a linefeed after the text on each
	 *                     invocation
	 */
	public Print(PrintWriter writer, String format, boolean withLinefeed) {
		super(writer, withLinefeed ? "println" : "print");

		this.format = format;
		this.withLinefeed = withLinefeed;
	}

	/**
	 * Prints the result of the input object's toString() method (or "null" for
	 * NULL objects) to this instance's PrintWriter. At the end the writer's
	 * flush() method will be invoked.
	 *
	 * @param input  The input value to print
	 * @param writer The print writer to print to
	 * @return The unchanged input object to allow function chaining
	 */
	@Override
	public I evaluate(I input, PrintWriter writer) {
		writer.print(String.format(format, input));

		if (withLinefeed) {
			writer.println();
		}

		writer.flush();

		return input;
	}

	/**
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> other) {
		Print<?> otherPrint = (Print<?>) other;

		return withLinefeed == otherPrint.withLinefeed &&
			format.equals(otherPrint.format) && super.paramsEqual(other);
	}

	/**
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode() {
		return ((super.paramsHashCode() * 37) + format.hashCode()) * 37 +
			(withLinefeed ? 1 : 0);
	}
}
