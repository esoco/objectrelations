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


/********************************************************************
 * A function that prints text to a {@link PrintWriter} in a user-defined
 * format.
 *
 * @author eso
 */
public class Print<I> extends AbstractBinaryFunction<I, PrintWriter, I>
{
	//~ Instance fields --------------------------------------------------------

	private final String  sFormat;
	private final boolean bWithLinefeed;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that prints input values to System.out with a
	 * trailing linefeed.
	 *
	 * @param sFormat The format string to be applied to input values with
	 *                {@link String#format(String, Object...)}
	 */
	public Print(String sFormat)
	{
		this(System.out, sFormat, true);
	}

	/***************************************
	 * Creates a new instance that prints input values to a certain stream.
	 *
	 * @param rOut          The stream to print to
	 * @param sFormat       The format string to be applied to input values with
	 *                      {@link String#format(String, Object...)}
	 * @param bWithLinefeed TRUE to print a linefeed after the text on each
	 *                      invocation
	 */
	public Print(OutputStream rOut, String sFormat, boolean bWithLinefeed)
	{
		this(new PrintWriter(rOut, true), sFormat, bWithLinefeed);
	}

	/***************************************
	 * Creates a new instance that prints input values to a certain PrintWriter.
	 *
	 * @param rWriter       The writer to print to
	 * @param sFormat       The format string to be applied to input values with
	 *                      {@link String#format(String, Object...)}
	 * @param bWithLinefeed TRUE to print a linefeed after the text on each
	 *                      invocation
	 */
	public Print(PrintWriter rWriter, String sFormat, boolean bWithLinefeed)
	{
		super(rWriter, bWithLinefeed ? "println" : "print");

		this.sFormat	   = sFormat;
		this.bWithLinefeed = bWithLinefeed;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Prints the result of the input object's toString() method (or "null" for
	 * NULL objects) to this instance's PrintWriter. At the end the writer's
	 * flush() method will be invoked.
	 *
	 * @param  rInput  The input value to print
	 * @param  rWriter The print writer to print to
	 *
	 * @return The unchanged input object to allow function chaining
	 */
	@Override
	public I evaluate(I rInput, PrintWriter rWriter)
	{
		rWriter.print(String.format(sFormat, rInput));

		if (bWithLinefeed)
		{
			rWriter.println();
		}

		rWriter.flush();

		return rInput;
	}

	/***************************************
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		Print<?> rOtherPrint = (Print<?>) rOther;

		return bWithLinefeed == rOtherPrint.bWithLinefeed &&
			   sFormat.equals(rOtherPrint.sFormat) && super.paramsEqual(rOther);
	}

	/***************************************
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		return ((super.paramsHashCode() * 37) + sFormat.hashCode()) * 37 +
			   (bWithLinefeed ? 1 : 0);
	}
}
