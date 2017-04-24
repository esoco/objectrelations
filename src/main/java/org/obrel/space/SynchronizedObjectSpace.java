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
package org.obrel.space;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Functions;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.TransformedRelation;


/********************************************************************
 * A wrapper for other object spaces that synchronizes concurrent access to the
 * object space methods. This is done through a {@link ReadWriteLock} so that
 * concurrent read accesses won't block each other.
 *
 * @author eso
 */
public class SynchronizedObjectSpace<O> extends MappedSpace<O, O>
{
	//~ Instance fields --------------------------------------------------------

	ReadWriteLock aAccessLock = new ReentrantReadWriteLock();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rWrappedSpace
	 */
	public SynchronizedObjectSpace(ObjectSpace<O> rWrappedSpace)
	{
		super(rWrappedSpace, Functions.identity());
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String sUrl)
	{
		synchronizedModification(() -> super.delete(sUrl));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void deleteRelation(Relation<?> rRelation)
	{
		synchronizedModification(() -> super.deleteRelation(rRelation));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public O get(String sUrl)
	{
		return synchronizedGet(() -> super.get(sUrl));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T> T get(RelationType<T> rType)
	{
		return synchronizedGet(() -> super.get(rType));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T> Relation<T> getRelation(RelationType<T> rType)
	{
		return synchronizedGet(() -> super.getRelation(rType));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> rFilter)
	{
		return synchronizedGet(() -> super.getRelations(rFilter));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void put(String sUrl, O rValue)
	{
		synchronizedModification(() -> super.put(sUrl, rValue));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T> Relation<T> set(RelationType<T> rType, T rTarget)
	{
		return synchronizedUpdate(() -> super.set(rType, rTarget));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T, V> Relation<T> set(RelationType<T> rType,
								  Function<V, T>  fTargetResolver,
								  V				  rIntermediateTarget)
	{
		return synchronizedUpdate(() ->
								  super.set(rType,
											fTargetResolver,
											rIntermediateTarget));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T, D> TransformedRelation<T, D> transform(
		RelationType<T>			 rType,
		InvertibleFunction<T, D> fTransformation)
	{
		return synchronizedUpdate(() -> super.transform(rType, fTransformation));
	}

	/***************************************
	 * Performs a synchronized access that returns a value.
	 *
	 * @param  fAccess The function that performs the read access
	 *
	 * @return The result of the invocation
	 */
	<T> T synchronizedGet(Supplier<T> fAccess)
	{
		aAccessLock.readLock().lock();

		try
		{
			return fAccess.get();
		}
		finally
		{
			aAccessLock.readLock().unlock();
		}
	}

	/***************************************
	 * Performs a synchronized modification of the wrapped object space.
	 *
	 * @param fUpdate The function that provides the result of the invocation
	 */
	void synchronizedModification(Runnable fUpdate)
	{
		aAccessLock.writeLock().lock();

		try
		{
			fUpdate.run();
		}
		finally
		{
			aAccessLock.writeLock().unlock();
		}
	}

	/***************************************
	 * Performs a synchronized update that returns a value.
	 *
	 * @param  fSet The function that performs the read access
	 *
	 * @return The result of the invocation
	 */
	<T> T synchronizedUpdate(Supplier<T> fSet)
	{
		aAccessLock.writeLock().lock();

		try
		{
			return fSet.get();
		}
		finally
		{
			aAccessLock.writeLock().unlock();
		}
	}
}
