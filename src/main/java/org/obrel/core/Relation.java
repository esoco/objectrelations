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
import de.esoco.lib.event.EventHandler;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Functions;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.property.Immutability;
import org.obrel.type.ListenerTypes;
import org.obrel.type.MetaTypes;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static org.obrel.type.MetaTypes.IMMUTABLE;

/**
 * This is the abstract base class for relations from a certain origin object to
 * a target object. Relations are related objects themselves so that certain
 * relation type implementations can associate data with relation instances by
 * using relations recursively. The generic type parameter defines the type of
 * the target object.
 *
 * @author eso
 */
public abstract class Relation<T> extends SerializableRelatedObject {

	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private final RelationType<T> type;

	/**
	 * Package-internal constructor that creates a new relation instance.
	 *
	 * @param type The relation type
	 */
	public Relation(RelationType<T> type) {
		this.type = type;
	}

	/**
	 * Adds a listener to update events of this particular relation. This
	 * method
	 * provides a type-safe interface for adding relation event listeners to
	 * the
	 * relation with the type {@link ListenerTypes#RELATION_UPDATE_LISTENERS}.
	 * To remove a listener that relation can be modified directly because type
	 * safety is not needed then.
	 *
	 * @param listener The relation event listener to add
	 */
	public void addUpdateListener(EventHandler<RelationEvent<T>> listener) {
		get(ListenerTypes.RELATION_UPDATE_LISTENERS).add(listener);
	}

	/**
	 * Creates an alias with the same datatype as the relation type of this
	 * relation. This is achieved by using an identity function as the
	 * conversion.
	 *
	 * @see #aliasAs(RelationType, Relatable, InvertibleFunction)
	 */
	public final Relation<T> aliasAs(RelationType<T> aliasType,
		Relatable inParent) {
		return aliasAs(aliasType, inParent, Functions.identity());
	}

	/**
	 * Creates an alias for this relation with another relation type in a
	 * certain related object. Relation aliases refer directly to the original
	 * relation's target. Therefore changes to the original relation will be
	 * visible in it's aliases too. If the original relation is deleted all
	 * it's
	 * aliases will be deleted too. On the other hand deleting an alias won't
	 * effect neither the original relation nor any other alias.
	 *
	 * <p>The parent of the alias can be any related object, it doesn't need to
	 * be the same parent object of the original relation. The alias relation
	 * type can be different from the type of this relation. The given
	 * conversion function must be invertible and convert from original target
	 * value to the datatype of the alias relation type and vice versa. This
	 * conversion will be performed on each read or write access to the
	 * relation.</p>
	 *
	 * <p>To create a read-only alias the method {@link #viewAs(RelationType,
	 * Relatable, Function)} can be used instead.</p>
	 *
	 * @param aliasType       The relation type of the relation alias
	 * @param inParent        The parent object to add the relation alias to
	 * @param aliasConversion A conversion function that produces the target
	 *                        value of the alias and can be inverted for the
	 *                        setting of new targets
	 * @return The alias relation
	 */
	public final <A> Relation<A> aliasAs(RelationType<A> aliasType,
		Relatable inParent, InvertibleFunction<T, A> aliasConversion) {
		return addAlias(
			new RelationAlias<A, T>(inParent, aliasType, this,
				aliasConversion),
			inParent);
	}

	/**
	 * A convenience method to set boolean annotations to TRUE.
	 *
	 * @see #annotate(RelationType, Object)
	 */
	public final Relation<T> annotate(RelationType<Boolean> annotationType) {
		return annotate(annotationType, Boolean.TRUE);
	}

	/**
	 * Creates an annotation on this relation with a certain relation type and
	 * value. Annotations are meta-relations that provide additional
	 * information
	 * about relations. This method is just a semantic variant of the relatable
	 * method {@link Relatable#set(RelationType, Object)}. The only difference
	 * is that it returns the relation instance instead of the created (meta-)
	 * relation to allow the concatenation of annotation calls.
	 *
	 * <p>Like all other relations annotations can be queried through the get
	 * methods that relations inherit from {@link RelatedObject}. But in some
	 * cases it may also make sense to set meta-information on the relation
	 * type
	 * instead of the relation. To support this relations have additional
	 * methods like {@link #hasAnnotation(RelationType)} that first check the
	 * relation for annotations and if not found also the relation type.</p>
	 *
	 * @param annotationType The relation type of the annotation
	 * @param value          The annotation value
	 * @return Returns this instance to allow concatenation of annotation
	 * setting
	 */
	public final <V> Relation<T> annotate(RelationType<V> annotationType,
		V value) {
		set(annotationType, value);

		return this;
	}

	/**
	 * Implements the equality test for relations. Subclasses must implement
	 * the
	 * abstract method {@link #dataEqual(Relation)}.
	 *
	 * @see Object#equals(Object)
	 */
	@Override
	public final boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object == null || getClass() != object.getClass()) {
			return false;
		}

		final Relation<?> other = (Relation<?>) object;

		return (type == other.type && dataEqual(other) &&
			relationsEqual(other));
	}

	/**
	 * Returns the value of an annotation of this relation or it's type.
	 * Annotations are meta-informations on relations or their type. This is a
	 * convenience method to retrieve annotations from either this relation or
	 * from it's type in which the relation has precedence before the type. See
	 * {@link #annotate(RelationType, Object)} for more information about
	 * annotations.
	 *
	 * @param annotationType The annotation type
	 * @return The annotation value from either this relation or from it's
	 * type;
	 * will be NULL if no such annotation is available
	 */
	public final <V> V getAnnotation(RelationType<V> annotationType) {
		V value;

		if (hasRelation(annotationType)) {
			value = get(annotationType);
		} else if (type.hasRelation(annotationType)) {
			value = type.get(annotationType);
		} else {
			value = null;
		}

		return value;
	}

	/**
	 * Returns the target object of this relation.
	 *
	 * @return The target object
	 */
	public abstract T getTarget();

	/**
	 * Returns the relation's type.
	 *
	 * @return The type of this relation
	 */
	public final RelationType<T> getType() {
		return type;
	}

	/**
	 * Checks whether this relation or it's type have been annotated with a
	 * certain relation type. Annotations are meta-informations on relations or
	 * their type. This method provides a convenience check for both this
	 * relation and it's type for a certain annotation in which the relation
	 * has
	 * precedence before the type. See {@link #annotate(RelationType, Object)}
	 * for more information about annotations.
	 *
	 * @param annotationType The annotation type
	 * @return TRUE if either this relation or it's type have an annotation
	 * with
	 * the given type
	 */
	public final boolean hasAnnotation(RelationType<?> annotationType) {
		return hasRelation(annotationType) || type.hasRelation(annotationType);
	}

	/**
	 * A convenience method to check annotations that have a boolean value. For
	 * details see method {@link #getAnnotation(RelationType)}.
	 *
	 * @param annotationType The annotation type
	 * @return TRUE if the flag is set on either this relation or it's relation
	 * type
	 */
	public final boolean hasFlagAnnotation(
		RelationType<Boolean> annotationType) {
		Boolean flag = getAnnotation(annotationType);

		return flag != null && flag.booleanValue();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		int result = type.hashCode();

		result = 31 * result + dataHashCode();
		result = 31 * result + relationsHashCode();

		return result;
	}

	/**
	 * Adds an event listener for changes of this relation's target. Other than
	 * with {@link #onUpdate(Consumer)} change listeners are only notified if
	 * the target value has changed according to it's equals() method. This
	 * method is a simplified form of {@link #addUpdateListener(EventHandler)}
	 * for listeners that are only interested in {@link EventType#UPDATE
	 * UPDATE}
	 * events and don't need the full event data.
	 *
	 * @param changeHandler The handler for update events
	 * @return The registered event handler (needed for de-registration of the
	 * event listener)
	 */
	public EventHandler<RelationEvent<T>> onChange(Consumer<T> changeHandler) {
		EventHandler<RelationEvent<T>> handler = e -> {
			if (e.getType() == EventType.UPDATE &&
				!Objects.equals(e.getUpdateValue(),
					e.getElement().getTarget())) {
				changeHandler.accept(e.getUpdateValue());
			}
		};

		addUpdateListener(handler);

		return handler;
	}

	/**
	 * Adds an event listener for updates of this relation's target. This
	 * listener will be notified of any update event for the relation target,
	 * whether it has really changed or not. To be notified of changes only the
	 * method {@link #onChange(Consumer)} can be used instead. This method is a
	 * simplified form of {@link #addUpdateListener(EventHandler)} for
	 * listeners
	 * that are only interested in {@link EventType#UPDATE UPDATE} events and
	 * don't need the full event data.
	 *
	 * @param updateHandler The handler for update events
	 * @return The registered event handler (needed for de-registration of the
	 * event listener)
	 */
	public EventHandler<RelationEvent<T>> onUpdate(Consumer<T> updateHandler) {
		EventHandler<RelationEvent<T>> handler = e -> {
			if (e.getType() == EventType.UPDATE) {
				updateHandler.accept(e.getUpdateValue());
			}
		};

		addUpdateListener(handler);

		return handler;
	}

	/**
	 * Sets this relation to be immutable and tries to apply the immutable
	 * state
	 * recursively to the relation's target object. For this it checks whether
	 * the target either implements the {@link Immutability} interface or,
	 * if it
	 * is a {@link Relatable} object, sets the {@link MetaTypes#IMMUTABLE}
	 * flag.
	 * Else if the target is a collection or a map it will be wrapped in a
	 * corresponding unmodifiable instance.
	 */
	@SuppressWarnings("unchecked")
	public void setImmutable() {
		Class<?> targetType = type.getTargetType();
		Object target = getTarget();

		if (target instanceof Immutability) {
			((Immutability) target).setImmutable();
		} else if (target instanceof Relatable) {
			Relatable relatableTarget = (Relatable) target;

			if (!relatableTarget.hasRelation(IMMUTABLE)) {
				relatableTarget.set(IMMUTABLE);
			}
		} else if (targetType == List.class) {
			setTarget((T) Collections.unmodifiableList((List<?>) target));
		} else if (targetType == Set.class) {
			setTarget((T) Collections.unmodifiableSet((Set<?>) target));
		} else if (targetType == Collection.class) {
			setTarget(
				(T) Collections.unmodifiableCollection((Collection<?>) target));
		} else if (targetType == Map.class) {
			setTarget((T) Collections.unmodifiableMap((Map<?, ?>) target));
		}

		if (!hasFlag(IMMUTABLE)) {
			set(IMMUTABLE);
		}
	}

	/**
	 * Returns a string representation of this relation.
	 *
	 * @return A string describing this relation
	 */
	@Override
	public String toString() {
		return "Relation[" + type + "=" + getTarget() + "]";
	}

	/**
	 * Creates a view with the same datatype as the relation type of this
	 * relation. This is achieved by using an identity function as the
	 * conversion.
	 *
	 * @see #viewAs(RelationType, Relatable, Function)
	 */
	@SuppressWarnings({ "unchecked" })
	public final Relation<T> viewAs(RelationType<? super T> viewType,
		Relatable inParent) {
		return viewAs((RelationType<T>) viewType, inParent,
			Functions.identity());
	}

	/**
	 * Creates a view for this relation with another relation type. Like
	 * aliases
	 * created with
	 * {@link #aliasAs(RelationType, Relatable, InvertibleFunction)} views
	 * refer
	 * directly to the original relation's target but are always readonly so
	 * that modifications of the relation can only be performed through the
	 * original relation. The parent of the view can be any related object, it
	 * doesn't need to be the same parent object of the original relation.
	 *
	 * <p>The view relation type can be different from the type of this
	 * relation. The given conversion function must convert the original target
	 * value to the datatype of the view relation type. This conversion will be
	 * performed on each read access to the relation.</p>
	 *
	 * @param viewType       The relation type of the relation view
	 * @param inParent       The parent object to add the relation view to
	 * @param viewConversion A conversion function that produces the target
	 *                       value of the view
	 * @return The view relation
	 */
	public final <V> Relation<V> viewAs(RelationType<V> viewType,
		Relatable inParent, Function<T, V> viewConversion) {
		return addAlias(
			new RelationView<V, T>(inParent, viewType, this, viewConversion),
			inParent);
	}

	/**
	 * Will be invoked after a relation has been removed from it's parent
	 * Relatable. The default implementation does nothing.
	 */
	protected void removed() {
	}

	/**
	 * Adds a new relation wrapper as an alias or view to this relation and
	 * it's
	 * parent.
	 *
	 * @param alias    The relation wrapper to add
	 * @param inParent The parent to add the wrapper to
	 * @return The alias relation
	 */
	final <A> Relation<A> addAlias(RelationWrapper<A, ?, ?> alias,
		Relatable inParent) {
		((RelatedObject) inParent).addRelation(alias, true);

		return alias;
	}

	/**
	 * Must be implemented by a subclass to create a correctly typed copy of
	 * this relation instance. A subclass may prevent the creation of a copy by
	 * returning NULL. The copying must not include any sub-relations, these
	 * will be handled by the method {@link #copyTo(RelatedObject, boolean)}
	 * which invokes this method.
	 *
	 * @param target The target object the copy will belong to
	 * @return A new relation instance or NULL if copying is not possible
	 */
	abstract Relation<T> copyTo(Relatable target);

	/**
	 * Copies this relation to another related object. The copying will happen
	 * recursively, i.e. all relations of this instance will be copied too.
	 *
	 * @param target  The target object to copy this relation to
	 * @param replace TRUE to replace an existing relation, FALSE to keep it
	 */
	void copyTo(Relatable target, boolean replace) {
		boolean exists = target.hasRelation(type);

		// The alias list will be rebuilt separately, therefore ignore here
		if (!exists || (replace && !type.isFinal())) {
			Relation<T> copy = copyTo(target);

			if (copy != null) {
				ObjectRelations.copyRelations(this, copy, replace);
			}
		}
	}

	/**
	 * Must be implemented by a subclass to compare the subclass-specific data
	 * for equality with another relation. This method will be invoked from the
	 * {@link #equals(Object)} method. The argument relation will never be null
	 * and of the same class as this instance.
	 *
	 * @param other The other relation to compare this instance's data with
	 * @return TRUE if the relation data of both relations is equal
	 */
	abstract boolean dataEqual(Relation<?> other);

	/**
	 * This method must be implemented to calculate a hash code for the
	 * subclass-specific data. It will be invoked from the {@link #hashCode()}
	 * method.
	 *
	 * @return The data hash code
	 */
	abstract int dataHashCode();

	/**
	 * Must be implemented by a subclass to store the target object of this
	 * relation.
	 *
	 * @param newTarget The new target object
	 */
	abstract void setTarget(T newTarget);

	/**
	 * Package-internal method that will be invoked by the relation type to
	 * modify the reference to the target object.
	 *
	 * @param newTarget The new target object
	 */
	final void updateTarget(T newTarget) {
		// prevent changing of an immutable relation; although lookup is less
		// efficient the IMMUTABLE flag is used to prevent storing a boolean
		// in each relation instance
		if (hasFlag(IMMUTABLE)) {
			throw new UnsupportedOperationException(
				"Relation is immutable: " + type);
		}

		if (!type.isValidTarget(newTarget)) {
			throw new IllegalArgumentException(String.format(
				"Invalid target for type '%s': %s (is %s - expected %s)", type,
				newTarget, newTarget.getClass().getName(),
				type.getTargetType()));
		}

		setTarget(newTarget);
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

		if (type == null) {
			throw new InvalidObjectException("RelationType is NULL");
		}
	}
}
