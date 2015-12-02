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
	 * @see Relation#Relation(Relatable, RelationType)
	 */
	DirectRelation(RelationType<T> rType, T rTarget)
	{
		super(rType);

		this.rTarget = rTarget;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see Relation#getTarget()
	 */
	@Override
	public T getTarget()
	{
		return rTarget;
	}

	/***************************************
	 * @see Relation#copy()
	 */
	@Override
	Relation<T> copy()
	{
		return new DirectRelation<T>(getType(), getTarget());
	}

	/***************************************
	 * @see Relation#dataEqual(Relation)
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
	 * @see Relation#dataHashCode()
	 */
	@Override
	int dataHashCode()
	{
		return 17 + (rTarget != null ? rTarget.hashCode() : 0);
	}

	/***************************************
	 * @see Relation#setTarget()
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

		if (!isValidTargetForType(getType(), rTarget))
		{
			throw new InvalidObjectException("Target value invalid for type: " +
											 rTarget + "/" + getType());
		}
	}
}
