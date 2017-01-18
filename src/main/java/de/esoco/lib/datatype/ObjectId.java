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
package de.esoco.lib.datatype;

import java.io.Serializable;


/********************************************************************
 * A generic representation of a unique object identifier. How exactly an object
 * ID is defined and in which context it is unique depends on the concrete
 * subclasses. This base class defines the general properties of object IDs:
 *
 * <ol>
 *   <li><b>Each object ID must have a string representation that is considered
 *     to be equivalent to the object ID instance itself.</b> To enforce this
 *     fact the methods {@link #equals(Object)} and {@link #hashCode()} are
 *     overridden as final and are based on the result of the IDs {@link
 *     #toString()} method. It is advised to use a readable string
 *     representation for all implementations.</li>
 *   <li>An object ID represents a certain type of object that is indicated by
 *     the generic type parameter T. This type can be used by applications to
 *     define generic methods or classes that accept or return objects only if
 *     it matches the generic type of an ID that it is associated with. See
 *     below for a further explanation of the restrictions of this generic
 *     type.</li>
 *   <li>The internal representation of an object ID implementation is not used
 *     for the general handling of IDs. But for some applications, e.g. storage
 *     frameworks, it may be necessary to know the internal value of an ID. For
 *     such purposes the method {@link #internalValue()} provides access to that
 *     value. Implementations of this method should only return immutable
 *     objects.</li>
 * </ol>
 *
 * <p>Because of these properties it is recommended to reference even the
 * instances of subclasses only as {@link ObjectId}. To simplify this kind of
 * usage this class contains factory methods that create new ID instances for
 * different value types. This scheme of instance creation should be repeated by
 * application-specific ID implementations.</p>
 *
 * <p>The generic type of an object ID has no internal meaning for the ID
 * instance itself. It is only declarative and provided to reduce the necessity
 * of explicit type casts when using object ID constants to access the
 * associated objects. This will reduce possible class cast errors when a
 * referenced type changes because by using object IDs the types of values will
 * be enforced automatically on both setting and querying. To support this
 * generic object ID usage the corresponding interfaces must be implemented
 * cautiously. Most importantly they should implement symmetric generic methods
 * to set and query objects based on the generic type of an object ID to ensure
 * that the same type is used in both directions.</p>
 *
 * <p>Due to the generic type erasure any access based on the generic type of an
 * object ID will be replaced by a corresponding type cast. That means that
 * using a certain object ID to access an object of a different type will result
 * in a {@link ClassCastException} being thrown. Therefore, if the type is not
 * exactly known an unspecific ID type like {@literal ObjectId<?>} should be
 * used instead. Generally speaking, using an object ID with a specific type is
 * the same as applying a type cast and must therefore be performed with exactly
 * the same precautions.</p>
 *
 * @author eso
 */
public abstract class ObjectId<T> implements Serializable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	protected ObjectId()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method that returns an object ID for a certain integer value.
	 *
	 * @param  nId The integer value
	 *
	 * @return An object ID instance
	 */
	public static <T> ObjectId<T> intId(int nId)
	{
		return new IntegerId<T>(nId);
	}

	/***************************************
	 * Factory method that returns an object ID for a certain long value.
	 *
	 * @param  nId The long value
	 *
	 * @return An object ID instance
	 */
	public static <T> ObjectId<T> longId(long nId)
	{
		return new LongId<T>(nId);
	}

	/***************************************
	 * Factory method that returns an object ID for a certain string value.
	 *
	 * @param  sId The string value
	 *
	 * @return An object ID instance
	 */
	public static <T> ObjectId<T> stringId(String sId)
	{
		return new StringId<T>(sId);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Must be implemented to return the internal value of this instance. This
	 * method exists mainly to provide access to the internal ID representation
	 * for frameworks. Implementations should only return immutable objects. For
	 * primitive types an instance the corresponding wrapper class must be
	 * returned.
	 *
	 * @return The internal value of this instance
	 */
	public abstract Object internalValue();

	/***************************************
	 * Must be re-implemented by subclasses to provide a readable string
	 * representation of this ID instance.
	 *
	 * @see Object#toString()
	 */
	@Override
	public abstract String toString();

	/***************************************
	 * Compares this instance for equality with another object. Two object IDs
	 * are considered equal if their string representations are equal.
	 *
	 * @see Object#equals(Object)
	 */
	@Override
	public final boolean equals(Object rOther)
	{
		if (this == rOther)
		{
			return true;
		}

		if (rOther == null || getClass() != rOther.getClass())
		{
			return false;
		}

		return toString().equals(rOther.toString());
	}

	/***************************************
	 * Returns the hash code of this objects string representation.
	 *
	 * @see Object#hashCode()
	 */
	@Override
	public final int hashCode()
	{
		return toString().hashCode();
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An {@link ObjectId} implementation that is based on integer values.
	 *
	 * @author eso
	 */
	public static class IntegerId<T> extends ObjectId<T>
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Instance fields ----------------------------------------------------

		private final int nId;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param nId The ID value
		 */
		public IntegerId(int nId)
		{
			this.nId = nId;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see ObjectId#internalValue()
		 */
		@Override
		public Object internalValue()
		{
			return Integer.valueOf(nId);
		}

		/***************************************
		 * Returns the string representation of the integer value.
		 *
		 * @see ObjectId#toString()
		 */
		@Override
		public String toString()
		{
			return Integer.toString(nId);
		}
	}

	/********************************************************************
	 * An {@link ObjectId} implementation that is based on long integer values.
	 *
	 * @author eso
	 */
	public static class LongId<T> extends ObjectId<T>
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Instance fields ----------------------------------------------------

		private final long nId;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param nId The ID value
		 */
		public LongId(long nId)
		{
			this.nId = nId;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see ObjectId#internalValue()
		 */
		@Override
		public Object internalValue()
		{
			return Long.valueOf(nId);
		}

		/***************************************
		 * Returns the string representation of the long value.
		 *
		 * @see ObjectId#toString()
		 */
		@Override
		public String toString()
		{
			return Long.toString(nId);
		}
	}

	/********************************************************************
	 * An {@link ObjectId} implementation that is based on string values.
	 *
	 * @author eso
	 */
	public static class StringId<T> extends ObjectId<T>
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Instance fields ----------------------------------------------------

		private final String sId;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param sId The ID string
		 */
		public StringId(String sId)
		{
			this.sId = sId;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see ObjectId#internalValue()
		 */
		@Override
		public Object internalValue()
		{
			return sId;
		}

		/***************************************
		 * Returns the ID string.
		 *
		 * @see ObjectId#toString()
		 */
		@Override
		public String toString()
		{
			return sId;
		}
	}
}
