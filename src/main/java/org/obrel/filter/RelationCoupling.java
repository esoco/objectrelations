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
package org.obrel.filter;

import de.esoco.lib.expression.function.RelationAccessor;
import de.esoco.lib.property.Gettable;
import de.esoco.lib.property.HasValue;
import de.esoco.lib.property.Settable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;


/********************************************************************
 * Implements the (loose) coupling of a relation with other sources or targets.
 * The coupling is loose because it is not performed automatically but must be
 * performed explicitly. This is done through the methods {@link #set()} (update
 * the coupled target from the relation) or {@link #get()} (update the relation
 * from the coupled source).
 *
 * <p>Couplings are created with one of the coupling methods which are all based
 * on {@link #couple(Relatable, RelationType, Consumer, Supplier)}. The {@link
 * Consumer} function is used to update the target with the current relation
 * value upon a call of {@link #set()} and the {@link Supplier} will be invoked
 * to update the relation target if {@link #get()} is called.</p>
 *
 * <p>Couplings are stored in a meta-relation of the type {@link #COUPLINGS}. If
 * a coupling is no longer needed it can be discarded with {@link #remove()}. To
 * process multiple couplings at once the corresponding static methods like
 * {@link #setAll(Relatable, Collection)} can be invoked with multiple relation
 * types at once.</p>
 *
 * @author eso
 */
public class RelationCoupling<T>
{
	//~ Static fields/initializers ---------------------------------------------

	/** The relation type that contains the couplings of a relation. */
	public static final RelationType<Set<RelationCoupling<?>>> COUPLINGS =
		RelationTypes.newSetType(false);

	static
	{
		RelationTypes.init(RelationCoupling.class);
	}

	//~ Instance fields --------------------------------------------------------

	private final Relatable		  rRelatable;
	private final RelationType<T> rType;
	private Consumer<T>			  fUpdateTarget;
	private Supplier<T>			  fQuerySource;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param  rRelatable    The parent object of the relation to couple
	 * @param  rType         rRelationType The type of the relation to couple
	 * @param  fUpdateTarget A function that updates the coupled target (NULL
	 *                       for none)
	 * @param  fQuerySource  A function that queries the coupled source (NULL
	 *                       for none)
	 *
	 * @throws IllegalArgumentException If both functions are NULL
	 */
	private RelationCoupling(Relatable		 rRelatable,
							 RelationType<T> rType,
							 Consumer<T>	 fUpdateTarget,
							 Supplier<T>	 fQuerySource)
	{
		if (fUpdateTarget == null && fQuerySource == null)
		{
			throw new IllegalArgumentException("At least one coupling function must be provided");
		}

		this.rRelatable    = rRelatable;
		this.rType		   = rType;
		this.fUpdateTarget = fUpdateTarget;
		this.fQuerySource  = fQuerySource;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Couples a relation with a target and a source.
	 *
	 * @param  rRelatable    The parent object of the relation to couple
	 * @param  rType         rRelationType The type of the relation to couple
	 * @param  fUpdateTarget A function that updates the coupled target (NULL
	 *                       for none)
	 * @param  fQuerySource  A function that queries the coupled source (NULL
	 *                       for none)
	 *
	 * @return The new coupling
	 *
	 * @throws IllegalArgumentException If both functions are NULL
	 */
	public static <T> RelationCoupling<T> couple(Relatable		 rRelatable,
												 RelationType<T> rType,
												 Consumer<T>	 fUpdateTarget,
												 Supplier<T>	 fQuerySource)
	{
		RelationCoupling<T> aCoupling =
			new RelationCoupling<>(rRelatable,
								   rType,
								   fUpdateTarget,
								   fQuerySource);

		rRelatable.init(rType).get(COUPLINGS).add(aCoupling);

		return aCoupling;
	}

	/***************************************
	 * Bi-directionally couples a relation with a different relation in another
	 * relatable object.
	 *
	 * @see #couple(Relatable, RelationType, Consumer, Supplier)
	 */
	public static <T> RelationCoupling<T> couple(
		Relatable		rRelatable,
		RelationType<T> rType,
		Relatable		rCoupledRelatable,
		RelationType<T> rCoupledType)
	{
		RelationAccessor<T> fTarget =
			new RelationAccessor<>(rCoupledRelatable, rCoupledType);

		return couple(rRelatable, rType, fTarget, fTarget);
	}

	/***************************************
	 * Couples a relation with a certain source only.
	 *
	 * @see #couple(Relatable, RelationType, Consumer, Supplier)
	 */
	public static <T> RelationCoupling<T> coupleSource(
		Relatable		rRelatable,
		RelationType<T> rType,
		Supplier<T>		fQuerySource)
	{
		return couple(rRelatable, rType, null, fQuerySource);
	}

	/***************************************
	 * Couples a relation with a certain target only.
	 *
	 * @see #couple(Relatable, RelationType, Consumer, Supplier)
	 */
	public static <T> RelationCoupling<T> coupleTarget(
		Relatable		rRelatable,
		RelationType<T> rType,
		Consumer<T>		fUpdateTarget)
	{
		return couple(rRelatable, rType, fUpdateTarget, null);
	}

	/***************************************
	 * Creates a new coupling with a {@link HasValue} instance.
	 *
	 * @see #coupleValue(Relatable, RelationType, Settable, Gettable)
	 */
	public static <T> RelationCoupling<T> coupleValue(
		Relatable		rRelatable,
		RelationType<T> rType,
		HasValue<T>		rValueHolder)
	{
		return coupleValue(rRelatable, rType, rValueHolder, rValueHolder);
	}

	/***************************************
	 * Creates a new coupling from {@link Settable} and {@link Gettable}
	 * functions.
	 *
	 * @see #couple(Relatable, RelationType, Consumer, Supplier)
	 */
	public static <T> RelationCoupling<T> coupleValue(
		Relatable		rRelatable,
		RelationType<T> rType,
		Settable<T>		fSettable,
		Gettable<T>		fGettable)
	{
		Consumer<T> fSet = fSettable::setValue;
		Supplier<T> fGet = fGettable::getValue;

		return couple(rRelatable, rType, fSet, fGet);
	}

	/***************************************
	 * Invokes {@link #get()} for all couplings of relations with certain
	 * relation types.
	 *
	 * @param rRelatable The relatable to get the relations from
	 * @param rTypes     The relation types to invoke {@link #get()} for
	 */
	public static void getAll(
		Relatable					rRelatable,
		Collection<RelationType<?>> rTypes)
	{
		couplings(rRelatable, rTypes).forEach(s -> s.forEach(c -> c.get()));
	}

	/***************************************
	 * Invokes {@link #remove()} for all couplings of relations with certain
	 * relation types.
	 *
	 * @param rRelatable The relatable to get the relations from
	 * @param rTypes     The relation types to invoke {@link #set()} for
	 */
	public static void removeAll(
		Relatable					rRelatable,
		Collection<RelationType<?>> rTypes)
	{
		couplings(rRelatable, rTypes).forEach(s -> s.forEach(c -> c.remove()));
	}

	/***************************************
	 * Invokes {@link #set()} for all couplings of relations with certain
	 * relation types.
	 *
	 * @param rRelatable The relatable to get the relations from
	 * @param rTypes     The relation types to invoke {@link #set()} for
	 */
	public static void setAll(
		Relatable					rRelatable,
		Collection<RelationType<?>> rTypes)
	{
		couplings(rRelatable, rTypes).forEach(s -> s.forEach(c -> c.set()));
	}

	/***************************************
	 * Returns a stream with the couplings of the relations with certain types
	 * in a relatable object.
	 *
	 * @param  rRelatable The relatable to get the relations from
	 * @param  rTypes     The types of the relations to get the couplings of
	 *
	 * @return A stream of the sets containing the relation couplings
	 */
	private static Stream<Set<RelationCoupling<?>>> couplings(
		Relatable					rRelatable,
		Collection<RelationType<?>> rTypes)
	{
		Stream<Set<RelationCoupling<?>>> rCouplings =
			rRelatable.getRelations().stream()
					  .filter(r ->
							  rTypes.contains(r.getType()) &&
							  r.hasRelation(COUPLINGS))
					  .map(r -> r.get(COUPLINGS));

		return rCouplings;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Updates the relation from the value returned by the coupled source.
	 *
	 * @return The new relation value (or the existing if no source coupling has
	 *         been set)
	 */
	public T get()
	{
		if (fQuerySource != null)
		{
			return rRelatable.set(rType, fQuerySource.get()).getTarget();
		}
		else
		{
			return rRelatable.get(rType);
		}
	}

	/***************************************
	 * Removes this coupling from the relation it has been registered on. This
	 * will remove it from the relation's set of {@link #COUPLINGS} and disable
	 * it internally so that calls to {@link #set()} and {@link #get()} will no
	 * further have any effect on the relation.
	 */
	public void remove()
	{
		Relation<T> rRelation = rRelatable.getRelation(rType);

		fUpdateTarget = null;
		fQuerySource  = null;

		if (rRelation != null)
		{
			rRelation.get(COUPLINGS).remove(this);
		}
	}

	/***************************************
	 * Updates the coupled target from the current relation value.
	 */
	public void set()
	{
		if (fUpdateTarget != null)
		{
			fUpdateTarget.accept(rRelatable.get(rType));
		}
	}
}
