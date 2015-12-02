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
package de.esoco.lib.expression;

/********************************************************************
 * An extended function interface for invertible functions. Invoking the method
 * {@link #invert(Object)} with a particular output value of a previous
 * evaluation of the same function will return the original input value of the
 * evaluation. It is the responsibility of the implementation to provide
 * consistent behavior, so that {@code invert(evaluate(I).equals(I) == true}.
 *
 * <p>Whether the result of the inversion is an object that is identical to the
 * original input value or just equals is not defined by the contract of this
 * interface. It depends on the implementation and must be documented
 * accordingly.</p>
 *
 * @author eso
 */
public interface InvertibleFunction<I, O> extends Function<I, O>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Inverts the evaluation of this function. The returned object must be at
	 * least equal to an input value that would yield the argument value as the
	 * output of the {@link Function#evaluate(Object)} method so that the
	 * expression {@code invert(evaluate(I)).equals(I) == true} is valid. It is
	 * not required but possible to provide an identity relation so that even
	 * the expression {@code invert(evaluate(I)) == I} is valid.
	 *
	 * @param  rValue The evaluation output value to invert
	 *
	 * @return The inverted output value
	 */
	public I invert(O rValue);
}
