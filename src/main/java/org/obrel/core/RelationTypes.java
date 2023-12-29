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
import org.obrel.core.Annotations.NoRelationNameCheck;
import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.type.MetaTypes;

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

import static de.esoco.lib.expression.Functions.value;
import static de.esoco.lib.expression.ReflectionFuntions.newInstanceOf;
import static org.obrel.type.MetaTypes.OBJECT_TYPE_ATTRIBUTE;
import static org.obrel.type.MetaTypes.ORDERED;

/**
 * This is a factory class to create relation types with different target data
 * types and parameters.
 *
 * @author eso
 */
public class RelationTypes {

	// types declared because they are needed during relation type
	// initialization

	/**
	 * @see MetaTypes#DECLARING_CLASS
	 */
	public static final RelationType<Class<?>> DECLARING_CLASS =
		new RelationType<>("DECLARING_CLASS", Class.class);

	/**
	 * @see MetaTypes#RELATION_TYPE_NAMESPACE
	 */
	public static final RelationType<String> RELATION_TYPE_NAMESPACE =
		new RelationType<>("RELATION_TYPE_NAMESPACE", String.class);

	/**
	 * @see MetaTypes#RELATION_TYPE_INIT_ACTION
	 */
	public static final RelationType<Action<RelationType<?>>>
		RELATION_TYPE_INIT_ACTION =
		new RelationType<>("RELATION_TYPE_INIT_ACTION", Action.class);

	/**
	 * @see MetaTypes#ELEMENT_DATATYPE
	 */
	public static final RelationType<Class<?>> ELEMENT_DATATYPE =
		new RelationType<>("ELEMENT_DATATYPE", Class.class);

	/**
	 * @see MetaTypes#KEY_DATATYPE
	 */
	public static final RelationType<Class<?>> KEY_DATATYPE =
		new RelationType<>("KEY_DATATYPE", Class.class);

	/**
	 * @see MetaTypes#VALUE_DATATYPE
	 */
	public static final RelationType<Class<?>> VALUE_DATATYPE =
		new RelationType<>("VALUE_DATATYPE", Class.class);

	// needs to be declared last as List type references ELEMENT_DATATYPE

	/**
	 * @see MetaTypes#DECLARED_RELATION_TYPES
	 */
	public static final RelationType<List<RelationType<?>>>
		DECLARED_RELATION_TYPES =
		newRelationType("DECLARED_RELATION_TYPES", List.class,
			RelationTypeModifier.FINAL);

	/**
	 * Returns the relation type namespace for a certain class. This will
	 * either
	 * be the value of the annotation {@link RelationTypeNamespace} or if that
	 * is not present, the name of the class itself, including it's own
	 * namespace.
	 *
	 * @param datatype The class to determine the relation type namespace of
	 * @return The relation type namespace
	 */
	public static String getRelationTypeNamespace(Class<?> datatype) {
		String classNamespace;

		if (datatype.isAnnotationPresent(RelationTypeNamespace.class)) {
			classNamespace =
				datatype.getAnnotation(RelationTypeNamespace.class).value();
		} else {
			classNamespace = datatype.getName();
		}

		return classNamespace;
	}

	/**
	 * Initializes the relation type constants of certain classes.
	 *
	 * @param datatypees The classes to initialize
	 */
	public static void init(Class<?>... datatypees) {
		for (Class<?> datatype : datatypees) {
			initRelationTypesOf(datatype);
		}
	}

	/**
	 * Initializes a certain relation type from informations retrieved from
	 * it's
	 * declaring field.
	 *
	 * @param relationType The relation type
	 * @param name         The name of the type
	 * @param targetType   field The (possibly generic) type information
	 */
	@SuppressWarnings("unchecked")
	private static <T> void initRelationType(RelationType<T> relationType,
		String name, Type targetType) {
		Class<T> datatype = (Class<T>) ReflectUtil.getRawType(targetType);

		if (targetType instanceof ParameterizedType) {
			ParameterizedType type = (ParameterizedType) targetType;
			Type[] elemTypes = type.getActualTypeArguments();

			if (Collection.class.isAssignableFrom(datatype)) {
				relationType.set(ELEMENT_DATATYPE,
					ReflectUtil.getRawType(elemTypes[0]));
			} else if (Map.class.isAssignableFrom(datatype)) {
				relationType.set(KEY_DATATYPE,
					ReflectUtil.getRawType(elemTypes[0]));
				relationType.set(VALUE_DATATYPE,
					ReflectUtil.getRawType(elemTypes[1]));
			}
		}

		Action<RelationType<?>> initAction =
			relationType.get(RELATION_TYPE_INIT_ACTION);

		relationType.deleteRelation(RELATION_TYPE_INIT_ACTION);

		relationType.init(name, datatype, initAction);
	}

	/**
	 * Initializes a final static field that refers to a relation type.
	 *
	 * @param field          The final static relation type field
	 * @param relationType   The relation type referenced by the field
	 * @param classNamespace The class relation type namespace
	 */
	private static void initRelationTypeField(Field field,
		RelationType<?> relationType, String classNamespace) {
		String fieldName = field.getName();

		if (relationType == null) {
			throw new IllegalArgumentException(
				"Unitialized relation type " + fieldName);
		}

		Class<?> declaringClass = field.getDeclaringClass();
		String typeName = relationType.getName();
		String typeNamespace = relationType.get(RELATION_TYPE_NAMESPACE);

		Type targetType =
			((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

		if (typeName == RelationType.INIT_TYPE) {
			String name;

			if (typeNamespace != null) {
				name = typeNamespace + "." + fieldName;
			} else {
				name = classNamespace + fieldName;
			}

			initRelationType(relationType, name, targetType);
		} else {
			assert ReflectUtil.getRawType(targetType) ==
				relationType.getTargetType() : String.format(
				"Invalid target type for RelationType %s: %s (expected: %s)",
				typeName, relationType.getTargetType(), field.getType());

			assert field.isAnnotationPresent(NoRelationNameCheck.class) ||
				fieldName.equals(relationType.getSimpleName()) :
				String.format("RelationType name mismatch for %s.%s: %s",
					declaringClass.getName(), fieldName, typeName);

			if (typeNamespace != null && !typeName.startsWith(typeNamespace)) {
				initRelationType(relationType, typeNamespace + "." + fieldName,
					targetType);
			}
		}

		// only set declaring class on first declaration to not override it in
		// relation type references in other classes
		if (!relationType.hasRelation(DECLARING_CLASS)) {
			relationType.annotate(DECLARING_CLASS, declaringClass);
		}
	}

	/**
	 * Initializes the relation type constants of a certain class.
	 *
	 * @param datatype The class to initialize
	 */
	public static void initRelationTypesOf(Class<?> datatype) {
		List<Field> fields = ReflectUtil.getAllFields(datatype);
		String classNamespace = getRelationTypeNamespace(datatype);

		List<RelationType<?>> declaredTypes = new ArrayList<>();

		if (classNamespace.length() > 0) {
			classNamespace += ".";
		}

		for (Field field : fields) {
			try {
				int modifiers = field.getModifiers();

				if (RelationType.class.isAssignableFrom(field.getType())) {
					if (Modifier.isStatic(modifiers)) {
						field.setAccessible(true);

						RelationType<?> relationType =
							(RelationType<?>) field.get(null);

						if (Modifier.isFinal(modifiers)) {
							initRelationTypeField(field, relationType,
								classNamespace);

							if (!relationType.hasModifier(
								RelationTypeModifier.PRIVATE)) {
								declaredTypes.add(relationType);
							}
						} else
							assert relationType == null ||
								relationType.getName() !=
									RelationType.INIT_TYPE :
								"Relation type not final static: " +
									field.getName();
					}
				}
			} catch (Exception e) {
				String message = String.format("Access to %s.%s failed",
					field.getDeclaringClass().getSimpleName(),
					field.getName());

				throw new IllegalArgumentException(message, e);
			}
		}

		if (!declaredTypes.isEmpty()) {
			Relatable datatypeRelatable =
				ObjectRelations.getRelatable(datatype);

			if (!datatypeRelatable.hasRelation(DECLARED_RELATION_TYPES)) {
				datatypeRelatable.set(DECLARED_RELATION_TYPES,
					Collections.unmodifiableList(declaredTypes));
			}
		}
	}

	/**
	 * Creates a new relation type with a boolean datatype. The initial
	 * value of
	 * relations with the returned type will be FALSE.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<Boolean> newBooleanType(String name,
		RelationTypeModifier... modifiers) {
		return newRelationType(name, Boolean.class, value(Boolean.FALSE),
			modifiers);
	}

	/**
	 * Creates a new relation type with a class datatype. The initial value of
	 * relations with the returned type will be NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<Class<? extends T>> newClassType(String name,
		RelationTypeModifier... modifiers) {
		return newRelationType(name, Class.class, modifiers);
	}

	/**
	 * Creates a new relation type with a {@link Date} datatype. The initial
	 * value of relations with the returned type will be NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<Date> newDateType(String name,
		RelationTypeModifier... modifiers) {
		return newRelationType(name, Date.class, modifiers);
	}

	/**
	 * Creates a new partially initialized relation type with a
	 * {@link BigDecimal} datatype and an initial value of zero.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	@SuppressWarnings("boxing")
	public static RelationType<BigDecimal> newDecimalType(
		RelationTypeModifier... modifiers) {
		return newInitialValueType(BigDecimal.ZERO, modifiers);
	}

	/**
	 * Creates a new relation type with a certain default value. See the
	 * generic
	 * variant #newDefaultValueType(Function, RelationTypeModifier...) for more
	 * information.
	 *
	 * @param defaultValue The default value of relations of this type
	 * @param modifiers    The type modifiers
	 * @see #newDefaultValueType(Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newDefaultValueType(T defaultValue,
		RelationTypeModifier... modifiers) {
		return newDefaultValueType(r -> defaultValue, modifiers);
	}

	/**
	 * Creates a new relation type with a default value function. <b>
	 * IMPORTANT</b>: default values are returned if no relation with the type
	 * exists, but querying them will not create a new relation. That means
	 * that
	 * checks for relation existence will yield FALSE. To make sure a relation
	 * exists use {@link #newInitialValueType(Object, RelationTypeModifier...)}
	 * instead.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newDefaultValueType(
		Function<? super Relatable, ? super T> defaultValue,
		RelationTypeModifier... modifiers) {
		return newType(defaultValue, null, modifiers);
	}

	/**
	 * Creates a new relation type with an enum datatype and no default value.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static <E extends Enum<E>> RelationType<E> newEnumType(String name,
		Class<E> enumType, RelationTypeModifier... modifiers) {
		return newRelationType(name, enumType, modifiers);
	}

	/**
	 * Creates a new relation type with an enum datatype and a certain default
	 * value that also defines the datatype.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> RelationType<E> newEnumType(String name,
		E defaultValue, RelationTypeModifier... modifiers) {
		return newRelationType(name, (Class<E>) defaultValue.getClass(),
			value(defaultValue), modifiers);
	}

	/**
	 * Creates a new partially initialized relation type with a {@link Boolean}
	 * datatype and an initial value of of FALSE.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	public static RelationType<Boolean> newFlagType(
		RelationTypeModifier... modifiers) {
		return newInitialValueType(Boolean.FALSE, modifiers);
	}

	/**
	 * Creates a new relation type with a certain initial value. The value will
	 * be set and returned on the first get of an unset relation of this type.
	 *
	 * @see #newInitialValueType(Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newInitialValueType(T initialValue,
		RelationTypeModifier... modifiers) {
		return newInitialValueType(value(initialValue), modifiers);
	}

	/**
	 * Creates a new relation type with an initial value function. The value
	 * will be set and returned on the first get of an unset relation of this
	 * type.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newInitialValueType(
		Function<? super Relatable, ? super T> initialValue,
		RelationTypeModifier... modifiers) {
		return newType(initialValue, modifiers);
	}

	/**
	 * Creates a new partially initialized relation type with an integer
	 * datatype and an initial value of zero.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	@SuppressWarnings("boxing")
	public static RelationType<Integer> newIntType(
		RelationTypeModifier... modifiers) {
		return newInitialValueType(0, modifiers);
	}

	/**
	 * Creates a new partially initialized relation type with an integer
	 * datatype and a certain initial value.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	@SuppressWarnings("boxing")
	public static RelationType<Integer> newIntType(int defaultValue,
		RelationTypeModifier... modifiers) {
		return newType(value(defaultValue), modifiers);
	}

	/**
	 * Creates a new relation type with an integer datatype. The initial value
	 * of relations with the returned type will be zero.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<Integer> newIntegerType(String name,
		RelationTypeModifier... modifiers) {
		return newIntegerType(name, 0, modifiers);
	}

	/**
	 * Creates a new relation type with an integer datatype and a certain
	 * default value.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	@SuppressWarnings("boxing")
	public static RelationType<Integer> newIntegerType(String name,
		int defaultValue, RelationTypeModifier... modifiers) {
		return newRelationType(name, Integer.class, value(defaultValue),
			modifiers);
	}

	/**
	 * Creates a partially initialized relation type with a {@link List}
	 * datatype that will have an instance of {@link ArrayList} as it's initial
	 * value.
	 *
	 * @param modifiers The optional type modifiers
	 * @return A new relation type for relations to lists
	 */
	public static <T> RelationType<List<T>> newListType(
		RelationTypeModifier... modifiers) {
		@SuppressWarnings("unchecked")
		Class<List<T>> listClass = (Class<List<T>>) (Class<?>) ArrayList.class;

		return newType(null, newInstanceOf(listClass), modifiers);
	}

	/**
	 * Creates a new relation type with a {@link List} datatype. The initial
	 * value of relations with the returned type will be a new instance of
	 * {@link ArrayList}.
	 *
	 * <p>
	 * The datatype of the list elements is stored as a relation with the meta
	 * relation type {@link MetaTypes#ELEMENT_DATATYPE} on the new type.
	 * </p>
	 *
	 * @param name        The name of the new type
	 * @param elementType The datatype of the list elements
	 * @param modifiers   The optional type modifiers
	 * @return A new relation type for list relations
	 */
	@SuppressWarnings("unchecked")
	public static <T> RelationType<List<T>> newListType(String name,
		Class<? super T> elementType, RelationTypeModifier... modifiers) {
		Class<List<T>> listClass = (Class<List<T>>) (Class<?>) ArrayList.class;

		RelationType<List<T>> type =
			newRelationType(name, List.class, newInstanceOf(listClass),
				modifiers);

		if (elementType != null) {
			type.set(ELEMENT_DATATYPE, elementType);
		}

		return type;
	}

	/**
	 * Creates a new partially initialized relation type with a long datatype
	 * and an initial value of zero.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	@SuppressWarnings("boxing")
	public static RelationType<Long> newLongType(
		RelationTypeModifier... modifiers) {
		return newInitialValueType(0L, modifiers);
	}

	/**
	 * Creates a partially initialized relation type with a {@link Map}
	 * datatype. Depending on the boolean parameter the initial value will
	 * either be an instance of {@link LinkedHashMap} or of {@link HashMap}.
	 *
	 * @param ordered   TRUE for a map that preserves the order in which
	 *                  elements are added
	 * @param modifiers The optional type modifiers
	 * @return A new relation type for relations to maps
	 */
	@SuppressWarnings({ "boxing" })
	public static <K, V> RelationType<Map<K, V>> newMapType(boolean ordered,
		RelationTypeModifier... modifiers) {
		Class<?> mapClass = ordered ? LinkedHashMap.class : HashMap.class;

		@SuppressWarnings("unchecked")
		Function<Object, Map<K, V>> initialValue =
			newInstanceOf((Class<Map<K, V>>) mapClass);

		RelationType<Map<K, V>> type = newType(null, initialValue, modifiers);

		type.annotate(ORDERED, ordered);

		return type;
	}

	/**
	 * Creates a new relation type with a {@link Map} datatype. Depending on
	 * the
	 * value of the boolean argument the initial value of relations with the
	 * returned type will either be a new instance of {@link HashMap}
	 * (FALSE) or
	 * {@link LinkedHashMap} (TRUE).
	 *
	 * <p>
	 * The datatype of the keys and values in the map are stored as meta
	 * relations on the returned relation type with the meta relation types
	 * {@link MetaTypes#KEY_DATATYPE} and {@link MetaTypes#VALUE_DATATYPE}.
	 * </p>
	 *
	 * @param name         The name of the new type
	 * @param keyType      The datatype of the map keys
	 * @param valueType    The datatype of the map values
	 * @param initialValue TRUE if the returned relation type has an initial
	 *                     value, FALSE to return NULL if no relation is set
	 * @param ordered      If defaultValue is TRUE, TRUE for a map that
	 *                     preserves the order in which elements are added
	 * @param modifiers    The optional type modifiers
	 * @return A new relation type for map relations
	 */
	@SuppressWarnings("boxing")
	public static <K, V> RelationType<Map<K, V>> newMapType(String name,
		Class<? super K> keyType, Class<? super V> valueType,
		boolean initialValue, boolean ordered,
		RelationTypeModifier... modifiers) {
		Function<Object, Map<K, V>> getInitialValue = null;

		if (initialValue) {
			Class<?> mapClass = ordered ? LinkedHashMap.class : HashMap.class;

			@SuppressWarnings("unchecked")
			Class<Map<K, V>> defaultValueClass = (Class<Map<K, V>>) mapClass;

			getInitialValue = newInstanceOf(defaultValueClass);
		}

		RelationType<Map<K, V>> type =
			newRelationType(name, Map.class, getInitialValue, modifiers);

		type.set(KEY_DATATYPE, keyType);
		type.set(VALUE_DATATYPE, valueType);
		type.set(ORDERED, ordered);

		return type;
	}

	/**
	 * Creates a new relation type with an object datatype. The initial
	 * value of
	 * relations with the returned type will be NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<Object> newObjectType(String name,
		RelationTypeModifier... modifiers) {
		return newRelationType(name, Object.class, modifiers);
	}

	/**
	 * Creates a new relation type for an object type attribute with no default
	 * value. The datatype must be an enum class. To indicate that the type
	 * contains the type attribute of an object a marker relation of type
	 * {@link MetaTypes#OBJECT_TYPE_ATTRIBUTE} is set on the returned type.
	 *
	 * @see #newEnumType(String, Class, RelationTypeModifier...)
	 */
	public static <E extends Enum<E>> RelationType<E> newObjectTypeType(
		RelationTypeModifier... modifiers) {
		RelationType<E> type = newType(modifiers);

		return type.annotate(OBJECT_TYPE_ATTRIBUTE);
	}

	/**
	 * Returns a new relation type with values that are wrapped into an
	 * instance
	 * of {@link Option}. The returned type will always return a default value
	 * of {@link Option#none()} if the relation doesn't exist in the queried
	 * object, without creating a new relation. Hence option relations can be
	 * used to prevent access to NULL values.
	 *
	 * <p>
	 * The Relatable method {@link Relatable#setOption(RelationType, Object)}
	 * can be used to set relations of such a type without an explicit creation
	 * of an {@link Option} instance.
	 * </p>
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	public static <T> RelationType<Option<T>> newOptionType(
		RelationTypeModifier... modifiers) {
		return newType(r -> Option.none(), null, modifiers);
	}

	/**
	 * Creates a new relation type with an relatable object datatype. The
	 * initial value of relations with the returned type will be NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<Relatable> newRelatableType(String name,
		RelationTypeModifier... modifiers) {
		return newRelationType(name, Relatable.class, modifiers);
	}

	/**
	 * Creates a new relation type instance with an initial value of NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newRelationType(String name,
		Class<? super T> datatype, RelationTypeModifier... flags) {
		return newRelationType(name, datatype, null, flags);
	}

	/**
	 * Creates a new relation type instance with default and initial values of
	 * NULL.
	 *
	 * @see #newRelationType(String, Class, Function, Function,
	 * RelationTypeModifier...)
	 */
	public static <T> RelationType<T> newRelationType(String name,
		Class<? super T> datatype,
		Function<? super Relatable, ? super T> initialValue,
		RelationTypeModifier... modifiers) {
		return new RelationType<T>(name, datatype, initialValue, modifiers);
	}

	/**
	 * Creates a new relation type instance with a certain initial value. The
	 * initial values for relations with the new type will be created by
	 * evaluating the initial value function argument with the argument of the
	 * relation type method {@link RelationType#initialValue(Relatable)}.
	 *
	 * <p>
	 * For some standard datatypes this class contains more specific factory
	 * methods like {@link #newStringType(String, RelationTypeModifier...)}
	 * which provide better readability for the creation of standard property
	 * types with typical default values.
	 * </p>
	 *
	 * <p>
	 * The generic types of the datatype parameter and of the return value of
	 * the initial value function have been relaxed to '? super T' so that this
	 * method can also be used for relation types that reference generic types.
	 * Without this widening generic types in the relation type declaration
	 * would not be valid when using the (base) class literal. On the other
	 * hand
	 * this means that it is possible to use a wrong type in the method call,
	 * thus creating a type that will probably cause runtime exceptions when
	 * used. Therefore this method should be used cautiously.
	 * </p>
	 *
	 * @param name         The name of the new relation type
	 * @param datatype     The datatype of relation targets with the type
	 * @param defaultValue The default value function
	 * @param initialValue The initial value function
	 * @param modifiers    The optional relation type modifiers
	 * @return A new relation type instance
	 */
	public static <T> RelationType<T> newRelationType(String name,
		Class<? super T> datatype,
		Function<? super Relatable, ? super T> defaultValue,
		Function<? super Relatable, ? super T> initialValue,
		RelationTypeModifier... modifiers) {
		return new RelationType<T>(name, datatype, defaultValue, initialValue,
			modifiers);
	}

	/**
	 * Creates a partially initialized relation type with a {@link Set}
	 * datatype. Depending on the boolean parameter the initial value will
	 * either be an instance of {@link LinkedHashSet} or of {@link HashSet}.
	 *
	 * @param ordered   TRUE for a set that preserves the order in which
	 *                  elements are added
	 * @param modifiers The optional type modifiers
	 * @return A new relation type for relations to sets
	 */
	@SuppressWarnings({ "boxing" })
	public static <T> RelationType<Set<T>> newSetType(boolean ordered,
		RelationTypeModifier... modifiers) {
		Class<?> setClass = ordered ? LinkedHashSet.class : HashSet.class;

		@SuppressWarnings("unchecked")
		Function<Object, Set<T>> initialValue =
			newInstanceOf((Class<Set<T>>) setClass);

		RelationType<Set<T>> type = newType(null, initialValue, modifiers);

		type.annotate(ORDERED, ordered);

		return type;
	}

	/**
	 * Creates a new relation type with a {@link Set} datatype. The boolean
	 * parameters control whether the referenced set will be ordered (i.e. an
	 * instance of {@link LinkedHashSet} instead of {@link HashSet}) and if a
	 * default value (an empty set) will be created on first access.
	 *
	 * <p>
	 * The datatype of the set elements is stored as a relation with the meta
	 * relation type {@link MetaTypes#ELEMENT_DATATYPE} on the new type.
	 * </p>
	 *
	 * @param name         The name of the new type
	 * @param elementType  The datatype of the set elements
	 * @param initialValue TRUE if the returned relation type has an initial
	 *                     value, FALSE to return NULL if no relation is set
	 * @param ordered      TRUE for a set that preserves the order in which
	 *                     elements are added
	 * @param modifiers    The optional type modifiers
	 * @return A new relation type for set relations
	 */
	@SuppressWarnings("boxing")
	public static <T> RelationType<Set<T>> newSetType(String name,
		Class<? super T> elementType, boolean initialValue, boolean ordered,
		RelationTypeModifier... modifiers) {
		Function<Object, Set<T>> getInitialValue = null;

		if (initialValue) {
			Class<?> setClass = ordered ? LinkedHashSet.class : HashSet.class;

			@SuppressWarnings("unchecked")
			Class<Set<T>> defaultValueClass = (Class<Set<T>>) setClass;

			getInitialValue = newInstanceOf(defaultValueClass);
		}

		RelationType<Set<T>> type =
			newRelationType(name, Set.class, getInitialValue, modifiers);

		type.set(ELEMENT_DATATYPE, elementType);
		type.set(ORDERED, ordered);

		return type;
	}

	/**
	 * Creates a new partially initialized relation type with an string
	 * datatype.
	 *
	 * @see #newType(RelationTypeModifier...)
	 */
	public static RelationType<String> newStringType(
		RelationTypeModifier... modifiers) {
		return newStringType(null, modifiers);
	}

	/**
	 * Creates a new relation type with a string datatype. The initial value of
	 * relations with the returned type will be NULL.
	 *
	 * @see #newRelationType(String, Class, Function, RelationTypeModifier...)
	 */
	public static RelationType<String> newStringType(String name,
		RelationTypeModifier... modifiers) {
		return newRelationType(name, String.class, modifiers);
	}

	/**
	 * Creates a new partially initialized relation type that will only be
	 * valid
	 * after a call to {@link #init(Class...)} from the static initializer of
	 * the defining class.
	 *
	 * @param modifiers The relation type modifiers
	 * @return The new uninitialized relation type
	 */
	public static <T> RelationType<T> newType(
		RelationTypeModifier... modifiers) {
		return newType(null, null, modifiers);
	}

	/**
	 * Creates a new partially initialized relation type that will only be
	 * valid
	 * after a call to {@link #init(Class...)} from the static initializer of
	 * the defining class.
	 *
	 * @param flag      A flag annotation type to be set on the new type
	 * @param modifiers The relation type modifiers
	 * @return The new uninitialized relation type
	 */
	public static <T> RelationType<T> newType(RelationType<Boolean> flag,
		RelationTypeModifier... modifiers) {
		return RelationTypes.<T>newType(modifiers).annotate(flag);
	}

	/**
	 * Creates a new partially initialized relation type that will only be
	 * valid
	 * after a call to {@link #init(Class...)} from the static initializer of
	 * the defining class.
	 *
	 * @param initialValue A function that returns the initial value for
	 *                     relations of this type
	 * @param modifiers    The relation type modifiers
	 * @return The new uninitialized relation type
	 */
	public static <T> RelationType<T> newType(
		Function<? super Relatable, ? super T> initialValue,
		RelationTypeModifier... modifiers) {
		return newType(null, initialValue, modifiers);
	}

	/**
	 * Creates a new partially initialized relation type that will only be
	 * valid
	 * after a call to {@link #init(Class...)} from the static initializer of
	 * the defining class.
	 *
	 * @param defaultValue The default value
	 * @param initialValue A function that returns the initial value for
	 *                     relations of this type
	 * @param modifiers    The relation type modifiers
	 * @return The new uninitialized relation type
	 */
	public static <T> RelationType<T> newType(
		Function<? super Relatable, ? super T> defaultValue,
		Function<? super Relatable, ? super T> initialValue,
		RelationTypeModifier... modifiers) {
		return new RelationType<T>(RelationType.INIT_TYPE, null, defaultValue,
			initialValue, modifiers);
	}
}
