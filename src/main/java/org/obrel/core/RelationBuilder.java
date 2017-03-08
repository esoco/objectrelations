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
import de.esoco.lib.expression.InvertibleFunction;


/********************************************************************
 * An interface for {@link Relatable} implementations that provides default
 * methods for classes that need builder pattern relation setting methods. The
 * {@link #with(RelationType, Object) with(...)} methods invoke the method
 * {@link Relatable#set(RelationType, Object)} but return the instance of the
 * interface implementation (i.e. the target object of the {@code with()} call).
 * This allows to concatenate multiple such calls to build a relatable object
 * that contains different relation with a single invocation. The generic type
 * of an implementation must be the implementation itself, similar to
 * self-referencing enums.
 *
 * @author eso
 */
public interface RelationBuilder<R extends RelationBuilder<R>> extends Relatable
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * A builder pattern method to annotate a relation. Queries the relation
	 * with the first type (which must exist) and annotates it with the second
	 * type and the given value.
	 *
	 * @param  rType           The type of the relation to annotate
	 * @param  rAnnotationType The relation type of the annotation
	 * @param  rValue          The value to annotate the relation with
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	default public <T> R annotate(RelationType<?> rType,
								  RelationType<T> rAnnotationType,
								  T				  rValue)
	{
		Relation<?> rRelation = getRelation(rType);

		if (rRelation == null)
		{
			throw new IllegalArgumentException("No relation with type " +
											   rType);
		}

		rRelation.annotate(rAnnotationType, rValue);

		return (R) this;
	}

	/***************************************
	 * Invokes {@link Relatable#set(RelationData...)} and returns this instance.
	 *
	 * @param  rRelations The relations to set
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	default public R with(RelationData<?>... rRelations)
	{
		set(rRelations);

		return (R) this;
	}

	/***************************************
	 * Invokes {@link Relatable#set(RelationType)} and returns this instance.
	 *
	 * @param  rType The relation type
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	default public R with(RelationType<Boolean> rType)
	{
		set(rType);

		return (R) this;
	}

	/***************************************
	 * Builder method for transformed relations.
	 *
	 * @see #transform(RelationType, InvertibleFunction)
	 */
	@SuppressWarnings("unchecked")
	default public <T, D> R with(
		RelationType<T>			 rType,
		InvertibleFunction<T, D> fTransformation)
	{
		transform(rType, fTransformation);

		return (R) this;
	}

	/***************************************
	 * Invokes {@link Relatable#set(RelationType, Object)} and returns this
	 * instance.
	 *
	 * @param  rType  The relation type
	 * @param  rValue rTarget The unresolved target object of the relation
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	default public <T> R with(RelationType<T> rType, T rValue)
	{
		set(rType, rValue);

		return (R) this;
	}

	/***************************************
	 * A convenience method to set integer relations without auto-boxing
	 * warnings.
	 *
	 * @see #set(RelationType, Object)
	 */
	@SuppressWarnings("unchecked")
	default public R with(RelationType<Integer> rType, int nValue)
	{
		set(rType, Integer.valueOf(nValue));

		return (R) this;
	}

	/***************************************
	 * Builder method for intermediate relations.
	 *
	 * @see #set(RelationType, Function, Object)
	 */
	@SuppressWarnings("unchecked")
	default public <T, I> R with(RelationType<T> rType,
								 Function<I, T>  fTargetResolver,
								 I				 rIntermediateTarget)
	{
		set(rType, fTargetResolver, rIntermediateTarget);

		return (R) this;
	}
}
