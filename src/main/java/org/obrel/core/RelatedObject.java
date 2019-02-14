//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.event.ElementEvent.EventType;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obrel.type.ListenerTypes;


/********************************************************************
 * The base class for all relation-enabled objects. It can be used as a base
 * class but it is not abstract so that relations may also be added directly to
 * instances of this class.
 *
 * <p>If a subclass needs to intercept the relation handling of this class
 * completely it should override all public non-final methods with the exception
 * of the method {@link #toString()} and the addition of the protected method
 * {@link #deleteRelation(Relation)}.</p>
 *
 * @author eso
 */
public class RelatedObject implements Relatable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final Map<RelationType<?>, Relation<?>> NO_RELATIONS =
		Collections.emptyMap();

	//~ Instance fields --------------------------------------------------------

	transient Map<RelationType<?>, Relation<?>> aRelations = NO_RELATIONS;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void deleteRelation(Relation<?> rRelation)
	{
		RelationType<?> rType = rRelation.getType();

		// notify type and listeners before removing so that they may prevent it
		// by throwing an exception
		rType.checkUpdateAllowed();
		rType.deleteRelation(this, rRelation);
		notifyRelationListeners(EventType.REMOVE, rRelation, null);

		aRelations.remove(rType);
		rRelation.removed();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T> T get(RelationType<T> rType)
	{
		assert rType.getName() != RelationType.INIT_TYPE : "Uninitialized relation type";

		Relation<T> rRelation = getRelation(rType);

		if (rRelation == null)
		{
			T rInitialValue = rType.initialValue(this);

			if (rInitialValue == null)
			{
				return rType.defaultValue(this);
			}
			else
			{
				rRelation = rType.newRelation(this, rInitialValue);

				addRelation(rRelation, true);
			}
		}

		return rRelation.getTarget();
	}

	/***************************************
	 * @see Relatable#getRelation(RelationType)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> Relation<T> getRelation(RelationType<T> rType)
	{
		return (Relation<T>) aRelations.get(rType);
	}

	/***************************************
	 * @see Relatable#getRelations(Predicate)
	 */
	@Override
	@SuppressWarnings("boxing")
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> rFilter)
	{
		List<Relation<?>> aResult = new ArrayList<Relation<?>>();

		for (Relation<?> rRelation : aRelations.values())
		{
			if (!rRelation.getType().isPrivate() &&
				(rFilter == null || rFilter.test(rRelation)))
			{
				aResult.add(rRelation);
			}
		}

		return aResult;
	}

	/***************************************
	 * Compares this instance's relations for equality with another related
	 * object. This method should be invoked by subclasses that implement the
	 * {@link #equals(Object)} and {@link #hashCode()} methods. From the latter
	 * subclasses should also invoke {@link #relationsHashCode()} for a
	 * consistent implementation of these methods.
	 *
	 * @param  rOther The other object to compare this instance's relations with
	 *
	 * @return TRUE if the relations of this instance equal that of the other
	 *         object
	 */
	public final boolean relationsEqual(RelatedObject rOther)
	{
		return aRelations.equals(rOther.aRelations);
	}

	/***************************************
	 * Returns a string description of this object's relations.
	 *
	 * @param  sJoin      The join between relation type and value
	 * @param  sSeparator The separator between relations
	 * @param  nIndent    The indentation level (zero for the first, -1 for no
	 *                    indentation)
	 *
	 * @return The relations string
	 */
	public String relationsString(String sJoin, String sSeparator, int nIndent)
	{
		return relationsString(
			sJoin,
			sSeparator,
			nIndent,
			new HashSet<Object>());
	}

	/***************************************
	 * @see Relatable#set(RelationType, Object)
	 */
	@Override
	public <T> Relation<T> set(RelationType<T> rType, T rTarget)
	{
		Relation<T> rRelation = getRelation(rType);

		if (rRelation != null)
		{
			// notify type and listeners before updating so that they may
			// prevent the update by throwing an exception
			rType.checkUpdateAllowed();
			rType.prepareRelationUpdate(rRelation, rTarget);
			notifyRelationListeners(EventType.UPDATE, rRelation, rTarget);
			rRelation.updateTarget(rTarget);
		}
		else
		{
			if (!rType.isInitialized())
			{
				RelationTypes.init(getClass());
			}

			rType.checkReadonly();
			rRelation = new DirectRelation<T>(rType, rTarget);
			addRelation(rRelation, true);
		}

		return rRelation;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public final <T, I> Relation<T> set(RelationType<T> rType,
										Function<I, T>  fTargetResolver,
										I				rIntermediateTarget)
	{
		Relation<T> rRelation = getRelation(rType);

		if (rRelation == null)
		{
			rRelation =
				rType.newIntermediateRelation(
					this,
					fTargetResolver,
					rIntermediateTarget);

			// addRelation() will replace an existing relation with the given type
			addRelation(rRelation, true);
		}
		else
		{
			throw new IllegalStateException(
				"Relation already exists: " +
				rRelation);
		}

		return rRelation;
	}

	/***************************************
	 * Returns a string representation of this instance. The returned string
	 * will be composed of the simple class name followed by a comma-separated
	 * list of all relations in brackets. In this list all non-private relations
	 * will be formatted as {@literal <TYPE>=<TARGET>}.
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder aStringBuilder =
			new StringBuilder(getClass().getSimpleName());

		aStringBuilder.append('[');
		aStringBuilder.append(relationsString("=", ",", -1));
		aStringBuilder.append(']');

		return aStringBuilder.toString();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <T, D> TransformedRelation<T, D> transform(
		RelationType<T>			 rType,
		InvertibleFunction<T, D> fTransformation)
	{
		Relation<T> rRelation = getRelation(rType);
		T		    rTarget;

		TransformedRelation<T, D> rTransformedRelation =
			rType.newTransformedRelation(this, fTransformation);

		if (rRelation instanceof RelationWrapper<?, ?, ?>)
		{
			throw new IllegalStateException(
				"Cannot transform alias relation " +
				rType);
		}
		else if (rRelation != null)
		{
			rTarget = rRelation.getTarget();
			rTransformedRelation.transferRelationsFrom(rRelation, false);
		}
		else
		{
			rTarget = rType.initialValue(this);
		}

		rTransformedRelation.setTarget(rTarget);

		// addRelation() will replace an existing relation with the given type
		addRelation(rTransformedRelation, rRelation == null);

		return rTransformedRelation;
	}

	/***************************************
	 * A package-internal method that notifies all registered relation listeners
	 * of a certain relation event.
	 *
	 * @param rEventType   The event type
	 * @param rRelation    The relation that is affected by the event
	 * @param rUpdateValue The update value in case of a relation update
	 */
	protected <T> void notifyRelationListeners(EventType   rEventType,
											   Relation<T> rRelation,
											   T		   rUpdateValue)
	{
		RelationType<T> rType = rRelation.getType();

		if (!rType.isPrivate())
		{
			if (hasRelation(ListenerTypes.RELATION_LISTENERS))
			{
				get(ListenerTypes.RELATION_LISTENERS).dispatch(
					new RelationEvent<T>(
						rEventType,
						this,
						rRelation,
						rUpdateValue,
						this));
			}

			if (rRelation.hasRelation(ListenerTypes.RELATION_UPDATE_LISTENERS))
			{
				rRelation.get(ListenerTypes.RELATION_UPDATE_LISTENERS)
						 .dispatch(
		 					new RelationEvent<T>(
		 						rEventType,
		 						this,
		 						rRelation,
		 						rUpdateValue,
		 						rRelation));
			}

			if (rType.hasRelation(ListenerTypes.RELATION_TYPE_LISTENERS))
			{
				rType.get(ListenerTypes.RELATION_TYPE_LISTENERS)
					 .dispatch(
	 					new RelationEvent<T>(
	 						rEventType,
	 						this,
	 						rRelation,
	 						rUpdateValue,
	 						rType));
			}
		}
	}

	/***************************************
	 * Helper method for serializable subclasses to read the relations of this
	 * instance from the given input stream in the format that has been written
	 * by the method {@link #writeRelations(ObjectOutputStream)}.
	 *
	 * @param  rIn The object input stream to read from
	 *
	 * @throws IOException            If reading a relation fails
	 * @throws ClassNotFoundException If a relation class is not available
	 */
	protected final void readRelations(ObjectInputStream rIn)
		throws IOException, ClassNotFoundException
	{
		int nCount = rIn.readInt();

		while (nCount-- > 0)
		{
			addRelation((Relation<?>) rIn.readObject(), false);
		}
	}

	/***************************************
	 * Calculates the hash code of this instance's relations. This method should
	 * be invoked by subclasses that implement the {@link #hashCode()} and
	 * {@link #equals(Object)} methods. From the latter subclasses should also
	 * invoke {@link #relationsEqual(RelatedObject)} for a consistent
	 * implementation of these methods.
	 *
	 * @return The hash code of this instance's relations
	 */
	protected final int relationsHashCode()
	{
		return aRelations.hashCode();
	}

	/***************************************
	 * Helper method for serializable subclasses to write the relations of this
	 * instance to the given output stream. It will first write the count of the
	 * relations and then all non-transient relations by directly serializing
	 * them to the stream.
	 *
	 * @param      rOut The output stream to write the relations to
	 *
	 * @throws     IOException If writing a relation fails
	 *
	 * @serialData First the integer count of non-transient relations is stored
	 *             followed by the relations
	 */
	protected final void writeRelations(ObjectOutputStream rOut)
		throws IOException
	{
		int nCount = 0;

		for (Relation<?> rRelation : aRelations.values())
		{
			if (!rRelation.getType().isTransient())
			{
				nCount++;
			}
		}

		rOut.writeInt(nCount);

		for (Relation<?> rRelation : aRelations.values())
		{
			if (!rRelation.getType().isTransient())
			{
				rOut.writeObject(rRelation);
			}
		}
	}

	/***************************************
	 * Adds a relation to this object.
	 *
	 * @param rRelation The relation to add
	 * @param bNotify   TRUE if the relation type and listeners shall be
	 *                  notified of the added relation; FALSE if the call is for
	 *                  internal relation management only
	 */
	<T> void addRelation(Relation<T> rRelation, boolean bNotify)
	{
		RelationType<T> rType = rRelation.getType();

		rRelation = rType.addRelation(this, rRelation);

		if (bNotify)
		{
			// notify listeners before adding so that they may prevent it
			// by throwing an exception
			notifyRelationListeners(EventType.ADD, rRelation, null);
		}

		if (aRelations == NO_RELATIONS)
		{
			aRelations = new LinkedHashMap<RelationType<?>, Relation<?>>();
		}

		aRelations.put(rType, rRelation);
	}

	/***************************************
	 * Returns a string description of this object's relations.
	 *
	 * @param  sJoin            The join between relation type and value
	 * @param  sSeparator       The separator between relations
	 * @param  nIndent          The indentation level (zero for the first, -1
	 *                          for no indentation)
	 * @param  rExcludedObjects Objects that shall not be converted to prevent
	 *                          recursion
	 *
	 * @return The relations string
	 */
	String relationsString(String	   sJoin,
						   String	   sSeparator,
						   int		   nIndent,
						   Set<Object> rExcludedObjects)
	{
		StringBuilder aStringBuilder = new StringBuilder();

		for (Relation<?> rRelation : aRelations.values())
		{
			RelationType<?> rType   = rRelation.getType();
			Object		    rTarget = rRelation.getTarget();

			rExcludedObjects.add(this);

			if (!rType.isPrivate())
			{
				if (rTarget == this)
				{
					// prevent recursion
					rTarget = "<this>";
				}
				else if (rExcludedObjects.contains(rTarget))
				{
					rTarget = rTarget.getClass().getSimpleName();
				}
				else if (rTarget instanceof RelatedObject)
				{
					int nLevel = nIndent >= 0 ? nIndent + 1 : -1;

					String sTargetRelations =
						((RelatedObject) rTarget).relationsString(
							sJoin,
							sSeparator,
							nLevel,
							rExcludedObjects);

					if (nLevel >= 0)
					{
						rTarget =
							rTarget.getClass().getSimpleName() + "\n" +
							sTargetRelations;
					}
					else
					{
						rTarget =
							rTarget.getClass().getSimpleName() + "[" +
							sTargetRelations + "]";
					}
				}

				for (int i = 0; i < nIndent; i++)
				{
					aStringBuilder.append("  ");
				}

				aStringBuilder.append(rType);
				aStringBuilder.append(sJoin);
				aStringBuilder.append(rTarget);
				aStringBuilder.append(sSeparator);
			}
		}

		if (aRelations.size() > 0)
		{
			aStringBuilder.setLength(
				aStringBuilder.length() - sSeparator.length());
		}

		return aStringBuilder.toString();
	}

	/***************************************
	 * Transfers all relations from another object to this one. This method is
	 * intended to be used internally by the framework in cases where a certain
	 * related object is about to replace the argument object. The relations
	 * will be added to this object unchanged and it is the responsibility of
	 * the caller that the resulting object will be consistent.
	 *
	 * @param rSource The source object to transfer the relations from
	 * @param bNotify TRUE if the relation type and listeners shall be notified
	 *                of the added relation; FALSE if the call is for internal
	 *                relation management only
	 */
	void transferRelationsFrom(RelatedObject rSource, boolean bNotify)
	{
		for (Relation<?> rRelation : rSource.aRelations.values())
		{
			addRelation(rRelation, bNotify);
		}
	}
}
