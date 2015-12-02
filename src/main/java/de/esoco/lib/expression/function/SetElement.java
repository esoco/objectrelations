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

import java.util.List;
import java.util.Map;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;


/********************************************************************
 * An abstract function that sets a certain element in an input object. What
 * exactly these elements are depends on the actual subclass implementations.
 * Some typical kinds of get functions are implemented as inner classes. The
 * generic parameters designate the following types:
 *
 * <ul>
 *   <li>T: The type of the objects to set the element in; the object will also
 *     be returned as the output value to support function chaining</li>
 *   <li>V: the type of the value to set in an object</li>
 *   <li>E: The element descriptor that is used to identify object elements</li>
 * </ul>
 */
public abstract class SetElement<T, E, V>
	extends AbstractBinaryFunction<T, V, T>
{
	//~ Instance fields --------------------------------------------------------

	private final E rElementDescriptor;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new immutable instance with a certain value, element
	 * descriptor, and description.
	 *
	 * @param rElementDescriptor The element descriptor
	 * @param rValue             The right-side value to set on objects
	 * @param sDescription       The function description
	 */
	public SetElement(E rElementDescriptor, V rValue, String sDescription)
	{
		super(rValue, sDescription);

		this.rElementDescriptor = rElementDescriptor;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the element value from the given object. Invokes the abstract
	 * method {@link #setElementValue(Object, Object, Object)} which must be
	 * implemented by subclasses.
	 *
	 * @see AbstractBinaryFunction#evaluate(Object, Object)
	 */
	@Override
	public final T evaluate(T rObject, V rValue)
	{
		setElementValue(rElementDescriptor, rObject, rValue);

		return rObject;
	}

	/***************************************
	 * This method must be implemented by subclasses to set the element value.
	 *
	 * @param rElementDescriptor The element descriptor
	 * @param rObject            The object to set the element value on
	 * @param rValue             The value to set
	 */
	protected abstract void setElementValue(E rElementDescriptor,
											T rObject,
											V rValue);

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An element function that sets a certain element in a list.
	 */
	public static class SetListElement<V>
		extends SetElement<List<V>, Integer, V>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that sets a particular list element.
		 *
		 * @param nIndex The index of the element to return
		 * @param rValue The value to set
		 */
		@SuppressWarnings("boxing")
		public SetListElement(int nIndex, V rValue)
		{
			super(nIndex, rValue, "SetListElement");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see SetElement#setElementValue(Object, Object, Object)
		 */
		@Override
		protected void setElementValue(Integer rIndex, List<V> rList, V rValue)
		{
			rList.set(rIndex.intValue(), rValue);
		}
	}

	/********************************************************************
	 * An element function that sets a certain value in a map.
	 */
	public static class SetMapValue<K, V> extends SetElement<Map<K, V>, K, V>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that sets a particular map value.
		 *
		 * @param rKey   The key of the element to set
		 * @param rValue The value to set
		 */
		public SetMapValue(K rKey, V rValue)
		{
			super(rKey, rValue, "SetMapValue");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see SetElement#setElementValue(Object, Object, Object)
		 */
		@Override
		protected void setElementValue(K rKey, Map<K, V> rMap, V rValue)
		{
			rMap.put(rKey, rValue);
		}
	}

	/********************************************************************
	 * An element function that sets a certain relation in a {@link Relatable}
	 * object.
	 */
	public static class SetRelationValue<T extends Relatable, V>
		extends SetElement<T, RelationType<V>, V>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that sets a particular relation.
		 *
		 * @param rType  The type of the relation to set
		 * @param rValue The relation value to set
		 */
		public SetRelationValue(RelationType<V> rType, V rValue)
		{
			super(rType, rValue, "SetRelationValue");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see SetElement#setElementValue(Object, Object, Object)
		 */
		@Override
		protected void setElementValue(RelationType<V> rType,
									   T			   rObject,
									   V			   rValue)
		{
			rObject.set(rType, rValue);
		}
	}

//	/********************************************************************
//	 * An element function that uses reflection to write the value of a certain
//	 * field in target objects. The reflective access is done through the method
//	 * {@link ReflectUtil#setF} which will try to make
//	 * the field accessible if necessary. If that fails an exception will be
//	 * thrown by {@link #getElementValue(String, Object)}.
//	 *
//	 * <p><b>Attention:</b> if the function's output type (O) is not of type
//	 * Object the field value will be cast to that type at runtime. If it does
//	 * not match that type a ClassCastException will be thrown.</p>
//	 */
//	public static class WriteField<T, V> extends SetElement<T, String, V>
//	{
//		//~ Constructors -------------------------------------------------------
//
//		/***************************************
//		 * Creates a new instance that accesses a particular field.
//		 *
//		 * @param sFieldName The name of the field to access
//		 * @param rValue	 TODO: DOCUMENT ME!
//		 */
//		public WriteField(String sFieldName, V rValue)
//		{
//			super(sFieldName, rValue, "ReadField[%s]");
//		}
//
//		//~ Methods ------------------------------------------------------------
//
//		/***************************************
//		 * @see SetElement#getElementValue(Object, Object)
//		 */
//		@Override
//		protected V getElementValue(String sFieldName, T rObject)
//		{
//			return (V) ReflectUtil.getFieldValue(sFieldName, rObject);
//		}
//	}
}
