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

/**
 * A subclass of related object that is intended to hold relation-based
 * parameters. It implements the relation builder interface so that relations
 * can easily be added to an instance. It also provides the factory method
 * {@link #params(RelationData...)} which returns a new instance initialized
 * with the given parameter relations.
 *
 * <p>This class also implements the {@link ProvidesConfiguration} interface so
 * that it can be used as a holder for configuration data.</p>
 *
 * @author eso
 */
public class Params extends RelatedObject
	implements RelationBuilder<Params>, ProvidesConfiguration {

	/**
	 * Factory method to create a new instance of this class that is
	 * initialized
	 * with the given relations.
	 *
	 * @param relations The initial relations of this instance
	 * @return The new instance
	 */
	public static Params params(RelationData<?>... relations) {
		return new Params().with(relations);
	}

	/**
	 * Copies the relations of this instance to another relatable.
	 *
	 * @param target          The target relatable
	 * @param replaceExisting TRUE to overwrite existing relations in the
	 *                        target, FALSE to keep them
	 */
	public void applyTo(Relatable target, boolean replaceExisting) {
		ObjectRelations.copyRelations(this, target, replaceExisting);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T getConfigValue(RelationType<T> type, T defaultValue) {
		T value = defaultValue;

		if (hasRelation(type)) {
			value = get(type);
		}

		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void setConfigValue(RelationType<T> type, T value) {
		set(type, value);
	}
}
