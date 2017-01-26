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
package org.obrel.type;

import de.esoco.lib.collection.ImmutableCollection;
import de.esoco.lib.event.ElementEvent.EventType;
import de.esoco.lib.expression.BinaryFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;


/********************************************************************
 * An automatic relation type that collects values from other relations of the
 * object it is set on. This type registers itself as a relation listener on
 * it's parent object. The values to be collected are determined by evaluating
 * the relation of each event with a collector function that must return either
 * the value to collect or NULL if nothing should be collected.
 *
 * <p>A collector type can be used to either collect only distinct values which
 * are then stored in an ordered {@link Set} collection so that each distinct
 * value can only appear once. If a relation is removed from the parent object
 * the value returned for the relation by the collector function will also be
 * removed from the collection of a distinct collector type. Non-distinct types
 * just collect all returned values in the order in which they appear into a
 * sequential collection (a {@link List}). In that mode removals of relations
 * don't affect the contents of the collected values.</p>
 *
 * <p>If a collector type is set on a relation or a relation type it will
 * collect all values that are set in the relation or of with the relation type
 * as they are returned by the collector function, not of the other relations of
 * the relation (type) if seen as a relatable object.</p>
 *
 * <p>By default the collection of collector type instances is mutable, i.e. it
 * can be modified externally by adding or removing values. If it is desired
 * that the collection is not modifiable either one of the relation type
 * modifiers {@link RelationTypeModifier#READONLY READONLY} or {@link
 * RelationTypeModifier#FINAL FINAL} can be set. Besides their standard property
 * of making the relation itself unmodifiable this will also make the underlying
 * collection immutable from the outside while the collection functionality will
 * continue to work.</p>
 *
 * @author eso
 */
public class CollectorType<T> extends AutomaticType<Collection<T>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private final BinaryFunction<RelationType<?>, Object, T> fCollector;
	private final boolean									 bDistinctValues;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sName           The name of this type
	 * @param rCollectedType  The datatype of the collected values
	 * @param fCollector      The function that determines the values to be
	 *                        collected from relation types and target values;
	 *                        if it returns NULL no value will be collected
	 * @param bDistinctValues TRUE to collect only distinct values; FALSE to
	 *                        collect all values that are added to the object
	 * @param rModifiers      The relation type modifiers
	 */
	@SuppressWarnings("unchecked")
	public CollectorType(
		String									   sName,
		Class<? super T>						   rCollectedType,
		BinaryFunction<RelationType<?>, Object, T> fCollector,
		boolean									   bDistinctValues,
		RelationTypeModifier... 				   rModifiers)
	{
		super(sName,
			  (Class<Collection<T>>) (bDistinctValues ? Set.class : List.class),
			  o -> bDistinctValues ? new LinkedHashSet<>() : new ArrayList<>(),
			  rModifiers);

		this.fCollector		 = fCollector;
		this.bDistinctValues = bDistinctValues;

		set(MetaTypes.ELEMENT_DATATYPE, rCollectedType);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method for an instance that collects all values of the given type
	 * and is initialized by {@link RelationTypes#init(Class)}.
	 *
	 * @param  rCollectedType The datatype of the collected values
	 * @param  fCollector     The function that determines the values to be
	 *                        collected from relation types and target values
	 * @param  rModifiers     The relation type modifiers
	 *
	 * @return The new instance
	 */
	public static <T> CollectorType<T> newCollector(
		Class<? super T>						   rCollectedType,
		BinaryFunction<RelationType<?>, Object, T> fCollector,
		RelationTypeModifier... 				   rModifiers)
	{
		return new CollectorType<>(null,
								   rCollectedType,
								   fCollector,
								   false,
								   rModifiers);
	}

	/***************************************
	 * Factory method for an instance that collects only distinct values of the
	 * given type and is initialized by {@link RelationTypes#init(Class)}.
	 *
	 * @param  rCollectedType The datatype of the collected values
	 * @param  fCollector     The function that determines the values to be
	 *                        collected from relation types and target values
	 * @param  rModifiers     The relation type modifiers
	 *
	 * @return The new instance
	 */
	public static <T> CollectorType<T> newDistinctCollector(
		Class<? super T>						   rCollectedType,
		BinaryFunction<RelationType<?>, Object, T> fCollector,
		RelationTypeModifier... 				   rModifiers)
	{
		return new CollectorType<>(null,
								   rCollectedType,
								   fCollector,
								   true,
								   rModifiers);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Evaluates the event relation and collects the value if the collector
	 * function returns a non-NULL value.
	 *
	 * @see AutomaticType#processEvent(RelationEvent)
	 */
	@Override
	protected void processEvent(RelationEvent<?> rEvent)
	{
		RelationType<?> rRelationType = rEvent.getElement().getType();
		EventType	    eEventType    = rEvent.getType();

		Object rValue =
			eEventType == EventType.UPDATE ? rEvent.getUpdateValue()
										   : rEvent.getElement().getTarget();

		T rCollectValue = fCollector.evaluate(rRelationType, rValue);

		if (rCollectValue != null)
		{
			Collection<T> rValueCollection = rEvent.getEventScope().get(this);

			if (rValueCollection instanceof CollectionWrapper)
			{
				rValueCollection =
					((CollectionWrapper<T>) rValueCollection).rCollection;
			}

			if (eEventType == EventType.ADD || eEventType == EventType.UPDATE)
			{
				rValueCollection.add(rCollectValue);
			}
			else if (bDistinctValues && eEventType == EventType.REMOVE)
			{
				rValueCollection.remove(rCollectValue);
			}
		}
	}

	/***************************************
	 * Overridden to protect the target collection.
	 *
	 * @see RelationType#addRelation(Relatable, Relation)
	 */
	@Override
	protected void protectTarget(
		Relatable				rParent,
		Relation<Collection<T>> rRelation)
	{
		setRelationTarget(rRelation,
						  new CollectionWrapper<>(rRelation.getTarget()));
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An internal wrapper for the collection of final or readonly connections
	 * that provides access to the wrapped collection to allow it's internal
	 * modification by {@link CollectorType}.
	 *
	 * @author eso
	 */
	private static class CollectionWrapper<E> extends ImmutableCollection<E>
	{
		//~ Instance fields ----------------------------------------------------

		private final Collection<E> rCollection;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rWrappedCollection The wrapped collection
		 */
		public CollectionWrapper(Collection<E> rWrappedCollection)
		{
			super(rWrappedCollection);

			rCollection = rWrappedCollection;
		}
	}
}
