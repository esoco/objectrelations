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
package de.esoco.lib.datatype;

/********************************************************************
 * A {@link Tuple} subclass that holds a pair of two values and has generic
 * types for it's values.
 *
 * @author eso
 */
public class Pair<F, S> extends Tuple
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new Pair instance.
	 *
	 * @param rFirst  The first object
	 * @param rSecond The second object
	 */
	public Pair(F rFirst, S rSecond)
	{
		super(rFirst, rSecond);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a new {@link Pair}. This factory method provides a semantic
	 * alternative to the constructor or the {@link #t(Object, Object)} method.
	 *
	 * @param  rFirst  The first value
	 * @param  rSecond The second value
	 *
	 * @return A new pair instance
	 */
	public static <F, S> Pair<F, S> of(F rFirst, S rSecond)
	{
		return new Pair<>(rFirst, rSecond);
	}

	/***************************************
	 * Creates a new {@link Pair}. This factory method is intended to be used
	 * with static imports to provide a short syntax for defining dual-value
	 * tuples, especially in varargs lists.
	 *
	 * @param  rFirst  The first value
	 * @param  rSecond The second value
	 *
	 * @return A new pair instance
	 */
	public static <F, S> Pair<F, S> t(F rFirst, S rSecond)
	{
		return Pair.of(rFirst, rSecond);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the first value.
	 *
	 * @return The first value
	 */
	@SuppressWarnings("unchecked")
	public final F first()
	{
		return (F) get(0);
	}

	/***************************************
	 * Returns the second value.
	 *
	 * @return The second value
	 */
	@SuppressWarnings("unchecked")
	public final S second()
	{
		return (S) get(1);
	}
}
