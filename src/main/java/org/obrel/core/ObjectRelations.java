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
package org.obrel.core;

import java.util.Map;
import java.util.WeakHashMap;


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
							  org.obrel.type.MetaTypes.class,
							  org.obrel.type.StandardTypes.class);
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
}
