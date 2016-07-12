//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.event.EventHandler;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;

import java.util.Iterator;
import java.util.List;

import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import static org.obrel.core.RelationTypeModifier.PRIVATE;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.type.StandardTypes.RELATION_UPDATE_LISTENERS;


/********************************************************************
 * This is the abstract base class for relations from a certain origin object to
 * a target object. Relations are related objects themselves so that certain
 * relation type implementations can associate data with relation instances by
 * using relations recursively. The generic type parameter defines the type of
 * the target object.
 *
 * @author eso
 */
public abstract class Relation<T> extends SerializableRelatedObject
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	static final RelationType<List<RelationWrapper<?>>> ALIASES =
		newListType(PRIVATE);

	static
	{
		RelationTypes.init(Relation.class);
	}

	//~ Instance fields --------------------------------------------------------

	/** @serial The relation type */
	private final RelationType<T> rType;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Package-internal constructor that creates a new relation instance.
	 *
	 * @param rType The relation type
	 */
	Relation(RelationType<T> rType)
	{
		this.rType = rType;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Internal method to throw an exception if a certain target value is not
	 * valid for the given relation type.
	 *
	 * @param rType   The relation type
	 * @param rTarget The target candidate
	 */
	static void checkValidTargetForType(RelationType<?> rType, Object rTarget)
	{
		if (!isValidTargetForType(rType, rTarget))
		{
			throw new IllegalArgumentException(String.format("Invalid target for type '%s': %s (is %s - expected %s)",
															 rType,
															 rTarget,
															 rTarget.getClass()
															 .getName(),
															 rType
															 .getTargetType()));
		}
	}

	/***************************************
	 * Internal method to check whether a certain target value is valid for the
	 * given relation type.
	 *
	 * @param  rType   The relation type
	 * @param  rTarget The target candidate
	 *
	 * @return TRUE if the given object is a valid target
	 */
	static boolean isValidTargetForType(RelationType<?> rType, Object rTarget)
	{
		assert rType != null;

		return rTarget == null ||
			   rType.getTargetType().isAssignableFrom(rTarget.getClass());
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the target object of this relation.
	 *
	 * @return The target object
	 */
	public abstract T getTarget();

	/***************************************
	 * Adds a listener to update events of this particular relation. This method
	 * provides a type-safe interface for adding relation event listeners to the
	 * relation with the type {@link StandardTypes#RELATION_UPDATE_LISTENERS}.
	 * To remove a listener that relation can be modified directly because type
	 * safety is not needed then.
	 *
	 * @param rListener The relation event listener to add
	 */
	public void addUpdateListener(EventHandler<RelationEvent<T>> rListener)
	{
		get(RELATION_UPDATE_LISTENERS).add(rListener);
	}

	/***************************************
	 * Creates an alias for this relation with another relation type in a
	 * certain related object. Relation aliases refer directly to the original
	 * relation's target. Therefore changes to the original relation will be
	 * visible in it's aliases too. If the original relation is deleted all it's
	 * aliases will be deleted too. On the other hand deleting an alias won't
	 * effect neither the original relation nor any other alias.
	 *
	 * <p>The type parameters of the relation alias type must be exactly the
	 * same as that of the original relation's type. This allows to modify the
	 * original relation through either it's own {@link #setTarget(Object)}
	 * method or through that of an alias relation (unless any of the involved
	 * types is declared as final). To create a read-only alias with more
	 * generic relation types the {@link #viewAs(RelationType, Relatable)}
	 * method can be used instead.</p>
	 *
	 * <p>The parent of the alias can be any related object, it doesn't need to
	 * be the same parent object of the original relation.</p>
	 *
	 * @param rAliasType The relation type of the relation alias
	 * @param rInParent  The parent object to add the relation alias to
	 */
	public final void aliasAs(RelationType<T> rAliasType, Relatable rInParent)
	{
		addAlias(new RelationAlias<T>(rAliasType, this), rInParent);
	}

	/***************************************
	 * A convenience method to set boolean annotations to TRUE.
	 *
	 * @see #annotate(RelationType, Object)
	 */
	public final Relation<T> annotate(RelationType<Boolean> rAnnotationType)
	{
		return annotate(rAnnotationType, Boolean.TRUE);
	}

	/***************************************
	 * Creates an annotation on this relation with a certain relation type and
	 * value. Annotations are meta-relations that provide additional information
	 * about relations. This method is just a semantic variant of the relatable
	 * method {@link Relatable#set(RelationType, Object)}. The only difference
	 * is that it returns the relation instance instead of the created (meta-)
	 * relation to allow the concatenation of annotation calls.
	 *
	 * <p>Like all other relations annotations can be queried through the get
	 * methods that relations inherit from {@link RelatedObject}. But in some
	 * cases it may also make sense to set meta-information on the relation type
	 * instead of the relation. To support this relations have additional
	 * methods like {@link #hasAnnotation(RelationType)} that first check the
	 * relation for annotations and if not found also the relation type.</p>
	 *
	 * @param  rAnnotationType The relation type of the annotation
	 * @param  rValue          The annotation value
	 *
	 * @return Returns this instance to allow concatenation of annotation
	 *         setting
	 */
	public final <V> Relation<T> annotate(
		RelationType<V> rAnnotationType,
		V				rValue)
	{
		set(rAnnotationType, rValue);

		return this;
	}

	/***************************************
	 * Implements the equality test for relations. Subclasses must implement the
	 * abstract method {@link #dataEqual(Relation)}.
	 *
	 * @see Object#equals(Object)
	 */
	@Override
	public final boolean equals(Object rObject)
	{
		if (this == rObject)
		{
			return true;
		}

		if (rObject == null || getClass() != rObject.getClass())
		{
			return false;
		}

		final Relation<?> rOther = (Relation<?>) rObject;

		return (rType == rOther.rType && dataEqual(rOther) &&
				relationsEqual(rOther));
	}

	/***************************************
	 * Returns the value of an annotation of this relation or it's type.
	 * Annotations are meta-informations on relations or their type. This is a
	 * convenience method to retrieve annotations from either this relation or
	 * from it's type in which the relation has precedence before the type. See
	 * {@link #annotate(RelationType, Object)} for more information about
	 * annotations.
	 *
	 * @param  rAnnotationType The annotation type
	 *
	 * @return The annotation value from either this relation or from it's type;
	 *         will be NULL if no such annotation is available
	 */
	public final <V> V getAnnotation(RelationType<V> rAnnotationType)
	{
		V rValue;

		if (hasRelation(rAnnotationType))
		{
			rValue = get(rAnnotationType);
		}
		else if (rType.hasRelation(rAnnotationType))
		{
			rValue = rType.get(rAnnotationType);
		}
		else
		{
			rValue = null;
		}

		return rValue;
	}

	/***************************************
	 * Returns the relation's type.
	 *
	 * @return The type of this relation
	 */
	public final RelationType<T> getType()
	{
		return rType;
	}

	/***************************************
	 * Checks whether this relation or it's type have been annotated with a
	 * certain relation type. Annotations are meta-informations on relations or
	 * their type. This method provides a convenience check for both this
	 * relation and it's type for a certain annotation in which the relation has
	 * precedence before the type. See {@link #annotate(RelationType, Object)}
	 * for more information about annotations.
	 *
	 * @param  rAnnotationType The annotation type
	 *
	 * @return TRUE if either this relation or it's type have an annotation with
	 *         the given type
	 */
	public final boolean hasAnnotation(RelationType<?> rAnnotationType)
	{
		return hasRelation(rAnnotationType) ||
			   rType.hasRelation(rAnnotationType);
	}

	/***************************************
	 * A convenience method to check annotations that have a boolean value. For
	 * details see method {@link #getAnnotation(RelationType)}.
	 *
	 * @param  rAnnotationType The annotation type
	 *
	 * @return TRUE if the flag is set on either this relation or it's relation
	 *         type
	 */
	public final boolean hasFlagAnnotation(
		RelationType<Boolean> rAnnotationType)
	{
		Boolean rFlag = getAnnotation(rAnnotationType);

		return rFlag != null ? rFlag.booleanValue() : false;
	}

	/***************************************
	 * @see Object#hashCode()
	 */
	@Override
	public final int hashCode()
	{
		int nResult = rType.hashCode();

		nResult = 31 * nResult + dataHashCode();
		nResult = 31 * nResult + relationsHashCode();

		return nResult;
	}

	/***************************************
	 * Returns a string representation of this relation.
	 *
	 * @return A string describing this relation
	 */
	@Override
	public String toString()
	{
		return "Relation[" + rType + "=" + getTarget() + "]";
	}

	/***************************************
	 * Creates a view for this relation with another relation type. Like aliases
	 * created with {@link #aliasAs(RelationType, Relatable)} views refer
	 * directly to the original relation's target but are always readonly so
	 * that modifications of the relation can only be performed through the
	 * original relation. This allows the type parameters of the view type to be
	 * of any supertype of that of the original relation's type because they
	 * cannot be overwritten with illegal values through the view.
	 *
	 * <p>The parent of the view can be any related object, it doesn't need to
	 * be the same parent object of the original relation.</p>
	 *
	 * @param rViewType The relation type of the relation view
	 * @param rInParent The parent object to add the relation view to
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final void viewAs(
		RelationType<? super T> rViewType,
		Relatable				rInParent)
	{
		addAlias(new RelationView(this, rViewType), rInParent);
	}

	/***************************************
	 * Must be implemented by a subclass to create a correctly typed copy of
	 * this relation instance. A subclass may prevent the creation of a copy by
	 * returning NULL. The copying must not include any sub-relations, these
	 * will be handled by the method {@link #copyTo(RelatedObject, boolean)}
	 * which invokes this method.
	 *
	 * @return A new relation instance or NULL if copying is not possible
	 */
	abstract Relation<T> copy();

	/***************************************
	 * Must be implemented by a subclass to compare the subclass-specific data
	 * for equality with another relation. This method will be invoked from the
	 * {@link #equals(Object)} method. The argument relation will never be null
	 * and of the same class as this instance.
	 *
	 * @param  rOther The other relation to compare this instance's data with
	 *
	 * @return TRUE if the relation data of both relations is equal
	 */
	abstract boolean dataEqual(Relation<?> rOther);

	/***************************************
	 * This method must be implemented to calculate a hash code for the
	 * subclass-specific data. It will be invoked from the {@link #hashCode()}
	 * method.
	 *
	 * @return The data hash code
	 */
	abstract int dataHashCode();

	/***************************************
	 * Must be implemented by a subclass to store the target object of this
	 * relation.
	 *
	 * @param rNewTarget The new target object
	 */
	abstract void setTarget(T rNewTarget);

	/***************************************
	 * Adds a new relation wrapper as an alias or view to this relation and it's
	 * parent.
	 *
	 * @param rAlias    The relation wrapper to add
	 * @param rInParent The parent to add the wrapper to
	 */
	final void addAlias(RelationWrapper<?> rAlias, Relatable rInParent)
	{
		ObjectRelations.getRelationContainer(rInParent, true)
					   .addRelation(rAlias, true);
		get(ALIASES).add(rAlias);
	}

	/***************************************
	 * Copies this relation to another related object. The copying will happen
	 * recursively, i.e. all relations of this instance will be copied too.
	 *
	 * @param rTarget  The target object to copy this relation to
	 * @param bReplace TRUE to replace an existing relation, FALSE to keep it
	 */
	@SuppressWarnings("unchecked")
	void copyTo(RelatedObject rTarget, boolean bReplace)
	{
		boolean bExists = rTarget.hasRelation(rType);

		// The alias list will be rebuilt separately, therefore ignore here
		if (rType != ALIASES && (!bExists || (bReplace && !rType.isFinal())))
		{
			Relation<T> aCopy = copy();

			if (aCopy != null)
			{
				aCopy.copyRelations(this, bReplace);
				rTarget.addRelation(aCopy, true);

				for (RelationWrapper<?> rAlias : get(ALIASES))
				{
					RelationType<?> rAliasType = rAlias.getType();

					if (rAlias instanceof RelationAlias<?>)
					{
						aCopy.aliasAs((RelationType<T>) rAliasType, rTarget);
					}
					else
					{
						aCopy.viewAs((RelationType<? super T>) rAliasType,
									 rTarget);
					}
				}
			}
		}
	}

	/***************************************
	 * Internal method to prepare this relation to be used as the replacement
	 * for another relation. This is done by copying all relations from the
	 * other relation and by redirecting all aliases of the other relation to
	 * this instance.
	 *
	 * @param rOther The relation to be replaced by this instance
	 */
	void prepareReplace(Relation<T> rOther)
	{
		transferRelationsFrom(rOther, false);

		for (RelationWrapper<?> rAlias : get(ALIASES))
		{
			rAlias.updateWrappedRelation(this);
		}
	}

	/***************************************
	 * Package-internal management method that will be invoked by the framework
	 * after this relation has been removed from it's parent. It will remove all
	 * alias relations for this instance and set all fields to NULL.
	 *
	 * @param rParent The parent of this relation
	 */
	void removed(RelatedObject rParent)
	{
		Iterator<RelationWrapper<?>> i = get(ALIASES).iterator();

		while (i.hasNext())
		{
			RelationWrapper<?> rAlias = i.next();

			// deleteRelation() will invoke removed() on the alias which will
			// cause it to remove itself from this relation's alias list; to
			// prevent concurrency conflicts we remove it first
			i.remove();
			rParent.deleteRelation(rAlias);
		}

		// do not remove type and target to allow access from containers that
		// still contain the relation
	}

	/***************************************
	 * Package-internal method that will be invoked by the relation type to
	 * modify the reference to the target object.
	 *
	 * @param rNewTarget The new target object
	 */
	final void updateTarget(T rNewTarget)
	{
		if (hasFlag(MetaTypes.IMMUTABLE))
		{
			throw new UnsupportedOperationException("Relation is immutable: " +
													rType);
		}

		checkValidTargetForType(rType, rNewTarget);
		setTarget(rNewTarget);
	}

	/***************************************
	 * Restores this relation by reading it's state from the given input stream.
	 * Uses the default reading of {@link ObjectInputStream} but adds safeguards
	 * to ensure relation consistency.
	 *
	 * @param      rIn The input stream
	 *
	 * @throws     IOException            If reading data fails
	 * @throws     ClassNotFoundException If the class couldn't be found
	 *
	 * @serialData This class reads uses the default serialized form and only
	 *             implements readObject() to perform a validation of the values
	 *             read by the default serialization handler
	 */
	private void readObject(ObjectInputStream rIn) throws IOException,
														  ClassNotFoundException
	{
		rIn.defaultReadObject();

		if (rType == null)
		{
			throw new InvalidObjectException("RelationType is NULL");
		}
	}
}
