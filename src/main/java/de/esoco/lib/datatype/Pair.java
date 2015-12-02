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
package de.esoco.lib.datatype;

/********************************************************************
 * Simple class that holds a pair of two values. Pairs are immutable, the
 * assigned values cannot be changed after creation.
 *
 * @author eso
 */
public class Pair<F, S>
{
	//~ Instance fields --------------------------------------------------------

	private final F rFirst;
	private final S rSecond;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new Pair instance.
	 *
	 * @param rFirst  The first object
	 * @param rSecond The second object
	 */
	public Pair(F rFirst, S rSecond)
	{
		this.rFirst  = rFirst;
		this.rSecond = rSecond;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object rObj)
	{
		if (rObj == this)
		{
			return true;
		}

		if (rObj instanceof Pair<?, ?>)
		{
			Pair<?, ?> rOther = (Pair<?, ?>) rObj;

			return (rFirst.equals(rOther.rFirst) &&
					rSecond.equals(rOther.rSecond));
		}

		return false;
	}

	/***************************************
	 * Returns the first value.
	 *
	 * @return The first value
	 */
	public final F first()
	{
		return rFirst;
	}

	/***************************************
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return (37 * rFirst.hashCode()) + rSecond.hashCode() + 17;
	}

	/***************************************
	 * Returns the second value.
	 *
	 * @return The second value
	 */
	public final S second()
	{
		return rSecond;
	}
}
