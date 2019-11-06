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

import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.monad.Option;
import de.esoco.lib.reflect.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obrel.core.Annotations.NoRelationNameCheck;
import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.type.MetaTypes;

import static de.esoco.lib.expression.Functions.value;
import static de.esoco.lib.expression.ReflectionFuntions.newInstanceOf;

import static org.obrel.type.MetaTypes.OBJECT_TYPE_ATTRIBUTE;
import static org.obrel.type.MetaTypes.ORDERED;


/********************************************************************
 * This is a factory class to create relation types with different target data
 * types and parameters.
 *
 * @author eso
 */
public class RelationTypes
{
	//~ Static fields/initializers ---------------------------------------------

	// types declared because they are needed during relation type initialization

	/** @see MetaTypes#DECLARING_CLASS */
	public static final RelationType<Class<?>> DECLARING_CLASS =
		new RelationType<>("DECLARING_CLASS", Class.class);

	/** @see MetaTypes#RELATION_TYPE_NAMESPACE */
	public static final RelationType<String> RELATION_TYPE_NAMESPACE =
		new RelationType<>("RELATION_TYPE_NAMESPACE", String.class);

	/** @see MetaTypes#RELATION_TYPE_INIT_ACTION */
	public static final RelationType<Action<RelationType<?>>> RELATION_TYPE_INIT_ACTION =
		new RelationType<>("RELATION_TYPE_INIT_ACTION", Action.class);

	/** @see MetaTypes#ELEMENT_DATATYPE */
	public static final RelationType<Class<?>> ELEMENT_DATATYPE =
		new RelationType<>("ELEMENT_DATATYPE", Class.class);

	/** @see MetaTypes#KEY_DATATYPE */
	public static final RelationType<Class<?>> KEY_DATATYPE =
		new RelationType<>("KEY_DATATYPE", Class.class);

	/** @see MetaTypes#VALUE_DATATYPE */
	public static final RelationType<Class<?>> VALUE_DATATYPE =
		new RelationType<>("VALUE_DATATYPE", Class.class);

	// needs to be declared last as List type references ELEMENT_DATATYPE
	/** @see MetaTypes#DECLARED_RELATION_TYPES */
	public static final RelationType<List<RelationType<?>>> DECLARED_RELATION_TYPES =
		newRelationType(
			"DECLARED_RELATION_TYPES",
			List.class,
			RelationTypeModifier.FINAL);

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns the relation type namespace for a certain class. This will either
	 * be the value of the annotation {@link RelationTypeNamespace} or if that
	 * is not present, the name of the class itself, including it's own
	 * namespace.
	 *
	 * @param  rClass The class to determine the relation type namespace of
	 *
	 * @return The relation type namespace
	 */
	public static String getRelationTypeNamespace(Class<?> rClass)
	{
		String sClassNamespace;

		if (rClass.isAnnotationPresent(RelationTypeNamespace.class))
		{
			sClassNamespace =
				rClass.getAnnotation(RelationTypeNamespace.class).value();
		}
		else
		{
			sClassNamespace = rClass.getName();
		}

		return sClassNamespace;
	}

	/***************************************
	 * Initializes the relation type constants of certain classes.
	 *
	 * @param rClasses The classes to initialize
	 */
	public static void init(Class<?>... rClasses)
	{
		for (Class<?> rClass : rClasses)
		{
			initRelationTypesOf(rClass);
		}
	}

	/***************************************
	 * Initializes the relation type constants of a certain class.
	 *
	 * @param rClass The class to initialize
	 */
	public static void initRelationTypesOf(Class<?> rClass)
	{
		List<Field> rFields		    = ReflectUtil.getAllFields(rClass);
		String	    sClassNamespace = getRelationTypeNamespace(rClass);

		List<RelationType<?>> aDeclaredTypes = new ArrayList<>();

		if (sClassNamespace.length() > 0)
		{
			sClassNamespace += ".";
		}

		for (Field rField : rFields)
		{
			try
			{
				int nModifiers = rField.getModifiers();

				if (RelationType.class.isAssignableFrom(rField.getType()))
				{
					if (Modifier.isStatic(nModifiers))
					{
						rField.setAccessible(true);

						RelationType<?> rRelationType =
							(RelationType<?>) rField.get(null);

						if (Modifier.isFinal(nModifiers))
						{
							initRelationTypeField(
								rField,
								rRelationType,
								sClassNamespace);

							if (!rRelationType.hasModifier(
									RelationTypeModifier.PRIVATE))
							{
								aDeclaredTypes.add(rRelationType);
							}
						}
						else if (rRelationType != null)
						{
							assert rRelationType.getName() !=
								   RelationType.INIT_TYPE : "Relation type not final static: " +
								   rField.getName();
						}
					}
				}
			}
			catch (Exception e)
			{
				String sMessage =
					String.format(
						"Access to %s.%s failed",
						rField.getDeclaringClass().getSimpleName(),
						rField.getName());

				throw new IllegalArgumentException(sMessage, e);
			}
		}

		if (!aDeclaredTypes.isEmpty())
		{
			Relatable rClassRelatable = ObjectRelations.getRelatable(rClass);

			if (!rClassRelatable.hasRelation(DECLARED_RELATION_TYPES))
			{
				rClassRelatable.set(
					DECLARED_RELATION_TYPES,
					Collections.unmodifiableList(aDeclaredTypes));
			}
		}
	}

	/***************************************
	 * Creates a new relation type with a boolean datatype. The initial value of
	 * relations with the returned type will be FALSE.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<Boolean> newBooleanType(
		String					sName,
		RelationTypeModifier... rModifiers)
	{
		return newRelationType(
			sName,
			Boolean.class,
			value(Boolean.FALSE),
			rModifiers);
	}

	/***************************************
	 * Creates a new relation type with a class datatype. The initial value of
	 * relations with the returned type will be NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<Class<? extends T>> newClassType(
		String					sName,
		RelationTypeModifier... rModifiers)
	{
		return newRelationType(sName, Class.class, rModifiers);
	}

	/***************************************
	 * Creates a new relation type with a {@link Date} datatype. The initial
	 * value of relations with the returned type will be NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<Date> newDateType(
		String					sName,
		RelationTypeModifier... rModifiers)
	{
		return newRelationType(sName, Date.class, rModifiers);
	}

	/***************************************
	 * Creates a new partially initialized relation type with a {@link
	 * BigDecimal} datatype and an initial value of zero.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	@SuppressWarnings("boxing")
	public static RelationType<BigDecimal> newDecimalType(
		RelationTypeModifier... rModifiers)
	{
		return newInitialValueType(BigDecimal.ZERO, rModifiers);
	}

	/***************************************
	 * Creates a new relation type with a certain default value. See the generic
	 * variant #newDefaultValueType(Function, RelationTypeModifier...) for more
	 * information.
	 *
	 * @param rDefaultValue The default value of relations of this type
	 * @param rModifiers    The type modifiers
	 *
	 * @see   #newDefaultValueType(Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newDefaultValueType(
		T						rDefaultValue,
		RelationTypeModifier... rModifiers)
	{
		return newDefaultValueType(r -> rDefaultValue, rModifiers);
	}

	/***************************************
	 * Creates a new relation type with a default value function. <b>
	 * IMPORTANT</b>: default values are returned if no relation with the type
	 * exists, but querying them will not create a new relation. That means that
	 * checks for relation existence will yield FALSE. To make sure a relation
	 * exists use {@link #newInitialValueType(Object, RelationTypeModifier...)}
	 * instead.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newDefaultValueType(
		Function<? super Relatable, ? super T> fDefaultValue,
		RelationTypeModifier... 			   rModifiers)
	{
		return newType(fDefaultValue, null, rModifiers);
	}

	/***************************************
	 * Creates a new relation type with an enum datatype and no default value.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static <E extends Enum<E>> RelationType<E> newEnumType(
		String					sName,
		Class<E>				rEnumType,
		RelationTypeModifier... rModifiers)
	{
		return newRelationType(sName, rEnumType, rModifiers);
	}

	/***************************************
	 * Creates a new relation type with an enum datatype and a certain default
	 * value that also defines the datatype.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> RelationType<E> newEnumType(
		String					sName,
		E						rDefaultValue,
		RelationTypeModifier... rModifiers)
	{
		return newRelationType(
			sName,
			(Class<E>) rDefaultValue.getClass(),
			value(rDefaultValue),
			rModifiers);
	}

	/***************************************
	 * Creates a new partially initialized relation type with a {@link Boolean}
	 * datatype and an initial value of of FALSE.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	public static RelationType<Boolean> newFlagType(
		RelationTypeModifier... rModifiers)
	{
		return newInitialValueType(Boolean.FALSE, rModifiers);
	}

	/***************************************
	 * Creates a new relation type with a certain initial value. The value will
	 * be set and returned on the first get of an unset relation of this type.
	 *
	 * @see #newInitialValueType(Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newInitialValueType(
		T						rInitialValue,
		RelationTypeModifier... rModifiers)
	{
		return newInitialValueType(value(rInitialValue), rModifiers);
	}

	/***************************************
	 * Creates a new relation type with an initial value function. The value
	 * will be set and returned on the first get of an unset relation of this
	 * type.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newInitialValueType(
		Function<? super Relatable, ? super T> fInitialValue,
		RelationTypeModifier... 			   rModifiers)
	{
		return newType(fInitialValue, rModifiers);
	}

	/***************************************
	 * Creates a new relation type with an integer datatype. The initial value
	 * of relations with the returned type will be zero.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<Integer> newIntegerType(
		String					sName,
		RelationTypeModifier... rModifiers)
	{
		return newIntegerType(sName, 0, rModifiers);
	}

	/***************************************
	 * Creates a new relation type with an integer datatype and a certain
	 * default value.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	@SuppressWarnings("boxing")
	public static RelationType<Integer> newIntegerType(
		String					sName,
		int						nDefault,
		RelationTypeModifier... rModifiers)
	{
		return newRelationType(
			sName,
			Integer.class,
			value(nDefault),
			rModifiers);
	}

	/***************************************
	 * Creates a new partially initialized relation type with an integer
	 * datatype and an initial value of zero.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	@SuppressWarnings("boxing")
	public static RelationType<Integer> newIntType(
		RelationTypeModifier... rModifiers)
	{
		return newInitialValueType(0, rModifiers);
	}

	/***************************************
	 * Creates a new partially initialized relation type with an integer
	 * datatype and a certain initial value.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	@SuppressWarnings("boxing")
	public static RelationType<Integer> newIntType(
		int						nDefault,
		RelationTypeModifier... rModifiers)
	{
		return newType(value(nDefault), rModifiers);
	}

	/***************************************
	 * Creates a partially initialized relation type with a {@link List}
	 * datatype that will have an instance of {@link ArrayList} as it's initial
	 * value.
	 *
	 * @param  rModifiers The optional type modifiers
	 *
	 * @return A new relation type for relations to lists
	 */
	public static <T> RelationType<List<T>> newListType(
		RelationTypeModifier... rModifiers)
	{
		@SuppressWarnings("unchecked")
		Class<List<T>> rListClass = (Class<List<T>>) (Class<?>) ArrayList.class;

		return newType(null, newInstanceOf(rListClass), rModifiers);
	}

	/***************************************
	 * Creates a new relation type with a {@link List} datatype. The initial
	 * value of relations with the returned type will be a new instance of
	 * {@link ArrayList}.
	 *
	 * <p>The datatype of the list elements is stored as a relation with the
	 * meta relation type {@link MetaTypes#ELEMENT_DATATYPE} on the new
	 * type.</p>
	 *
	 * @param  sName        The name of the new type
	 * @param  rElementType The datatype of the list elements
	 * @param  rModifiers   The optional type modifiers
	 *
	 * @return A new relation type for list relations
	 */
	@SuppressWarnings("unchecked")
	public static <T> RelationType<List<T>> newListType(
		String					sName,
		Class<? super T>		rElementType,
		RelationTypeModifier... rModifiers)
	{
		Class<List<T>> rListClass = (Class<List<T>>) (Class<?>) ArrayList.class;

		RelationType<List<T>> aType =
			newRelationType(
				sName,
				List.class,
				newInstanceOf(rListClass),
				rModifiers);

		if (rElementType != null)
		{
			aType.set(ELEMENT_DATATYPE, rElementType);
		}

		return aType;
	}

	/***************************************
	 * Creates a new partially initialized relation type with a long datatype
	 * and an initial value of zero.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	@SuppressWarnings("boxing")
	public static RelationType<Long> newLongType(
		RelationTypeModifier... rModifiers)
	{
		return newInitialValueType(0L, rModifiers);
	}

	/***************************************
	 * Creates a partially initialized relation type with a {@link Map}
	 * datatype. Depending on the boolean parameter the initial value will
	 * either be an instance of {@link LinkedHashMap} or of {@link HashMap}.
	 *
	 * @param  bOrdered   TRUE for a map that preserves the order in which
	 *                    elements are added
	 * @param  rModifiers The optional type modifiers
	 *
	 * @return A new relation type for relations to maps
	 */
	@SuppressWarnings({ "boxing" })
	public static <K, V> RelationType<Map<K, V>> newMapType(
		boolean					bOrdered,
		RelationTypeModifier... rModifiers)
	{
		Class<?> rMapClass = bOrdered ? LinkedHashMap.class : HashMap.class;

		@SuppressWarnings("unchecked")
		Function<Object, Map<K, V>> fInitialValue =
			newInstanceOf((Class<Map<K, V>>) rMapClass);

		RelationType<Map<K, V>> aType =
			newType(null, fInitialValue, rModifiers);

		aType.annotate(ORDERED, bOrdered);

		return aType;
	}

	/***************************************
	 * Creates a new relation type with a {@link Map} datatype. Depending on the
	 * value of the boolean argument the initial value of relations with the
	 * returned type will either be a new instance of {@link HashMap} (FALSE) or
	 * {@link LinkedHashMap} (TRUE).
	 *
	 * <p>The datatype of the keys and values in the map are stored as meta
	 * relations on the returned relation type with the meta relation types
	 * {@link MetaTypes#KEY_DATATYPE} and {@link MetaTypes#VALUE_DATATYPE}.</p>
	 *
	 * @param  sName         The name of the new type
	 * @param  rKeyType      The datatype of the map keys
	 * @param  rValueType    The datatype of the map values
	 * @param  bInitialValue TRUE if the returned relation type has an initial
	 *                       value, FALSE to return NULL if no relation is set
	 * @param  bOrdered      If bDefaultValue is TRUE, TRUE for a map that
	 *                       preserves the order in which elements are added
	 * @param  rModifiers    The optional type modifiers
	 *
	 * @return A new relation type for map relations
	 */
	@SuppressWarnings("boxing")
	public static <K, V> RelationType<Map<K, V>> newMapType(
		String					sName,
		Class<? super K>		rKeyType,
		Class<? super V>		rValueType,
		boolean					bInitialValue,
		boolean					bOrdered,
		RelationTypeModifier... rModifiers)
	{
		Function<Object, Map<K, V>> fInitialValue = null;

		if (bInitialValue)
		{
			Class<?> rMapClass = bOrdered ? LinkedHashMap.class : HashMap.class;

			@SuppressWarnings("unchecked")
			Class<Map<K, V>> rDefaultValueClass = (Class<Map<K, V>>) rMapClass;

			fInitialValue = newInstanceOf(rDefaultValueClass);
		}

		RelationType<Map<K, V>> aType =
			newRelationType(
				sName,
				(Class<? super Map<K, V>>) Map.class,
				fInitialValue,
				rModifiers);

		aType.set(KEY_DATATYPE, rKeyType);
		aType.set(VALUE_DATATYPE, rValueType);
		aType.set(ORDERED, bOrdered);

		return aType;
	}

	/***************************************
	 * Creates a new relation type with an object datatype. The initial value of
	 * relations with the returned type will be NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<Object> newObjectType(
		String					sName,
		RelationTypeModifier... rModifiers)
	{
		return newRelationType(sName, Object.class, rModifiers);
	}

	/***************************************
	 * Creates a new relation type for an object type attribute with no default
	 * value. The datatype must be an enum class. To indicate that the type
	 * contains the type attribute of an object a marker relation of type {@link
	 * MetaTypes#OBJECT_TYPE_ATTRIBUTE} is set on the returned type.
	 *
	 * @see #newEnumType(String, Class, RelationTypeModifier...)
	 */
	public static <E extends Enum<E>> RelationType<E> newObjectTypeType(
		RelationTypeModifier... rModifiers)
	{
		RelationType<E> aType = newType(rModifiers);

		return aType.annotate(OBJECT_TYPE_ATTRIBUTE);
	}

	/***************************************
	 * Returns a new relation type with values that are wrapped into an instance
	 * of {@link Option}. The returned type will always return a default value
	 * of {@link Option#none()} if the relation doesn't exist in the queried
	 * object, without creating a new relation. Hence option relations can be
	 * used to prevent access to NULL values.
	 *
	 * <p>The Relatable method {@link Relatable#setOption(RelationType, Object)}
	 * can be used to set relations of such a type without an explicit creation
	 * of an {@link Option} instance.</p>
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	public static <T> RelationType<Option<T>> newOptionType(
		RelationTypeModifier... rModifiers)
	{
		return newType(r -> Option.none(), null, rModifiers);
	}

	/***************************************
	 * Creates a new relation type with an relatable object datatype. The
	 * initial value of relations with the returned type will be NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<Relatable> newRelatableType(
		String					sName,
		RelationTypeModifier... rModifiers)
	{
		return newRelationType(sName, Relatable.class, rModifiers);
	}

	/***************************************
	 * Creates a new relation type instance with an initial value of NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newRelationType(
		String					sName,
		Class<? super T>		rDatatype,
		RelationTypeModifier... rFlags)
	{
		return newRelationType(sName, rDatatype, null, rFlags);
	}

	/***************************************
	 * Creates a new relation type instance with default and initial values of
	 * NULL.
	 *
	 * @see #newRelationType(String, Class, Function, Function,
	 *      RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newRelationType(
		String								   sName,
		Class<? super T>					   rDatatype,
		Function<? super Relatable, ? super T> fInitialValue,
		RelationTypeModifier... 			   rModifiers)
	{
		return new RelationType<T>(sName, rDatatype, fInitialValue, rModifiers);
	}

	/***************************************
	 * Creates a new relation type instance with a certain initial value. The
	 * initial values for relations with the new type will be created by
	 * evaluating the initial value function argument with the argument of the
	 * relation type method {@link RelationType#initialValue(Relatable)}.
	 *
	 * <p>For some standard datatypes this class contains more specific factory
	 * methods like {@link #newStringType(String, RelationTypeModifier...)}
	 * which provide better readability for the creation of standard property
	 * types with typical default values.</p>
	 *
	 * <p>The generic types of the datatype parameter and of the return value of
	 * the initial value function have been relaxed to '? super T' so that this
	 * method can also be used for relation types that reference generic types.
	 * Without this widening generic types in the relation type declaration
	 * would not be valid when using the (base) class literal. On the other hand
	 * this means that it is possible to use a wrong type in the method call,
	 * thus creating a type that will probably cause runtime exceptions when
	 * used. Therefore this method should be used cautiously.</p>
	 *
	 * @param  sName         The name of the new relation type
	 * @param  rDatatype     The datatype of relation targets with the type
	 * @param  fDefaultValue The default value function
	 * @param  fInitialValue The initial value function
	 * @param  rModifiers    The optional relation type modifiers
	 *
	 * @return A new relation type instance
	 */
	public static <T> RelationType<T> newRelationType(
		String								   sName,
		Class<? super T>					   rDatatype,
		Function<? super Relatable, ? super T> fDefaultValue,
		Function<? super Relatable, ? super T> fInitialValue,
		RelationTypeModifier... 			   rModifiers)
	{
		return new RelationType<T>(
			sName,
			rDatatype,
			fDefaultValue,
			fInitialValue,
			rModifiers);
	}

	/***************************************
	 * Creates a partially initialized relation type with a {@link Set}
	 * datatype. Depending on the boolean parameter the initial value will
	 * either be an instance of {@link LinkedHashSet} or of {@link HashSet}.
	 *
	 * @param  bOrdered   TRUE for a set that preserves the order in which
	 *                    elements are added
	 * @param  rModifiers The optional type modifiers
	 *
	 * @return A new relation type for relations to sets
	 */
	@SuppressWarnings({ "boxing" })
	public static <T> RelationType<Set<T>> newSetType(
		boolean					bOrdered,
		RelationTypeModifier... rModifiers)
	{
		Class<?> rSetClass = bOrdered ? LinkedHashSet.class : HashSet.class;

		@SuppressWarnings("unchecked")
		Function<Object, Set<T>> fInitialValue =
			newInstanceOf((Class<Set<T>>) rSetClass);

		RelationType<Set<T>> aType = newType(null, fInitialValue, rModifiers);

		aType.annotate(ORDERED, bOrdered);

		return aType;
	}

	/***************************************
	 * Creates a new relation type with a {@link Set} datatype. The boolean
	 * parameters control whether the referenced set will be ordered (i.e. an
	 * instance of {@link LinkedHashSet} instead of {@link HashSet}) and if a
	 * default value (an empty set) will be created on first access.
	 *
	 * <p>The datatype of the set elements is stored as a relation with the meta
	 * relation type {@link MetaTypes#ELEMENT_DATATYPE} on the new type.</p>
	 *
	 * @param  sName         The name of the new type
	 * @param  rElementType  The datatype of the set elements
	 * @param  bInitialValue TRUE if the returned relation type has an initial
	 *                       value, FALSE to return NULL if no relation is set
	 * @param  bOrdered      TRUE for a set that preserves the order in which
	 *                       elements are added
	 * @param  rModifiers    The optional type modifiers
	 *
	 * @return A new relation type for set relations
	 */
	@SuppressWarnings("boxing")
	public static <T> RelationType<Set<T>> newSetType(
		String					sName,
		Class<? super T>		rElementType,
		boolean					bInitialValue,
		boolean					bOrdered,
		RelationTypeModifier... rModifiers)
	{
		Function<Object, Set<T>> fInitialValue = null;

		if (bInitialValue)
		{
			Class<?> rSetClass = bOrdered ? LinkedHashSet.class : HashSet.class;

			@SuppressWarnings("unchecked")
			Class<Set<T>> rDefaultValueClass = (Class<Set<T>>) rSetClass;

			fInitialValue = newInstanceOf(rDefaultValueClass);
		}

		RelationType<Set<T>> aType =
			newRelationType(
				sName,
				(Class<? super Set<T>>) Set.class,
				fInitialValue,
				rModifiers);

		aType.set(ELEMENT_DATATYPE, rElementType);
		aType.set(ORDERED, bOrdered);

		return aType;
	}

	/***************************************
	 * Creates a new partially initialized relation type with an string
	 * datatype.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	public static RelationType<String> newStringType(
		RelationTypeModifier... rModifiers)
	{
		return newStringType(null, rModifiers);
	}

	/***************************************
	 * Creates a new relation type with a string datatype. The initial value of
	 * relations with the returned type will be NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<String> newStringType(
		String					sName,
		RelationTypeModifier... rModifiers)
	{
		return newRelationType(sName, String.class, rModifiers);
	}

	/***************************************
	 * Creates a new partially initialized relation type that will only be valid
	 * after a call to {@link #init(Class...)} from the static initializer of
	 * the defining class.
	 *
	 * @param  rModifiers The relation type modifiers
	 *
	 * @return The new uninitialized relation type
	 */
	public static <T> RelationType<T> newType(
		RelationTypeModifier... rModifiers)
	{
		return newType(null, null, rModifiers);
	}

	/***************************************
	 * Creates a new partially initialized relation type that will only be valid
	 * after a call to {@link #init(Class...)} from the static initializer of
	 * the defining class.
	 *
	 * @param  rFlag      A flag annotation type to be set on the new type
	 * @param  rModifiers The relation type modifiers
	 *
	 * @return The new uninitialized relation type
	 */
	public static <T> RelationType<T> newType(
		RelationType<Boolean>   rFlag,
		RelationTypeModifier... rModifiers)
	{
		return RelationTypes.<T>newType(rModifiers).annotate(rFlag);
	}

	/***************************************
	 * Creates a new partially initialized relation type that will only be valid
	 * after a call to {@link #init(Class...)} from the static initializer of
	 * the defining class.
	 *
	 * @param  fInitialValue A function that returns the initial value for
	 *                       relations of this type
	 * @param  rModifiers    The relation type modifiers
	 *
	 * @return The new uninitialized relation type
	 */
	public static <T> RelationType<T> newType(
		Function<? super Relatable, ? super T> fInitialValue,
		RelationTypeModifier... 			   rModifiers)
	{
		return newType(null, fInitialValue, rModifiers);
	}

	/***************************************
	 * Creates a new partially initialized relation type that will only be valid
	 * after a call to {@link #init(Class...)} from the static initializer of
	 * the defining class.
	 *
	 * @param  fDefaultValue The default value
	 * @param  fInitialValue A function that returns the initial value for
	 *                       relations of this type
	 * @param  rModifiers    The relation type modifiers
	 *
	 * @return The new uninitialized relation type
	 */
	public static <T> RelationType<T> newType(
		Function<? super Relatable, ? super T> fDefaultValue,
		Function<? super Relatable, ? super T> fInitialValue,
		RelationTypeModifier... 			   rModifiers)
	{
		return new RelationType<T>(
			RelationType.INIT_TYPE,
			null,
			fDefaultValue,
			fInitialValue,
			rModifiers);
	}

	/***************************************
	 * Initializes a certain relation type from informations retrieved from it's
	 * declaring field.
	 *
	 * @param rRelationType The relation type
	 * @param sName         The name of the type
	 * @param rTargetType   rField The (possibly generic) type information
	 */
	@SuppressWarnings("unchecked")
	private static <T> void initRelationType(RelationType<T> rRelationType,
											 String			 sName,
											 Type			 rTargetType)
	{
		Class<T> rDatatype = (Class<T>) ReflectUtil.getRawType(rTargetType);

		if (rTargetType instanceof ParameterizedType)
		{
			ParameterizedType rType		 = (ParameterizedType) rTargetType;
			Type[]			  rElemTypes = rType.getActualTypeArguments();

			if (Collection.class.isAssignableFrom(rDatatype))
			{
				rRelationType.set(
					ELEMENT_DATATYPE,
					ReflectUtil.getRawType(rElemTypes[0]));
			}
			else if (Map.class.isAssignableFrom(rDatatype))
			{
				rRelationType.set(
					KEY_DATATYPE,
					ReflectUtil.getRawType(rElemTypes[0]));
				rRelationType.set(
					VALUE_DATATYPE,
					ReflectUtil.getRawType(rElemTypes[1]));
			}
		}

		Action<RelationType<?>> fInitAction =
			rRelationType.get(RELATION_TYPE_INIT_ACTION);

		rRelationType.deleteRelation(RELATION_TYPE_INIT_ACTION);

		rRelationType.init(sName, rDatatype, fInitAction);
	}

	/***************************************
	 * Initializes a final static field that refers to a relation type.
	 *
	 * @param rField          The final static relation type field
	 * @param rRelationType   The relation type referenced by the field
	 * @param sClassNamespace The class relation type namespace
	 */
	private static void initRelationTypeField(Field			  rField,
											  RelationType<?> rRelationType,
											  String		  sClassNamespace)
	{
		String sFieldName = rField.getName();

		if (rRelationType == null)
		{
			throw new IllegalArgumentException(
				"Unitialized relation type " +
				sFieldName);
		}

		Class<?> rDeclaringClass = rField.getDeclaringClass();
		String   sTypeName		 = rRelationType.getName();
		String   sTypeNamespace  = rRelationType.get(RELATION_TYPE_NAMESPACE);

		Type rTargetType =
			((ParameterizedType) rField.getGenericType())
			.getActualTypeArguments()[0];

		if (sTypeName == RelationType.INIT_TYPE)
		{
			String sName;

			if (sTypeNamespace != null)
			{
				sName = sTypeNamespace + "." + sFieldName;
			}
			else
			{
				sName = sClassNamespace + sFieldName;
			}

			initRelationType(rRelationType, sName, rTargetType);
		}
		else
		{
			assert ReflectUtil.getRawType(rTargetType) ==
				   rRelationType.getTargetType() : String.format(
				"Invalid target type for RelationType %s: %s (expected: %s)",
				sTypeName,
				rRelationType.getTargetType(),
				rField.getType());

			if (!rField.isAnnotationPresent(NoRelationNameCheck.class))
			{
				// check if field and type names match
				assert sFieldName.equals(rRelationType.getSimpleName()) : String
					   .format(
					"RelationType name mismatch for %s.%s: %s",
					rDeclaringClass.getName(),
					sFieldName,
					sTypeName);
			}

			if (sTypeNamespace != null && !sTypeName.startsWith(sTypeNamespace))
			{
				initRelationType(
					rRelationType,
					sTypeNamespace + "." + sFieldName,
					rTargetType);
			}
		}

		// only set declaring class on first declaration to not override it in
		// relation type references in other classes
		if (!rRelationType.hasRelation(DECLARING_CLASS))
		{
			rRelationType.annotate(DECLARING_CLASS, rDeclaringClass);
		}
	}
}
