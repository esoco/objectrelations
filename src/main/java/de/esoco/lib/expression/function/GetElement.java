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
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;

import java.util.List;
import java.util.Map;

/**
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
	implements ElementAccessFunction<E, I, O> {

	/**
	 * Creates a new immutable instance with a certain element descriptor and
	 * description.
	 *
	 * @param elementDescriptor The element descriptor
	 * @param description       The function description
	 * @see AbstractBinaryFunction#AbstractBinaryFunction(Object, String)
	 */
	public GetElement(E elementDescriptor, String description) {
		super(elementDescriptor, description);
	}

	/**
	 * Returns the element value from the given object. Invokes the abstract
	 * method {@link #getElementValue(Object, Object)} which must be
	 * implemented
	 * by subclasses.
	 *
	 * @see GetElement#evaluate(Object)
	 */
	@Override
	public final O evaluate(I object, E elementDescriptor) {
		return getElementValue(object, elementDescriptor);
	}

	/**
	 * @see ElementAccess#getElementDescriptor()
	 */
	@Override
	public E getElementDescriptor() {
		return getRightValue();
	}

	/**
	 * This method must be implemented by subclasses to returns the actual
	 * element value.
	 *
	 * @param object            The object to read the element value from
	 * @param elementDescriptor The element descriptor
	 * @return The value of the element
	 */
	protected abstract O getElementValue(I object, E elementDescriptor);

	/**
	 * An element accessor that uses reflection to query the value of a certain
	 * field in target objects by invoking the associated "get" method. This is
	 * done by means of the {@link ReflectUtil#invokePublic(Object, String)}
	 * method.
	 *
	 * <p><b>Attention:</b> if the function's output type (O) is not of type
	 * Object the property value will be cast to that type at runtime. If it
	 * does not match that type a ClassCastException will be thrown.</p>
	 */
	public static class GetField<I, O> extends GetElement<I, String, O> {

		/**
		 * Creates a new instance that accesses a particular field through the
		 * associated "get" method.
		 *
		 * @param field The name of the property to access
		 */
		public GetField(String field) {
			super(getFieldAccessMethodName(field),
				getFieldAccessMethodName(field));
		}

		/**
		 * Helper method to return the field access method name.
		 *
		 * @param field The field name
		 * @return The field access method name
		 */
		private static String getFieldAccessMethodName(String field) {
			return "get" + field.substring(0, 1).toUpperCase() +
				field.substring(1);
		}

		/**
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		protected O getElementValue(I object, String fieldQueryMethod) {
			return (O) ReflectUtil.invokePublic(object, fieldQueryMethod);
		}
	}

	/**
	 * An element accessor that returns a certain element from a list.
	 */
	public static class GetListElement<O>
		extends GetElement<List<O>, Integer, O> {

		/**
		 * Creates a new instance that returns a particular list element.
		 *
		 * @param index The index of the element to return
		 */
		@SuppressWarnings("boxing")
		public GetListElement(int index) {
			super(index, "GetListElement");
		}

		/**
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected O getElementValue(List<O> list, Integer index) {
			return list.get(index.intValue());
		}
	}

	/**
	 * An element accessor that returns a certain value from a map.
	 */
	public static class GetMapValue<K, V> extends GetElement<Map<K, V>, K, V> {

		/**
		 * Creates a new instance that returns a particular map value.
		 *
		 * @param key The key of the element to return
		 */
		public GetMapValue(K key) {
			super(key, "GetMapValue");
		}

		/**
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected V getElementValue(Map<K, V> map, K key) {
			return map.get(key);
		}
	}

	/**
	 * An element access function that returns a particular relation from a
	 * {@link Relatable} object.
	 */
	public static class GetRelation<I extends Relatable, O>
		extends GetElement<I, RelationType<O>, Relation<O>> {

		/**
		 * Creates a new instance that accesses a particular single-type
		 * relation.
		 *
		 * @param type The single-relation type to access
		 */
		public GetRelation(RelationType<O> type) {
			super(type, "GetRelation");
		}

		/**
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected Relation<O> getElementValue(I object, RelationType<O> type) {
			return object.getRelation(type);
		}
	}

	/**
	 * An element access function that returns the value of a particular
	 * relation from a {@link Relatable} object.
	 */
	public static class GetRelationValue<I extends Relatable, O>
		extends GetElement<I, RelationType<O>, O> {

		/**
		 * Creates a new instance that accesses a particular single-type
		 * relation.
		 *
		 * @param type The single-relation type to access
		 */
		public GetRelationValue(RelationType<O> type) {
			super(type, "GetRelationValue");
		}

		/**
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected O getElementValue(I object, RelationType<O> type) {
			return object.get(type);
		}
	}

	/**
	 * An element accessor that uses reflection to read the value of a certain
	 * field in target objects. The reflective access is done through the
	 * method
	 * {@link ReflectUtil#getFieldValue(String, Object)} which will try to make
	 * the field accessible if necessary. If that fails an exception will be
	 * thrown by {@link #getElementValue(Object, String)}.
	 *
	 * <p><b>Attention:</b> if the function's output type (O) is not of type
	 * Object the field value will be cast to that type at runtime. If it does
	 * not match that type a ClassCastException will be thrown.</p>
	 */
	public static class ReadField<I, O> extends GetElement<I, String, O> {

		/**
		 * Creates a new instance that accesses a particular field.
		 *
		 * @param fieldName The name of the field to access
		 */
		public ReadField(String fieldName) {
			super(fieldName, "ReadField");
		}

		/**
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		protected O getElementValue(I object, String fieldName) {
			return (O) ReflectUtil.getFieldValue(fieldName, object);
		}
	}
}
