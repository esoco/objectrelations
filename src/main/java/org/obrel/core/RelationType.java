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

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.event.EventHandler;
import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.ElementAccess;
import de.esoco.lib.expression.ElementAccessFunction;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Functions;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.expression.predicate.ElementPredicate;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.obrel.type.ListenerTypes;
import org.obrel.type.StandardTypes;


/********************************************************************
 * Instances of this class define the type and behavior of relations. The
 * generic type parameter defines the type of the relation's target object.
 * Instances cannot be created directly but must be created through one of the
 * factory methods in the class {@link RelationTypes}.
 *
 * <p>The names of all relation types must be unique. If an application tries to
 * create a new type instance with a name that already exists an exception will
 * be thrown. To avoid name conflicts or to reuse the names of existing types in
 * a different context applications should prepend type names with a namespace.
 * A namespace is similar to a Java package name, i.e. lower case words that are
 * separated by the point character '.'. Types without a namespace prefix are
 * automatically members of the default (= empty) namespace. The namespace of a
 * type can be queried by means of the method {@link #getNamespace()}. Although
 * it is not required it is recommended to use the package name of the class
 * that defines a type as the type's namespace too.</p>
 *
 * <p>Relation types implement the {@link ElementAccessFunction} interface. The
 * implementation of the {@link Function#evaluate(Object)} method returns the
 * relation value from a {@link Relatable} input object. This allows to use
 * relation types for easy access to relations in function-based APIs.</p>
 *
 * <p>All relations types are serializable. Because relation types are defined
 * as singleton instances, subclasses should declare all additional fields that
 * they declare as <code>transient</code> because instances will be replaced by
 * their singleton on deserialization so that all additional data will be lost
 * anyway and serializing them would therefore only have a negative effect.</p>
 *
 * @author eso
 */
public class RelationType<T> extends RelatedObject
	implements ElementAccessFunction<RelationType<T>, Relatable, T>,
			   Serializable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** Marker name for uninitialized relation types. */
	static final String INIT_TYPE = "!INIT";

	/** The default namespace (= the empty string). */
	public static final String DEFAULT_NAMESPACE = "";

	/** A regular expression describing the allowed type names. */
	public static final Pattern NAME_PATTERN =
		Pattern.compile(
			"([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*");

	private static Map<String, RelationType<?>> aTypeRegistry =
		new HashMap<String, RelationType<?>>();

	//~ Instance fields --------------------------------------------------------

	/** @serial The type name, including the namespace (if set). */
	private String sName = INIT_TYPE;

	private transient Class<? super T>		    rTargetType;
	private transient Set<RelationTypeModifier> aModifiers;

	// ? super T necessary to support nested generic types
	private transient Function<? super Relatable, ? super T> fDefaultValue =
		null;
	private transient Function<? super Relatable, ? super T> fInitialValue =
		null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain name, data types, and flags.
	 *
	 * @see #RelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public RelationType(String					sName,
						Class<? super T>		rTargetType,
						RelationTypeModifier... rModifiers)
	{
		this(sName, rTargetType, null, rModifiers);
	}

	/***************************************
	 * @see #RelationType(String, Class, Function, Function,
	 *      RelationTypeModifier...)
	 */
	public RelationType(String								   sName,
						Class<? super T>					   rTargetType,
						Function<? super Relatable, ? super T> fInitialValue,
						RelationTypeModifier... 			   rModifiers)
	{
		this(sName, rTargetType, null, fInitialValue, rModifiers);
	}

	/***************************************
	 * Creates a new instance with a certain name, data types, and flags. The
	 * relation type constructors are not public because instances of relation
	 * types must be created through one of the the factory methods in the class
	 * {@link RelationTypes}.
	 *
	 * <p>The generic declaration of the datatype and function arguments has
	 * been relaxed to '? super T' to allow Classes and Functions that represent
	 * or create generic types, respectively. Because of this there isn't an
	 * exact type checking of the datatype parameters and it is the
	 * responsibility of the application code to declare relation types with the
	 * most specific type possible.</p>
	 *
	 * @param  sName         The name of the type instance or NULL for automatic
	 *                       name initialization by {@link
	 *                       RelationTypes#init(Class...)}
	 * @param  rTargetType   The class of the target value datatype
	 * @param  fDefaultValue The default value
	 * @param  fInitialValue A function that returns the initial value for
	 *                       relations of this type
	 * @param  rModifiers    The modifiers to be set on this instance
	 *
	 * @throws IllegalArgumentException If the type name is invalid or if a type
	 *                                  with the given name exists already
	 */
	public RelationType(String								   sName,
						Class<? super T>					   rTargetType,
						Function<? super Relatable, ? super T> fDefaultValue,
						Function<? super Relatable, ? super T> fInitialValue,
						RelationTypeModifier... 			   rModifiers)
	{
		init(sName != null ? sName : INIT_TYPE, rTargetType, null);

		this.fDefaultValue = fDefaultValue;
		this.fInitialValue = fInitialValue;

		if (rModifiers.length > 0)
		{
			aModifiers = EnumSet.copyOf(Arrays.asList(rModifiers));
		}
		else
		{
			aModifiers = EnumSet.noneOf(RelationTypeModifier.class);
		}
	}

	/***************************************
	 * Creates a new instance that is only partially initialized. The defining
	 * class must invoke the method {@link RelationTypes#init(Class...)} from
	 * it's static initializer to fully initialize this relation type before it
	 * is used.
	 *
	 * <p>The generic declaration of the datatype and function arguments has
	 * been relaxed to '? super T' to allow Classes and Functions that represent
	 * or create generic types, respectively. Because of this there isn't an
	 * exact type checking of the datatype parameters and it is the
	 * responsibility of the application code to declare relation types with the
	 * most specific type possible.</p>
	 *
	 * @param  rModifiers The modifiers to be set on this instance
	 *
	 * @throws IllegalArgumentException If the type name is invalid or if a type
	 *                                  with the given name exists already
	 */
	protected RelationType(RelationTypeModifier... rModifiers)
	{
		if (rModifiers.length > 0)
		{
			aModifiers = EnumSet.copyOf(Arrays.asList(rModifiers));
		}
		else
		{
			aModifiers = EnumSet.noneOf(RelationTypeModifier.class);
		}
	}

	/***************************************
	 * Creates a new instance that is only partially initialized. The defining
	 * class must invoke the method {@link RelationTypes#init(Class...)} from
	 * it's static initializer to fully initialize this relation type before it
	 * is used.
	 *
	 * <p>The generic declaration of the datatype and function arguments has
	 * been relaxed to '? super T' to allow Classes and Functions that represent
	 * or create generic types, respectively. Because of this there isn't an
	 * exact type checking of the datatype parameters and it is the
	 * responsibility of the application code to declare relation types with the
	 * most specific type possible.</p>
	 *
	 * @param  fDefaultValue The default value
	 * @param  fInitialValue A function that returns the initial value for
	 *                       relations of this type
	 * @param  rModifiers    The modifiers to be set on this instance
	 *
	 * @throws IllegalArgumentException If the type name is invalid or if a type
	 *                                  with the given name exists already
	 */
	protected RelationType(Function<? super Relatable, ? super T> fDefaultValue,
						   Function<? super Relatable, ? super T> fInitialValue,
						   RelationTypeModifier... 				  rModifiers)
	{
		this(rModifiers);

		this.fDefaultValue = fDefaultValue;
		this.fInitialValue = fInitialValue;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a collection of all currently registered relation types.
	 *
	 * @return The collection of registered relation types
	 */
	public static Collection<RelationType<?>> getRegisteredRelationTypes()
	{
		return Collections.unmodifiableCollection(aTypeRegistry.values());
	}

	/***************************************
	 * Returns a collection of all registered relation types that fulfill
	 * certain criteria.
	 *
	 * @param  pCriteria A predicate defining the search criteria
	 *
	 * @return A new collection of relation types matching the criteria (empty
	 *         for none)
	 */
	public static Collection<RelationType<?>> getRelationTypes(
		Predicate<? super RelationType<?>> pCriteria)
	{
		return CollectionUtil.collect(aTypeRegistry.values(), pCriteria);
	}

	/***************************************
	 * Allows to remove a relation type from the global type registry. This
	 * method is intended to discard temporary relation types that are no longer
	 * needed by an application. Caution: using a unregistered relation type
	 * afterwards will have undefined results.
	 *
	 * @param rType The relation type to unregister
	 */
	public static void unregisterRelationType(RelationType<?> rType)
	{
		aTypeRegistry.remove(rType.getName());
	}

	/***************************************
	 * Returns the relation type instance with a certain name.
	 *
	 * @param  sName The name of the instance to return
	 *
	 * @return The instance with the given name or NULL if no such instance
	 *         exists
	 */
	public static RelationType<?> valueOf(String sName)
	{
		RelationType<?> rRelationType = aTypeRegistry.get(sName);

		if (rRelationType == null)
		{
			try
			{
				// try to load enclosing class
				Class.forName(sName.substring(0, sName.lastIndexOf('.')));
				rRelationType = aTypeRegistry.get(sName);
			}
			catch (Exception e)
			{
				// just return NULL if unsuccessful
			}
		}

		return rRelationType;
	}

	/***************************************
	 * Clears the global relation type registry. Invoked by the cleanup method
	 * {@link ObjectRelations#shutdown()}.
	 */
	static void clearTypeRegistry()
	{
		aTypeRegistry.clear();
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a listener to relation events in this relation type. This method
	 * provides a type-safe interface for adding relation event listeners to the
	 * relation with the type {@link ListenerTypes#RELATION_TYPE_LISTENERS}. To
	 * remove a listener that relation can be modified directly because type
	 * safety is not needed then.
	 *
	 * @param rListener The relation event listener to add
	 */
	public void addTypeListener(EventHandler<RelationEvent<T>> rListener)
	{
		get(ListenerTypes.RELATION_TYPE_LISTENERS).add(rListener);
	}

	/***************************************
	 * A convenience method to set one or more boolean annotations to TRUE.
	 *
	 * @see #annotate(RelationType, Object)
	 */
	@SafeVarargs
	public final RelationType<T> annotate(
		RelationType<Boolean>... rAdditionalFlags)
	{
		for (RelationType<Boolean> rFlag : rAdditionalFlags)
		{
			annotate(rFlag, Boolean.TRUE);
		}

		return this;
	}

	/***************************************
	 * Creates an annotation on this relation type with a certain relation type
	 * and value. Annotations are meta-relations that provide additional
	 * information about relation types. This method is just a semantic variant
	 * of the relatable method {@link Relatable#set(RelationType, Object)}. The
	 * only difference is that it returns the relation type instance instead of
	 * the created (meta-) relation to allow the concatenation of annotation
	 * calls.
	 *
	 * @param  rAnnotationType The relation type of the annotation
	 * @param  rValue          The annotation value
	 *
	 * @return Returns this instance to allow concatenation of annotation
	 *         setting
	 */
	public final <V> RelationType<T> annotate(
		RelationType<V> rAnnotationType,
		V				rValue)
	{
		set(rAnnotationType, rValue);

		return this;
	}

	/***************************************
	 * Returns a default resolved value for relations with this type. This value
	 * will be returned if a relation of this type is queried with the method
	 * {@link Relatable#get(RelationType)} but doesn't exist. This default
	 * implementation always returns NULL but subclasses may return arbitrary
	 * values of the resolved datatype. It is important to understand that no
	 * relation will be created from values that are returned from this method.
	 * To initialize non-existing relations on the first access the method
	 * {@link #initialValue(Relatable)} must be overridden instead.
	 *
	 * <p>The method argument is the parent object from which the relation has
	 * been queried. Subclasses can use it to create default values that are
	 * specific to certain parent object. Subclasses should also create separate
	 * instances for default values if the value class is not immutable.</p>
	 *
	 * @param  rParent The parent object to return the default value for
	 *
	 * @return The default value for non-existing relations of this type
	 */
	@SuppressWarnings("unchecked")
	public T defaultValue(Relatable rParent)
	{
		return fDefaultValue != null ? (T) fDefaultValue.apply(rParent) : null;
	}

	/***************************************
	 * Implemented to return the relation value for this type from the given
	 * input object.
	 *
	 * @see Function#evaluate(Object)
	 */
	@Override
	public T evaluate(Relatable rObject)
	{
		return rObject != null ? rObject.get(this) : null;
	}

	/***************************************
	 * @see Function#from(Function)
	 */
	@Override
	public <I> Function<I, T> from(Function<I, ? extends Relatable> rOther)
	{
		return Functions.chain(this, rOther);
	}

	/***************************************
	 * Returns the function that provides the default value of relations with
	 * this type.
	 *
	 * @return The initial value function or NULL for none
	 */
	public final Function<? super Relatable, ? super T>
	getDefaultValueFunction()
	{
		return fDefaultValue;
	}

	/***************************************
	 * Returns this relation type instance as the descriptor of the accessed
	 * element.
	 *
	 * @see ElementAccess#getElementDescriptor()
	 */
	@Override
	public RelationType<T> getElementDescriptor()
	{
		return this;
	}

	/***************************************
	 * Returns the function that creates the initial value of relations with
	 * this type.
	 *
	 * @return The initial value function or NULL for none
	 */
	public final Function<? super Relatable, ? super T>
	getInitialValueFunction()
	{
		return fInitialValue;
	}

	/***************************************
	 * Returns the full name of this relation type, including it's namespace.
	 *
	 * @return The type name
	 */
	public final String getName()
	{
		return sName;
	}

	/***************************************
	 * Returns the namespace of this type. If no special namespace has been set
	 * the constant {@link #DEFAULT_NAMESPACE} will be returned.
	 *
	 * @return The namespace of this type
	 */
	public final String getNamespace()
	{
		int nPos = sName.lastIndexOf('.');

		if (nPos > 0)
		{
			return sName.substring(0, nPos);
		}
		else
		{
			return DEFAULT_NAMESPACE;
		}
	}

	/***************************************
	 * Returns the simple name of this type, which is the type name without any
	 * possible namespace prefix.
	 *
	 * @return The namespace of this type
	 */
	public final String getSimpleName()
	{
		return sName.substring(sName.lastIndexOf('.') + 1);
	}

	/***************************************
	 * Returns the datatype of the target objects of relations of this type.
	 *
	 * @return The datatype of target objects
	 */
	public final Class<? super T> getTargetType()
	{
		return rTargetType;
	}

	/***************************************
	 * Returns the relation type name.
	 *
	 * @see RelatedObject#toString()
	 */
	@Override
	public String getToken()
	{
		return getName();
	}

	/***************************************
	 * Checks if this type has a specific modifier set.
	 *
	 * @param  rModifier The modifier to check for
	 *
	 * @return TRUE if the argument modifier is set in this type
	 */
	public final boolean hasModifier(RelationTypeModifier rModifier)
	{
		return aModifiers.contains(rModifier);
	}

	/***************************************
	 * Returns the initial target value of relations with this type. This value
	 * will be used if a non-existing relation of this type is queried for the
	 * first time. If an initial value function has been set and it or a
	 * subclass returns a value that is not NULL (which is the default) a new
	 * relation will be created with that target value.
	 *
	 * <p>The method argument is the parent object from which the relation has
	 * been queried. Subclasses can use it to create initial values that are
	 * specific to certain parent object. Subclasses should also create separate
	 * instances for initial values if the value class is not immutable.</p>
	 *
	 * @param  rParent The parent object of the relation to be initialized
	 *
	 * @return The initial value for new relations of this type or NULL to
	 *         suppress the creation of new relations on querying
	 */
	@SuppressWarnings("unchecked")
	public T initialValue(Relatable rParent)
	{
		return fInitialValue != null ? (T) fInitialValue.apply(rParent) : null;
	}

	/***************************************
	 * Overridden to return an {@link ElementPredicate} by invoking the method
	 * {@link Predicates#ifRelation(RelationType, Predicate)}.
	 *
	 * @see Function#is(Predicate)
	 */
	@Override
	public <O extends Relatable> Predicate<O> is(
		Predicate<? super T> pComparison)
	{
		return Predicates.ifRelation(this, pComparison);
	}

	/***************************************
	 * Checks the {@link RelationTypeModifier#FINAL} modifier of this type. If
	 * this modifier is set it is not possible to modify or delete a relation of
	 * this type after it has been set on an object.
	 *
	 * @return TRUE if relations with this type are final
	 */
	public final boolean isFinal()
	{
		return hasModifier(RelationTypeModifier.FINAL);
	}

	/***************************************
	 * Checks whether this type has been fully initialized.
	 *
	 * @return TRUE if the type is fully initialized
	 */
	public final boolean isInitialized()
	{
		return sName != INIT_TYPE;
	}

	/***************************************
	 * Checks the {@link RelationTypeModifier#PRIVATE} modifier of this type. If
	 * this modifier is set relations of this type will not be listed when
	 * relations are queried through the methods in the {@link Relatable}
	 * interface with predicate arguments.
	 *
	 * @return TRUE if relations with this type are private
	 */
	public final boolean isPrivate()
	{
		return hasModifier(RelationTypeModifier.PRIVATE);
	}

	/***************************************
	 * Checks the {@link RelationTypeModifier#READONLY} modifier of this type.
	 * If this modifier is set it is not possible to modify or delete a relation
	 * at all. To be useful a readonly relation type must return a value from
	 * it's {@link #initialValue(Relatable)} method because the only way to
	 * create a new relation of such a type is to query it through the method
	 * {@link Relatable#get(RelationType)}.
	 *
	 * @return TRUE if relations with this type are readonly
	 */
	public final boolean isReadonly()
	{
		return hasModifier(RelationTypeModifier.READONLY);
	}

	/***************************************
	 * Checks the {@link RelationTypeModifier#TRANSIENT} modifier of this type.
	 * If this modifier is set relations of this type will be ignored if a
	 * relatable object is serialized.
	 *
	 * @return TRUE if relations with this type are transient
	 */
	public final boolean isTransient()
	{
		return hasModifier(RelationTypeModifier.TRANSIENT);
	}

	/***************************************
	 * Check whether a certain object is a valid target for this relation type.
	 *
	 * @param  rTarget The object to check
	 *
	 * @return TRUE if the given object is a valid target
	 */
	public boolean isValidTarget(Object rTarget)
	{
		return rTarget == null ||
			   rTargetType.isAssignableFrom(rTarget.getClass());
	}

	/***************************************
	 * @see Function#then(Function)
	 */
	@Override
	public <O> Function<Relatable, O> then(Function<? super T, O> fFollowUp)
	{
		return Functions.chain(fFollowUp, this);
	}

	/***************************************
	 * Returns the name of this relation type.
	 *
	 * @see Object#toString()
	 */
	@Override
	public final String toString()
	{
		return sName;
	}

	/***************************************
	 * This method will be invoked just before a new relation of this type is
	 * added to an object. It may be overridden by subclasses to intercept this.
	 * To prevent the addition of the new relation a subclass may throw an
	 * exception. Subclasses may also replace the given relation with a
	 * different instave if necessary and return that instead.
	 *
	 * <p>Subclasses should always invoke the superclass method. The default
	 * implementation currently just returns the input relation.</p>
	 *
	 * @param  rParent   The parent object of the new relation
	 * @param  rRelation The relation to be added to the parent
	 *
	 * @return The relation to add
	 */
	protected Relation<T> addRelation(Relatable rParent, Relation<T> rRelation)
	{
		return rRelation;
	}

	/***************************************
	 * This method will be invoked just before a relation of this type will be
	 * deleted. It may be overridden by subclasses to intercept the deletion of
	 * a relation from an object. To prevent the deletion of the relation a
	 * subclass may throw an exception.
	 *
	 * <p>Subclasses should normally always invoke the superclass method because
	 * it will notify the relation event listeners of this type.</p>
	 *
	 * @param  rParent   The parent object of the relation
	 * @param  rRelation The relation to be deleted
	 *
	 * @throws UnsupportedOperationException If this relation type is final
	 */
	protected void deleteRelation(Relatable rParent, Relation<?> rRelation)
	{
		assert rRelation.getType() == this;
	}

	/***************************************
	 * Returns a new relation for this type in the given parent object that
	 * stores the relation target in an intermediate format until the relation
	 * is queried for the first time. The default implementation returns a new
	 * instance of {@link IntermediateRelation}.
	 *
	 * @param  rParent             The parent object of the new relation
	 * @param  fTargetResolver     The function that must be applied to the
	 *                             intermediate target value to resolve the
	 *                             final target value
	 * @param  rIntermediateTarget The intermediate target value (must not be
	 *                             NULL)
	 *
	 * @return The new relation
	 */
	protected <I> Relation<T> newIntermediateRelation(
		RelatedObject  rParent,
		Function<I, T> fTargetResolver,
		I			   rIntermediateTarget)
	{
		return new IntermediateRelation<T, I>(
			this,
			fTargetResolver,
			rIntermediateTarget);
	}

	/***************************************
	 * Returns a new relation for this type in the given parent object. The
	 * default implementation returns a new instance of {@link DirectRelation}.
	 *
	 * @param  rParent The parent object of the new relation
	 * @param  rTarget The target value of the relation
	 *
	 * @return The new relation
	 */
	protected Relation<T> newRelation(RelatedObject rParent, T rTarget)
	{
		return new DirectRelation<T>(this, rTarget);
	}

	/***************************************
	 * Returns a new relation for this type in the given parent object that
	 * stores the relation target in a transformed format. The default
	 * implementation returns a new instance of {@link TransformedRelation}.
	 *
	 * @param  rParent         The parent object of the new relation
	 * @param  fTransformation The transformation to apply to target values
	 *
	 * @return The new relation
	 */
	protected <D> TransformedRelation<T, D> newTransformedRelation(
		RelatedObject			 rParent,
		InvertibleFunction<T, D> fTransformation)
	{
		return new TransformedRelation<>(this, fTransformation);
	}

	/***************************************
	 * This method will be invoked just before a relation of this type is
	 * updated. It may be overridden by subclasses to intercept the updating of
	 * a relation. To prevent the updating of the relation a subclass may throw
	 * an exception. This method does not receive the parent object of a
	 * relation because that information may not be available from the current
	 * context. If a type needs the parent object of the relation in this method
	 * it should set it in the method {@link #addRelation(Relatable, Relation)}
	 * as a relation of type {@link StandardTypes#PARENT} when the relation is
	 * added.
	 *
	 * <p>Subclasses should normally always invoke the superclass method because
	 * it will notify the relation event listeners of this type.</p>
	 *
	 * @param  rRelation  The relation to be deleted
	 * @param  rNewTarget The new target object
	 *
	 * @throws UnsupportedOperationException If this relation type is final
	 */
	protected void prepareRelationUpdate(Relation<T> rRelation, T rNewTarget)
	{
		assert rRelation.getType() == this;
	}

	/***************************************
	 * Returns the type instance that corresponds to the type name that has been
	 * read by the deserialization.
	 *
	 * @return The resolved relation type instance
	 *
	 * @throws ObjectStreamException If no type instance exists for the
	 *                               deserialized name
	 */
	protected final Object readResolve() throws ObjectStreamException
	{
		RelationType<?> rType = aTypeRegistry.get(sName);

		if (rType == null)
		{
			throw new InvalidObjectException(
				"Undefined relation type: " +
				sName);
		}

		return rType;
	}

	/***************************************
	 * Updates the target of a relation with this type to a new value. This
	 * method provides the possibility for relation types to update the targets
	 * of their relations under certain circumstances (e.g. replacing mutable
	 * targets with immutable ones). The code checks that the type of the
	 * relations is this instance or else throws an exception.
	 *
	 * @param rRelation The relation to set the target of
	 * @param rValue    The new relation target
	 */
	protected void setRelationTarget(Relation<T> rRelation, T rValue)
	{
		if (rRelation.getType() != this)
		{
			throw new IllegalArgumentException(
				"Relation must be for type " +
				this);
		}

		rRelation.setTarget(rValue);
	}

	/***************************************
	 * Internal method to throw an {@link UnsupportedOperationException} if this
	 * type is readonly.
	 */
	void checkReadonly()
	{
		assert isInitialized() : "Uninitialized relation type";

		if (isReadonly())
		{
			throw new UnsupportedOperationException(
				"Relation is readonly: " +
				this);
		}
	}

	/***************************************
	 * Internal method to throw an {@link UnsupportedOperationException} if this
	 * type cannot be updated.
	 */
	void checkUpdateAllowed()
	{
		checkReadonly();

		if (isFinal())
		{
			throw new UnsupportedOperationException(
				"Relation is final: " +
				this);
		}
	}

	/***************************************
	 * Package-internal method to initialize a relation type after creation.
	 *
	 * @param  sName       The name of the type instance
	 * @param  rTargetType The class of the target value datatype
	 * @param  fInitAction An optional initialization function to be invoked on
	 *                     this instance or NULL for none
	 *
	 * @throws IllegalArgumentException If the type name is invalid or if a type
	 *                                  with the given name exists already
	 */
	final void init(String					sName,
					Class<? super T>		rTargetType,
					Action<RelationType<?>> fInitAction)
	{
		this.sName		 = sName;
		this.rTargetType = rTargetType;

		if (sName != INIT_TYPE)
		{
			if (!NAME_PATTERN.matcher(sName).matches())
			{
				throw new IllegalArgumentException(
					"Invalid relation type name: " +
					sName);
			}

			if (aTypeRegistry.containsKey(sName))
			{
				throw new IllegalArgumentException(
					String.format(
						"Duplicate relation type name %s; " +
						"already defined in %s",
						sName,
						aTypeRegistry.get(sName)));
			}

			if (fInitAction != null)
			{
				fInitAction.execute(this);
			}

			aTypeRegistry.put(sName, this);
		}
	}
}
