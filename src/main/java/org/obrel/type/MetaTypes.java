//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package org.obrel.type;

import de.esoco.lib.event.EventDispatcher;
import de.esoco.lib.event.EventHandler;
import de.esoco.lib.expression.Action;
import de.esoco.lib.property.Immutability;

import java.io.Closeable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypeModifier.FINAL;
import static org.obrel.core.RelationTypes.newFlagType;
import static org.obrel.core.RelationTypes.newType;
import static org.obrel.type.StandardTypes.RELATION_LISTENERS;


/********************************************************************
 * This class contains definitions of standard meta-relation types. Relations
 * with meta-types contain information about the objects they are set on. These
 * objects are often other relations which are annotated with meta relations to
 * further define their structure or behavior. For example, a meta-relation with
 * the type {@link #MODIFIED} indicates whether a certain object (e.g. a
 * relation) has been modified or not. How such a relation is set depends on the
 * respective application although some of the standard types are already
 * supported by the object relations framework.
 *
 * <p>It is recommended that applications use the standard types defined in this
 * class if applicable to increase the interoperability of relation-based code
 * because using standard types allows library code and tools to access and
 * display such relations in a standard way.</p>
 *
 * @author eso
 */
@RelationTypeNamespace(RelationType.DEFAULT_NAMESPACE)
public class MetaTypes
{
	//~ Static fields/initializers ---------------------------------------------

	//- meta-information types -------------------------------------------------

	/**
	 * The class that declares the target type. Used for relations type to
	 * record the class in which they are declared. Final as this will never
	 * change after the initial declaration.
	 */
	public static final RelationType<Class<?>> DECLARING_CLASS =
		RelationTypes.DECLARING_CLASS;

	/**
	 * The datatype class of a certain element; this type is final because it is
	 * intended to define a datatype that doesn't change.
	 */
	public static final RelationType<Class<?>> DATATYPE = newType(FINAL);

	/** The datatype of elements in a collection or container. */
	public static final RelationType<Class<?>> ELEMENT_DATATYPE =
		RelationTypes.ELEMENT_DATATYPE;

	/** The datatype of keys in a map. */
	public static final RelationType<Class<?>> KEY_DATATYPE =
		RelationTypes.KEY_DATATYPE;

	/** The datatype of values in a map. */
	public static final RelationType<Class<?>> VALUE_DATATYPE =
		RelationTypes.VALUE_DATATYPE;

	// Boolean marker types that indicate certain object properties; some are
	// final to indicate a condition that never changes

	/** Marks a relation type that defines an object identifier */
	public static final RelationType<Boolean> OBJECT_ID_ATTRIBUTE =
		newFlagType(FINAL);

	/** Marks a relation type that defines an object type */
	public static final RelationType<Boolean> OBJECT_TYPE_ATTRIBUTE =
		newFlagType(FINAL);

	/** Marks a relation type that represents the parent in a hierarchy */
	public static final RelationType<Boolean> PARENT_ATTRIBUTE =
		newFlagType(FINAL);

	/** Marks a relation type that represents the children in a hierarchy */
	public static final RelationType<Boolean> CHILD_ATTRIBUTE =
		newFlagType(FINAL);

	//- Marker types ----------------------------

	/**
	 * Marks an element to be immutable. This is an "active" relation type that
	 * will prevent any further modifications of the relations of the target
	 * object after a relation with this type has been set. This type can only
	 * be set to TRUE, trying to set it to FALSE will cause an exception.
	 *
	 * <p>The immutable state is applied to all relations of the target object,
	 * thus preventing any further modifications of the relation target and
	 * meta-relations. If the target object of this relation type implements the
	 * interface {@link Immutability} the object will be set to be immutable by
	 * invoking the corresponding interface method.</p>
	 *
	 * <p>The immutability enforced by this type only prevents the modification
	 * of the relations themselves, not of their target value objects. These
	 * must be handled separately by the application code if necessary. The only
	 * exception are relation types that have a generic collection interface as
	 * their target type (i.e. {@link List}, {@link Set}, or {@link Map}). These
	 * will be converted to immutable collections with the corresponding methods
	 * of the {@link Collections} class.</p>
	 *
	 * <p>If this type is used on objects that contain fields besides their
	 * relations or are not relatable at all it is also the responsibility of
	 * the application to implement the corresponding behavior. This can be done
	 * by either checking for a relation with this type or by implementing the
	 * {@link Immutability} interface.</p>
	 */
	public static final RelationType<Boolean> IMMUTABLE =
		new ImmutableFlagType();

	/**
	 * Marks an object to be mandatory; the reverse can be expressed with the
	 * type {@link #OPTIONAL}. It should be evaluated carefully whether an
	 * application marks objects as mandatory or as optional by default. Both
	 * approaches can be reasonable depending on the respective use case.
	 */
	public static final RelationType<Boolean> MANDATORY = newFlagType();

	/**
	 * Marks an object to be optional. See the documentation of the relation
	 * type {@link #MANDATORY} for details.
	 */
	public static final RelationType<Boolean> OPTIONAL = newFlagType();

	/** Marks an object to have a certain kind of order (e.g. a ordered map). */
	public static final RelationType<Boolean> ORDERED = newFlagType();

	/**
	 * Marks an object by some other object. This flag is final because managed
	 * objects are considered to be owned by the managing instance.
	 */
	public static final RelationType<Boolean> MANAGED = newFlagType(FINAL);

	/**
	 * Marks an object to be unique in a certain context; the definition of this
	 * context is up to the application code using this type.
	 */
	public static final RelationType<Boolean> UNIQUE = newFlagType();

	/** Marks an object that has been modified. */
	public static final RelationType<Boolean> MODIFIED = newFlagType();

	/** Marks an object to be invalid. */
	public static final RelationType<Boolean> INVALID = newFlagType();

	/**
	 * Marks an object that has been closed, typically as an implementation of
	 * the {@link Closeable} interface.
	 */
	public static final RelationType<Boolean> CLOSED = newFlagType();

	/** Marks an object that is generated automatically. */
	public static final RelationType<Boolean> AUTOGENERATED = newFlagType();

	/** Marks an object that is interactive, e.g. in a user interface. */
	public static final RelationType<Boolean> INTERACTIVE = newFlagType();

	/** Can be used to mark objects during their initialization phase. */
	public static final RelationType<Boolean> INITIALIZING = newFlagType();

	/**
	 * Can be used to mark objects that have been fully initialized. This type
	 * has the flag {@link RelationTypeModifier#FINAL FINAL} which means it can
	 * only be assigned once because objects with this flag are intended to
	 * remain in that state.
	 */
	public static final RelationType<Boolean> INITIALIZED = newType(FINAL);

	/**
	 * Marks an object or relation that is hierarchical, i.e. an element in a
	 * hierarchy of objects.
	 */
	public static final RelationType<Boolean> HIERARCHICAL = newFlagType();

	/**
	 * Marks an object to be evaluated in a transactional context. This must not
	 * be confused with objects that implement the Transactional interface. The
	 * latter implement transaction handling while objects marked with this flag
	 * can be surrounded by transaction, e.g. because they create or execute
	 * transactional elements.
	 */
	public static final RelationType<Boolean> TRANSACTIONAL = newFlagType();

	/**
	 * Marks an element that shall be used for sorting. TRUE defines ascending
	 * and FALSE descending order. If this flag is not set (to be checked with
	 * {@link Relatable#hasRelation(RelationType)}) the element should not be
	 * considered for sorting.
	 */
	public static final RelationType<Boolean> SORT_ASCENDING = newFlagType();

	/**
	 * Contains the sort order (or priority) for elements that are used for
	 * sorting. Can be used in conjunction with {@link #SORT_ASCENDING}.
	 */
	public static final RelationType<Integer> SORT_ORDER = newType();

	/**
	 * The namespace of a relation type. This meta type is only intended to be
	 * used by frameworks that need to modify the namespace of certain relation
	 * types. Other code should either rely on the default namespace or use the
	 * annotation {@link RelationTypeNamespace} to define a different namespace
	 * for a class.
	 */
	public static final RelationType<String> RELATION_TYPE_NAMESPACE =
		RelationTypes.RELATION_TYPE_NAMESPACE;

	/**
	 * An optional initialization action for relation types that if set on a
	 * relation type will be invoked after a relation type has been initialized
	 * by the framework. After the initialization this meta-relation will be
	 * deleted from it's relation type. The initialization will only be invoked
	 * for relation types that are initialized globally through the framework
	 * method {@link RelationTypes#init(Class)}. It can be used to establish
	 * additional constraints on certain relations types, e.g. special
	 * namespaces.
	 */
	public static final RelationType<Action<RelationType<?>>> RELATION_TYPE_INIT_ACTION =
		RelationTypes.RELATION_TYPE_INIT_ACTION;

	static
	{
		RelationTypes.init(MetaTypes.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private MetaTypes()
	{
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * Inner class for the {@link #IMMUTABLE} relation type that marks the
	 * parent object of a relation with this type as immutable. It registers
	 * itself as an event handler for relation events of the parent object and
	 * subsequently prevents the modification of any of the parent's relations
	 * by throwing an exception.
	 */
	private static class ImmutableFlagType extends RelationType<Boolean>
		implements EventHandler<RelationEvent<?>>
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		public ImmutableFlagType()
		{
			super(RelationTypeModifier.FINAL);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Overridden to add this type as a listener for relation events on the
		 * parent object when a relation with this type is set. Also sets an
		 * immutable relation on all other relations of the parent to set the
		 * state recursively.
		 *
		 * @see RelationType#addRelation(Relatable, Relation)
		 */
		@Override
		public void addRelation(Relatable rParent, Relation<Boolean> rRelation)
		{
			if (rRelation.getTarget() != Boolean.TRUE)
			{
				throw new IllegalArgumentException(getName() +
												   " must always be set to TRUE");
			}

			super.addRelation(rParent, rRelation);

			// first make all relations of the parent immutable
			for (Relation<?> rOtherRelation : rParent.getRelations(null))
			{
				RelationType<?> rRelationType = rOtherRelation.getType();
				Object		    rTarget		  = rOtherRelation.getTarget();

				if (rTarget instanceof Collection)
				{
					setCollectionImmutable(rParent,
										   rRelationType,
										   (Collection<?>) rTarget);
				}
				else if (rTarget instanceof Map &&
						 rRelationType.getTargetType() == Map.class)
				{
					setMapImmutable(rParent,
									rRelationType,
									(Map<?, ?>) rTarget);
				}

				if (!rOtherRelation.hasFlag(IMMUTABLE))
				{
					rOtherRelation.set(IMMUTABLE);
				}
			}

			if (rParent instanceof Immutability)
			{
				((Immutability) rParent).setImmutable();
			}

			// activate by adding listener after all changes have been made
			EventDispatcher<RelationEvent<?>> rListeners =
				rParent.get(RELATION_LISTENERS);

			rListeners.add(this);
			rListeners.setImmutable();
		}

		/***************************************
		 * Implemented to throw an exception on any attempt to add, modify, or
		 * delete a relation on the parent object.
		 *
		 * @param rEvent The relation event that occurred
		 */
		@Override
		public void handleEvent(RelationEvent<?> rEvent)
		{
			if (rEvent.getSource().hasRelation(this))
			{
				String sMessage =
					String.format("Could not %s %s; " +
								  "object is immutable: %s",
								  rEvent.getType(),
								  rEvent.getElement(),
								  rEvent.getSource());

				throw new UnsupportedOperationException(sMessage);
			}
		}

		/***************************************
		 * Makes the target collection of a relation immutable if the target
		 * datatype of the relation (not of the actual relation target!) is
		 * exactly one of the interface types {@link List} or {@link Set}.
		 * Derived types will not be affected.
		 *
		 * @param rParent       The parent object of the relation
		 * @param rRelationType The collection relation type
		 * @param rCollection   The original target collection
		 */
		private void setCollectionImmutable(Relatable		rParent,
											RelationType<?> rRelationType,
											Collection<?>   rCollection)
		{
			Class<?> rTargetType = rRelationType.getTargetType();

			if (rTargetType == List.class)
			{
				rCollection =
					Collections.unmodifiableList((List<?>) rCollection);
			}
			else if (rTargetType == Set.class)
			{
				rCollection = Collections.unmodifiableSet((Set<?>) rCollection);
			}

			@SuppressWarnings("unchecked")
			RelationType<Collection<?>> rCollectionType =
				(RelationType<Collection<?>>) rRelationType;

			rParent.set(rCollectionType, rCollection);
		}

		/***************************************
		 * Makes the target map of a relation immutable if the target datatype
		 * of the relation (not of the actual relation target!) is exactly of
		 * the interface datatype {@link Map}. Derived types will not be
		 * affected.
		 *
		 * @param rParent       The parent object of the relation
		 * @param rRelationType The collection relation type
		 * @param rMap          rCollection The original target collection
		 */
		private void setMapImmutable(Relatable		 rParent,
									 RelationType<?> rRelationType,
									 Map<?, ?>		 rMap)
		{
			@SuppressWarnings("unchecked")
			RelationType<Map<?, ?>> rMapType =
				(RelationType<Map<?, ?>>) rRelationType;

			rParent.set(rMapType, Collections.unmodifiableMap(rMap));
		}
	}
}
