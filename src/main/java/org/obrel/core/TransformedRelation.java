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

/**
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
public class TransformedRelation<T, D> extends Relation<T> {

	private static final long serialVersionUID = 1L;

	private final InvertibleFunction<T, D> transformation;

	private D data;

	/**
	 * Creates a new instance.
	 *
	 * @param type           The type of this relation
	 * @param transformation The transformation to be applied to target values
	 */
	public TransformedRelation(RelationType<T> type,
		InvertibleFunction<T, D> transformation) {
		super(type);

		this.transformation = transformation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getTarget() {
		return transformation.invert(data);
	}

	/**
	 * Returns the transformation function.
	 *
	 * @return The transformation function
	 */
	public final InvertibleFunction<T, D> getTransformation() {
		return transformation;
	}

	/**
	 * Returns the transformed target value.
	 *
	 * @return The transformed target value
	 */
	public final D getTransformedTarget() {
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Relation<T> copyTo(Relatable target) {
		TransformedRelation<T, D> copy =
			target.transform(getType(), transformation);

		copy.data = data;

		return copy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean dataEqual(Relation<?> other) {
		TransformedRelation<?, ?> otherRelation =
			(TransformedRelation<?, ?>) other;

		boolean result = false;

		if (transformation.equals(otherRelation.transformation)) {
			if (data == null) {
				result = (otherRelation.data == null);
			} else {
				result = data.equals(otherRelation.data);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int dataHashCode() {
		return 31 * transformation.hashCode() +
			(data != null ? data.hashCode() : 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void setTarget(T newTarget) {
		data = transformation.evaluate(newTarget);
	}
}
