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

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;

import java.util.List;
import java.util.Map;

/**
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
	extends AbstractBinaryFunction<T, V, T> {

	private final E elementDescriptor;

	/**
	 * Creates a new immutable instance with a certain value, element
	 * descriptor, and description.
	 *
	 * @param elementDescriptor The element descriptor
	 * @param value             The right-side value to set on objects
	 * @param description       The function description
	 */
	public SetElement(E elementDescriptor, V value, String description) {
		super(value, description);

		this.elementDescriptor = elementDescriptor;
	}

	/**
	 * Returns the element value from the given object. Invokes the abstract
	 * method {@link #setElementValue(Object, Object, Object)} which must be
	 * implemented by subclasses.
	 *
	 * @see AbstractBinaryFunction#evaluate(Object, Object)
	 */
	@Override
	public final T evaluate(T object, V value) {
		setElementValue(elementDescriptor, object, value);

		return object;
	}

	/**
	 * This method must be implemented by subclasses to set the element value.
	 *
	 * @param elementDescriptor The element descriptor
	 * @param object            The object to set the element value on
	 * @param value             The value to set
	 */
	protected abstract void setElementValue(E elementDescriptor, T object,
		V value);

	/**
	 * An element function that sets a certain element in a list.
	 */
	public static class SetListElement<V>
		extends SetElement<List<V>, Integer, V> {

		/**
		 * Creates a new instance that sets a particular list element.
		 *
		 * @param index The index of the element to return
		 * @param value The value to set
		 */
		@SuppressWarnings("boxing")
		public SetListElement(int index, V value) {
			super(index, value, "SetListElement");
		}

		/**
		 * @see SetElement#setElementValue(Object, Object, Object)
		 */
		@Override
		protected void setElementValue(Integer index, List<V> list, V value) {
			list.set(index.intValue(), value);
		}
	}

	/**
	 * An element function that sets a certain value in a map.
	 */
	public static class SetMapValue<K, V> extends SetElement<Map<K, V>, K, V> {

		/**
		 * Creates a new instance that sets a particular map value.
		 *
		 * @param key   The key of the element to set
		 * @param value The value to set
		 */
		public SetMapValue(K key, V value) {
			super(key, value, "SetMapValue");
		}

		/**
		 * @see SetElement#setElementValue(Object, Object, Object)
		 */
		@Override
		protected void setElementValue(K key, Map<K, V> map, V value) {
			map.put(key, value);
		}
	}

	/**
	 * An element function that sets a certain relation in a {@link Relatable}
	 * object.
	 */
	public static class SetRelationValue<T extends Relatable, V>
		extends SetElement<T, RelationType<V>, V> {

		/**
		 * Creates a new instance that sets a particular relation.
		 *
		 * @param type  The type of the relation to set
		 * @param value The relation value to set
		 */
		public SetRelationValue(RelationType<V> type, V value) {
			super(type, value, "SetRelationValue");
		}

		/**
		 * @see SetElement#setElementValue(Object, Object, Object)
		 */
		@Override
		protected void setElementValue(RelationType<V> type, T object,
			V value) {
			object.set(type, value);
		}
	}

//	/**
//	 * An element function that uses reflection to write the value of a certain
//	 * field in target objects. The reflective access is done through the
//	 method
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
//		
//
//		/**
//		 * Creates a new instance that accesses a particular field.
//		 *
//		 * @param fieldName The name of the field to access
//		 * @param value	 TODO: DOCUMENT ME!
//		 */
//		public WriteField(String fieldName, V value)
//		{
//			super(fieldName, value, "ReadField[%s]");
//		}
//
//		
//
//		/**
//		 * @see SetElement#getElementValue(Object, Object)
//		 */
//		@Override
//		protected V getElementValue(String fieldName, T object)
//		{
//			return (V) ReflectUtil.getFieldValue(fieldName, object);
//		}
//	}
}
