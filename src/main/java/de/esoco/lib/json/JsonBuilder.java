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
package de.esoco.lib.json;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.json.Json.JsonStructure;
import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextConvert.IdentifierStyle;

import java.lang.reflect.Array;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.type.ListenerTypes;

import static de.esoco.lib.json.Json.JSON_DATE_FORMAT;

import static org.obrel.type.MetaTypes.IMMUTABLE;


/********************************************************************
 * A builder for JSON strings that can append arbitrary data objects to a JSON
 * string. This includes support for {@link Relatable} and {@link Relation}
 * objects. If such an object is appended either all or the given relations of
 * that object will be appended to the JSON string. This includes the recursive
 * evaluation for other relatable objects that are referenced from relations.
 *
 * @author eso
 * @see    JsonParser
 * @see    Json
 */
public class JsonBuilder
{
	//~ Static fields/initializers ---------------------------------------------

	private static final ConvertJson CONVERT_JSON = new ConvertJson();

	@SuppressWarnings("boxing")
	private static final Predicate<Relation<?>> IS_NOT_TRANSIENT =
		r -> !r.getType().hasModifier(RelationTypeModifier.TRANSIENT);

	private static final Collection<RelationType<?>> DEFAULT_EXCLUDED_RELATION_TYPES =
		CollectionUtil.<RelationType<?>>setOf(
			ListenerTypes.RELATION_LISTENERS,
			ListenerTypes.RELATION_TYPE_LISTENERS,
			ListenerTypes.RELATION_UPDATE_LISTENERS,
			IMMUTABLE);

	//~ Instance fields --------------------------------------------------------

	private StringBuilder aJson = new StringBuilder();

	private String sIndent		  = "";
	private String sCurrentIndent = sIndent;

	private boolean bWhitespace		    = true;
	private boolean bMultiLine		    = true;
	private boolean bRecursiveRelations = false;
	private boolean bNamespaces		    = false;

	private Collection<RelationType<?>> aExcludedRelationTypes =
		new HashSet<>(DEFAULT_EXCLUDED_RELATION_TYPES);

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that creates JSON without indentations.
	 */
	public JsonBuilder()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a function that builds a JSON string from arbitrary input
	 * objects.
	 *
	 * @param  sIndent The indentation of generated JSON objects (empty string
	 *                 for none)
	 *
	 * @return A function that converts objects into JSON
	 */
	public static <T> Function<T, String> buildJson(String sIndent)
	{
		return rValue ->
			   new JsonBuilder().indent(sIndent)
								.append(rValue)
								.toString();
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
		else if (rValue instanceof JsonSerializable)
		{
			((JsonSerializable<?>) rValue).appendTo(this);
		}
		else if (rValue instanceof Boolean || rValue instanceof Number)
		{
			aJson.append(rValue.toString());
		}
		else if (rValue instanceof Date)
		{
			appendString(JSON_DATE_FORMAT.format((Date) rValue));
		}
		else if (rValue.getClass().isArray())
		{
			if (rValue.getClass().getComponentType().isPrimitive())
			{
				int		 nCount		    = Array.getLength(rValue);
				Object[] aWrappedValues = new Object[nCount];

				for (int i = 0; i < nCount; i++)
				{
					aWrappedValues[i] = Array.get(rValue, i);
				}

				rValue = aWrappedValues;
			}

			appendArray(Arrays.asList((Object[]) rValue));
		}
		else if (rValue instanceof Iterable)
		{
			appendArray((Iterable<?>) rValue);
		}
		else if (rValue instanceof Map)
		{
			appendObject((Map<?, ?>) rValue);
		}
		else if (rValue instanceof RelationType)
		{
			appendString(Json.escape(rValue.toString()));
		}
		else if (bRecursiveRelations && rValue instanceof Relatable)
		{
			appendRelatable((Relatable) rValue, null, bRecursiveRelations);
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

			appendString(Json.escape(sValue));
		}

		return this;
	}

	/***************************************
	 * Appends a relation of a {@link Relatable} object to a JSON string
	 * builder.
	 *
	 * @param  rRelation         The relation to append
	 * @param  eNamingStyle      The style for converting relation type names to
	 *                           JSON properties
	 * @param  bAppendNullValues TRUE if NULL values should be appended, FALSE
	 *                           if they should be omitted
	 *
	 * @return TRUE if a relation has been appended (can only be FALSE if
	 *         bAppendNullValues is FALSE)
	 */
	public boolean append(Relation<?>	  rRelation,
						  IdentifierStyle eNamingStyle,
						  boolean		  bAppendNullValues)
	{
		Object  rValue    = rRelation.getTarget();
		boolean bHasValue = (rValue != null || bAppendNullValues);

		if (bHasValue)
		{
			RelationType<?> rRelationType = rRelation.getType();
			String		    sName		  = rRelationType.getSimpleName();

			if (eNamingStyle != IdentifierStyle.UPPERCASE)
			{
				sName = TextConvert.convertTo(eNamingStyle, sName);
			}

			if (bNamespaces)
			{
				String sNamespace = rRelationType.getNamespace();

				if (!sNamespace.isEmpty())
				{
					sName = sNamespace + '.' + sName;
				}
			}

			appendName(sName);
			append(rValue);
		}

		return bHasValue;
	}

	/***************************************
	 * Appends values from an iterable object as a JSON array.
	 *
	 * @param  rElements The iterable object to append (may be empty or NULL)
	 *
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendArray(Iterable<?> rElements)
	{
		aJson.append(JsonStructure.ARRAY.cOpen);

		if (rElements != null)
		{
			Iterator<?> rIterator = rElements.iterator();
			boolean     bHasNext  = rIterator.hasNext();

			while (bHasNext)
			{
				append(rIterator.next());
				bHasNext = rIterator.hasNext();

				if (bHasNext)
				{
					aJson.append(',');

					if (bWhitespace)
					{
						aJson.append(' ');
					}
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
		appendString(sName);
		aJson.append(':');

		if (bWhitespace)
		{
			aJson.append(' ');
		}

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
					newLine();
				}
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
	 * @param  bRecursive     TRUE to recursively append all relatables stored
	 *                        in the relations of the object
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #appendRelations(Relatable, Collection)
	 */
	public JsonBuilder appendRelatable(
		Relatable					rObject,
		Collection<RelationType<?>> rRelationTypes,
		boolean						bRecursive)
	{
		bRecursiveRelations = bRecursive;

		beginObject();
		appendRelations(rObject, rRelationTypes);
		endObject();

		return this;
	}

	/***************************************
	 * Appends the relations of a {@link Relatable} object to this JSON string.
	 * If enabled references to other related objects are converted recursively.
	 * If no explicit relation types are provided all relations of the object
	 * will be converted to JSON. In that case it is necessary that there are no
	 * cycles in the relations, i.e. objects referring each other either
	 * directly (like in parent-child relationships) or indirectly. It is
	 * possible to exclude certain relation types from the processing by
	 * indicating them through {@link #exclude(RelationType)}.
	 *
	 * <p>Alternatively the object can specify the relation types to serialize
	 * with the annotation {@link Json#JSON_SERIALIZED_TYPES}. If that is
	 * present only the relation types stored therein will be serialized.</p>
	 *
	 * <p>Furthermore all relation values in the source object must be
	 * compatible with JSON. That means they must either have a datatype that
	 * can be converted directly or valid a string representation. The latter
	 * can be achieved by registering a global string conversion through {@link
	 * Conversions#registerStringConversion(Class,InvertibleFunction)}. If these
	 * requirements are not met the resulting JSON string will probably not be
	 * parseable by the class {@link JsonParser}.</p>
	 *
	 * @param  rObject        The object to append the relations of
	 * @param  rRelationTypes The types of the relation to be converted to JSON
	 *                        (NULL for all)
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("boxing")
	public JsonBuilder appendRelations(
		Relatable					rObject,
		Collection<RelationType<?>> rRelationTypes)
	{
		IdentifierStyle eNamingStyle = rObject.get(Json.JSON_PROPERTY_NAMING);

		if (rRelationTypes == null &&
			rObject.hasRelation(Json.JSON_SERIALIZED_TYPES))
		{
			rRelationTypes = rObject.get(Json.JSON_SERIALIZED_TYPES);

			if (eNamingStyle == null)
			{
				eNamingStyle = IdentifierStyle.LOWER_CAMELCASE;
			}
		}
		else if (eNamingStyle == null)
		{
			eNamingStyle = IdentifierStyle.UPPERCASE;
		}

		Predicate<Relation<?>> pMatchesType;

		if (rRelationTypes != null)
		{
			Collection<RelationType<?>> rTypes = rRelationTypes;

			pMatchesType = r -> rTypes.contains(r.getType());
		}
		else
		{
			pMatchesType = r -> !aExcludedRelationTypes.contains(r.getType());
		}

		appendRelations(
			rObject.getRelations(IS_NOT_TRANSIENT.and(pMatchesType)),
			eNamingStyle,
			true);

		return this;
	}

	/***************************************
	 * Appends a string value by wrapping it in the JSON string delimiters. The
	 * string will not be escaped. That need to be done separately by invoking
	 * {@link Json#escape(String)} if necessary.
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
	 * Disables the addition of whitespace and linefeeds to create compact
	 * output.
	 *
	 * @return This instance for fluent invocation
	 *
	 * @see    #noWhitespace()
	 * @see    #noLinefeeds()
	 */
	public JsonBuilder compact()
	{
		return noWhitespace().noLinefeeds();
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
			sCurrentIndent.substring(
				0,
				sCurrentIndent.length() - sIndent.length());
		newLine();
		aJson.append(JsonStructure.OBJECT.cClose);

		return this;
	}

	/***************************************
	 * Excludes a certain relation type from the relation-based conversions in
	 * this class. A relation type added through this method will be ignored by
	 * {@link JsonBuilder#appendRelations(Relatable, Collection)} if this method
	 * is invoked without an explicit list of relation types. This class already
	 * contains a default list of relation types that either have no meaningful
	 * JSON representation or would prevent the JSON generation. Appending a
	 * single single relation explicitly is not affected by this setting.
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
	 * Sets the indent of this builder.
	 *
	 * @param  sIndent The indentation string to be prefixed per level
	 *
	 * @return This instance for concatenation
	 */
	public JsonBuilder indent(String sIndent)
	{
		this.sIndent = sIndent;

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
	 * Disables the adding of linefeeds to the generated JSON.
	 *
	 * @return This instance for fluent invocation
	 */
	public JsonBuilder noLinefeeds()
	{
		this.bMultiLine = false;

		return this;
	}

	/***************************************
	 * Disables the adding of whitespace to the generated JSON.
	 *
	 * @return This instance for fluent invocation
	 */
	public JsonBuilder noWhitespace()
	{
		this.bWhitespace = false;

		return this;
	}

	/***************************************
	 * Converts a value into a JSON string.
	 *
	 * @param  rValue The value to convert
	 *
	 * @return The JSON string
	 */
	public String toJson(Object rValue)
	{
		JsonBuilder aJsonBuilder = new JsonBuilder();

		if (rValue instanceof JsonSerializable)
		{
			((JsonSerializable<?>) rValue).appendTo(aJsonBuilder);
		}
		else if (rValue instanceof Relatable)
		{
			aJsonBuilder.appendRelatable((Relatable) rValue, null, false);
		}
		else
		{
			aJsonBuilder.append(rValue);
		}

		return aJsonBuilder.toString();
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
	 * Enables the addition of namespaces to the names of properties created
	 * from relation types. The namespace will always be lower case names
	 * separated by dots, independent of the {@link IdentifierStyle} used for
	 * the actual property name.
	 *
	 * <p>By default this option is disabled.</p>
	 *
	 * @return This instance for fluent invocation
	 */
	public JsonBuilder withNamespaces()
	{
		this.bNamespaces = true;

		return this;
	}

	/***************************************
	 * Appends a collection of relations to this JSON string.
	 *
	 * @param  rRelations        The relations to append
	 * @param  eNamingStyle      The style for converting relation type names to
	 *                           JSON properties
	 * @param  bAppendNullValues TRUE if NULL values should be appended, FALSE
	 *                           if they should be omitted
	 *
	 * @return This instance for concatenation
	 */
	private JsonBuilder appendRelations(
		Collection<Relation<?>> rRelations,
		IdentifierStyle			eNamingStyle,
		boolean					bAppendNullValues)
	{
		int nCount = rRelations.size();

		for (Relation<?> rRelation : rRelations)
		{
			append(rRelation, eNamingStyle, bAppendNullValues);

			if (--nCount > 0)
			{
				aJson.append(',');
				newLine();
			}
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
		if (bMultiLine)
		{
			aJson.append('\n');
			aJson.append(sCurrentIndent);
		}

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
			return new JsonParser().parse(sJson);
		}
	}
}
