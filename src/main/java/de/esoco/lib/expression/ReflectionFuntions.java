//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.expression;

import de.esoco.lib.expression.function.AbstractBinaryFunction;
import de.esoco.lib.expression.function.AbstractFunction;
import de.esoco.lib.expression.function.Cast;


/********************************************************************
 * Contains factory methods for reflection-related methods.
 *
 * @author eso
 */
public class ReflectionFuntions
{
	//~ Static fields/initializers ---------------------------------------------

	private static final Function<?, ?> GET_CLASS =
		new AbstractFunction<Object, Class<?>>("GetClass")
		{
			@Override
			public Class<?> evaluate(Object rObject)
			{
				return rObject.getClass();
			}
		};

	private static final Function<Class<?>, String> GET_CLASS_NAME =
		new AbstractFunction<Class<?>, String>("GetClassName")
		{
			@Override
			public String evaluate(Class<?> rClass)
			{
				return rClass.getName();
			}
		};

	private static final Function<Class<?>, String> GET_SIMPLE_NAME =
		new AbstractFunction<Class<?>, String>("GetSimpleName")
		{
			@Override
			public String evaluate(Class<?> rClass)
			{
				return rClass.getSimpleName();
			}
		};

	private static final Function<?, ?> NEW_INSTANCE =
		new AbstractFunction<Class<?>, Object>("NewInstance")
		{
			@Override
			public Object evaluate(Class<?> rClass)
			{
				try
				{
					return rClass.newInstance();
				}
				catch (Exception e)
				{
					throw new IllegalArgumentException(e);
				}
			}
		};

	private static final Function<?, ?> NEW_INSTANCE_OF_CLASS =
		newInstance().from(getObjectClass());

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private ReflectionFuntions()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a new instance of the {@link Cast} function for a certain target
	 * datatype.
	 *
	 * @param  rCastType The type that the function shall cast input values to
	 *
	 * @return A new function instance
	 */
	public static <I, O> BinaryFunction<I, Class<O>, O> cast(Class<O> rCastType)
	{
		return new Cast<I, O>(rCastType);
	}

	/***************************************
	 * Returns a static function that invokes {@link Class#getName()}.
	 *
	 * @return A static function instance
	 */
	public static final Function<Class<?>, String> getClassName()
	{
		return GET_CLASS_NAME;
	}

	/***************************************
	 * Returns a new binary function instance that returns the instance of a
	 * enum class for a certain name.
	 *
	 * @param  rEnumClass The enum class (right value of the function)
	 *
	 * @return The new function instance
	 */
	public static <E extends Enum<E>> BinaryFunction<String, Class<E>, E> getEnumValue(
		Class<E> rEnumClass)
	{
		return new AbstractBinaryFunction<String, Class<E>, E>(rEnumClass,
															   "getEnumValue")
		{
			@Override
			public E evaluate(String sName, Class<E> rEnumClass)
			{
				return Enum.valueOf(rEnumClass, sName);
			}
		};
	}

	/***************************************
	 * Returns a static function that invokes {@link Object#getClass()}.
	 *
	 * @return A static function instance
	 */
	@SuppressWarnings("unchecked")
	public static final <T> Function<T, Class<? extends T>> getObjectClass()
	{
		return (Function<T, Class<? extends T>>) GET_CLASS;
	}

	/***************************************
	 * Returns a static function that invokes {@link Class#getSimpleName()}.
	 *
	 * @return A static function instance
	 */
	public static final Function<Class<?>, String> getSimpleName()
	{
		return GET_SIMPLE_NAME;
	}

	/***************************************
	 * Returns a static function object that creates a new instance of a class
	 * that it receives as the input value.
	 *
	 * @return A static function instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<Class<? extends T>, T> newInstance()
	{
		return (Function<Class<? extends T>, T>) NEW_INSTANCE;
	}

	/***************************************
	 * Returns a new function object that creates a new instance of a certain
	 * class. This is a shortcut method for the concatenation of the methods
	 * {@link #newInstance()} and {@link Functions#value(Object)}.
	 *
	 * @param  rClass The class to create a new instance of
	 *
	 * @return A new function instance
	 */
	public static <T> Function<Object, T> newInstanceOf(Class<T> rClass)
	{
		return ReflectionFuntions.<T>newInstance()
								 .from(Functions.value(rClass));
	}

	/***************************************
	 * Returns a new function instance that will create a new instance of the
	 * class of the input object. This is a shortcut method for the
	 * concatenation of {@link #newInstance()} and {@link #getObjectClass()}.
	 *
	 * @return A new function instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<T, T> newInstanceOfClass()
	{
		return (Function<T, T>) NEW_INSTANCE_OF_CLASS;
	}
}
