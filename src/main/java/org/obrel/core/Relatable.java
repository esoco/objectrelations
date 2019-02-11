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

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.monad.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


/********************************************************************
 * This interface defines the public methods that are provided by related
 * objects. It's main purpose is to be used as a base interface for other
 * interfaces, so that implementations of such interfaces can be used as related
 * objects without casting to {@link RelatedObject}. But implementations cannot
 * implement the methods of this interface by themselves. Instead, they must
 * inherit the implementation by extending the base class {@link RelatedObject}
 * because this base class contains additional management methods.
 *
 * <p>If subclassing {@link RelatedObject} is not possible this interface must
 * not be used. The handling of object relations should then be done through the
 * corresponding methods of the {@link ObjectRelations} class which can be
 * applied to arbitrary objects.</p>
 *
 * <p>Application code that needs to check for relation handling capability of
 * objects with the instanceof operator should do so by checking against this
 * interface (and not {@link RelatedObject}). That will make the code compatible
 * with possible future extensions which could provide other implementations of
 * this interface.</p>
 *
 * <p>All methods in this interface that expect a predicate parameter will only
 * work on public relations, i.e. relations with a type that doesn't have the
 * relation type modifier {@link RelationTypeModifier#PRIVATE} set.</p>
 *
 * @author eso
 */
public interface Relatable
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Deletes a certain relation from this instance.
	 *
	 * @param rRelation The relation to be removed from this instance
	 */
	public void deleteRelation(Relation<?> rRelation);

	/***************************************
	 * Returns the resolved value of the relation that matches a certain
	 * relation type. If that relation does not exist already but the type
	 * implements the method {@link RelationType#initialValue(Relatable)} which
	 * returns a value that is not NULL a new relation with that target value
	 * will be created and it's resolved value returned. Otherwise the result of
	 * {@link RelationType#defaultValue(Relatable)} will be returned (which is
	 * NULL by default).
	 *
	 * <p>The initial and default value mechanisms imply that this method cannot
	 * be used to check the existence of relations in an object. If a type
	 * provides an initial value relations will be created automatically when
	 * queried through this method. If it returns a default value the relation
	 * doesn't exist at all although a value is available. Therefore, to check
	 * for relations the method {@link #hasRelation(RelationType)} must be
	 * invoked instead. This will also avoid the automatic creation of the
	 * relations that are queried. In addition, other relation query methods
	 * like {@link #getAll(Predicate)} and {@link #getRelation(RelationType)}
	 * will ignore initial and default values.</p>
	 *
	 * @param  rType The relation type to return the target of
	 *
	 * @return The resolved target object or NULL if no relation with the given
	 *         type exists
	 */
	public <T> T get(RelationType<T> rType);

	/***************************************
	 * Returns a certain relation of this instance.
	 *
	 * @param  rType The relation type to return the relation of
	 *
	 * @return The corresponding relation or NULL if no such relation exists
	 */
	public <T> Relation<T> getRelation(RelationType<T> rType);

	/***************************************
	 * Returns a list of all public relations that match a certain filter. The
	 * order in which the relations appear in the collection will be the same on
	 * multiple invocations of this method. The returned list can be freely
	 * modified by the caller.
	 *
	 * @param  rFilter The relation filter or NULL for all relations
	 *
	 * @return A list containing the matching relations (may be empty but will
	 *         never be NULL)
	 */
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> rFilter);

	/***************************************
	 * Sets a relation to the given target object with a certain relation type
	 * to this instance. If this is the first call for the given type a new
	 * relation with that type will be created.
	 *
	 * @param  rType   The relation type
	 * @param  rTarget The unresolved target object of the relation
	 *
	 * @return Returns the relation that has been modified or created
	 *
	 * @throws IllegalArgumentException If the given type is not applicable for
	 *                                  the object
	 */
	public <T> Relation<T> set(RelationType<T> rType, T rTarget);

	/***************************************
	 * Creates a relation that initially stores the target value in an
	 * intermediate format. When the relation target is queried it will be
	 * created by applying a conversion function to the intermediate value. This
	 * allows to lazily initialize relations only when they are really needed.
	 *
	 * <p>If a relation with the given type exists already this method will
	 * throw an exception to prevent the inadvertent overriding of relations. If
	 * the caller explicitly wants to replace an existing relation it must
	 * delete the previous relation before invoking this method.</p>
	 *
	 * <p>The serialized format of an intermediate relation depends on the
	 * intermediate target value and the conversion function. If both are
	 * serializable they will be serialized directly. If one of them is not
	 * serializable the intermediate target value will be converted to the final
	 * target format before which will then be serialized.</p>
	 *
	 * <p>This method is intended to be used by frameworks that need to prevent
	 * the early resolving of relations. A possible application would be a
	 * persistence framework that wants to perform lazy loading of child
	 * objects.</p>
	 *
	 * @param  rType               The relation type
	 * @param  fTargetResolver     The function that must be applied to the
	 *                             intermediate target value to resolve the
	 *                             final target value
	 * @param  rIntermediateTarget The intermediate target value (must not be
	 *                             NULL)
	 *
	 * @return The new intermediate relation
	 *
	 * @throws IllegalStateException If a relation with the given type exists
	 *                               already
	 */
	public <T, I> Relation<T> set(RelationType<T> rType,
								  Function<I, T>  fTargetResolver,
								  I				  rIntermediateTarget);

	/***************************************
	 * Creates a relation that stores the target value in a transformed format.
	 * If a new target value is set it will be converted to the transformed
	 * format by applying a transformation function. If the target is queried
	 * from the relation it will be created from the transformed value by
	 * inverting the transformation. Therefore the function argument must
	 * implement the {@link InvertibleFunction} interface.
	 *
	 * <p>If a relation with the given type exists already it will be replaced
	 * with the transformed relation and it's current target value will be
	 * transformed automatically. If no such relation exists the transformation
	 * will be created with the initial value of the relation type.</p>
	 *
	 * <p>If a transformed relation shall be serializable both it's transformed
	 * target value and the transformation function must be serializable too.
	 * Otherwise an exception will occur when an attempt is made to serialize
	 * the relation.</p>
	 *
	 * <p>This method is intended to be used by frameworks that want to provide
	 * a way to store relations in a specific format. A possible application
	 * would be a framework that encrypts relations.</p>
	 *
	 * @param  rType           The relation type
	 * @param  fTransformation The transformation to apply to target values
	 *
	 * @return The new transformed relation
	 *
	 * @throws IllegalStateException If the relation to be transformed is an
	 *                               alias or view
	 */
	public <T, D> TransformedRelation<T, D> transform(
		RelationType<T>			 rType,
		InvertibleFunction<T, D> fTransformation);

	/***************************************
	 * Deletes a particular relation from this object. This will also cause all
	 * alias relations that have been created for this relation to be deleted.
	 * The alias removal is unconditional (i.e. it cannot be prevented by the
	 * alias relation type) because aliases won't work without the original
	 * relation.
	 *
	 * @param  rType The type of the relation to delete
	 *
	 * @throws UnsupportedOperationException If the relation type has the flag
	 *                                       {@link RelationTypeModifier#FINAL}
	 *                                       set
	 */
	default void deleteRelation(RelationType<?> rType)
	{
		Relation<?> rRelation = getRelation(rType);

		if (rRelation != null)
		{
			deleteRelation(rRelation);
		}
	}

	/***************************************
	 * Deletes all public relations from this instance that match a certain
	 * filter. See the method {@link #deleteRelation(RelationType)} for more
	 * details.
	 *
	 * @param rFilter The relation filter or NULL for all relations
	 */
	default void deleteRelations(Predicate<? super Relation<?>> rFilter)
	{
		for (Relation<?> rRelation : getRelations(rFilter))
		{
			deleteRelation(rRelation);
		}
	}

	/***************************************
	 * Returns a list of all resolved target objects of an object's public
	 * relations that match a certain filter. The order in which the relations
	 * appear in the collection will be the same on multiple invocations of this
	 * method. The returned list can be freely modified by the caller.
	 *
	 * @param  rFilter The relation filter or NULL for all relation values
	 *
	 * @return A list containing the resolved targets of all matching relations
	 *         (may be empty but will never be NULL)
	 */
	default List<Object> getAll(Predicate<? super Relation<?>> rFilter)
	{
		List<Relation<?>> rRelations = getRelations(rFilter);
		List<Object>	  aResult    = new ArrayList<Object>(rRelations.size());

		for (Relation<?> rRelation : rRelations)
		{
			aResult.add(rRelation.getTarget());
		}

		return aResult;
	}

	/***************************************
	 * Returns an {@link Option} for the value of a certain relation or {@link
	 * Option#none()} if the value is NULL or no relation exists. In the latter
	 * case no relation with an initial value will be created even if such is
	 * provided by the relation type.
	 *
	 * <p>This method is only indirectly related to {@link
	 * #setOption(RelationType, Object)} which expects a relation type with an
	 * {@link Option} datatype. Such types can be queried directly with {@link
	 * #get(RelationType)} as they return an option. This method instead
	 * provides a NULL-safe access to relations that refer to arbitrary targets.
	 * </p>
	 *
	 * @param  rType The relation type
	 *
	 * @return The option representing the relation value
	 */
	default <T> Option<T> getOption(RelationType<T> rType)
	{
		return hasRelation(rType) ? Option.of(get(rType)) : Option.none();
	}

	/***************************************
	 * Returns the number of public relations that match a certain filter.
	 *
	 * @param  rFilter The relation filter
	 *
	 * @return The number of relations that match the filter
	 */
	default int getRelationCount(Predicate<? super Relation<?>> rFilter)
	{
		return getRelations(rFilter).size();
	}

	/***************************************
	 * Returns all relations.
	 *
	 * @return The relations
	 *
	 * @see    #getRelations(Predicate)
	 */
	default List<Relation<?>> getRelations()
	{
		return getRelations(null);
	}

	/***************************************
	 * A convenience method that checks the state of a relation with a type that
	 * resolves to a boolean value. Other than {@link #get(RelationType)} this
	 * method doesn't perform an automatic initialization of relations with the
	 * given type.
	 *
	 * @param  rType A relation type that resolves to a boolean value
	 *
	 * @return TRUE if this instance has a relation for the given type which is
	 *         set to TRUE; FALSE if no relation exists of if it's value is
	 *         FALSE
	 */
	default boolean hasFlag(RelationType<Boolean> rType)
	{
		Relation<Boolean> rRelation = getRelation(rType);

		return rRelation != null && rRelation.getTarget() == Boolean.TRUE;
	}

	/***************************************
	 * A shortcut method that checks whether this instance contains a relation
	 * with the given relation type. This call is equivalent to {@code
	 * (rObj.getRelationCount(rType) > 0)}.
	 *
	 * @param  rType The relation type to check the
	 *
	 * @return TRUE if this instance has a relation for the given type
	 */
	default boolean hasRelation(RelationType<?> rType)
	{
		return getRelation(rType) != null;
	}

	/***************************************
	 * A shortcut method that checks whether this instance contains at least one
	 * public relation that matches a certain filter. This call is equivalent to
	 * {@code (rObj.getRelationCount(rFilter) > 0)}.
	 *
	 * @param  rFilter The relation filter to search matching relations for
	 *
	 * @return TRUE if this instance has at least one public relation that
	 *         matches the given filter
	 */
	default boolean hasRelations(Predicate<? super Relation<?>> rFilter)
	{
		return getRelations(rFilter).size() > 0;
	}

	/***************************************
	 * Initializes a relation with a certain type so that the relation exists
	 * afterwards. First tries to create the initial value of the relation type
	 * with {@link #get(RelationType)}. If that yields NULL indicating that no
	 * initial value exists and therefore no relation has been created it
	 * invokes {@link #set(RelationType, Object)} with a NULL value to create a
	 * new creation. the given relation type to set it's initial value.
	 *
	 * @param  rType The type of the relation to initialize
	 *
	 * @return The relation for the given type
	 */
	default <T> Relation<T> init(RelationType<T> rType)
	{
		Relation<T> rRelation;

		if (get(rType) == null)
		{
			rRelation = set(rType, null);
		}
		else
		{
			rRelation = getRelation(rType);
		}

		return rRelation;
	}

	/***************************************
	 * A convenience method that sets a relation with a type that has a boolean
	 * target value to TRUE. This is often used for flag relation types and a
	 * shortcut for invoking {@link #set(RelationType, Object)} with a target
	 * value of TRUE.
	 *
	 * @param  rFlagType A relation type with a boolean target
	 *
	 * @return Returns the relation that has been modified or created
	 */
	default Relation<Boolean> set(RelationType<Boolean> rFlagType)
	{
		return set(rFlagType, Boolean.TRUE);
	}

	/***************************************
	 * Sets multiple relations at once by applying them from instances of the
	 * class {@link RelationData}. If used with a static import of the factory
	 * method {@link RelationData#r(RelationType, Object)} it allows a compact
	 * assignment of multiple relations at once.
	 *
	 * @param rRelations rRelationDatas The relation data objects to set the
	 *                   relations from
	 */
	default void set(RelationData<?>... rRelations)
	{
		for (RelationData<?> rData : rRelations)
		{
			rData.applyTo(this);
		}
	}

	/***************************************
	 * A convenience method to set integer relations from an int value without
	 * auto-boxing.
	 *
	 * @param  rIntType The integer relation type
	 * @param  nValue   The int value to set
	 *
	 * @return The relation that has been created or updated
	 */
	default Relation<Integer> set(RelationType<Integer> rIntType, int nValue)
	{
		return set(rIntType, Integer.valueOf(nValue));
	}

	/***************************************
	 * Sets the value of an optional relation. An optional relation has a
	 * relation type with an {@link Option} datatype that wraps the actual
	 * target value. This method is a shortcut for wrapping values that can be
	 * NULL with {@link Option#of(Object)}.
	 *
	 * <p>This method is only indirectly related to {@link
	 * #getOption(RelationType)} which wraps the values of arbitrary relation
	 * types into options. This method instead requires a relation type with an
	 * {@link Option} datatype.</p>
	 *
	 * @param  rType  The type of the optional relation
	 * @param  rValue The optional value
	 *
	 * @return The resulting relation
	 */
	default <T> Relation<Option<T>> setOption(
		RelationType<Option<T>> rType,
		T						rValue)
	{
		return set(rType, Option.of(rValue));
	}

	/***************************************
	 * Returns a stream of all relations in this object.
	 *
	 * @return The stream of relations
	 */
	default Stream<Relation<?>> streamRelations()
	{
		return getRelations(null).stream();
	}
}
