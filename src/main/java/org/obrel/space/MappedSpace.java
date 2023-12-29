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
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.TransformedRelation;
import org.obrel.type.StandardTypes;

import java.util.List;

/**
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
public class MappedSpace<I, O> implements ObjectSpace<I> {

	private final ObjectSpace<O> wrappedSpace;

	private final Function<O, I> valueMapper;

	private InvertibleFunction<O, I> putMapper = null;

	/**
	 * Creates a new instance with a certain value mapping function.
	 *
	 * @param wrappedSpace The target object space to map values from and to
	 * @param valueMapper  The value mapping function
	 */
	public MappedSpace(ObjectSpace<O> wrappedSpace,
		Function<O, I> valueMapper) {
		this.wrappedSpace = wrappedSpace;
		this.valueMapper = valueMapper;

		if (valueMapper instanceof InvertibleFunction) {
			putMapper = (InvertibleFunction<O, I>) valueMapper;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String url) {
		wrappedSpace.delete(url);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteRelation(Relation<?> relation) {
		wrappedSpace.deleteRelation(relation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public I get(String url) {
		return valueMapper.evaluate(wrappedSpace.get(url));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T get(RelationType<T> type) {
		return wrappedSpace.get(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Relation<T> getRelation(RelationType<T> type) {
		return wrappedSpace.getRelation(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> filter) {
		return wrappedSpace.getRelations(filter);
	}

	/**
	 * Returns the value mapping function of this space.
	 *
	 * @return The value mapping function
	 */
	public final Function<O, I> getValueMapper() {
		return valueMapper;
	}

	/**
	 * Returns the wrapped object space.
	 *
	 * @return The wrapped object space
	 */
	public final ObjectSpace<O> getWrappedSpace() {
		return wrappedSpace;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(String url, I value) {
		if (putMapper != null) {
			wrappedSpace.put(url, putMapper.invert(value));
		} else {
			throw new UnsupportedOperationException(
				"Value mapping not invertible");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Relation<T> set(RelationType<T> type, T target) {
		return wrappedSpace.set(type, target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T, V> Relation<T> set(RelationType<T> type,
		Function<V, T> targetResolver, V intermediateTarget) {
		return wrappedSpace.set(type, targetResolver, intermediateTarget);
	}

	/**
	 * Overridden to return the value of the {@link StandardTypes#NAME}
	 * relation.
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return getWrappedSpace().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T, D> TransformedRelation<T, D> transform(RelationType<T> type,
		InvertibleFunction<T, D> transformation) {
		return wrappedSpace.transform(type, transformation);
	}
}
