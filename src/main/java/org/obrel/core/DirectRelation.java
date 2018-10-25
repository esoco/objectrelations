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
package org.obrel.core;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;


/********************************************************************
 * A standard relation implementation which stores the target object directly.
 *
 * @author eso
 */
public class DirectRelation<T> extends Relation<T>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	/** @serial The target value */
	private T rTarget;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see Relation#Relation(RelationType)
	 */
	public DirectRelation(RelationType<T> rType, T rTarget)
	{
		super(rType);

		this.rTarget = rTarget;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public T getTarget()
	{
		return rTarget;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	Relation<T> copyTo(Relatable rTarget)
	{
		return rTarget.set(getType(), getTarget());
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	boolean dataEqual(Relation<?> rOther)
	{
		DirectRelation<?> rOtherRelation = (DirectRelation<?>) rOther;

		if (rTarget == null)
		{
			return rOtherRelation.rTarget == null;
		}
		else
		{
			return rTarget.equals(rOtherRelation.rTarget);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	int dataHashCode()
	{
		return 17 + (rTarget != null ? rTarget.hashCode() : 0);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	void setTarget(T rNewTarget)
	{
		rTarget = rNewTarget;
	}

	/***************************************
	 * Restores this relation by reading it's state from the given input stream.
	 * Uses the default reading of {@link ObjectInputStream} but adds safeguards
	 * to ensure relation consistency.
	 *
	 * @param      rIn The input stream
	 *
	 * @throws     IOException            If reading data fails
	 * @throws     ClassNotFoundException If the class couldn't be found
	 *
	 * @serialData This class reads uses the default serialized form and only
	 *             implements readObject() to perform a validation of the values
	 *             read by the default serialization handler
	 */
	private void readObject(ObjectInputStream rIn) throws IOException,
														  ClassNotFoundException
	{
		rIn.defaultReadObject();

		if (!getType().isValidTarget(rTarget))
		{
			throw new InvalidObjectException(
				"Target value invalid for type: " +
				rTarget + "/" + getType());
		}
	}
}
