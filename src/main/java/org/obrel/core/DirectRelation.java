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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;

/**
 * A standard relation implementation which stores the target object directly.
 *
 * @author eso
 */
public class DirectRelation<T> extends Relation<T> {

	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private T target;

	/**
	 * @see Relation#Relation(RelationType)
	 */
	public DirectRelation(RelationType<T> type, T target) {
		super(type);

		this.target = target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getTarget() {
		return target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Relation<T> copyTo(Relatable target) {
		return target.set(getType(), getTarget());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean dataEqual(Relation<?> other) {
		DirectRelation<?> otherRelation = (DirectRelation<?>) other;

		if (target == null) {
			return otherRelation.target == null;
		} else {
			return target.equals(otherRelation.target);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int dataHashCode() {
		return 17 + (target != null ? target.hashCode() : 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void setTarget(T newTarget) {
		target = newTarget;
	}

	/**
	 * Restores this relation by reading it's state from the given input
	 * stream.
	 * Uses the default reading of {@link ObjectInputStream} but adds
	 * safeguards
	 * to ensure relation consistency.
	 *
	 * @param in The input stream
	 * @throws IOException            If reading data fails
	 * @throws ClassNotFoundException If the class couldn't be found
	 */
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		if (!getType().isValidTarget(target)) {
			throw new InvalidObjectException(
				"Target value invalid for type: " + target + "/" + getType());
		}
	}
}
