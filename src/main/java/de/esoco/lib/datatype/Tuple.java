//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.json.Json;
import de.esoco.lib.json.JsonBuilder;

import java.io.Serializable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


/********************************************************************
 * A class that contains value tuples. Tuples are immutable and cannot be
 * changed after creation.
 *
 * @author eso
 */
public class Tuple implements Iterable<Object>, Serializable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private final List<Object> aValues;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rValues The tuple values
	 */
	protected Tuple(Object... rValues)
	{
		aValues = CollectionUtil.fixedListOf(rValues);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a new {@link Tuple}. This factory method is intended to be used
	 * with static imports to provide a short syntax for defining value tuples.
	 * If more specific value datatypes are needed a corresponding subclass
	 * should be used (or created if necessary).
	 *
	 * @param  rValues The tuple values
	 *
	 * @return A new tuple instance
	 */
	public static Tuple t(Object... rValues)
	{
		return new Tuple(rValues);
	}

	/***************************************
	 * Parses a tuple from a JSON string as generated by the {@link #toString()}
	 * method.
	 *
	 * @param  sTuple The JSON representation of the tuple
	 *
	 * @return The parsed tuple
	 */
	public static Tuple valueOf(String sTuple)
	{
		List<Object> aValues = Json.parseArray(sTuple);

		return new Tuple(aValues.toArray());
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object rObj)
	{
		if (rObj == this)
		{
			return true;
		}

		if (!(rObj instanceof Tuple))
		{
			return false;
		}

		Tuple rOther = (Tuple) rObj;

		if (rOther.size() != size())
		{
			return false;
		}

		for (int i = aValues.size() - 1; i >= 0; i--)
		{
			if (!Objects.equals(aValues.get(i), rOther.aValues.get(i)))
			{
				return false;
			}
		}

		return true;
	}

	/***************************************
	 * Returns a certain value from this tuple.
	 *
	 * @param  i The index of the value (0 &lt;= i &lt; {@link #size()})
	 *
	 * @return The value at the given index
	 */
	public Object get(int i)
	{
		return aValues.get(i);
	}

	/***************************************
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int nHashCode = 37;

		for (Object rValue : aValues)
		{
			nHashCode =
				nHashCode * (rValue != null ? rValue.hashCode() : 0) + 17;
		}

		return nHashCode;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Object> iterator()
	{
		return aValues.iterator();
	}

	/***************************************
	 * Returns the size of this tuple.
	 *
	 * @return The tuple size
	 */
	public int size()
	{
		return aValues.size();
	}

	/***************************************
	 * Returns a {@link Stream} of the values in this tuple.
	 *
	 * @return The value stream
	 */
	public Stream<Object> stream()
	{
		return aValues.stream();
	}

	/***************************************
	 * @see de.esoco.lib.datatype.Tuple#toString()
	 */
	@Override
	public String toString()
	{
		return new JsonBuilder().appendArray(aValues).toString();
	}
}
