//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'ObjectRelations' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.expression.predicate;

/********************************************************************
 * A predicate that checks the class of target objects.
 *
 * @author eso
 */
public abstract class ClassPredicate<T>
	extends AbstractBinaryPredicate<T, Class<?>>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that checks target objects for a certain class.
	 *
	 * @param rClass The class to check target objects against
	 * @param sToken The description of this predicate
	 */
	public ClassPredicate(Class<?> rClass, String sToken)
	{
		super(rClass, sToken);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Checks if the target is not NULL and it's class is equal to the check
	 * class of this predicate.
	 *
	 * @see AbstractBinaryPredicate#evaluate(Object, Object)
	 */
	@Override
	@SuppressWarnings("boxing")
	public final Boolean evaluate(T rTarget, Class<?> rClass)
	{
		return rTarget != null && checkClass(rTarget.getClass(), rClass);
	}

	/***************************************
	 * Returns the class checked by this predicate.
	 *
	 * @return The checked class
	 */
	public final Class<?> getCheckedClass()
	{
		return getRightValue();
	}

	/***************************************
	 * Must be implemented by subclasses to perform the actual class check.
	 *
	 * @param  rCheckClass The class of the object that is currently evaluated
	 * @param  rClass      The class to check the first argument against
	 *
	 * @return The result of the class check
	 */
	protected abstract boolean checkClass(
		Class<?> rCheckClass,
		Class<?> rClass);

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A class predicate that checks whether target objects have a particular
	 * base class by invoking the {@link Class#isAssignableFrom(Class)} method
	 * on the compare class with the class to be checked as the argument.
	 *
	 * @author eso
	 */
	public static class HasBaseClass<T> extends ClassPredicate<T>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * @see ClassPredicate#ClassPredicate(Class, String)
		 */
		public HasBaseClass(Class<?> rClass)
		{
			super(rClass, "HasBaseClass");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Invokes the {@link Class#isAssignableFrom(Class)} method on the
		 * second argument with the check class as the argument.
		 *
		 * @see ClassPredicate#checkClass(Class, Class)
		 */
		@Override
		protected boolean checkClass(Class<?> rCheckClass, Class<?> rClass)
		{
			return rClass.isAssignableFrom(rCheckClass);
		}
	}

	/********************************************************************
	 * A class predicate that checks whether target objects have a particular
	 * class by performing an identity comparison.
	 *
	 * @author eso
	 */
	public static class HasClass<T> extends ClassPredicate<T>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * @see ClassPredicate#ClassPredicate(Class, String)
		 */
		public HasClass(Class<?> rClass)
		{
			super(rClass, "HasClass");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Performs an identity comparison.
		 *
		 * @see ClassPredicate#checkClass(Class, Class)
		 */
		@Override
		protected boolean checkClass(Class<?> rCheckClass, Class<?> rClass)
		{
			return rCheckClass == rClass;
		}
	}
}
