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
import de.esoco.lib.expression.Predicate;

import java.util.List;


/********************************************************************
 * Base class for relations that wraps another relation with a different
 * relation type and possibly datatype and delegates all method calls to the
 * wrapped relation.
 */
public abstract class RelationWrapper<T, R, F extends Function<R, T>>
	extends Relation<T>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Relation<R> rWrappedRelation;
	private F		    fConversion;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain relation.
	 *
	 * @param rType       The relation type of this wrapper
	 * @param rWrapped    The relation to be wrapped
	 * @param fConversion The conversion function
	 */
	RelationWrapper(RelationType<T> rType, Relation<R> rWrapped, F fConversion)
	{
		super(rType);

		rWrappedRelation = rWrapped;
		this.fConversion = fConversion;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Redirected to the wrapped relation.
	 *
	 * @see RelatedObject#deleteRelation(Relation)
	 */
	@Override
	public <D> void deleteRelation(Relation<D> rRelation)
	{
		rWrappedRelation.deleteRelation(rRelation);
	}

	/***************************************
	 * Redirected to the wrapped relation.
	 *
	 * @see Relatable#get(RelationType)
	 */
	@Override
	@SuppressWarnings("hiding")
	public <T> T get(RelationType<T> rType)
	{
		return rWrappedRelation.get(rType);
	}

	/***************************************
	 * Returns the conversion function of this instance.
	 *
	 * @return The conversion function
	 */
	public final F getConversion()
	{
		return fConversion;
	}

	/***************************************
	 * Redirected to the wrapped relation.
	 *
	 * @see RelatedObject#getRelation(RelationType)
	 */
	@Override
	@SuppressWarnings("hiding")
	public <T> Relation<T> getRelation(RelationType<T> rType)
	{
		return rWrappedRelation.getRelation(rType);
	}

	/***************************************
	 * Redirected to the wrapped relation.
	 *
	 * @see RelatedObject#getRelations(Predicate)
	 */
	@Override
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> rFilter)
	{
		return rWrappedRelation.getRelations(rFilter);
	}

	/***************************************
	 * Returns the target of the wrapped relation as converted by the conversion
	 * function.
	 *
	 * @see Relation#getTarget()
	 */
	@Override
	public T getTarget()
	{
		return fConversion.evaluate(rWrappedRelation.getTarget());
	}

	/***************************************
	 * Returns the wrapped relation.
	 *
	 * @return The wrapped relation
	 */
	public final Relation<R> getWrappedRelation()
	{
		return rWrappedRelation;
	}

	/***************************************
	 * Redirected to the wrapped relation.
	 *
	 * @see RelatedObject#set(RelationType, Object)
	 */
	@Override
	@SuppressWarnings("hiding")
	public <T> Relation<T> set(RelationType<T> rType, T rTarget)
	{
		return rWrappedRelation.set(rType, rTarget);
	}

	/***************************************
	 * Overridden to do nothing because instances delegate all relation handling
	 * to the wrapped relation and this method should never be invoked. This
	 * will be ensured with an assertion during development time.
	 *
	 * @see RelatedObject#addRelation(Relation, boolean)
	 */
	@Override
	@SuppressWarnings("hiding")
	<T> void addRelation(Relation<T> rRelation, boolean bNotify)
	{
		assert false;
	}

	/***************************************
	 * Asserts false and always returns NULL because it should never be invoked
	 * outside of the method {@link #copyTo(RelatedObject, boolean)} which is
	 * disabled in this class.
	 *
	 * @see Relation#copy()
	 */
	@Override
	Relation<T> copy()
	{
		assert false;

		return null;
	}

	/***************************************
	 * Overridden to always assert false because the copying of relation
	 * wrappers will be handled by the wrapped relation.
	 *
	 * @see Relation#copyTo(RelatedObject, boolean)
	 */
	@Override
	void copyTo(RelatedObject rTarget, boolean bReplace)
	{
		assert false;
	}

	/***************************************
	 * @see Relation#dataEqual(Relation)
	 */
	@Override
	boolean dataEqual(Relation<?> rOther)
	{
		return rWrappedRelation ==
			   ((RelationWrapper<?, ?, ?>) rOther).rWrappedRelation;
	}

	/***************************************
	 * @see Relation#dataHashCode()
	 */
	@Override
	int dataHashCode()
	{
		return rWrappedRelation.dataHashCode();
	}

	/***************************************
	 * Overridden to remove this relation wrapper from the wrapped relation
	 * before invoking super.
	 *
	 * @see Relation#removed(RelatedObject)
	 */
	@Override
	void removed(RelatedObject rParent)
	{
		super.removed(rParent);

		rWrappedRelation.get(Relation.ALIASES).remove(this);
	}

	/***************************************
	 * Internal method to update the wrapped relation. This is used by the
	 * method {@link Relation#prepareReplace(Relation)} to update wrappers to a
	 * new relation.
	 *
	 * @param rRelation The new wrapped relation
	 */
	@SuppressWarnings("unchecked")
	void updateWrappedRelation(Relation<?> rRelation)
	{
		rWrappedRelation = (Relation<R>) rRelation;
	}
}
