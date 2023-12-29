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
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.TransformedRelation;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * A wrapper for other object spaces that synchronizes concurrent access to the
 * object space methods. This is done through a {@link ReadWriteLock} so that
 * concurrent read accesses won't block each other.
 *
 * @author eso
 */
public class SynchronizedObjectSpace<O> extends MappedSpace<O, O> {

	ReadWriteLock accessLock = new ReentrantReadWriteLock();

	/**
	 * Creates a new instance.
	 */
	public SynchronizedObjectSpace(ObjectSpace<O> wrappedSpace) {
		super(wrappedSpace, Functions.identity());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String url) {
		synchronizedModification(() -> super.delete(url));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteRelation(Relation<?> relation) {
		synchronizedModification(() -> super.deleteRelation(relation));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public O get(String url) {
		return synchronizedGet(() -> super.get(url));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T get(RelationType<T> type) {
		return synchronizedGet(() -> super.get(type));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Relation<T> getRelation(RelationType<T> type) {
		return synchronizedGet(() -> super.getRelation(type));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> filter) {
		return synchronizedGet(() -> super.getRelations(filter));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(String url, O value) {
		synchronizedModification(() -> super.put(url, value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Relation<T> set(RelationType<T> type, T target) {
		return synchronizedUpdate(() -> super.set(type, target));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T, V> Relation<T> set(RelationType<T> type,
		Function<V, T> targetResolver, V intermediateTarget) {
		return synchronizedUpdate(
			() -> super.set(type, targetResolver, intermediateTarget));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T, D> TransformedRelation<T, D> transform(RelationType<T> type,
		InvertibleFunction<T, D> transformation) {
		return synchronizedUpdate(() -> super.transform(type, transformation));
	}

	/**
	 * Performs a synchronized access that returns a value.
	 *
	 * @param access The function that performs the read access
	 * @return The result of the invocation
	 */
	<T> T synchronizedGet(Supplier<T> access) {
		accessLock.readLock().lock();

		try {
			return access.get();
		} finally {
			accessLock.readLock().unlock();
		}
	}

	/**
	 * Performs a synchronized modification of the wrapped object space.
	 *
	 * @param update The function that provides the result of the invocation
	 */
	void synchronizedModification(Runnable update) {
		accessLock.writeLock().lock();

		try {
			update.run();
		} finally {
			accessLock.writeLock().unlock();
		}
	}

	/**
	 * Performs a synchronized update that returns a value.
	 *
	 * @param set The function that performs the read access
	 * @return The result of the invocation
	 */
	<T> T synchronizedUpdate(Supplier<T> set) {
		accessLock.writeLock().lock();

		try {
			return set.get();
		} finally {
			accessLock.writeLock().unlock();
		}
	}
}
