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
package org.obrel.space;

import de.esoco.lib.expression.BinaryFunction;

import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;


/********************************************************************
 * A binary function extension that resolves URLs in object spaces. Invoked by
 * {@link ObjectRelations#urlResolve(Relatable, String, boolean,
 * ObjectSpaceResolver)}.
 *
 * @author eso
 */
public interface ObjectSpaceResolver
	extends BinaryFunction<Relatable, RelationType<?>, Object>
{
	//~ Static fields/initializers ---------------------------------------------

	/** Standard delete resolver. */
	public static final DeleteResolver URL_DELETE =
		(r, t) ->
		{
			r.deleteRelation(t);

			return null;
		};

	/** Standard get resolver. */
	public static final GetResolver URL_GET = (r, t) -> r.get(t);

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Resolves a URL in a certain {@link ObjectSpace}. Will be invoked by
	 * {@link ObjectRelations#urlResolve(Relatable, String, boolean,
	 * ObjectSpaceResolver)} for the remaining URL if the URL traversal
	 * encounters an object space element.
	 *
	 * @param  rSpace       The object space to resolve the URL in
	 * @param  sRelativeUrl The space-relative URL to resolve
	 *
	 * @return The resolved object (NULL for none)
	 */
	public Object resolve(ObjectSpace<?> rSpace, String sRelativeUrl);

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * An object space resolver implementation that invokes {@link
	 * ObjectSpace#delete(String)}.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	public static interface DeleteResolver extends ObjectSpaceResolver
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		default public Object resolve(
			ObjectSpace<?> rSpace,
			String		   sRelativeUrl)
		{
			rSpace.delete(sRelativeUrl);

			return null;
		}
	}

	/********************************************************************
	 * An object space resolver implementation that invokes {@link
	 * ObjectSpace#get(String)}.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	public static interface GetResolver extends ObjectSpaceResolver
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		default public Object resolve(
			ObjectSpace<?> rSpace,
			String		   sRelativeUrl)
		{
			return rSpace.get(sRelativeUrl);
		}
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An object space resolver implementation that invokes {@link
	 * ObjectSpace#delete(String)}.
	 *
	 * @author eso
	 */
	public static class PutResolver<T> implements ObjectSpaceResolver
	{
		//~ Instance fields ----------------------------------------------------

		private final T rValue;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rValue The value to put
		 */
		public PutResolver(T rValue)
		{
			this.rValue = rValue;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object evaluate(Relatable rRelatable, RelationType<?> rType)
		{
			Object rValue = getValue();

			if (!rType.getTargetType().isAssignableFrom(rValue.getClass()))
			{
				String sMessage =
					String.format("Invalid value for type '%s': %s",
								  rType,
								  rValue);

				throw new IllegalArgumentException(sMessage);
			}

			rRelatable.set((RelationType<Object>) rType, rValue);

			return null;
		}

		/***************************************
		 * Returns the value of this instance.
		 *
		 * @return The put value
		 */
		public T getValue()
		{
			return rValue;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object resolve(ObjectSpace<?> rSpace, String sRelativeUrl)
		{
			((ObjectSpace<Object>) rSpace).put(sRelativeUrl, rValue);

			return null;
		}
	}
}
