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
package de.esoco.lib.json;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.predicate.AbstractPredicate;
import de.esoco.lib.json.JsonUtil.JsonStructure;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;

import static de.esoco.lib.json.JsonUtil.JSON_DATE_FORMAT;

import static org.obrel.type.MetaTypes.IMMUTABLE;
import static org.obrel.type.StandardTypes.RELATION_LISTENERS;
import static org.obrel.type.StandardTypes.RELATION_TYPE_LISTENERS;
import static org.obrel.type.StandardTypes.RELATION_UPDATE_LISTENERS;


/********************************************************************
 * A builder for JSON strings that can append arbitrary data objects to a JSON
 * string. This includes support for {@link Relatable} and {@link Relation}
 * objects. If such an object is appended by invoking the method {@link
 * #appendObject(Relatable, Collection, boolean)} all given relations of that
 * object will be appended to the JSON string by invoking {@link
 * #append(Relation, boolean, boolean)}. This includes the recursive evaluation
 * for other relatable objects that are referenced from relations.
 *
 * @author eso
 * @see    JsonParser
 * @see    JsonUtil
 */
public class JsonBuilder
{
	//~ Static fields/initializers ---------------------------------------------

	private static final ConvertJson CONVERT_JSON = new ConvertJson();

	private static final Collection<RelationType<?>> DEFAULT_EXCLUDED_RELATION_TYPES =
		CollectionUtil.<RelationType<?>>setOf(RELATION_LISTENERS,
											  RELATION_TYPE_LISTENERS,
											  RELATION_UPDATE_LISTENERS,
											  IMMUTABLE);

	//~ Instance fields --------------------------------------------------------

	private final String sIndent;

	private StringBuilder aJson			 = new StringBuilder();
	private String		  sCurrentIndent = "";
	private boolean		  bRecursive     = false;

	private Collection<RelationType<?>> aExcludedRelationTypes =
		new HashSet<>(DEFAULT_EXCLUDED_RELATION_TYPES);

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that creates JSON without indentations.
	 */
	public JsonBuilder()
	{
		this("");
	}

	/***************************************
	 * Creates a new instance with a certain indentation per hierarchy level.
	 *
	 * @param sIndent The indentation string to be prefixed per level
	 */
	public JsonBuilder(String sIndent)
	{
		this.sIndent = sIndent;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a function that builds a JSON string from arbitrary input
	 * objects.
	 *
	 * @return A function that converts objects into JSON
	 */
	public static <T> Function<T, String> buildJson()
	{
		return rValue -> new JsonBuilder().append(rValue).toString();
	}

	/***************************************
	 * Returns an invertible function that converts objects into JSON strings
	 * and in inverted form parses JSON strings into objects.
	 *
	 * @return An instance of {@link ConvertJson}
	 */
	public static InvertibleFunction<Object, String> convertJson()
	{
		return CONVERT_JSON;
	}

	/***************************************
	 * Converts a value into a JSON string. If the value is a relatable object
	 * it's relations will also be converted but not recursively.
	 *
	 * @param  rValue The value to convert
	 *
	 * @return The JSON string
	 */
	public static String toJson(Object rValue)
	{
		JsonBuilder aJsonBuilder = new JsonBuilder();

		if (rValue instanceof Relatable)
		{
			Collection<RelationType<?>> rRelationTypes =
				CollectionUtil.map(((Relatable) rValue).getRelations(),
								   r -> r.getType());

			aJsonBuilder.appendObject((Relatable) rValue,
									  rRelationTypes,
									  false);
		}
		else
		{
			aJsonBuilder.append(rValue);
		}

		return aJsonBuilder.toString();
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Appends a value to a JSON string builder and converts it according to
	 * it's datatype.
	 *
	 * @param  rValue The value to append (can be NULL)
	 *
	 * @return This instance for concatenation
	 */
	public JsonBuilder append(Object rValue)
	{
		if (rValue == null)
		{
			aJson.append("null");
		}
		else
		{
			Class<?> rDatatype = rValue.getClass();

			if (rDatatype == Boolean.class ||
				Number.class.isAssignableFrom(rDatatype))
			{
				aJson.append(rValue.toString());
			}
			else if (Collection.class.isAssignableFrom(rDatatype))
			{
				appendArray((Collection<?>) rValue);
			}
			else if (Map.class.isAssignableFrom(rDatatype))
			{
				Map<?, ?> rMap = (Map<?, ?>) rValue;

				appendObject(rMap);
			}
			else if (Date.class.isAssignableFrom(rDatatype))
			{
				appendString(JSON_DATE_FORMAT.format((Date) rValue));
			}
			else if (RelationType.class.isAssignableFrom(rDatatype))
			{
				appendString(JsonUtil.escape(rValue.toString()));
			}
			else if (bRecursive && Relatable.class.isAssignableFrom(rDatatype))
			{
				appendObject((Relatable) rValue, null, true);
			}
			else
			{
				String sValue;

				try
				{
					sValue = Conversions.asString(rValue);
				}
				catch (Exception e)
				{
					// if conversion not possible use toString()
					sValue = rValue.toString();
				}

				appendString(JsonUtil.escape(sValue));
			}
		}

		return this;
	}

	/***************************************
	 * Appends a relation of a {@link Relatable} object to a JSON string
	 * builder.
	 *
	 * @param  rRelation         The relation to append
	 * @param  bWithNamespace    TRUE to include the relation type namespace,
	 *                           FALSE to only use it's simple name
	 * @param  bAppendNullValues TRUE if NULL values should be appended, FALSE
	 *                           if they should be omitted
	 *
	 * @return TRUE if a relation has been appended (can only be FALSE if
	 *         bAppendNullValues is FALSE)
	 */
	public boolean append(Relation<?> rRelation,
						  boolean	  bWithNamespace,
						  boolean	  bAppendNullValues)
	{
		Object  rValue    = rRelation.getTarget();
		boolean bHasValue = (rValue != null || bAppendNullValues);

		if (bHasValue)
		{
			RelationType<?> rRelationType = rRelation.getType();

			String sName =
				bWithNamespace ? rRelationType.getName()
							   : rRelationType.getSimpleName();

			appendName(sName);
			append(rValue);
		}

		return bHasValue;
	}

	/***************************************
	 * Appends a collection of values to a string builder as a JSON array.
	 *
	 * @param  rCollection The collection to append (may be empty or NULL)
	 *
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendArray(Collection<?> rCollection)
	{
		aJson.append(JsonStructure.ARRAY.cOpen);

		if (rCollection != null && !rCollection.isEmpty())
		{
			int nCount = rCollection.size();

			for (Object rElement : rCollection)
			{
				append(rElement);

				if (--nCount > 0)
				{
					aJson.append(", ");
				}
			}
		}

		aJson.append(JsonStructure.ARRAY.cClose);

		return this;
	}

	/***************************************
	 * Appends a JSON attribute name to a string builder. Attribute names will
	 * not be escaped because it is assumed that they do not contain JSON
	 * control characters.
	 *
	 * @param  sName The attribute name
	 *
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendName(String sName)
	{
		appendString(sName).appendText(": ");

		return this;
	}

	/***************************************
	 * Appends a mapping of key/value pairs to this instance as a JSON object.
	 *
	 * @param  rMap The key/value mapping to append (may be empty or NULL)
	 *
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendObject(Map<?, ?> rMap)
	{
		beginObject();

		if (rMap != null && !rMap.isEmpty())
		{
			int nCount = rMap.size();

			for (Entry<?, ?> rEntry : rMap.entrySet())
			{
				appendName(rEntry.getKey().toString());
				append(rEntry.getValue());

				if (--nCount > 0)
				{
					aJson.append(',');
				}

				newLine();
			}
		}

		endObject();

		return this;
	}

	/***************************************
	 * Appends the relations of a {@link Relatable} object to this JSON string
	 * as a JSON object structure. See {@link #appendRelations(Relatable,
	 * Collection)} for details about how the relations are appended.
	 *
	 * <p>The boolean parameter defines whether this method will be applied
	 * recursively to relatable objects in relation or if only their string
	 * representation will be appended. Using recursion should be used with
	 * caution as circular references can cause stack overflows.</p>
	 *
	 * @param  rObject        The object to append the relations of
	 * @param  rRelationTypes The types of the relation to be converted to JSON
	 *                        (NULL for all)
	 * @param  bRecursive     TRUE to recursively
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #appendRelations(Relatable, Collection)
	 */
	public JsonBuilder appendObject(Relatable					rObject,
									Collection<RelationType<?>> rRelationTypes,
									boolean						bRecursive)
	{
		beginObject();
		this.bRecursive = bRecursive;
		appendRelations(rObject, rRelationTypes);
		this.bRecursive = false;
		endObject();

		return this;
	}

	/***************************************
	 * Appends the relations of a {@link Relatable} object to this JSON string.
	 * References to other related objects are converted recursively. If no
	 * explicit relation types are provided all relations of the object will be
	 * converted to JSON. In that case it is necessary that there are no cycles
	 * in the relations, i.e. objects referring each other directly (like in
	 * parent-child relationships) or indirectly. It is possible to exclude
	 * certain relation types from the processing by indicating them through
	 * {@link #exclude(RelationType)}.
	 *
	 * <p>Furthermore all relations in the source object must be convertible to
	 * strings, i.e. should either have a basic datatype or a conversion to
	 * string registered with {@link
	 * Conversions#registerStringConversion(Class,InvertibleFunction)}. If not
	 * the resulting JSON string will probably not be parseable by the class
	 * {@link JsonParser}.</p>
	 *
	 * @param  rObject        The object to append the relations of
	 * @param  rRelationTypes The types of the relation to be converted to JSON
	 *                        (NULL for all)
	 *
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendRelations(
		Relatable					rObject,
		Collection<RelationType<?>> rRelationTypes)
	{
		Predicate<? super Relation<?>> pRelations = null;

		if (rRelationTypes != null)
		{
			pRelations =
				new AbstractPredicate<Relation<?>>("JsonRelationType")
				{
					@Override
					@SuppressWarnings("boxing")
					public Boolean evaluate(Relation<?> rRelation)
					{
						return rRelationTypes.contains(rRelation.getType());
					}
				};
		}
		else
		{
			pRelations =
				new AbstractPredicate<Relation<?>>("JsonRelationType")
				{
					@Override
					@SuppressWarnings("boxing")
					public Boolean evaluate(Relation<?> rRelation)
					{
						return !aExcludedRelationTypes.contains(rRelation
																.getType());
					}
				};
		}

		List<Relation<?>> rRelations = rObject.getRelations(pRelations);

		appendRelations(rRelations);

		return this;
	}

	/***************************************
	 * Appends a string value by wrapping it in the JSON string delimiters.
	 *
	 * @param  sStringValue sText The text string to append
	 *
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendString(String sStringValue)
	{
		aJson.append(JsonStructure.STRING.cOpen);
		aJson.append(sStringValue);
		aJson.append(JsonStructure.STRING.cClose);

		return this;
	}

	/***************************************
	 * Appends an arbitrary text. The caller is responsible that the resulting
	 * string is valid according to the JSON specification.
	 *
	 * @param  sText The text string to append
	 *
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendText(String sText)
	{
		aJson.append(sText);

		return this;
	}

	/***************************************
	 * Starts the output of a new JSON object by inserting the corresponding
	 * delimiter.
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #endObject()
	 */
	public JsonBuilder beginObject()
	{
		aJson.append(JsonStructure.OBJECT.cOpen);
		sCurrentIndent += sIndent;
		newLine();

		return this;
	}

	/***************************************
	 * Ends the output of the current JSON object by inserting the corresponding
	 * delimiter.
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #beginObject()
	 */
	public JsonBuilder endObject()
	{
		sCurrentIndent =
			sCurrentIndent.substring(0,
									 sCurrentIndent.length() -
									 sIndent.length());
		aJson.append(JsonStructure.OBJECT.cClose);

		return this;
	}

	/***************************************
	 * Excludes a certain relation type from the relation-based conversions in
	 * this class. A relation type added through this method will be ignored by
	 * {@link #appendRelations(Relatable, Collection)} if this method is invoked
	 * without an explicit list of relation types. This class already contains a
	 * default list of relation types that either have no meaningful JSON
	 * representation or would prevent the JSON generation. The single-relation
	 * method {@link #append(Relation, boolean, boolean)} is not affected by
	 * this setting.
	 *
	 * @param  rExcludedType The relation type to be excluded from the JSON
	 *                       generation
	 *
	 * @return This instance for concatenation
	 */
	public JsonBuilder exclude(RelationType<?> rExcludedType)
	{
		aExcludedRelationTypes.add(rExcludedType);

		return this;
	}

	/***************************************
	 * Returns the current length of the JSON string.
	 *
	 * @return The JSON string length
	 */
	public int length()
	{
		return aJson.length();
	}

	/***************************************
	 * Returns the current JSON string representation of this instance.
	 *
	 * @return The JSON string
	 */
	@Override
	public String toString()
	{
		return aJson.toString();
	}

	/***************************************
	 * Appends a collection of relations to this JSON string.
	 *
	 * @param  rRelations The relations to append
	 *
	 * @return This instance for concatenation
	 */
	private JsonBuilder appendRelations(Collection<Relation<?>> rRelations)
	{
		int nCount = rRelations.size();

		for (Relation<?> rRelation : rRelations)
		{
			append(rRelation, true, false);

			if (--nCount > 0)
			{
				aJson.append(',');
			}

			newLine();
		}

		return this;
	}

	/***************************************
	 * Appends a line break to the current JSON string to start a new line.
	 *
	 * @return This instance for concatenation
	 */
	private JsonBuilder newLine()
	{
		aJson.append('\n');
		aJson.append(sCurrentIndent);

		return this;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An invertible function to convert objects to ({@link #evaluate(Object)})
	 * and from ({@link #invert(String)} JSON. Can be subclassed to extend the
	 * base functionality.
	 *
	 * @author eso
	 */
	public static class ConvertJson
		implements InvertibleFunction<Object, String>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public String evaluate(Object rValue)
		{
			return new JsonBuilder().append(rValue).toString();
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public Object invert(String sJson)
		{
			return new JsonParser().parseValue(sJson);
		}
	}
}
