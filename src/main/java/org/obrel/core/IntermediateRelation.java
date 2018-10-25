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
package org.obrel.core;

import de.esoco.lib.expression.Function;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.obrel.core.RelationTypeModifier.PRIVATE;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * A relation class that stores the target value in an intermediate format. When
 * the target value is queried it will be created by applying a conversion
 * function to the intermediate value. The converted value is then stored in the
 * superclass {@link DirectRelation} and then used for all further target
 * queries.
 *
 * <p>Copies of this class will be created by the {@link #copyTo(Relatable)}
 * method of the base class {@link DirectRelation} that will convert the
 * intermediate value by invoking the method {@link #getTarget()} and then
 * return a new instance of the base class. Therefore the copy of an
 * intermediate relation will always be a direct relation.</p>
 *
 * <p>The serialized format of an intermediate relation depends on the
 * intermediate target value and the conversion function. If both are
 * serializable they will be serialized directly. If one of them is not
 * serializable the intermediate target value will be converted to the final
 * target format which will then be serialized.</p>
 *
 * <p>See the documentation of {@link RelatedObject#set(RelationType, Function,
 * Object)} for details about this kind of relation.</p>
 *
 * @author eso
 */
@SuppressWarnings("unchecked")
public class IntermediateRelation<T, I> extends DirectRelation<T>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	static final RelationType<Function<?, ?>> TARGET_CONVERSION =
		newType(PRIVATE);

	static final RelationType<Object> INTERMEDIATE_TARGET = newType(PRIVATE);

	static
	{
		RelationTypes.init(IntermediateRelation.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rType               The type of this relation
	 * @param fTargetConversion   The conversion to apply to the intermediate
	 *                            target value
	 * @param rIntermediateTarget The intermediate target value (must not be
	 *                            NULL)
	 */
	public IntermediateRelation(RelationType<T> rType,
								Function<I, T>  fTargetConversion,
								I				rIntermediateTarget)
	{
		super(rType, null);

		assert fTargetConversion != null;
		assert rIntermediateTarget != null;

		set(TARGET_CONVERSION, fTargetConversion);
		set(INTERMEDIATE_TARGET, rIntermediateTarget);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the intermediate target value of this relation.
	 *
	 * @return The intermediate target
	 */
	public final I getIntermediateTarget()
	{
		return (I) get(INTERMEDIATE_TARGET);
	}

	/***************************************
	 * If invoked for the first time this method transforms the intermediate
	 * target value and stores it in the base class before invoking the
	 * superclass method.
	 *
	 * @see DirectRelation#getTarget()
	 */
	@Override
	public T getTarget()
	{
		T			   rTarget     = super.getTarget();
		Function<I, T> fConversion = (Function<I, T>) get(TARGET_CONVERSION);

		if (fConversion != null && rTarget == null)
		{
			rTarget = fConversion.evaluate((I) get(INTERMEDIATE_TARGET));
			setTarget(rTarget);
		}

		return rTarget;
	}

	/***************************************
	 * @see Relation#dataEqual(Relation)
	 */
	@Override
	boolean dataEqual(Relation<?> rOther)
	{
		// the intermediate target must be resolved to prevent inconsistencies
		getTarget();

		return super.dataEqual(rOther);
	}

	/***************************************
	 * @see Relation#dataHashCode()
	 */
	@Override
	int dataHashCode()
	{
		// the intermediate target must be resolved to prevent inconsistencies
		getTarget();

		return super.dataHashCode();
	}

	/***************************************
	 * Overridden to clear the intermediate value and target conversion.
	 *
	 * @see DirectRelation#setTarget(Object)
	 */
	@Override
	void setTarget(T rNewTarget)
	{
		deleteRelation(TARGET_CONVERSION);
		deleteRelation(INTERMEDIATE_TARGET);

		super.setTarget(rNewTarget);
	}

	/***************************************
	 * Converts the intermediate target object to it's final format before
	 * writing this instance to the stream if either the conversion function or
	 * the intermediate target are not serializable.
	 *
	 * @param  rOut The output stream
	 *
	 * @throws IOException If the serialization fails
	 */
	private void writeObject(ObjectOutputStream rOut) throws IOException
	{
		if (!(get(TARGET_CONVERSION) instanceof Serializable &&
			  get(INTERMEDIATE_TARGET) instanceof Serializable))
		{
			// if the intermediate data is not serializable convert it to
			// the final target format which deletes the intermediate values
			getTarget();
		}

		rOut.defaultWriteObject();
	}
}
