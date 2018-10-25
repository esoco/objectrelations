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

import de.esoco.lib.expression.InvertibleFunction;


/********************************************************************
 * A relation implementation that stores the target value in a transformed form.
 * To transform the target value it applies a transformation function to it
 * which must be defined in an instance of the {@link InvertibleFunction}
 * interface which provides a two-way transformation.
 *
 * <p>If a transformed relation is to be serialized both the target data value
 * and the transformation function must be serializable too. Otherwise an
 * exception will occur when an attempt is made to serialize the relation.</p>
 *
 * <p>See {@link Relatable#transform(RelationType, InvertibleFunction)} for
 * details about this kind of relation.</p>
 *
 * @author eso
 */
public class TransformedRelation<T, D> extends Relation<T>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private final InvertibleFunction<T, D> fTransformation;

	private D rData;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rType           The type of this relation
	 * @param fTransformation The transformation to be applied to target values
	 */
	public TransformedRelation(
		RelationType<T>			 rType,
		InvertibleFunction<T, D> fTransformation)
	{
		super(rType);

		this.fTransformation = fTransformation;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public T getTarget()
	{
		return fTransformation.invert(rData);
	}

	/***************************************
	 * Returns the transformation function.
	 *
	 * @return The transformation function
	 */
	public final InvertibleFunction<T, D> getTransformation()
	{
		return fTransformation;
	}

	/***************************************
	 * Returns the transformed target value.
	 *
	 * @return The transformed target value
	 */
	public final D getTransformedTarget()
	{
		return rData;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	Relation<T> copyTo(Relatable rTarget)
	{
		TransformedRelation<T, D> aCopy =
			rTarget.transform(getType(), fTransformation);

		aCopy.rData = rData;

		return aCopy;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	boolean dataEqual(Relation<?> rOther)
	{
		TransformedRelation<?, ?> rOtherRelation =
			(TransformedRelation<?, ?>) rOther;

		boolean bResult = false;

		if (fTransformation.equals(rOtherRelation.fTransformation))
		{
			if (rData == null)
			{
				bResult = (rOtherRelation.rData == null);
			}
			else
			{
				bResult = rData.equals(rOtherRelation.rData);
			}
		}

		return bResult;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	int dataHashCode()
	{
		return 31 * fTransformation.hashCode() +
			   (rData != null ? rData.hashCode() : 0);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	void setTarget(T rNewTarget)
	{
		rData = fTransformation.evaluate(rNewTarget);
	}
}
