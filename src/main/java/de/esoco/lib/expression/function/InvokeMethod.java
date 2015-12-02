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
package de.esoco.lib.expression.function;

import de.esoco.lib.reflect.ReflectUtil;

import java.lang.reflect.Method;


/********************************************************************
 * Invokes a certain method on the target object (T) through reflection and
 * returns the result (R) of the method call.
 *
 * @author eso
 */
public class InvokeMethod<T, R> extends AbstractBinaryFunction<T, Object[], R>
{
	//~ Instance fields --------------------------------------------------------

	private final Method rMethod;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that will invoke a particular method without
	 * arguments on the target object.
	 *
	 * @param rMethod The method to invoke
	 */
	public InvokeMethod(Method rMethod)
	{
		this(rMethod, (Object[]) ReflectUtil.NO_ARGS);
	}

	/***************************************
	 * Creates a new instance that will invoke a particular method on the target
	 * object.
	 *
	 * @param rMethod The method to invoke
	 * @param rArgs   The arguments of the method call
	 */
	public InvokeMethod(Method rMethod, Object... rArgs)
	{
		super(rArgs,
			  rMethod.getDeclaringClass().getName() + "." + rMethod.getName());

		this.rMethod = rMethod;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Invokes this instance's method on the target object.
	 *
	 * @param  rTarget The target object to invoke the method on
	 * @param  rArgs   The arguments for the method call
	 *
	 * @return The result of the method call
	 */
	@Override
	@SuppressWarnings("unchecked")
	public R evaluate(T rTarget, Object[] rArgs)
	{
		return (R) ReflectUtil.invoke(rTarget, rMethod, rArgs);
	}

	/***************************************
	 * @see AbstractBinaryFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		return super.paramsEqual(rOther) &&
			   rMethod.equals(((InvokeMethod<?, ?>) rOther).rMethod);
	}

	/***************************************
	 * @see AbstractBinaryFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		return 31 * rMethod.hashCode() + super.paramsHashCode();
	}
}
