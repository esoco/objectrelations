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


/********************************************************************
 * Contains static utility methods for reflection.
 *
 * @author eso
 */
public final class ReflectUtil
{
	//~ Static fields/initializers ---------------------------------------------

	private static final String THIS_CLASS_NAME   = ReflectUtil.class.getName();
	private static final String THREAD_CLASS_NAME = Thread.class.getName();

	/** Constant to signal no-argument methods */
	public static final Class<?>[] NO_ARGS = new Class<?>[0];

	private static Map<Class<?>, Class<?>> aInterfaceImplementationMap =
		new HashMap<Class<?>, Class<?>>();

	private static final BidirectionalMap<Class<?>, Class<?>> aWrapperPrimitiveMap =
		new BidirectionalMap<Class<?>, Class<?>>();

	static
	{
		aWrapperPrimitiveMap.put(Boolean.class, boolean.class);
		aWrapperPrimitiveMap.put(Character.class, char.class);
		aWrapperPrimitiveMap.put(Byte.class, byte.class);
		aWrapperPrimitiveMap.put(Short.class, short.class);
		aWrapperPrimitiveMap.put(Integer.class, int.class);
		aWrapperPrimitiveMap.put(Long.class, long.class);
		aWrapperPrimitiveMap.put(Float.class, float.class);
		aWrapperPrimitiveMap.put(Double.class, double.class);

		registerImplementation(List.class, ArrayList.class);
		registerImplementation(Set.class, HashSet.class);
		registerImplementation(Map.class, HashMap.class);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * This is a helper method to implement the assertion of the correct field
	 * names for constant declarations in an enumeration pattern. This method
	 * will iterate through the final static fields (i.e. constants) of a
	 * certain class that are instances of a certain base class and checks
	 * whether the field names are equal to the internal name strings of the
	 * instance. To access the name strings the constant class must provide a
	 * public no-argument method with the given name.
	 *
	 * <p>This method will not check the superclasses of the declaring class.
	 * The names of the constants may include namespaces which will be ignored
	 * in the name comparisons.</p>
	 *
	 * <p>This method is intended to be used at development time and therefore
	 * uses assertions to perform it's checks. It returns a boolean value which
	 * will always be TRUE so that it can be invoked in an assert statement
	 * (which is the recommended way to use it). By doing so the constant
	 * assertions will only be performed if assertions are enabled and not in
	 * production code. A typical invocation should be done in a static
	 * initialization block after the constant declarations and would look like
	 * this:</p>
	 * <code>assert ReflectUtil.assertConstantDeclarations(AppConstants.class,
	 * TestEnum.class, "getName", false);</code>
	 *
	 * @param  rDeclaringClass     The class to check the declared fields of
	 * @param  rConstantClass      The (base) class of the constants to check
	 * @param  sNameMethod         The method to query the constant names with
	 * @param  bAllAccessModifiers TRUE to check constants with all access
	 *                             modifiers, FALSE for public constants only
	 *
	 * @return Always TRUE
	 */
	public static <T> boolean assertConstantDeclarations(
		Class<?>		 rDeclaringClass,
		Class<? super T> rConstantClass,
		String			 sNameMethod,
		boolean			 bAllAccessModifiers)
	{
		collectConstants(rDeclaringClass,
						 rConstantClass,
						 sNameMethod,
						 bAllAccessModifiers,
						 false,
						 true);

		return true;
	}

	/***************************************
	 * Checks whether a class member is publicly accessible. If not the method
	 * {@link AccessibleObject#setAccessible(boolean)} will be invoked on it to
	 * make it accessible.
	 *
	 * @param  rMember The member to check
	 *
	 * @return The input member to allow call concatenation
	 */
	public static <T extends Member> T checkAccessible(T rMember)
	{
		if (rMember instanceof AccessibleObject &&
			!((AccessibleObject) rMember).isAccessible())
		{
			Class<?> rMemberClass = rMember.getDeclaringClass();

			if (!Modifier.isPublic(rMember.getModifiers()) ||
				!Modifier.isPublic(rMemberClass.getModifiers()))
			{
				((AccessibleObject) rMember).setAccessible(true);
			}
		}

		return rMember;
	}

	/***************************************
	 * Collects all constants of a certain type from a declaring class and
	 * returns them in a list. The constants must be declared as public static
	 * final and must not be NULL. Non-public fields will be ignored and the
	 * static final modifiers will be checked with assertions so that errors can
	 * be detected at development time. If a constant is NULL or an exception
	 * occurs during the reflective access to a field an {@link AssertionError}
	 * will be thrown. Because of these checks no fields of the constant type
	 * must exist that are not static final and/or are NULL.
	 *
	 * <p>Because constants are often used with a name field that must match the
	 * name of the constant instance this method provides a way to assert the
	 * equality of the constant name and the name field. To compare the names
	 * with an assertion the third argument must be the name of a public
	 * no-argument method that returns the value of the name field (e.g.
	 * "getName"). To omit this check it must be NULL.</p>
	 *
	 * <p>This method is intended to be used during the initialization of
	 * classes that want to keep a list of their declared constants, and should
	 * not be used in instance methods. Therefore using assertions to signal
	 * uncritical errors is sufficient. If a class needs to verify the constants
	 * only without the need to access a list of them it can use the method
	 * {@link #assertConstantDeclarations(Class, Class, String, boolean)}
	 * instead.</p>
	 *
	 * @param  rDeclaringClass     The class that declares the constants to
	 *                             collect
	 * @param  rConstantClass      The datatype (super)class of the constants
	 *                             (relaxed to '? super T' to prevent conversion
	 *                             conflicts with generic types)
	 * @param  sCheckNameMethod    The name of the method to check the instance
	 *                             names with or NULL to omit this check
	 * @param  bAllAccessModifiers TRUE to check constants with all access
	 *                             modifiers, FALSE for public constants only
	 * @param  bWithSuperclasses   TRUE to collect all fields, including that of
	 *                             superclasses; FALSE to collect the fields of
	 *                             the given declaring class only
	 * @param  bAllowNamespace     TRUE to allow the instance names to begin
	 *                             with a namespace string, separated by the '.'
	 *                             char
	 *
	 * @return A list of all constants of the given base type that are declared
	 *         in the declaring class
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> collectConstants(
		Class<?>		 rDeclaringClass,
		Class<? super T> rConstantClass,
		String			 sCheckNameMethod,
		boolean			 bAllAccessModifiers,
		boolean			 bWithSuperclasses,
		boolean			 bAllowNamespace)
	{
		List<T>     aConstants = new ArrayList<T>();
		List<Field> rFields;

		try
		{
			if (bWithSuperclasses)
			{
				rFields =
					bAllAccessModifiers
					? getAllFields(rDeclaringClass)
					: Arrays.asList(rDeclaringClass.getFields());
			}
			else
			{
				rFields = Arrays.asList(rDeclaringClass.getDeclaredFields());
			}

			Method rCheckNameMethod =
				sCheckNameMethod != null
				? rConstantClass.getMethod(sCheckNameMethod) : null;

			for (Field rField : rFields)
			{
				int nModifiers = rField.getModifiers();

				if (Modifier.isStatic(nModifiers) &&
					(bAllAccessModifiers || Modifier.isPublic(nModifiers)) &&
					rConstantClass.isAssignableFrom(rField.getType()))
				{
					if (bAllAccessModifiers)
					{
						checkAccessible(rField);
					}

					String sFieldName  = rField.getName();
					Object rFieldValue = rField.get(null);

					assert Modifier.isFinal(nModifiers) : "Instance not final static: " +
						   sFieldName;

					if (rCheckNameMethod != null)
					{
						String sName =
							rCheckNameMethod.invoke(rFieldValue).toString();

						if (bAllowNamespace)
						{
							// remove namespace if exists
							sName = sName.substring(sName.lastIndexOf('.') + 1);
						}

						// check if enum string and instance names match
						assert sFieldName.equals(sName) : "Name mismatch of " +
							   rDeclaringClass.getSimpleName() + " constant " +
							   sFieldName + " (wrong name: " + sName + ")";
					}

					if (rFieldValue != null)
					{
						aConstants.add((T) rFieldValue);
					}
					else
					{
						throw new AssertionError("Field is NULL: " + rField);
					}
				}
			}
		}
		catch (Exception e)
		{
			String sMessage =
				String.format("Could not collect %s constants from %s",
							  rConstantClass.getSimpleName(),
							  rDeclaringClass.getSimpleName());

			throw new IllegalArgumentException(sMessage, e);
		}

		return aConstants;
	}

	/***************************************
	 * Tries to find an arbitrary public method with a certain name in the given
	 * class. Returns the first method (in the classes' declaration order) with
	 * the given name or NULL if no such method exists.
	 *
	 * @param  rClass  The class in which to search for the method
	 * @param  sMethod The name of the public method to search for
	 *
	 * @return The corresponding method instance or NULL if none could be found
	 */
	public static Method findAnyPublicMethod(Class<?> rClass, String sMethod)
	{
		for (Method rMethod : rClass.getMethods())
		{
			if (rMethod.getName().equals(sMethod))
			{
				return rMethod;
			}
		}

		return null;
	}

	/***************************************
	 * Tries to find a matching method with certain parameters in a list of
	 * methods. The method collection argument will be searched for methods that
	 * have a parameter list that is capable of accepting arguments of the types
	 * contained in the rArgTypes argument. That means that each parameter of a
	 * matching method must be of a type that is either the same as, or is a
	 * supertype of the corresponding type in the rArgTypes array.
	 *
	 * <p>If the rArgTypes array contains NULL values these will be considered
	 * as matching any parameter type at the same position. The application is
	 * then responsible of providing the correct argument value (i.e. NULL or of
	 * a type matching the method's parameter) on method invocation.</p>
	 *
	 * <p>This method will not distinguish between multiple variants of the
	 * method with compatible parameter lists. It will choose and return the
	 * first compatible method that can be found in the collection argument.</p>
	 *
	 * @param  rMethods  A collection containing the methods to search
	 * @param  rArgTypes An array containing the classes of the argument types
	 *                   (may either be NULL, NO_ARGS, or empty for no-argument
	 *                   methods)
	 *
	 * @return The corresponding method instance or NULL if none could be found
	 */
	public static Method findMethod(
		Collection<Method> rMethods,
		Class<?>... 	   rArgTypes)
	{
		if (rArgTypes == null)
		{
			rArgTypes = NO_ARGS;
		}

		for (Method m : rMethods)
		{
			Class<?>[] rParamTypes = m.getParameterTypes();

			if (rParamTypes.length == rArgTypes.length)
			{
				boolean bMatch = true;
				int     i	   = 0;

				while (bMatch && i < rParamTypes.length)
				{
					bMatch =
						rArgTypes[i] == null ||
						rParamTypes[i].isAssignableFrom(rArgTypes[i]);
					i++;
				}

				if (bMatch)
				{
					return m;
				}
			}
		}

		return null;
	}

	/***************************************
	 * A variant of {@link #findMethod(Collection, Class...)} for method arrays.
	 *
	 * @see #findMethod(Collection, Class...)
	 */
	public static Method findMethod(Method[] rMethods, Class<?>... rArgTypes)
	{
		return findMethod(Arrays.asList(rMethods), rArgTypes);
	}

	/***************************************
	 * Tries to find a method with a certain name in the complete hierarchy of
	 * the given class. Determines all declared methods with the given name with
	 * {@link #getAllMethods(Class, String) getDeclaredMethods()} and uses
	 * {@link #findMethod(Collection, Class[]) getMethod()} to select a method
	 * with a matching parameter list.
	 *
	 * @param  rClass    The class from which to return the method
	 * @param  sMethod   The name of the method to search for
	 * @param  rArgTypes An array containing the classes of the argument types
	 *                   (may either be NULL, NO_ARGS, or empty for no-argument
	 *                   methods)
	 *
	 * @return The corresponding method instance or NULL if none could be found
	 */
	public static Method findMethod(Class<?>    rClass,
									String		sMethod,
									Class<?>... rArgTypes)
	{
		return findMethod(getAllMethods(rClass, sMethod), rArgTypes);
	}

	/***************************************
	 * Tries to find a public method with a certain name and parameter types in
	 * the given class. Determines all public methods with the given name
	 * through {@link #getPublicMethods(Class, String) getPublicMethods()} and
	 * uses {@link #findMethod(Collection, Class[]) getMethod()} to select a
	 * method with a matching parameter list.
	 *
	 * @param  rClass    The class from which to return the method
	 * @param  sMethod   The name of the public method to search for
	 * @param  rArgTypes An array containing the classes of the argument types
	 *                   (may either be NULL, NO_ARGS, or empty for no-argument
	 *                   methods)
	 *
	 * @return The corresponding method instance or NULL if none could be found
	 */
	public static Method findPublicMethod(Class<?>    rClass,
										  String	  sMethod,
										  Class<?>... rArgTypes)
	{
		return findMethod(getPublicMethods(rClass, sMethod), rArgTypes);
	}

	/***************************************
	 * Forces the initialization of a certain class. Starting with Java 5
	 * classes that are referred to with class literals are not automatically
	 * initialized (see http://java.sun.com/j2se/1.5.0/compatibility.html). This
	 * method can be invoked to ensure that a class is initialized.
	 *
	 * @param rClass The class to force the initialization of
	 */
	public static void forceInit(Class<?> rClass)
	{
		try
		{
			Class.forName(rClass.getName(), true, rClass.getClassLoader());
		}
		catch (ClassNotFoundException e)
		{
			// Can't happen
			throw new AssertionError(e);
		}
	}

	/***************************************
	 * Returns a list of all fields in a class hierarchy. This includes all
	 * fields of the given class and all it's superclasses (but not any
	 * interfaces) with any access modifier.
	 *
	 * @param  rClass The class to return the fields of
	 *
	 * @return A list containing the fields (may be empty but will never be
	 *         NULL)
	 */
	public static List<Field> getAllFields(Class<?> rClass)
	{
		List<Field> aFields = new ArrayList<Field>();

		while (rClass != null)
		{
			aFields.addAll(Arrays.asList(rClass.getDeclaredFields()));
			rClass = rClass.getSuperclass();
		}

		return aFields;
	}

	/***************************************
	 * Returns all declared methods of a class that have a certain name. Only
	 * the name will be checked, the parameter lists of the methods will be
	 * ignored.
	 *
	 * <p><b>Attention:</b> Currently only the direct hierarchy of classes will
	 * be considered by this method. Any interfaces declared by classes in the
	 * hierarchy will be ignored.</p>
	 *
	 * @param  rClass      The class to get the methods from
	 * @param  sMethodName The method name to search for
	 *
	 * @return A new list containing all methods with the given in name in the
	 *         argument class; will be empty if no matching methods could be
	 *         found
	 */
	public static List<Method> getAllMethods(
		Class<?> rClass,
		String   sMethodName)
	{
		List<Method> aMethodList = new ArrayList<Method>();

		while (rClass != null)
		{
			for (Method rMethod : rClass.getDeclaredMethods())
			{
				if (rMethod.getName().equals(sMethodName))
				{
					aMethodList.add(rMethod);
				}
			}

			// TODO: consider interfaces too!
			rClass = rClass.getSuperclass();
		}

		return aMethodList;
	}

	/***************************************
	 * Returns an array containing the argument types of an array of argument
	 * values. If a value is NULL the type in the result array will also be
	 * NULL.
	 *
	 * @param  rArgs The argument values (may be NULL)
	 *
	 * @return An array of the argument type classes; will be the constant
	 *         NO_ARGS for no arguments
	 */
	public static Class<?>[] getArgumentTypes(Object[] rArgs)
	{
		return getArgumentTypes(rArgs, false);
	}

	/***************************************
	 * Returns an array containing the argument types of an array of argument
	 * values. If a value is NULL the type in the result array will also be
	 * NULL. If bUseNativeTypes is TRUE any type class that has a corresponding
	 * native type will be replaced with the latter one (e.g. Integer.class
	 * becomes int.class).
	 *
	 * @param  rArgs          The argument values (may be NULL)
	 * @param  bUsePrimitives TRUE to convert type classes to the corresponding
	 *                        primitive types
	 *
	 * @return An array of the argument type classes; will be the constant
	 *         NO_ARGS for no arguments
	 */
	public static Class<?>[] getArgumentTypes(
		Object[] rArgs,
		boolean  bUsePrimitives)
	{
		if (rArgs == null || rArgs.length == 0)
		{
			return NO_ARGS;
		}

		Class<?>[] argTypes = new Class<?>[rArgs.length];

		for (int i = 0; i < rArgs.length; i++)
		{
			if (rArgs[i] != null)
			{
				Class<?> rArgType = rArgs[i].getClass();

				if (bUsePrimitives)
				{
					Class<?> rPrimitive = getPrimitiveType(rArgType);

					argTypes[i] = (rPrimitive != null ? rPrimitive : rArgType);
				}
				else
				{
					argTypes[i] = rArgType;
				}
			}
			else
			{
				argTypes[i] = null;
			}
		}

		return argTypes;
	}

	/***************************************
	 * Returns the class that called the caller of this method.
	 *
	 * @param  bDifferentClass TRUE if the first different caller class should
	 *                         be returned
	 *
	 * @return The class of the caller of the calling method
	 *
	 * @see    #getCallerClassName(boolean)
	 */
	public static Class<?> getCallerClass(boolean bDifferentClass)
	{
		try
		{
			return Class.forName(getCallerClassName(bDifferentClass));
		}
		catch (ClassNotFoundException e)
		{
			// this should normally not be possible
			throw new IllegalStateException(e);
		}
	}

	/***************************************
	 * Returns the name of the class that called the caller of this method The
	 * boolean parameter controls whether the calling class of the calling
	 * method will be returned or if the first different class will be returned.
	 * The latter will make a difference if the calling method had been called
	 * from another method in it's own class. In that case a value of TRUE will
	 * cause the first different class to be returned, indepent from the call
	 * chain in the class that called this method.
	 *
	 * @param  bDifferentClass TRUE if the name of the first different caller
	 *                         class should be returned
	 *
	 * @return The name of the class of the caller of the calling method
	 */
	public static String getCallerClassName(boolean bDifferentClass)
	{
		StackTraceElement[] aStack = Thread.currentThread().getStackTrace();

		String sCaller = null;

		for (StackTraceElement rElement : aStack)
		{
			String sElementClass = rElement.getClassName();

			if (!sElementClass.equals(THIS_CLASS_NAME) &&
				sElementClass.indexOf(THREAD_CLASS_NAME) != 0)
			{
				if (sCaller == null)
				{
					sCaller = sElementClass;
				}
				else if (!bDifferentClass || (!sCaller.equals(sElementClass)))
				{
					sCaller = sElementClass;

					break;
				}
			}
		}

		return sCaller;
	}

	/***************************************
	 * Returns the Class object for a certain class name or NULL if no class
	 * could be found. This method can be used alternatively instead of the
	 * {@link Class#forName(String)} method in cases where a NULL check is more
	 * appropriate than a try-catch block.
	 *
	 * @param  sClassName The name of the class to return
	 *
	 * @return The corresponding class object or NULL for none
	 */
	public static Class<?> getClass(String sClassName)
	{
		Class<?> rClass;

		try
		{
			rClass = Class.forName(sClassName);
		}
		catch (ClassNotFoundException e)
		{
			rClass = null;
		}

		return rClass;
	}

	/***************************************
	 * Tries to find a certain field in a class hierarchy. This includes all
	 * declared fields of the given class and all it's superclasses (but not any
	 * interfaces).
	 *
	 * @param  rClass     The class to search the field in
	 * @param  sFieldName The name of the field to return
	 *
	 * @return The matching field instance or NULL if no matching field could be
	 *         found
	 */
	public static Field getField(Class<?> rClass, String sFieldName)
	{
		while (rClass != null)
		{
			for (Field rField : rClass.getDeclaredFields())
			{
				if (rField.getName().equals(sFieldName))
				{
					return rField;
				}
			}

			rClass = rClass.getSuperclass();
		}

		return null;
	}

	/***************************************
	 * Returns the value of a particular field in an object. The actual field is
	 * determined by means of the {@link #getField(Class, String)} method. Then
	 * the method {@link #getFieldValue(Field, Object)} will invoked and it's
	 * result returned.
	 *
	 * @param  sFieldName The name of the field to return the value of
	 * @param  rObject    The object to read the field value from
	 *
	 * @return The value of the given field
	 *
	 * @throws IllegalArgumentException If the field doesn't exist or accessing
	 *                                  the field value fails
	 */
	public static Object getFieldValue(String sFieldName, Object rObject)
	{
		return getFieldValue(getField(rObject.getClass(), sFieldName), rObject);
	}

	/***************************************
	 * Returns the value of a particular field in an object. If the field or
	 * it's class is not public this method will try to make it accessible by
	 * invoking {@link AccessibleObject#setAccessible(boolean)} on it. This
	 * requires that the caller's context has sufficient rights to do so, else a
	 * {@link SecurityException} will be thrown.
	 *
	 * @param  rField  The field to return the value of
	 * @param  rObject The object to read the field value from
	 *
	 * @return The value of the given field
	 *
	 * @throws IllegalArgumentException If the field doesn't exist or accessing
	 *                                  the field value fails
	 */
	public static Object getFieldValue(Field rField, Object rObject)
	{
		if (rField != null)
		{
			try
			{
				return checkAccessible(rField).get(rObject);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Field access failed: " +
												   rObject.getClass() + "." +
												   rField,
												   e);
			}
		}
		else
		{
			throw new IllegalArgumentException("Invalid field: " + rField);
		}
	}

	/***************************************
	 * Returns a standard implementation class for an abstract class or an
	 * interface. If the given class neither represents an interface nor an
	 * abstract class it will be returned unchanged. Otherwise an implementation
	 * class will be returned if it had been registered previously. Several
	 * standard mappings (e.g. for collection classes) are registered
	 * automatically, others can be registered by the application. This can be
	 * done with the {@link #registerImplementation(Class, Class)} method.
	 *
	 * @param  rClass The class to return the implementation for
	 *
	 * @return The implementation for the given class or NULL if no mapping
	 *         exists
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> getImplementationClass(Class<T> rClass)
	{
		if (rClass.isInterface() || Modifier.isAbstract(rClass.getModifiers()))
		{
			return (Class<? extends T>) aInterfaceImplementationMap.get(rClass);
		}
		else
		{
			return rClass;
		}
	}

	/***************************************
	 * Returns the name of the namespace of a certain class or package. If the
	 * argument is an inner class this method will return the name of the
	 * enclosing class. Otherwise it returns the name of the enclosing (parent)
	 * package.
	 *
	 * @param  sTypeName The name of the class or package to determine the
	 *                   parent package of
	 *
	 * @return The namespace of the type or NULL if the name denotes a top-level
	 *         element
	 *
	 * @throws NullPointerException If the argument is NULL
	 */
	public static String getNamespace(String sTypeName)
	{
		int    nPos		  = sTypeName.lastIndexOf('$');
		String sNamespace = null;

		if (nPos > 0)
		{
			sNamespace = sTypeName.substring(0, nPos);
		}
		else
		{
			nPos = sTypeName.lastIndexOf('.');

			if (nPos > 0)
			{
				sNamespace = sTypeName.substring(0, nPos);
			}
		}

		return sNamespace;
	}

	/***************************************
	 * Returns the corresponding primitive type class (e.g. int.class,
	 * char.class) for a certain wrapper class (Integer.class, Character.class).
	 *
	 * @param  rWrapperClass The class to return the corresponding primitive
	 *                       type for
	 *
	 * @return The corresponding primitive type or NULL if the argument is not
	 *         an instance of a wrapper class
	 */
	public static Class<?> getPrimitiveType(Class<?> rWrapperClass)
	{
		return aWrapperPrimitiveMap.get(rWrapperClass);
	}

	/***************************************
	 * Returns the public constructor of a class with the given argument types.
	 * If a checked exception occurs it will be mapped to a runtime exception.
	 *
	 * @param  rClass    The class to return the constructor of
	 * @param  rArgTypes The classes of the argument types
	 *
	 * @return The public constructor
	 */
	public static <T> Constructor<T> getPublicConstructor(
		Class<T>    rClass,
		Class<?>... rArgTypes)
	{
		try
		{
			return rClass.getConstructor(rArgTypes);
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("No constructor " +
											   rClass.getSimpleName() +
											   "(" + Arrays.asList(rArgTypes) +
											   ")",
											   e);
		}
	}

	/***************************************
	 * Returns a public method with a certain name and parameters. This
	 * implementation is based on the {@link Class#getMethod(String, Class[])}
	 * method and therefore must be invoked with either the exactly matching
	 * argument types from the method signature or with argument values that
	 * have exactly these types. Else the method will not be found by the Class
	 * method. In such cases, the method {@link #findPublicMethod(Class, String,
	 * Class[])} can be used instead because it considers the class hierarchy of
	 * method parameters.
	 *
	 * @param  rClass    The class from which to return the method
	 * @param  sMethod   The name of the public method to search for
	 * @param  rArgs     The arguments of the method call (NULL for none)
	 * @param  rArgTypes The classes of the argument types or NULL if they shall
	 *                   be determined from the argument values (only possible
	 *                   if these are not NULL and have the exact types)
	 *
	 * @return The corresponding method instance
	 *
	 * @throws IllegalArgumentException If no matching method could be found or
	 *                                  if the invocation fails
	 */
	public static Method getPublicMethod(Class<?>   rClass,
										 String		sMethod,
										 Object[]   rArgs,
										 Class<?>[] rArgTypes)
	{
		if (rArgTypes == null)
		{
			rArgTypes = getArgumentTypes(rArgs);
		}

		try
		{
			return rClass.getMethod(sMethod, rArgTypes);
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("Method not found: " + sMethod +
											   "(" + Arrays.asList(rArgTypes) +
											   ")",
											   e);
		}
	}

	/***************************************
	 * Returns all public methods with the given name from the argument class.
	 * The method arguments are ignored, only the method names are compared.
	 * That means if multiple overloaded methods with the same name exist all of
	 * them will be returned. When invoking one of the returned method the
	 * caller must make sure to use the correct count and type of parameters.
	 *
	 * @param  rClass      The class to search the method in
	 * @param  sMethodName The method name to search for
	 *
	 * @return A new list containing the matching public methods; will be empty
	 *         if no such methods could be found
	 */
	public static List<Method> getPublicMethods(
		Class<?> rClass,
		String   sMethodName)
	{
		List<Method> aMethodList = new ArrayList<Method>();

		for (Method m : rClass.getMethods())
		{
			if (m.getName().equals(sMethodName))
			{
				aMethodList.add(m);
			}
		}

		return aMethodList;
	}

	/***************************************
	 * Returns the raw type class for a certain reflection {@link Type}.
	 *
	 * @param  rType The reflection type
	 *
	 * @return The raw type class for the given type
	 *
	 * @throws IllegalArgumentException If the given type cannot be mapped to an
	 *                                  unambiguous raw type
	 */
	public static Class<?> getRawType(Type rType)
	{
		Class<?> rRawType;

		if (rType instanceof Class)
		{
			rRawType = (Class<?>) rType;
		}
		else if (rType instanceof ParameterizedType)
		{
			rRawType = (Class<?>) ((ParameterizedType) rType).getRawType();
		}
		else if (rType instanceof WildcardType)
		{
			rRawType = getRawType(((WildcardType) rType).getUpperBounds()[0]);
		}
		else if (rType instanceof GenericArrayType)
		{
			Type rComponentType =
				((GenericArrayType) rType).getGenericComponentType();

			rRawType =
				Array.newInstance(getRawType(rComponentType), 0).getClass();
		}
		else
		{
			throw new IllegalArgumentException(String.format("Unsupported type: %s",
															 rType));
		}

		return rRawType;
	}

	/***************************************
	 * Returns the corresponding wrapper type class (e.g. Integer.class,
	 * Character.class) for a certain primitive class (int.class, char.class).
	 *
	 * @param  rPrimitiveClass The class to return the corresponding wrapper
	 *                         type for
	 *
	 * @return The corresponding wrapper type or NULL if the argument is not an
	 *         instance of a primitive class
	 */
	public static Class<?> getWrapperType(Class<?> rPrimitiveClass)
	{
		return aWrapperPrimitiveMap.getKey(rPrimitiveClass);
	}

	/***************************************
	 * Invokes a particular method on a target object. If the invocation fails
	 * an IllegalArgumentException will be thrown. If the method or it's class
	 * is not public this method will try to make it accessible by invoking
	 * {@link AccessibleObject#setAccessible(boolean)} on it. This requires that
	 * the caller's context has sufficient rights to do so, else a {@link
	 * SecurityException} will be thrown.
	 *
	 * @param  rTarget The target object to invoke the method on
	 * @param  rMethod The method instance to invoke
	 * @param  rArgs   The arguments of the method call
	 *
	 * @return The return value of the method call
	 *
	 * @throws IllegalArgumentException If the invocation fails
	 */
	public static Object invoke(Object rTarget, Method rMethod, Object... rArgs)
	{
		try
		{
			return checkAccessible(rMethod).invoke(rTarget, rArgs);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Method invocation failed: " +
											   rMethod.getName(),
											   e);
		}
	}

	/***************************************
	 * Invokes any kind of declared no-argument methods on a target object.
	 *
	 * @see #invokeAny(Object, String, Object[], Class[])
	 */
	public static Object invokeAny(Object rTarget, String sMethod)
	{
		return invokeAny(rTarget, sMethod, null, NO_ARGS);
	}

	/***************************************
	 * Invokes any kind of declared method on a target object. If the method is
	 * not found in the target object itself it's class hierarchy will be
	 * searched for it. The actual method invocation is then performed by
	 * calling {@link #invoke(Object, Method, Object...)}
	 *
	 * @param  rTarget   The target object to invoke the method on
	 * @param  sMethod   The name of the public method to invoke
	 * @param  rArgs     The arguments of the method call (NULL for none)
	 * @param  rArgTypes The classes of the argument types or NULL if they shall
	 *                   be determined from the argument values (only possible
	 *                   if these are not NULL and do not span class
	 *                   hierarchies)
	 *
	 * @return The return value of the method call
	 *
	 * @throws IllegalArgumentException If no matching method could be found or
	 *                                  if the invocation fails
	 */
	public static Object invokeAny(Object	  rTarget,
								   String	  sMethod,
								   Object[]   rArgs,
								   Class<?>[] rArgTypes)
	{
		if (rArgTypes == null)
		{
			rArgTypes = getArgumentTypes(rArgs);
		}

		Method rMethod = findMethod(rTarget.getClass(), sMethod, rArgTypes);

		if (rMethod == null)
		{
			throw new IllegalArgumentException("Method not found: " + sMethod);
		}

		return invoke(rTarget, rMethod, rArgs);
	}

	/***************************************
	 * Invokes a declared method on a target object. This requires that the
	 * caller's context has sufficient rights to do so if the method is not
	 * public.
	 *
	 * @param  rTarget   The target object to invoke the method on
	 * @param  sMethod   The name of the public method to invoke
	 * @param  rArgs     The arguments of the method call (NULL for none)
	 * @param  rArgTypes The classes of the argument types or NULL if they shall
	 *                   be determined from the argument values (only possible
	 *                   if these are not NULL and do not span class
	 *                   hierarchies)
	 *
	 * @return The return value of the method call
	 *
	 * @throws IllegalArgumentException If no matching method could be found or
	 *                                  if the invocation fails
	 */
	public static Object invokeDeclared(Object	   rTarget,
										String	   sMethod,
										Object[]   rArgs,
										Class<?>[] rArgTypes)
	{
		Method rMethod;

		if (rArgTypes == null)
		{
			rArgTypes = getArgumentTypes(rArgs);
		}

		try
		{
			rMethod = rTarget.getClass().getDeclaredMethod(sMethod, rArgTypes);
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("Method not found: " + sMethod,
											   e);
		}

		return invoke(rTarget, rMethod, rArgs);
	}

	/***************************************
	 * Invokes a public no-argument method on a target object.
	 *
	 * @see #invokePublic(Object, String, Object[], Class[])
	 */
	public static Object invokePublic(Object rTarget, String sMethod)
	{
		return invokePublic(rTarget, sMethod, null, NO_ARGS);
	}

	/***************************************
	 * Invokes a public method on a target object. Uses the method {@link
	 * #getPublicMethod(Class, String, Object[], Class[])} to determine the
	 * corresponding Method instance.
	 *
	 * @param  rTarget   The target object to invoke the method on
	 * @param  sMethod   The name of the public method to invoke
	 * @param  rArgs     The arguments of the method call (NULL for none)
	 * @param  rArgTypes The classes of the argument types or NULL if they shall
	 *                   be determined from the argument values (only possible
	 *                   if these are not NULL and have the exact types)
	 *
	 * @return The return value of the method call
	 *
	 * @throws IllegalArgumentException If no matching method could be found or
	 *                                  if the invocation fails
	 */
	public static Object invokePublic(Object	 rTarget,
									  String	 sMethod,
									  Object[]   rArgs,
									  Class<?>[] rArgTypes)
	{
		Method m =
			getPublicMethod(rTarget.getClass(), sMethod, rArgs, rArgTypes);

		return invoke(rTarget, m, rArgs);
	}

	/***************************************
	 * Invokes a public static no-argument method on a target object.
	 *
	 * @see #invokeStatic(Class, String, Object[], Class[])
	 */
	public static Object invokeStatic(Class<?> rClass, String sMethod)
	{
		return invokeStatic(rClass, sMethod, null, NO_ARGS);
	}

	/***************************************
	 * Invokes a public static method on a target class. Uses the method {@link
	 * #getPublicMethod(Class, String, Object[], Class[])} to determine the
	 * corresponding Method instance.
	 *
	 * @param  rClass    The target class to invoke the static method on
	 * @param  sMethod   The name of the public method to invoke
	 * @param  rArgs     The arguments of the method call (NULL for none)
	 * @param  rArgTypes The classes of the argument types or NULL if they shall
	 *                   be determined from the argument values (only possible
	 *                   if these are not NULL and have the exact types)
	 *
	 * @return The return value of the method call
	 *
	 * @throws IllegalArgumentException If no matching method could be found or
	 *                                  if the invocation fails
	 */
	public static Object invokeStatic(Class<?>   rClass,
									  String	 sMethod,
									  Object[]   rArgs,
									  Class<?>[] rArgTypes)
	{
		Method m = getPublicMethod(rClass, sMethod, rArgs, rArgTypes);

		return invoke(null, m, rArgs);
	}

	/***************************************
	 * A convenience method to invoke {@link Class#newInstance()} without the
	 * need for explicit exception handling.
	 *
	 * @param  rClass The class to invoke the no-argument constructor of
	 *
	 * @return A new instance of the given class
	 *
	 * @throws IllegalArgumentException If the instantiation fails
	 */
	public static <T> T newInstance(Class<T> rClass)
	{
		try
		{
			return rClass.newInstance();
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not create instance of " +
											   rClass,
											   e);
		}
	}

	/***************************************
	 * Creates a new instance by invoking a public constructor with a certain
	 * number of arguments. If the invocation fails an IllegalArgumentException
	 * will be thrown, initialized with the causing exception.
	 *
	 * @param  rConstructor The constructor to invoke
	 * @param  rArgs        The arguments of the call (NULL for none)
	 *
	 * @return The new instance created by the constructor
	 *
	 * @throws IllegalArgumentException If the invocation fails
	 */
	public static <T> T newInstance(Constructor<T> rConstructor, Object[] rArgs)
	{
		try
		{
			return rConstructor.newInstance(rArgs);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Constructor invocation failed",
											   e);
		}
	}

	/***************************************
	 * Creates a new instance by invoking a public constructor of a target
	 * class.
	 *
	 * @param  rClass    The target class to invoke the constructor of
	 * @param  rArgs     The arguments of the constructor call (NULL for none)
	 * @param  rArgTypes The classes of the argument types or NULL if they shall
	 *                   be determined from the argument values (only possible
	 *                   if these are not NULL and do not span class
	 *                   hierarchies)
	 *
	 * @return The new instance created by the constructor
	 *
	 * @throws IllegalArgumentException If no matching constructor could be
	 *                                  found or if the invocation fails
	 */
	public static <T> T newInstance(Class<T>   rClass,
									Object[]   rArgs,
									Class<?>[] rArgTypes)
	{
		if (rArgTypes == null)
		{
			rArgTypes = getArgumentTypes(rArgs);
		}

		try
		{
			Constructor<T> c = rClass.getConstructor(rArgTypes);

			return c.newInstance(rArgs);
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("No matching constructor: " +
											   rClass);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Method invocation failed", e);
		}
	}

	/***************************************
	 * Registers an interface implementation class. Registered implementations
	 * can then be queried with {@link #getImplementationClass(Class)}. Some
	 * implementations are registered automatically, e.g. for the Java
	 * collection framework. Applications can register their own mappings to
	 * allow the easy reflective creation of instances for which only the
	 * interface type is known.
	 *
	 * @param rInterface      The interface class
	 * @param rImplementation The implementation class to be associated with the
	 *                        interface
	 */
	public static <I> void registerImplementation(
		Class<I>		   rInterface,
		Class<? extends I> rImplementation)
	{
		aInterfaceImplementationMap.put(rInterface, rImplementation);
	}
}
