//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.event.ElementEvent.EventType;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;
import org.obrel.type.ListenerTypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The base class for all relation-enabled objects. It can be used as a base
 * class but it is not abstract so that relations may also be added directly to
 * instances of this class.
 *
 * <p>If a subclass needs to intercept the relation handling of this class
 * completely it should override all public non-final methods with the exception
 * of the method {@link #toString()} and the addition of the protected method
 * {@link #deleteRelation(Relation)}.</p>
 *
 * @author eso
 */
public class RelatedObject implements Relatable {

	private static final Map<RelationType<?>, Relation<?>> NO_RELATIONS =
		Collections.emptyMap();

	transient Map<RelationType<?>, Relation<?>> relations = NO_RELATIONS;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteRelation(Relation<?> relation) {
		RelationType<?> type = relation.getType();

		// notify type and listeners before removing so that they may
		// prevent it
		// by throwing an exception
		type.checkUpdateAllowed();
		type.deleteRelation(this, relation);
		notifyRelationListeners(EventType.REMOVE, relation, null);

		relations.remove(type);
		relation.removed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T get(RelationType<T> type) {
		assert type.getName() != RelationType.INIT_TYPE :
			"Uninitialized relation type";

		Relation<T> relation = getRelation(type);

		if (relation == null) {
			T initialValue = type.initialValue(this);

			if (initialValue == null) {
				return type.defaultValue(this);
			} else {
				relation = type.newRelation(this, initialValue);

				addRelation(relation, true);
			}
		}

		return relation.getTarget();
	}

	/**
	 * @see Relatable#getRelation(RelationType)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> Relation<T> getRelation(RelationType<T> type) {
		return (Relation<T>) relations.get(type);
	}

	/**
	 * @see Relatable#getRelations(Predicate)
	 */
	@Override
	@SuppressWarnings("boxing")
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> filter) {
		List<Relation<?>> result = new ArrayList<Relation<?>>();

		for (Relation<?> relation : relations.values()) {
			if (!relation.getType().isPrivate() &&
				(filter == null || filter.test(relation))) {
				result.add(relation);
			}
		}

		return result;
	}

	/**
	 * Compares this instance's relations for equality with another related
	 * object. This method should be invoked by subclasses that implement the
	 * {@link #equals(Object)} and {@link #hashCode()} methods. From the latter
	 * subclasses should also invoke {@link #relationsHashCode()} for a
	 * consistent implementation of these methods.
	 *
	 * @param other The other object to compare this instance's relations with
	 * @return TRUE if the relations of this instance equal that of the other
	 * object
	 */
	public final boolean relationsEqual(RelatedObject other) {
		return relations.equals(other.relations);
	}

	/**
	 * Returns a string description of this object's relations.
	 *
	 * @param join      The join between relation type and value
	 * @param separator The separator between relations
	 * @param indent    The indentation level (zero for the first, -1 for no
	 *                  indentation)
	 * @return The relations string
	 */
	public String relationsString(String join, String separator, int indent) {
		return relationsString(join, separator, indent, new HashSet<Object>());
	}

	/**
	 * @see Relatable#set(RelationType, Object)
	 */
	@Override
	public <T> Relation<T> set(RelationType<T> type, T target) {
		Relation<T> relation = getRelation(type);

		if (relation != null) {
			// notify type and listeners before updating so that they may
			// prevent the update by throwing an exception
			type.checkUpdateAllowed();
			type.prepareRelationUpdate(relation, target);
			notifyRelationListeners(EventType.UPDATE, relation, target);
			relation.updateTarget(target);
		} else {
			if (!type.isInitialized()) {
				RelationTypes.init(getClass());
			}

			type.checkReadonly();
			relation = new DirectRelation<T>(type, target);
			addRelation(relation, true);
		}

		return relation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final <T, I> Relation<T> set(RelationType<T> type,
		Function<I, T> targetResolver, I intermediateTarget) {
		Relation<T> relation = getRelation(type);

		if (relation == null) {
			relation = type.newIntermediateRelation(this, targetResolver,
				intermediateTarget);

			// addRelation() will replace an existing relation with the given
			// type
			addRelation(relation, true);
		} else {
			throw new IllegalStateException(
				"Relation already exists: " + relation);
		}

		return relation;
	}

	/**
	 * Returns a string representation of this instance. The returned string
	 * will be composed of the simple class name followed by a comma-separated
	 * list of all relations in brackets. In this list all non-private
	 * relations
	 * will be formatted as {@literal <TYPE>=<TARGET>}.
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		String stringBuilder =
			getClass().getSimpleName() + '[' + relationsString("=", ",", -1) +
				']';

		return stringBuilder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T, D> TransformedRelation<T, D> transform(RelationType<T> type,
		InvertibleFunction<T, D> transformation) {
		Relation<T> relation = getRelation(type);
		T target;

		TransformedRelation<T, D> transformedRelation =
			type.newTransformedRelation(this, transformation);

		if (relation instanceof RelationWrapper<?, ?, ?>) {
			throw new IllegalStateException(
				"Cannot transform alias relation " + type);
		} else if (relation != null) {
			target = relation.getTarget();
			transformedRelation.transferRelationsFrom(relation, false);
		} else {
			target = type.initialValue(this);
		}

		transformedRelation.setTarget(target);

		// addRelation() will replace an existing relation with the given type
		addRelation(transformedRelation, relation == null);

		return transformedRelation;
	}

	/**
	 * A package-internal method that notifies all registered relation
	 * listeners
	 * of a certain relation event.
	 *
	 * @param eventType   The event type
	 * @param relation    The relation that is affected by the event
	 * @param updateValue The update value in case of a relation update
	 */
	protected <T> void notifyRelationListeners(EventType eventType,
		Relation<T> relation, T updateValue) {
		RelationType<T> type = relation.getType();

		if (!type.isPrivate()) {
			if (hasRelation(ListenerTypes.RELATION_LISTENERS)) {
				get(ListenerTypes.RELATION_LISTENERS).dispatch(
					new RelationEvent<T>(eventType, this, relation,
						updateValue,
						this));
			}

			if (relation.hasRelation(ListenerTypes.RELATION_UPDATE_LISTENERS)) {
				relation
					.get(ListenerTypes.RELATION_UPDATE_LISTENERS)
					.dispatch(new RelationEvent<T>(eventType, this, relation,
						updateValue, relation));
			}

			if (type.hasRelation(ListenerTypes.RELATION_TYPE_LISTENERS)) {
				type
					.get(ListenerTypes.RELATION_TYPE_LISTENERS)
					.dispatch(new RelationEvent<T>(eventType, this, relation,
						updateValue, type));
			}
		}
	}

	/**
	 * Helper method for serializable subclasses to read the relations of this
	 * instance from the given input stream in the format that has been written
	 * by the method {@link #writeRelations(ObjectOutputStream)}.
	 *
	 * @param in The object input stream to read from
	 * @throws IOException            If reading a relation fails
	 * @throws ClassNotFoundException If a relation class is not available
	 */
	protected final void readRelations(ObjectInputStream in)
		throws IOException, ClassNotFoundException {
		int count = in.readInt();

		while (count-- > 0) {
			addRelation((Relation<?>) in.readObject(), false);
		}
	}

	/**
	 * Calculates the hash code of this instance's relations. This method
	 * should
	 * be invoked by subclasses that implement the {@link #hashCode()} and
	 * {@link #equals(Object)} methods. From the latter subclasses should also
	 * invoke {@link #relationsEqual(RelatedObject)} for a consistent
	 * implementation of these methods.
	 *
	 * @return The hash code of this instance's relations
	 */
	protected final int relationsHashCode() {
		return relations.hashCode();
	}

	/**
	 * Helper method for serializable subclasses to write the relations of this
	 * instance to the given output stream. It will first write the count of
	 * the
	 * relations and then all non-transient relations by directly serializing
	 * them to the stream.
	 *
	 * @param out The output stream to write the relations to
	 * @throws IOException If writing a relation fails
	 */
	protected final void writeRelations(ObjectOutputStream out)
		throws IOException {
		int count = 0;

		for (Relation<?> relation : relations.values()) {
			if (!relation.getType().isTransient()) {
				count++;
			}
		}

		out.writeInt(count);

		for (Relation<?> relation : relations.values()) {
			if (!relation.getType().isTransient()) {
				out.writeObject(relation);
			}
		}
	}

	/**
	 * Adds a relation to this object.
	 *
	 * @param relation The relation to add
	 * @param notify   TRUE if the relation type and listeners shall be
	 *                    notified
	 *                 of the added relation; FALSE if the call is for internal
	 *                 relation management only
	 */
	<T> void addRelation(Relation<T> relation, boolean notify) {
		RelationType<T> type = relation.getType();

		relation = type.addRelation(this, relation);

		if (notify) {
			// notify listeners before adding so that they may prevent it
			// by throwing an exception
			notifyRelationListeners(EventType.ADD, relation, null);
		}

		if (relations == NO_RELATIONS) {
			relations = new LinkedHashMap<RelationType<?>, Relation<?>>();
		}

		relations.put(type, relation);
	}

	/**
	 * Returns a string description of this object's relations.
	 *
	 * @param join            The join between relation type and value
	 * @param separator       The separator between relations
	 * @param indent          The indentation level (zero for the first, -1 for
	 *                        no indentation)
	 * @param excludedObjects Objects that shall not be converted to prevent
	 *                        recursion
	 * @return The relations string
	 */
	String relationsString(String join, String separator, int indent,
		Set<Object> excludedObjects) {
		StringBuilder stringBuilder = new StringBuilder();

		for (Relation<?> relation : relations.values()) {
			RelationType<?> type = relation.getType();
			Object target = relation.getTarget();

			excludedObjects.add(this);

			if (!type.isPrivate()) {
				if (target == this) {
					// prevent recursion
					target = "<this>";
				} else if (excludedObjects.contains(target)) {
					target = target.getClass().getSimpleName();
				} else if (target instanceof RelatedObject) {
					int level = indent >= 0 ? indent + 1 : -1;

					String targetRelations =
						((RelatedObject) target).relationsString(join,
							separator, level, excludedObjects);

					if (level >= 0) {
						target = target.getClass().getSimpleName() + "\n" +
							targetRelations;
					} else {
						target = target.getClass().getSimpleName() + "[" +
							targetRelations + "]";
					}
				}

				for (int i = 0; i < indent; i++) {
					stringBuilder.append("  ");
				}

				stringBuilder.append(type);
				stringBuilder.append(join);
				stringBuilder.append(target);
				stringBuilder.append(separator);
			}
		}

		if (relations.size() > 0) {
			stringBuilder.setLength(
				stringBuilder.length() - separator.length());
		}

		return stringBuilder.toString();
	}

	/**
	 * Transfers all relations from another object to this one. This method is
	 * intended to be used internally by the framework in cases where a certain
	 * related object is about to replace the argument object. The relations
	 * will be added to this object unchanged and it is the responsibility of
	 * the caller that the resulting object will be consistent.
	 *
	 * @param source The source object to transfer the relations from
	 * @param notify TRUE if the relation type and listeners shall be notified
	 *               of the added relation; FALSE if the call is for internal
	 *               relation management only
	 */
	void transferRelationsFrom(RelatedObject source, boolean notify) {
		for (Relation<?> relation : source.relations.values()) {
			addRelation(relation, notify);
		}
	}
}
