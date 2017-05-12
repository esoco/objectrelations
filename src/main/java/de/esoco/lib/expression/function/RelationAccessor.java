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
package de.esoco.lib.expression.function;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;


/********************************************************************
 * An wrapper for a relatable object and a certain relation type that implements
 * the {@link Consumer} and {@link Supplier} interfaces for functional access to
 * the relation.
 *
 * @author eso
 */
public class RelationAccessor<T> implements Consumer<T>, Supplier<T>
{
	//~ Instance fields --------------------------------------------------------

	private final Relatable		  rRelatable;
	private final RelationType<T> rType;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rRelatable
	 * @param rType
	 */
	public RelationAccessor(Relatable rRelatable, RelationType<T> rType)
	{
		this.rRelatable = rRelatable;
		this.rType	    = rType;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Implemented to set a value in the target relation of the wrapped
	 * relatable.
	 *
	 * @param rValue The new relation value
	 */
	@Override
	public void accept(T rValue)
	{
		rRelatable.set(rType, rValue);
	}

	/***************************************
	 * Implemented to return the value of the target relation of the wrapped
	 * relatable.
	 *
	 * @return The relation value
	 */
	@Override
	public T get()
	{
		return rRelatable.get(rType);
	}
}
