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
package org.obrel.space;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;

import java.util.List;

import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.TransformedRelation;
import org.obrel.type.StandardTypes;


/********************************************************************
 * An {@link ObjectSpace} implementation that maps values from another object
 * space. The conversion between the generic type of this space and that of the
 * wrapped object space is performed by a value mapping function that must be
 * provided to the constructor. If the mapping function also implements the
 * {@link InvertibleFunction} interface the mapping will be bi-directional and
 * allows write access through the method {@link #put(String, Object)} too (if
 * the wrapped space allows modifications). Otherwise only read access through
 * {@link #get(String)} will be possible (and {@link #delete(String)}, if
 * supported by the wrapped space).
 *
 * <p>By using the same datatype for both type parameters and an identity
 * function this class can also be used as a base class that wraps another
 * object space and modifies or extends it's functionality in some way.</p>
 *
 * @author eso
 */
public class MappedSpace<I, O> implements ObjectSpace<I>
{
	//~ Instance fields --------------------------------------------------------

	private ObjectSpace<O>			 rWrappedSpace;
	private Function<O, I>			 fValueMapper;
	private InvertibleFunction<O, I> fPutMapper = null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain value mapping function.
	 *
	 * @param rWrappedSpace The target object space to map values from and to
	 * @param fValueMapper  The value mapping function
	 */
	public MappedSpace(
		ObjectSpace<O> rWrappedSpace,
		Function<O, I> fValueMapper)
	{
		this.rWrappedSpace = rWrappedSpace;
		this.fValueMapper  = fValueMapper;

		if (fValueMapper instanceof InvertibleFunction)
		{
			fPutMapper = (InvertibleFunction<O, I>) fValueMapper;
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String sUrl)
	{
		rWrappedSpace.delete(sUrl);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void deleteRelation(Relation<?> rRelation)
	{
		rWrappedSpace.deleteRelation(rRelation);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public I get(String sUrl)
	{
		return fValueMapper.evaluate(rWrappedSpace.get(sUrl));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T> T get(RelationType<T> rType)
	{
		return rWrappedSpace.get(rType);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T> Relation<T> getRelation(RelationType<T> rType)
	{
		return rWrappedSpace.getRelation(rType);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> rFilter)
	{
		return rWrappedSpace.getRelations(rFilter);
	}

	/***************************************
	 * Returns the value mapping function of this space.
	 *
	 * @return The value mapping function
	 */
	public final Function<O, I> getValueMapper()
	{
		return fValueMapper;
	}

	/***************************************
	 * Returns the wrapped object space.
	 *
	 * @return The wrapped object space
	 */
	public final ObjectSpace<O> getWrappedSpace()
	{
		return rWrappedSpace;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void put(String sUrl, I rValue)
	{
		if (fPutMapper != null)
		{
			rWrappedSpace.put(sUrl, fPutMapper.invert(rValue));
		}
		else
		{
			throw new UnsupportedOperationException("Value mapping not invertible");
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T> Relation<T> set(RelationType<T> rType, T rTarget)
	{
		return rWrappedSpace.set(rType, rTarget);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T, V> Relation<T> set(RelationType<T> rType,
								  Function<V, T>  fTargetResolver,
								  V				  rIntermediateTarget)
	{
		return rWrappedSpace.set(rType, fTargetResolver, rIntermediateTarget);
	}

	/***************************************
	 * Overridden to return the value of the {@link StandardTypes#NAME}
	 * relation.
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		return getWrappedSpace().toString();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T, D> TransformedRelation<T, D> transform(
		RelationType<T>			 rType,
		InvertibleFunction<T, D> fTransformation)
	{
		return rWrappedSpace.transform(rType, fTransformation);
	}
}
