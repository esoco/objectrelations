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

import de.esoco.lib.expression.InvertibleFunction;


/********************************************************************
 * An invertible function implementation that is the inversion of another
 * invertible function. That means the input parameter of that function becomes
 * the output parameter of this and vice versa.
 *
 * @author eso
 */
public class Invert<I, O> extends AbstractInvertibleFunction<I, O>
{
	//~ Instance fields --------------------------------------------------------

	private InvertibleFunction<O, I> rInvertedFunction;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rInvertedFunction The inverted function
	 */
	public Invert(InvertibleFunction<O, I> rInvertedFunction)
	{
		super(Invert.class.getSimpleName() + "[" +
			  rInvertedFunction.getToken() + "]");

		this.rInvertedFunction = rInvertedFunction;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see AbstractInvertibleFunction#evaluate(Object)
	 */
	@Override
	public O evaluate(I rInput)
	{
		return rInvertedFunction.invert(rInput);
	}

	/***************************************
	 * @see AbstractInvertibleFunction#invert(Object)
	 */
	@Override
	public I invert(O rValue)
	{
		return rInvertedFunction.evaluate(rValue);
	}
}
