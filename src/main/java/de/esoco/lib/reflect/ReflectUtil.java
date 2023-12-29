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
package de.esoco.lib.reflect;

import de.esoco.lib.collection.BidirectionalMap;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains static utility methods for reflection.
 *
 * @author eso
 */
public final class ReflectUtil {
	/**
	 * Constant to signal no-argument methods
	 */
	public static final Class<?>[] NO_ARGS = new Class<?>[0];

	private static final String THIS_CLASS_NAME = ReflectUtil.class.getName();

	private static final String THREAD_CLASS_NAME = Thread.class.getName();

	private static final Map<Class<?>, Class<?>> interfaceImplementationMap =
		new HashMap<Class<?>, Class<?>>();

	private static final BidirectionalMap<Class<?>, Class<?>>
		wrapperPrimitiveMap = new BidirectionalMap<Class<?>, Class<?>>();

	static {
		wrapperPrimitiveMap.put(Boolean.class, boolean.class);
		wrapperPrimitiveMap.put(Character.class, char.class);
		wrapperPrimitiveMap.put(Byte.class, byte.class);
		wrapperPrimitiveMap.put(Short.class, short.class);
		wrapperPrimitiveMap.put(Integer.class, int.class);
		wrapperPrimitiveMap.put(Long.class, long.class);
		wrapperPrimitiveMap.put(Float.class, float.class);
		wrapperPrimitiveMap.put(Double.class, double.class);

		registerImplementation(List.class, ArrayList.class);
		registerImplementation(Set.class, HashSet.class);
		registerImplementation(Map.class, HashMap.class);
	}

	/**
	 * This is a helper method to implement the assertion of the correct field
	 * names for constant declarations in an enumeration pattern. This method
	 * will iterate through the final static fields (i.e. constants) of a
	 * certain class that are instances of a certain base class and checks
	 * whether the field names are equal to the internal name strings of the
	 * instance. To access the name strings the constant class must provide a
	 * public no-argument method with the given name.
	 *
	 * <p>
	 * This method will not check the superclasses of the declaring class. The
	 * names of the constants may include namespaces which will be ignored in
	 * the name comparisons.
	 * </p>
	 *
	 * <p>
	 * This method is intended to be used at development time and therefore
	 * uses
	 * assertions to perform it's checks. It returns a boolean value which will
	 * always be TRUE so that it can be invoked in an assert statement
	 * (which is
	 * the recommended way to use it). By doing so the constant assertions will
	 * only be performed if assertions are enabled and not in production
	 * code. A
	 * typical invocation should be done in a static initialization block after
	 * the constant declarations and would look like this:
	 * </p>
	 * <code>assert ReflectUtil.assertConstantDeclarations(AppConstants.class,
	 * TestEnum.class, "getName", false);</code>
	 *
	 * @param declaringClass     The class to check the declared fields of
	 * @param constantClass      The (base) class of the constants to check
	 * @param nameMethod         The method to query the constant names with
	 * @param allAccessModifiers TRUE to check constants with all access
	 *                           modifiers, FALSE for public constants only
	 * @return Always TRUE
	 */
	public static <T> boolean assertConstantDeclarations(
		Class<?> declaringClass, Class<? super T> constantClass,
		String nameMethod, boolean allAccessModifiers) {
		collectConstants(declaringClass, constantClass, nameMethod,
			allAccessModifiers, false, true);

		return true;
	}

	/**
	 * Checks whether a class member is publicly accessible. If not the method
	 * {@link AccessibleObject#setAccessible(boolean)} will be invoked on it to
	 * make it accessible.
	 *
	 * @param member The member to check
	 * @return The input member to allow call concatenation
	 */
	public static <T extends Member> T checkAccessible(T member) {
		if (member instanceof AccessibleObject) {
			Class<?> membetype = member.getDeclaringClass();

			if (!Modifier.isPublic(member.getModifiers()) ||
				!Modifier.isPublic(membetype.getModifiers())) {
				((AccessibleObject) member).setAccessible(true);
			}
		}

		return member;
	}

	/**
	 * Collects all constants of a certain type from a declaring class and
	 * returns them in a list. The constants must be declared as public static
	 * final and must not be NULL. Non-public fields will be ignored and the
	 * static final modifiers will be checked with assertions so that errors
	 * can
	 * be detected at development time. If a constant is NULL or an exception
	 * occurs during the reflective access to a field an {@link AssertionError}
	 * will be thrown. Because of these checks no fields of the constant type
	 * must exist that are not static final and/or are NULL.
	 *
	 * <p>
	 * Because constants are often used with a name field that must match the
	 * name of the constant instance this method provides a way to assert the
	 * equality of the constant name and the name field. To compare the names
	 * with an assertion the third argument must be the name of a public
	 * no-argument method that returns the value of the name field (e.g.
	 * "getName"). To omit this check it must be NULL.
	 * </p>
	 *
	 * <p>
	 * This method is intended to be used during the initialization of classes
	 * that want to keep a list of their declared constants, and should not be
	 * used in instance methods. Therefore using assertions to signal
	 * uncritical
	 * errors is sufficient. If a class needs to verify the constants only
	 * without the need to access a list of them it can use the method
	 * {@link #assertConstantDeclarations(Class, Class, String, boolean)}
	 * instead.
	 * </p>
	 *
	 * @param declaringClass      The class that declares the constants to
	 *                            collect
	 * @param constantClass       The datatype (super)class of the constants
	 *                            (relaxed to '? super T' to prevent conversion
	 *                            conflicts with generic types)
	 * @param checkNameMethodName The name of the method to check the instance
	 *                            names with or NULL to omit this check
	 * @param allAccessModifiers  TRUE to check constants with all access
	 *                            modifiers, FALSE for public constants only
	 * @param withSuperclasses    TRUE to collect all fields, including that of
	 *                            superclasses; FALSE to collect the fields of
	 *                            the given declaring class only
	 * @param allowNamespace      TRUE to allow the instance names to begin
	 *                               with
	 *                            a namespace string, separated by the '.' char
	 * @return A list of all constants of the given base type that are declared
	 * in the declaring class
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> collectConstants(Class<?> declaringClass,
		Class<? super T> constantClass, String checkNameMethodName,
		boolean allAccessModifiers, boolean withSuperclasses,
		boolean allowNamespace) {
		List<T> constants = new ArrayList<T>();
		List<Field> fields;

		try {
			if (withSuperclasses) {
				fields = allAccessModifiers ?
				         getAllFields(declaringClass) :
				         Arrays.asList(declaringClass.getFields());
			} else {
				fields = Arrays.asList(declaringClass.getDeclaredFields());
			}

			Method checkNameMethod = checkNameMethodName != null ?
			                         constantClass.getMethod(
				                         checkNameMethodName) :
			                         null;

			for (Field field : fields) {
				int modifiers = field.getModifiers();

				if (Modifier.isStatic(modifiers) &&
					(allAccessModifiers || Modifier.isPublic(modifiers)) &&
					constantClass.isAssignableFrom(field.getType())) {
					if (allAccessModifiers) {
						checkAccessible(field);
					}

					String fieldName = field.getName();
					Object fieldValue = field.get(null);

					assert Modifier.isFinal(modifiers) :
						"Instance not final static: " + fieldName;

					if (checkNameMethod != null) {
						String name =
							checkNameMethod.invoke(fieldValue).toString();

						if (allowNamespace) {
							// remove namespace if exists
							name = name.substring(name.lastIndexOf('.') + 1);
						}

						// check if enum string and instance names match
						assert fieldName.equals(name) : "Name mismatch of " +
							declaringClass.getSimpleName() + " constant " +
							fieldName + " (wrong name: " + name + ")";
					}

					if (fieldValue != null) {
						constants.add((T) fieldValue);
					} else {
						throw new AssertionError("Field is NULL: " + field);
					}
				}
			}
		} catch (Exception e) {
			String message =
				String.format("Could not collect %s constants from %s",
					constantClass.getSimpleName(),
					declaringClass.getSimpleName());

			throw new IllegalArgumentException(message, e);
		}

		return constants;
	}

	/**
	 * Tries to find an arbitrary public method with a certain name in the
	 * given
	 * class. Returns the first method (in the classes' declaration order) with
	 * the given name or NULL if no such method exists.
	 *
	 * @param type       The class in which to search for the method
	 * @param methodName The name of the public method to search for
	 * @return The corresponding method instance or NULL if none could be found
	 */
	public static Method findAnyPublicMethod(Class<?> type,
		String methodName) {
		for (Method method : type.getMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}

		return null;
	}

	/**
	 * Tries to find a matching method with certain parameters in a list of
	 * methods. The method collection argument will be searched for methods
	 * that
	 * have a parameter list that is capable of accepting arguments of the
	 * types
	 * contained in the argTypes argument. That means that each parameter of a
	 * matching method must be of a type that is either the same as, or is a
	 * supertype of the corresponding type in the argTypes array.
	 *
	 * <p>
	 * If the argTypes array contains NULL values these will be considered as
	 * matching any parameter type at the same position. The application is
	 * then
	 * responsible of providing the correct argument value (i.e. NULL or of a
	 * type matching the method's parameter) on method invocation.
	 * </p>
	 *
	 * <p>
	 * This method will not distinguish between multiple variants of the method
	 * with compatible parameter lists. It will choose and return the first
	 * compatible method that can be found in the collection argument.
	 * </p>
	 *
	 * @param methods  A collection containing the methods to search
	 * @param argTypes An array containing the classes of the argument types
	 *                 (may either be NULL, NO_ARGS, or empty for no-argument
	 *                 methods)
	 * @return The corresponding method instance or NULL if none could be found
	 */
	public static Method findMethod(Collection<Method> methods,
		Class<?>... argTypes) {
		if (argTypes == null) {
			argTypes = NO_ARGS;
		}

		for (Method m : methods) {
			Class<?>[] paramTypes = m.getParameterTypes();

			if (paramTypes.length == argTypes.length) {
				boolean match = true;
				int i = 0;

				while (match && i < paramTypes.length) {
					match = argTypes[i] == null ||
						paramTypes[i].isAssignableFrom(argTypes[i]);
					i++;
				}

				if (match) {
					return m;
				}
			}
		}

		return null;
	}

	/**
	 * A variant of {@link #findMethod(Collection, Class...)} for method
	 * arrays.
	 *
	 * @see #findMethod(Collection, Class...)
	 */
	public static Method findMethod(Method[] methods, Class<?>... argTypes) {
		return findMethod(Arrays.asList(methods), argTypes);
	}

	/**
	 * Tries to find a method with a certain name in the complete hierarchy of
	 * the given class. Determines all declared methods with the given name
	 * with
	 * {@link #getAllMethods(Class, String) getDeclaredMethods()} and uses
	 * {@link #findMethod(Collection, Class[]) getMethod()} to select a method
	 * with a matching parameter list.
	 *
	 * @param type     The class from which to return the method
	 * @param method   The name of the method to search for
	 * @param argTypes An array containing the classes of the argument types
	 *                 (may either be NULL, NO_ARGS, or empty for no-argument
	 *                 methods)
	 * @return The corresponding method instance or NULL if none could be found
	 */
	public static Method findMethod(Class<?> type, String method,
		Class<?>... argTypes) {
		return findMethod(getAllMethods(type, method), argTypes);
	}

	/**
	 * Tries to find a public method with a certain name and parameter types in
	 * the given class. Determines all public methods with the given name
	 * through {@link #getPublicMethods(Class, String) getPublicMethods()} and
	 * uses {@link #findMethod(Collection, Class[]) getMethod()} to select a
	 * method with a matching parameter list.
	 *
	 * @param type     The class from which to return the method
	 * @param method   The name of the public method to search for
	 * @param argTypes An array containing the classes of the argument types
	 *                 (may either be NULL, NO_ARGS, or empty for no-argument
	 *                 methods)
	 * @return The corresponding method instance or NULL if none could be found
	 */
	public static Method findPublicMethod(Class<?> type, String method,
		Class<?>... argTypes) {
		return findMethod(getPublicMethods(type, method), argTypes);
	}

	/**
	 * Forces the initialization of a certain class. Starting with Java 5
	 * classes that are referred to with class literals are not automatically
	 * initialized (see http://java.sun.com/j2se/1.5.0/compatibility.html).
	 * This
	 * method can be invoked to ensure that a class is initialized.
	 *
	 * @param type The class to force the initialization of
	 */
	public static void forceInit(Class<?> type) {
		try {
			Class.forName(type.getName(), true, type.getClassLoader());
		} catch (ClassNotFoundException e) {
			// Can't happen
			throw new AssertionError(e);
		}
	}

	/**
	 * Returns a list of all fields in a class hierarchy. This includes all
	 * fields of the given class and all it's superclasses (but not any
	 * interfaces) with any access modifier.
	 *
	 * @param type The class to return the fields of
	 * @return A list containing the fields (may be empty but will never be
	 * NULL)
	 */
	public static List<Field> getAllFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();

		while (type != null) {
			fields.addAll(Arrays.asList(type.getDeclaredFields()));
			type = type.getSuperclass();
		}

		return fields;
	}

	/**
	 * Returns all declared methods of a class that have a certain name. Only
	 * the name will be checked, the parameter lists of the methods will be
	 * ignored.
	 *
	 * <p>
	 * <b>Attention:</b> Currently only the direct hierarchy of classes will
	 * be considered by this method. Any interfaces declared by classes in the
	 * hierarchy will be ignored.
	 * </p>
	 *
	 * @param type       The class to get the methods from
	 * @param methodName The method name to search for
	 * @return A new list containing all methods with the given in name in the
	 * argument class; will be empty if no matching methods could be found
	 */
	public static List<Method> getAllMethods(Class<?> type,
		String methodName) {
		List<Method> methodList = new ArrayList<Method>();

		while (type != null) {
			for (Method method : type.getDeclaredMethods()) {
				if (method.getName().equals(methodName)) {
					methodList.add(method);
				}
			}

			// TODO: consider interfaces too!
			type = type.getSuperclass();
		}

		return methodList;
	}

	/**
	 * Returns an array containing the argument types of an array of argument
	 * values. If a value is NULL the type in the result array will also be
	 * NULL.
	 *
	 * @param args The argument values (may be NULL)
	 * @return An array of the argument type classes; will be the constant
	 * NO_ARGS for no arguments
	 */
	public static Class<?>[] getArgumentTypes(Object[] args) {
		return getArgumentTypes(args, false);
	}

	/**
	 * Returns an array containing the argument types of an array of argument
	 * values. If a value is NULL the type in the result array will also be
	 * NULL. If useNativeTypes is TRUE any type class that has a corresponding
	 * native type will be replaced with the latter one (e.g. Integer.class
	 * becomes int.class).
	 *
	 * @param args          The argument values (may be NULL)
	 * @param usePrimitives TRUE to convert type classes to the corresponding
	 *                      primitive types
	 * @return An array of the argument type classes; will be the constant
	 * NO_ARGS for no arguments
	 */
	public static Class<?>[] getArgumentTypes(Object[] args,
		boolean usePrimitives) {
		if (args == null || args.length == 0) {
			return NO_ARGS;
		}

		Class<?>[] argTypes = new Class<?>[args.length];

		for (int i = 0; i < args.length; i++) {
			if (args[i] != null) {
				Class<?> argType = args[i].getClass();

				if (usePrimitives) {
					Class<?> primitive = getPrimitiveType(argType);

					argTypes[i] = (primitive != null ? primitive : argType);
				} else {
					argTypes[i] = argType;
				}
			} else {
				argTypes[i] = null;
			}
		}

		return argTypes;
	}

	/**
	 * Returns the class that called the caller of this method.
	 *
	 * @param differentClass TRUE if the first different caller class should be
	 *                       returned
	 * @return The class of the caller of the calling method
	 * @see #getCalletypeName(boolean)
	 */
	public static Class<?> getCalletype(boolean differentClass) {
		try {
			return Class.forName(getCalletypeName(differentClass));
		} catch (ClassNotFoundException e) {
			// this should normally not be possible
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Returns the name of the class that called the caller of this method The
	 * boolean parameter controls whether the calling class of the calling
	 * method will be returned or if the first different class will be
	 * returned.
	 * The latter will make a difference if the calling method had been called
	 * from another method in it's own class. In that case a value of TRUE will
	 * cause the first different class to be returned, indepent from the call
	 * chain in the class that called this method.
	 *
	 * @param differentClass TRUE if the name of the first different caller
	 *                       class should be returned
	 * @return The name of the class of the caller of the calling method
	 */
	public static String getCalletypeName(boolean differentClass) {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();

		String caller = null;

		for (StackTraceElement element : stack) {
			String elementClass = element.getClassName();

			if (!elementClass.equals(THIS_CLASS_NAME) &&
				elementClass.indexOf(THREAD_CLASS_NAME) != 0) {
				if (caller == null) {
					caller = elementClass;
				} else if (!differentClass || (!caller.equals(elementClass))) {
					caller = elementClass;

					break;
				}
			}
		}

		return caller;
	}

	/**
	 * Returns the Class object for a certain class name or NULL if no class
	 * could be found. This method can be used alternatively instead of the
	 * {@link Class#forName(String)} method in cases where a NULL check is more
	 * appropriate than a try-catch block.
	 *
	 * @param className The name of the class to return
	 * @return The corresponding class object or NULL for none
	 */
	public static Class<?> getClass(String className) {
		Class<?> type;

		try {
			type = Class.forName(className);
		} catch (ClassNotFoundException e) {
			type = null;
		}

		return type;
	}

	/**
	 * Tries to find a certain field in a class hierarchy. This includes all
	 * declared fields of the given class and all it's superclasses (but not
	 * any
	 * interfaces).
	 *
	 * @param type      The class to search the field in
	 * @param fieldName The name of the field to return
	 * @return The matching field instance or NULL if no matching field
	 * could be
	 * found
	 */
	public static Field getField(Class<?> type, String fieldName) {
		while (type != null) {
			for (Field field : type.getDeclaredFields()) {
				if (field.getName().equals(fieldName)) {
					return field;
				}
			}

			type = type.getSuperclass();
		}

		return null;
	}

	/**
	 * Returns the value of a particular field in an object. The actual
	 * field is
	 * determined by means of the {@link #getField(Class, String)} method. Then
	 * the method {@link #getFieldValue(Field, Object)} will invoked and it's
	 * result returned.
	 *
	 * @param fieldName The name of the field to return the value of
	 * @param object    The object to read the field value from
	 * @return The value of the given field
	 * @throws IllegalArgumentException If the field doesn't exist or accessing
	 *                                  the field value fails
	 */
	public static Object getFieldValue(String fieldName, Object object) {
		return getFieldValue(getField(object.getClass(), fieldName), object);
	}

	/**
	 * Returns the value of a particular field in an object. If the field or
	 * it's class is not public this method will try to make it accessible by
	 * invoking {@link AccessibleObject#setAccessible(boolean)} on it. This
	 * requires that the caller's context has sufficient rights to do so,
	 * else a
	 * {@link SecurityException} will be thrown.
	 *
	 * @param field  The field to return the value of
	 * @param object The object to read the field value from
	 * @return The value of the given field
	 * @throws IllegalArgumentException If the field doesn't exist or accessing
	 *                                  the field value fails
	 */
	public static Object getFieldValue(Field field, Object object) {
		if (field != null) {
			try {
				return checkAccessible(field).get(object);
			} catch (Exception e) {
				throw new IllegalArgumentException(
					"Field access failed: " + object.getClass() + "." + field,
					e);
			}
		} else {
			throw new IllegalArgumentException("Invalid field: " + field);
		}
	}

	/**
	 * Returns a standard implementation class for an abstract class or an
	 * interface. If the given class neither represents an interface nor an
	 * abstract class it will be returned unchanged. Otherwise an
	 * implementation
	 * class will be returned if it had been registered previously. Several
	 * standard mappings (e.g. for collection classes) are registered
	 * automatically, others can be registered by the application. This can be
	 * done with the {@link #registerImplementation(Class, Class)} method.
	 *
	 * @param type The class to return the implementation for
	 * @return The implementation for the given class or NULL if no mapping
	 * exists
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> getImplementationClass(Class<T> type) {
		if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
			return (Class<? extends T>) interfaceImplementationMap.get(type);
		} else {
			return type;
		}
	}

	/**
	 * Returns the name of the namespace of a certain class or package. If the
	 * argument is an inner class this method will return the name of the
	 * enclosing class. Otherwise it returns the name of the enclosing (parent)
	 * package.
	 *
	 * @param typeName The name of the class or package to determine the parent
	 *                 package of
	 * @return The namespace of the type or NULL if the name denotes a
	 * top-level
	 * element
	 * @throws NullPointerException If the argument is NULL
	 */
	public static String getNamespace(String typeName) {
		int pos = typeName.lastIndexOf('$');
		String namespace = null;

		if (pos > 0) {
			namespace = typeName.substring(0, pos);
		} else {
			pos = typeName.lastIndexOf('.');

			if (pos > 0) {
				namespace = typeName.substring(0, pos);
			}
		}

		return namespace;
	}

	/**
	 * Returns the corresponding primitive type class (e.g. int.class,
	 * char.class) for a certain wrapper class (Integer.class, Character
	 * .class).
	 *
	 * @param wrappetype The class to return the corresponding primitive type
	 *                   for
	 * @return The corresponding primitive type or NULL if the argument is not
	 * an instance of a wrapper class
	 */
	public static Class<?> getPrimitiveType(Class<?> wrappetype) {
		return wrapperPrimitiveMap.get(wrappetype);
	}

	/**
	 * Returns the public constructor of a class with the given argument types.
	 * If a checked exception occurs it will be mapped to a runtime exception.
	 *
	 * @param type     The class to return the constructor of
	 * @param argTypes The classes of the argument types
	 * @return The public constructor
	 */
	public static <T> Constructor<T> getPublicConstructor(Class<T> type,
		Class<?>... argTypes) {
		try {
			return type.getConstructor(argTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
				"No constructor " + type.getSimpleName() + "(" +
					Arrays.asList(argTypes) + ")", e);
		}
	}

	/**
	 * Returns a public method with a certain name and parameters. This
	 * implementation is based on the {@link Class#getMethod(String, Class[])}
	 * method and therefore must be invoked with either the exactly matching
	 * argument types from the method signature or with argument values that
	 * have exactly these types. Else the method will not be found by the Class
	 * method. In such cases, the method
	 * {@link #findPublicMethod(Class, String, Class[])} can be used instead
	 * because it considers the class hierarchy of method parameters.
	 *
	 * @param type     The class from which to return the method
	 * @param method   The name of the public method to search for
	 * @param args     The arguments of the method call (NULL for none)
	 * @param argTypes The classes of the argument types or NULL if they shall
	 *                 be determined from the argument values (only possible if
	 *                 these are not NULL and have the exact types)
	 * @return The corresponding method instance
	 * @throws IllegalArgumentException If no matching method could be found or
	 *                                  if the invocation fails
	 */
	public static Method getPublicMethod(Class<?> type, String method,
		Object[] args, Class<?>[] argTypes) {
		if (argTypes == null) {
			argTypes = getArgumentTypes(args);
		}

		try {
			return type.getMethod(method, argTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
				"Method not found: " + method + "(" + Arrays.asList(argTypes) +
					")", e);
		}
	}

	/**
	 * Returns all public methods with the given name from the argument class.
	 * The method arguments are ignored, only the method names are compared.
	 * That means if multiple overloaded methods with the same name exist
	 * all of
	 * them will be returned. When invoking one of the returned method the
	 * caller must make sure to use the correct count and type of parameters.
	 *
	 * @param type       The class to search the method in
	 * @param methodName The method name to search for
	 * @return A new list containing the matching public methods; will be empty
	 * if no such methods could be found
	 */
	public static List<Method> getPublicMethods(Class<?> type,
		String methodName) {
		List<Method> methodList = new ArrayList<Method>();

		for (Method m : type.getMethods()) {
			if (m.getName().equals(methodName)) {
				methodList.add(m);
			}
		}

		return methodList;
	}

	/**
	 * Returns the raw type class for a certain reflection {@link Type}.
	 *
	 * @param type The reflection type
	 * @return The raw type class for the given type
	 * @throws IllegalArgumentException If the given type cannot be mapped
	 * to an
	 *                                  unambiguous raw type
	 */
	public static Class<?> getRawType(Type type) {
		Class<?> rawType;

		if (type instanceof Class) {
			rawType = (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			rawType = (Class<?>) ((ParameterizedType) type).getRawType();
		} else if (type instanceof WildcardType) {
			rawType = getRawType(((WildcardType) type).getUpperBounds()[0]);
		} else if (type instanceof GenericArrayType) {
			Type componentType =
				((GenericArrayType) type).getGenericComponentType();

			rawType =
				Array.newInstance(getRawType(componentType), 0).getClass();
		} else {
			throw new IllegalArgumentException(
				String.format("Unsupported type: %s", type));
		}

		return rawType;
	}

	/**
	 * Returns the corresponding wrapper type class (e.g. Integer.class,
	 * Character.class) for a certain primitive class (int.class, char.class).
	 *
	 * @param primitiveClass The class to return the corresponding wrapper type
	 *                       for
	 * @return The corresponding wrapper type or NULL if the argument is not an
	 * instance of a primitive class
	 */
	public static Class<?> getWrapperType(Class<?> primitiveClass) {
		return wrapperPrimitiveMap.getKey(primitiveClass);
	}

	/**
	 * Invokes a particular method on a target object. If the invocation fails
	 * an IllegalArgumentException will be thrown. If the method or it's class
	 * is not public this method will try to make it accessible by invoking
	 * {@link AccessibleObject#setAccessible(boolean)} on it. This requires
	 * that
	 * the caller's context has sufficient rights to do so, else a
	 * {@link SecurityException} will be thrown.
	 *
	 * @param target The target object to invoke the method on
	 * @param method The method instance to invoke
	 * @param args   The arguments of the method call
	 * @return The return value of the method call
	 * @throws IllegalArgumentException If the invocation fails
	 */
	public static Object invoke(Object target, Method method, Object... args) {
		try {
			return checkAccessible(method).invoke(target, args);
		} catch (Exception e) {
			throw new IllegalArgumentException(
				"Method invocation failed: " + method.getName(), e);
		}
	}

	/**
	 * Invokes any kind of declared no-argument methods on a target object.
	 *
	 * @see #invokeAny(Object, String, Object[], Class[])
	 */
	public static Object invokeAny(Object target, String method) {
		return invokeAny(target, method, null, NO_ARGS);
	}

	/**
	 * Invokes any kind of declared methodName on a target object. If the
	 * methodName is not found in the target object itself it's class hierarchy
	 * will be searched for it. The actual methodName invocation is then
	 * performed by calling {@link #invoke(Object, Method, Object...)}
	 *
	 * @param target     The target object to invoke the methodName on
	 * @param methodName The name of the public methodName to invoke
	 * @param args       The arguments of the methodName call (NULL for none)
	 * @param argTypes   The classes of the argument types or NULL if they
	 *                      shall
	 *                   be determined from the argument values (only possible
	 *                   if these are not NULL and do not span class
	 *                   hierarchies)
	 * @return The return value of the methodName call
	 * @throws IllegalArgumentException If no matching methodName could be
	 * found
	 *                                  or if the invocation fails
	 */
	public static Object invokeAny(Object target, String methodName,
		Object[] args, Class<?>[] argTypes) {
		if (argTypes == null) {
			argTypes = getArgumentTypes(args);
		}

		Method method = findMethod(target.getClass(), methodName, argTypes);

		if (method == null) {
			throw new IllegalArgumentException("Method not found: " + method);
		}

		return invoke(target, method, args);
	}

	/**
	 * Invokes a declared method on a target object. This requires that the
	 * caller's context has sufficient rights to do so if the method is not
	 * public.
	 *
	 * @param target     The target object to invoke the method on
	 * @param methodName The name of the public method to invoke
	 * @param args       The arguments of the method call (NULL for none)
	 * @param argTypes   The classes of the argument types or NULL if they
	 *                   should be determined from the argument values (only
	 *                   possible if these are not NULL and do not span class
	 *                   hierarchies)
	 * @return The return value of the method call
	 * @throws IllegalArgumentException If no matching method could be found or
	 *                                  if the invocation fails
	 */
	public static Object invokeDeclared(Object target, String methodName,
		Object[] args, Class<?>[] argTypes) {
		Method method;

		if (argTypes == null) {
			argTypes = getArgumentTypes(args);
		}

		try {
			return invoke(target,
				target.getClass().getDeclaredMethod(methodName, argTypes),
				args);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
				"Method not found: " + methodName, e);
		}

	}

	/**
	 * Invokes a public no-argument method on a target object.
	 *
	 * @see #invokePublic(Object, String, Object[], Class[])
	 */
	public static Object invokePublic(Object target, String method) {
		return invokePublic(target, method, null, NO_ARGS);
	}

	/**
	 * Invokes a public method on a target object. Uses the method
	 * {@link #getPublicMethod(Class, String, Object[], Class[])} to determine
	 * the corresponding Method instance.
	 *
	 * @param target   The target object to invoke the method on
	 * @param method   The name of the public method to invoke
	 * @param args     The arguments of the method call (NULL for none)
	 * @param argTypes The classes of the argument types or NULL if they shall
	 *                 be determined from the argument values (only possible if
	 *                 these are not NULL and have the exact types)
	 * @return The return value of the method call
	 * @throws IllegalArgumentException If no matching method could be found or
	 *                                  if the invocation fails
	 */
	public static Object invokePublic(Object target, String method,
		Object[] args, Class<?>[] argTypes) {
		Method m = getPublicMethod(target.getClass(), method, args, argTypes);

		return invoke(target, m, args);
	}

	/**
	 * Invokes a public static no-argument method on a target object.
	 *
	 * @see #invokeStatic(Class, String, Object[], Class[])
	 */
	public static Object invokeStatic(Class<?> type, String method) {
		return invokeStatic(type, method, null, NO_ARGS);
	}

	/**
	 * Invokes a public static method on a target class. Uses the method
	 * {@link #getPublicMethod(Class, String, Object[], Class[])} to determine
	 * the corresponding Method instance.
	 *
	 * @param type     The target class to invoke the static method on
	 * @param method   The name of the public method to invoke
	 * @param args     The arguments of the method call (NULL for none)
	 * @param argTypes The classes of the argument types or NULL if they shall
	 *                 be determined from the argument values (only possible if
	 *                 these are not NULL and have the exact types)
	 * @return The return value of the method call
	 * @throws IllegalArgumentException If no matching method could be found or
	 *                                  if the invocation fails
	 */
	public static Object invokeStatic(Class<?> type, String method,
		Object[] args, Class<?>[] argTypes) {
		Method m = getPublicMethod(type, method, args, argTypes);

		return invoke(null, m, args);
	}

	/**
	 * A convenience method to invoke {@link Class#newInstance()} without the
	 * need for explicit exception handling.
	 *
	 * @param type The class to invoke the no-argument constructor of
	 * @return A new instance of the given class
	 * @throws IllegalArgumentException If the instantiation fails
	 */
	public static <T> T newInstance(Class<T> type) {
		try {
			return type.getConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(
				"Could not create instance of " + type, e);
		}
	}

	/**
	 * Creates a new instance by invoking a public constructor with a certain
	 * number of arguments. If the invocation fails an IllegalArgumentException
	 * will be thrown, initialized with the causing exception.
	 *
	 * @param constructor The constructor to invoke
	 * @param args        The arguments of the call (NULL for none)
	 * @return The new instance created by the constructor
	 * @throws IllegalArgumentException If the invocation fails
	 */
	public static <T> T newInstance(Constructor<T> constructor,
		Object[] args) {
		try {
			return constructor.newInstance(args);
		} catch (Exception e) {
			throw new IllegalArgumentException("Constructor invocation failed",
				e);
		}
	}

	/**
	 * Creates a new instance by invoking a public constructor of a target
	 * class.
	 *
	 * @param type     The target class to invoke the constructor of
	 * @param args     The arguments of the constructor call (NULL for none)
	 * @param argTypes The classes of the argument types or NULL if they shall
	 *                 be determined from the argument values (only possible if
	 *                 these are not NULL and do not span class hierarchies)
	 * @return The new instance created by the constructor
	 * @throws IllegalArgumentException If no matching constructor could be
	 *                                  found or if the invocation fails
	 */
	public static <T> T newInstance(Class<T> type, Object[] args,
		Class<?>[] argTypes) {
		if (argTypes == null) {
			argTypes = getArgumentTypes(args);
		}

		try {
			Constructor<T> c = type.getConstructor(argTypes);

			return c.newInstance(args);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
				"No matching constructor: " + type);
		} catch (Exception e) {
			throw new IllegalArgumentException("Method invocation failed", e);
		}
	}

	/**
	 * Registers an interface implementation class. Registered implementations
	 * can then be queried with {@link #getImplementationClass(Class)}. Some
	 * implementations are registered automatically, e.g. for the Java
	 * collection framework. Applications can register their own mappings to
	 * allow the easy reflective creation of instances for which only the
	 * interface type is known.
	 *
	 * @param interfaceType      The interface class
	 * @param implementationType The implementation class to be associated with
	 *                           the interface
	 */
	public static <I> void registerImplementation(Class<I> interfaceType,
		Class<? extends I> implementationType) {
		interfaceImplementationMap.put(interfaceType, implementationType);
	}
}
