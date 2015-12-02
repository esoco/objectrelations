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

import de.esoco.lib.expression.ElementAccess;
import de.esoco.lib.expression.ElementAccessFunction;
import de.esoco.lib.reflect.ReflectUtil;

import java.util.List;
import java.util.Map;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;


/********************************************************************
 * An abstract function that retrieves certain elements from input objects. What
 * exactly such elements are depends on the actual subclass implementations.
 * Some typical kinds of get functions are implemented as inner classes. The
 * generic parameters designate the following types:
 *
 * <ul>
 *   <li>I: The type of the input objects to access the elements in</li>
 *   <li>E: The element descriptor that is used to identify object elements</li>
 *   <li>O: The output type that is returned by {@link #evaluate(Object)}</li>
 * </ul>
 */
public abstract class GetElement<I, E, O>
	extends AbstractBinaryFunction<I, E, O>
	implements ElementAccessFunction<E, I, O>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new immutable instance with a certain element descriptor and
	 * description.
	 *
	 * @param rElementDescriptor The element descriptor
	 * @param sDescription       The function description
	 *
	 * @see   AbstractBinaryFunction#AbstractBinaryFunction(Object, String)
	 */
	public GetElement(E rElementDescriptor, String sDescription)
	{
		super(rElementDescriptor, sDescription);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the element value from the given object. Invokes the abstract
	 * method {@link #getElementValue(Object, Object)} which must be implemented
	 * by subclasses.
	 *
	 * @see GetElement#evaluate(Object)
	 */
	@Override
	public final O evaluate(I rObject, E rElementDescriptor)
	{
		return getElementValue(rObject, rElementDescriptor);
	}

	/***************************************
	 * @see ElementAccess#getElementDescriptor()
	 */
	@Override
	public E getElementDescriptor()
	{
		return getRightValue();
	}

	/***************************************
	 * This method must be implemented by subclasses to returns the actual
	 * element value.
	 *
	 * @param  rObject            The object to read the element value from
	 * @param  rElementDescriptor The element descriptor
	 *
	 * @return The value of the element
	 */
	protected abstract O getElementValue(I rObject, E rElementDescriptor);

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An element accessor that uses reflection to query the value of a certain
	 * field in target objects by invoking the associated "get" method. This is
	 * done by means of the {@link ReflectUtil#invokePublic(Object, String)}
	 * method.
	 *
	 * <p><b>Attention:</b> if the function's output type (O) is not of type
	 * Object the property value will be cast to that type at runtime. If it
	 * does not match that type a ClassCastException will be thrown.</p>
	 */
	public static class GetField<I, O> extends GetElement<I, String, O>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that accesses a particular field through the
		 * associated "get" method.
		 *
		 * @param sField The name of the property to access
		 */
		public GetField(String sField)
		{
			super(getFieldAccessMethodName(sField),
				  getFieldAccessMethodName(sField));
		}

		//~ Static methods -----------------------------------------------------

		/***************************************
		 * Helper method to return the field access method name.
		 *
		 * @param  sField The field name
		 *
		 * @return The field access method name
		 */
		private static String getFieldAccessMethodName(String sField)
		{
			return "get" +
				   sField.substring(0, 1).toUpperCase() + sField.substring(1);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		protected O getElementValue(I rObject, String sFieldQueryMethod)
		{
			return (O) ReflectUtil.invokePublic(rObject, sFieldQueryMethod);
		}
	}

	/********************************************************************
	 * An element accessor that returns a certain element from a list.
	 */
	public static class GetListElement<O>
		extends GetElement<List<O>, Integer, O>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that returns a particular list element.
		 *
		 * @param nIndex The index of the element to return
		 */
		@SuppressWarnings("boxing")
		public GetListElement(int nIndex)
		{
			super(nIndex, "GetListElement");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected O getElementValue(List<O> rList, Integer rIndex)
		{
			return rList.get(rIndex.intValue());
		}
	}

	/********************************************************************
	 * An element accessor that returns a certain value from a map.
	 */
	public static class GetMapValue<K, V> extends GetElement<Map<K, V>, K, V>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that returns a particular map value.
		 *
		 * @param rKey The key of the element to return
		 */
		public GetMapValue(K rKey)
		{
			super(rKey, "GetMapValue");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected V getElementValue(Map<K, V> rMap, K rKey)
		{
			return rMap.get(rKey);
		}
	}

	/********************************************************************
	 * An element access function that returns a particular relation from a
	 * {@link Relatable} object.
	 */
	public static class GetRelation<I extends Relatable, O>
		extends GetElement<I, RelationType<O>, Relation<O>>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that accesses a particular single-type
		 * relation.
		 *
		 * @param rType The single-relation type to access
		 */
		public GetRelation(RelationType<O> rType)
		{
			super(rType, "GetRelation");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected Relation<O> getElementValue(I rObject, RelationType<O> rType)
		{
			return rObject.getRelation(rType);
		}
	}

	/********************************************************************
	 * An element access function that returns the value of a particular
	 * relation from a {@link Relatable} object.
	 */
	public static class GetRelationValue<I extends Relatable, O>
		extends GetElement<I, RelationType<O>, O>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that accesses a particular single-type
		 * relation.
		 *
		 * @param rType The single-relation type to access
		 */
		public GetRelationValue(RelationType<O> rType)
		{
			super(rType, "GetRelationValue");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected O getElementValue(I rObject, RelationType<O> rType)
		{
			return rObject.get(rType);
		}
	}

	/********************************************************************
	 * An element accessor that uses reflection to read the value of a certain
	 * field in target objects. The reflective access is done through the method
	 * {@link ReflectUtil#getFieldValue(String, Object)} which will try to make
	 * the field accessible if necessary. If that fails an exception will be
	 * thrown by {@link #getElementValue(Object, String)}.
	 *
	 * <p><b>Attention:</b> if the function's output type (O) is not of type
	 * Object the field value will be cast to that type at runtime. If it does
	 * not match that type a ClassCastException will be thrown.</p>
	 */
	public static class ReadField<I, O> extends GetElement<I, String, O>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that accesses a particular field.
		 *
		 * @param sFieldName The name of the field to access
		 */
		public ReadField(String sFieldName)
		{
			super(sFieldName, "ReadField");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		protected O getElementValue(I rObject, String sFieldName)
		{
			return (O) ReflectUtil.getFieldValue(sFieldName, rObject);
		}
	}
}
