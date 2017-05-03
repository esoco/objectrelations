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
package org.obrel.core;

import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.json.JsonBuilder;
import de.esoco.lib.json.JsonParser;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;

import org.obrel.space.ObjectSpace;
import org.obrel.space.ObjectSpaceResolver;
import org.obrel.space.ObjectSpaceResolver.PutResolver;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;


/********************************************************************
 * A class containing static methods for the handling of object relations. This
 * comprises especially the handling of relations for objects that are not
 * subclasses of {@link RelatedObject}. For this purpose this class contains
 * static variants of the methods in the {@link Relatable} interface, with an
 * additional object parameter that defines an arbitrary target object.
 *
 * @author eso
 */
public class ObjectRelations
{
	//~ Static fields/initializers ---------------------------------------------

	private static final RelatedObject EMPTY_RELATION_CONTAINER =
		new RelatedObject();

	private static Map<Object, RelatedObject> aRelationContainerMap =
		new WeakHashMap<Object, RelatedObject>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private ObjectRelations()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Copies all relations from a certain object to another object. The
	 * relations will be copied recursively so that the relations of the source
	 * object's relations (also known as annotations) will be copied too. But
	 * the relation targets are only copied by reference. The invoking code is
	 * responsible to make sure that the duplication of target references won't
	 * cause problems.
	 *
	 * @param rSource  The source object to copy the relations from
	 * @param rTarget  The target object to copy the relations to
	 * @param bReplace TRUE to replace existing relations, FALSE to keep them
	 */
	public static void copyRelations(Relatable rSource,
									 Relatable rTarget,
									 boolean   bReplace)
	{
		((RelatedObject) rTarget).copyRelations((RelatedObject) rSource,
												bReplace);
	}

	/***************************************
	 * Parses a JSON string into the relations of a relatable object. The JSON
	 * string must be in a compatible format, e.g. like it is generated by
	 * {@link #toJson(Relatable, Collection)}.
	 *
	 * @param  rTarget The target for the parsed relations
	 * @param  sJson   The JSON string to parse
	 *
	 * @return The target object
	 */
	public static <T extends Relatable> T fromJson(T rTarget, String sJson)
	{
		return JsonParser.parseRelatable(sJson, rTarget);
	}

	/***************************************
	 * Returns a relatable object for an arbitrary object that can be used to
	 * store relations for that object. The returned {@link Relatable} instance
	 * will be available as long as the original object has not been garbage
	 * collected, even if the calling code does not keep a reference to the
	 * returned object. If the argument is a relatable object already it will be
	 * returned unchanged.
	 *
	 * @param  rForObject The object to return a relatable object for
	 *
	 * @return A {@link Relatable} instance associated with the argument object
	 */
	public static Relatable getRelatable(Object rForObject)
	{
		return getRelationContainer(rForObject, true);
	}

	/***************************************
	 * Globally initializes the object relations framework. This method should
	 * be invoked as early as possible by applications that use object
	 * relations. At the moment it only registers the standard relation types
	 * that are defined in the classes of the package {@code org.obrel.type} by
	 * means of the method {@link #registerRelationTypes(Class...)}. But future
	 * versions may perform additional tasks and therefore it is recommended to
	 * always invoke it.
	 */
	public static void init()
	{
		registerRelationTypes(RelationTypes.class,
							  MetaTypes.class,
							  StandardTypes.class);
	}

	/***************************************
	 * Registers all relation types that are defined in the given classes.
	 * Normally a relation type is registered automatically when it is used for
	 * the first time but there are possible situations where that may not have
	 * happened yet. A typical case is the deserialization of a related object
	 * which contains relations of a type class that hasn't been loaded at the
	 * time of deserialization.
	 *
	 * <p>For this reason applications should always register the relation types
	 * they use for all classes that contain application-defined relation types.
	 * This can be done either at initialization time (i.e. on application
	 * start) or (if possible) on demand when a type is accessed for the first
	 * time (e.g. by using a special relation type superclass with a static
	 * initializer that invokes this method).</p>
	 *
	 * <p>Because this method may be extended in the future to provide support
	 * for enhanced features (like distributed object spaces or remote
	 * relations) applications should always register relation types by invoking
	 * this method instead of simply loading the class that defines the types.
	 * </p>
	 *
	 * @param rClasses A list of classes to register all defined types of
	 */
	public static void registerRelationTypes(Class<?>... rClasses)
	{
		assert rClasses.length > 0 : "No classes to register";

		for (Class<?> rClass : rClasses)
		{
			try
			{
				// only accessing the class may not be sufficient because
				// ineffective code could be removed by compiler optimizations
				Class.forName(rClass.getName());
			}
			catch (ClassNotFoundException e)
			{
				// this should not be possible
				throw new IllegalStateException(e);
			}
		}
	}

	/***************************************
	 * Removes the relatable object that has been associated previously with a
	 * certain object. If no relatable object exists calling this method will
	 * have no effect.
	 *
	 * @param rForObject The object to remove the relatable object for
	 */
	public static void removeRelatable(Object rForObject)
	{
		aRelationContainerMap.remove(rForObject);
	}

	/***************************************
	 * Validates that certain relations exist on a target object or else throws
	 * an {@link IllegalArgumentException}.
	 *
	 * @see #require(Relatable, Predicate, RelationType...)
	 */
	public static void require(Relatable rRelatable, RelationType<?>... rTypes)
	{
		require(rRelatable, Predicates.alwaysTrue(), rTypes);
	}

	/***************************************
	 * Validates that certain relations exist on a target object and fulfill
	 * certain requirements or else throws an {@link IllegalArgumentException}.
	 * Non-existing relations with one of the given types will cause an
	 * immediate exception. All others will be tested with the argument
	 * predicate of which a return value of FALSE will also yield an exception.
	 *
	 * @param  rRelatable   The relatable to validate
	 * @param  pRequirement A predicate that tests whether a relation fulfills
	 *                      the requirements
	 * @param  rTypes       The relation types that must be set on the object
	 *                      with any kind of value, including NULL
	 *
	 * @throws IllegalArgumentException If one or more relations don't exist or
	 *                                  the requirement predicate yields FALSE
	 */
	public static void require(Relatable			  rRelatable,
							   Predicate<Relation<?>> pRequirement,
							   RelationType<?>...     rTypes)
	{
		Set<RelationType<?>> aMissingTypes = new LinkedHashSet<>();

		for (RelationType<?> rType : rTypes)
		{
			Relation<?> rRelation = rRelatable.getRelation(rType);

			if (rRelation == null || !pRequirement.test(rRelation))
			{
				aMissingTypes.add(rType);
			}
		}

		if (!aMissingTypes.isEmpty())
		{
			throw new IllegalArgumentException("Relations missing: " +
											   aMissingTypes);
		}
	}

	/***************************************
	 * Validates that certain relations exist on a target object with a non-null
	 * value or else throws an {@link IllegalArgumentException}.
	 *
	 * @see #require(Relatable, Predicate, RelationType...)
	 */
	@SuppressWarnings("boxing")
	public static void requireNonNull(
		Relatable		   rRelatable,
		RelationType<?>... rTypes)
	{
		require(rRelatable, r -> r.getTarget() != null, rTypes);
	}

	/***************************************
	 * A helper method to set a certain relation on multiple objects.
	 *
	 * @param rType    The type of the relation to set
	 * @param rTarget  The relation target value
	 * @param rObjects The objects to set the relation on
	 */
	public static <T> void setAll(RelationType<T> rType,
								  T				  rTarget,
								  Relatable...    rObjects)
	{
		for (Relatable rObject : rObjects)
		{
			rObject.set(rType, rTarget);
		}
	}

	/***************************************
	 * A helper method to set a certain boolean relation to TRUE on multiple
	 * objects.
	 *
	 * @param rFlagType The boolean type of the relation
	 * @param rObjects  The objects to set the flag on
	 */
	public static void setFlags(
		RelationType<Boolean> rFlagType,
		Relatable... 		  rObjects)
	{
		setAll(rFlagType, Boolean.TRUE, rObjects);
	}

	/***************************************
	 * Performs a shutdown by freeing global resources.
	 */
	public static void shutdown()
	{
		aRelationContainerMap.clear();
	}

	/***************************************
	 * Swaps the relations of two objects. All relations will be exchanged
	 * directly between the two objects, making the first object contain only
	 * the relations of the second and vice versa.
	 *
	 * @param rFirst  The first object to swap the relations of the second to
	 * @param rSecond The second object to swap the relations of the first to
	 */
	public static void swapRelations(
		RelatedObject rFirst,
		RelatedObject rSecond)
	{
		Map<RelationType<?>, Relation<?>> rSecondRelations = rSecond.aRelations;

		rSecond.aRelations = rFirst.aRelations;
		rFirst.aRelations  = rSecondRelations;
	}

	/***************************************
	 * Synchronizes the relations of a target object with that of a source
	 * object. All previous relations of the target object will be deleted and
	 * instead both objects will refer to the same relations and changes to the
	 * relations will be visible in both objects.
	 *
	 * @param rTarget The target object to replace the relations of
	 * @param rSource The object replace the target object's relations with
	 */
	public static void syncRelations(
		RelatedObject rTarget,
		RelatedObject rSource)
	{
		rTarget.aRelations = rSource.aRelations;
	}

	/***************************************
	 * Converts a relatable object into a JSON string from the object's
	 * relations. If no relation types are provided all relations of the object
	 * will be converted to JSON. In that case it is important that there are no
	 * cycles in the relations (i.e. objects referring each other) or else an
	 * endless loop will occur. Furthermore all relations in the source object
	 * must be convertible to strings, i.e. should either have a basic (JSON)
	 * datatype or a conversion to string registered with {@link
	 * Conversions#registerStringConversion(Class,InvertibleFunction)}. If not
	 * the resulting JSON string will probably not be parseable by the method
	 * {@link #fromJson(Relatable, String)}.
	 *
	 * @param  rObject        The object to convert
	 * @param  rRelationTypes The types of the relation to be converted to JSON
	 *                        (none for all)
	 *
	 * @return The resulting JSON string
	 */
	public static String toJson(
		Relatable		   rObject,
		RelationType<?>... rRelationTypes)
	{
		List<RelationType<?>> rTypes =
			rRelationTypes.length > 0 ? Arrays.asList(rRelationTypes) : null;

		return toJson(rObject, rTypes);
	}

	/***************************************
	 * Converts a certain relations of a relatable object into a JSON string
	 * from the object's relations. References to other related objects are
	 * converted recursively.
	 *
	 * @param  rObject        The object to convert
	 * @param  rRelationTypes The types of the relation to be converted to JSON
	 *
	 * @return The resulting JSON string
	 */
	public static String toJson(
		Relatable					rObject,
		Collection<RelationType<?>> rRelationTypes)
	{
		return new JsonBuilder().appendObject(rObject, rRelationTypes)
								.toString();
	}

	/***************************************
	 * Deletes a relation referenced by a URL. The URL will be split into
	 * relation type names that are looked up recursively from the relation
	 * hierarchy starting at the given root object. That means all intermediate
	 * elements of the URL must refer to {@link Relatable} instances. If the
	 * hierarchy doesn't match the URL an exception will be thrown.
	 *
	 * @param  rRoot The root relatable to start the URL lookup at
	 * @param  sUrl  The URL of the relation to get the value from
	 *
	 * @throws NoSuchElementException   If the URL could not be resolved
	 * @throws IllegalArgumentException If the URL doesn't resolve to a valid
	 *                                  relation type
	 */
	public static void urlDelete(Relatable rRoot, String sUrl)
	{
		urlResolve(rRoot, sUrl, true, ObjectSpaceResolver.URL_DELETE);
	}

	/***************************************
	 * Returns a relation value referenced by a URL. The URL will be split into
	 * relation type names that are looked up recursively from the relation
	 * hierarchy starting at the given root object. That means all intermediate
	 * elements of the URL must refer to {@link Relatable} instances. If the
	 * hierarchy doesn't match the URL an exception will be thrown.
	 *
	 * @param  rRoot The root relatable to start the URL lookup at
	 * @param  sUrl  The URL of the relation to get the value from
	 *
	 * @return The value at the given URL
	 *
	 * @throws NoSuchElementException   If the URL could not be resolved
	 * @throws IllegalArgumentException If the URL doesn't resolve to a valid
	 *                                  relation type
	 */
	public static Object urlGet(Relatable rRoot, String sUrl)
	{
		return urlResolve(rRoot, sUrl, true, ObjectSpaceResolver.URL_GET);
	}

	/***************************************
	 * Sets or updates a relation value referenced by a URL. The URL will be
	 * split into relation type names that are looked up recursively from the
	 * relation hierarchy starting at the given root object. That means all
	 * intermediate elements of the URL must refer to {@link Relatable}
	 * instances. If the hierarchy doesn't match the URL an exception will be
	 * thrown.
	 *
	 * @param  rRoot  The root relatable to start the URL lookup at
	 * @param  sUrl   The URL of the relation to update
	 * @param  rValue The new or updated value
	 *
	 * @throws NoSuchElementException   If the URL could not be resolved
	 * @throws IllegalArgumentException If the URL doesn't resolve to a valid
	 *                                  relation type or if the given value
	 *                                  cannot be assigned to the relation type
	 */
	public static void urlPut(Relatable rRoot, String sUrl, Object rValue)
	{
		urlResolve(rRoot, sUrl, true, new PutResolver<Object>(rValue));
	}

	/***************************************
	 * Performs a lookup of a relation through a URL by splitting it into
	 * relation type names and if possible recursively applying them to the
	 * hierarchy of relatable objects referenced by the relations types. If the
	 * URL could be successfully resolved the target handler will be invoked.
	 * This must be a binary function that receives the target relatable and
	 * relation type (representing the second-to-last and last URL elements) and
	 * return either a result value or NULL if the arguments are just consumed.
	 *
	 * @param  rRoot                 The root relatable for the lookup
	 * @param  sUrl                  The URL to resolve
	 * @param  bForwardToObjectSpace TRUE if the resolving should be forwarded
	 *                               to an object space on the first level
	 *                               instead of resolving a relation
	 * @param  fTargetHandler        The target handler function
	 *
	 * @return The result of the function evaluation
	 *
	 * @throws NoSuchElementException   If some element of the URL could not be
	 *                                  resolved
	 * @throws IllegalArgumentException If the URL doesn't resolve to a valid
	 *                                  relation type
	 */
	public static Object urlResolve(Relatable			rRoot,
									String				sUrl,
									boolean				bForwardToObjectSpace,
									ObjectSpaceResolver fTargetHandler)
	{
		String[]	    rElements	    = sUrl.split("/");
		Object		    rNextElement    = rRoot;
		Relatable	    rCurrentElement = rRoot;
		RelationType<?> rType		    = null;
		int			    nChildUrlIndex  = 0;

		for (String sElement : rElements)
		{
			// ignore empty URL elements (// or / at start or end)
			if (!sElement.isEmpty())
			{
				sElement = sElement.replaceAll("-", "_");

				if (bForwardToObjectSpace &&
					rNextElement instanceof ObjectSpace)
				{
					// let child-spaces perform the lookup by themselves
					return fTargetHandler.resolve((ObjectSpace<?>)
												  rNextElement,
												  sUrl.substring(nChildUrlIndex));
				}
				else if (rNextElement instanceof Relatable)
				{
					rCurrentElement = (Relatable) rNextElement;
				}
				else
				{
					urlLookupError(sUrl, sElement);
				}

				int nPackageEnd = sElement.lastIndexOf('.') + 1;

				if (nPackageEnd > 0)
				{
					sElement =
						sElement.substring(0, nPackageEnd) +
						sElement.substring(nPackageEnd).toUpperCase();
				}
				else
				{
					sElement = sElement.toUpperCase();
				}

				rType = RelationType.valueOf(sElement);

				if (rType != null)
				{
					rNextElement = rCurrentElement.get(rType);
				}
				else
				{
					String sType = sElement;

					Relation<?> rElementRelation =
						rCurrentElement.getRelations(null).stream()
									   .filter(r ->
											   r.getType()
											   .getName()
											   .endsWith(sType))
									   .findFirst()
									   .orElse(null);

					if (rElementRelation != null)
					{
						rNextElement = rElementRelation.getTarget();
						rType		 = rElementRelation.getType();
					}
					else
					{
						urlLookupError(sUrl, sElement);
					}
				}
			}

			// position after element and trailing '/'
			nChildUrlIndex += sElement.length() + 1;

			// only skip forwarding for the first level
			bForwardToObjectSpace = true;
		}

		if (rType == null)
		{
			throw new IllegalArgumentException("Could not resolve URL " + sUrl);
		}

		return fTargetHandler.evaluate(rCurrentElement, rType);
	}

	/***************************************
	 * Internal method to determine the container that stores the relations of a
	 * particular object. If the given object is a related object it will simply
	 * be returned. Else, if bCreate is TRUE a new relation container instance
	 * will be created and returned. And if bCreate is FALSE an empty default
	 * container will be returned.
	 *
	 * @param  rObject The object to return the data for
	 * @param  bCreate TRUE to create a new relation container, FALSE to return
	 *                 an empty default
	 *
	 * @return The corresponding related object data instance
	 */
	static RelatedObject getRelationContainer(Object rObject, boolean bCreate)
	{
		if (rObject instanceof RelatedObject)
		{
			return (RelatedObject) rObject;
		}
		else
		{
			synchronized (aRelationContainerMap)
			{
				RelatedObject rContainer = aRelationContainerMap.get(rObject);

				if (rContainer == null)
				{
					if (bCreate)
					{
						rContainer = new RelatedObject();
						aRelationContainerMap.put(rObject, rContainer);
					}
					else
					{
						rContainer = EMPTY_RELATION_CONTAINER;
					}
				}

				return rContainer;
			}
		}
	}

	/***************************************
	 * Internal helper method to throw a {@link NoSuchElementException} upon a
	 * URL resolving error.
	 *
	 * @param sUrl     The URL to resolve
	 * @param sElement The URL element that could not be resolved
	 */
	private static void urlLookupError(String sUrl, String sElement)
	{
		String sMessage =
			String.format("Could not resolve element '%s' in URL '%s'",
						  sElement,
						  sUrl);

		throw new NoSuchElementException(sMessage);
	}
}
