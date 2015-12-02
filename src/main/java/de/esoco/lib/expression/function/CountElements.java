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

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Function;

import java.util.HashMap;
import java.util.Map;


/********************************************************************
 * A function that associates input elements with a key and then stores the
 * element counts in a map that is indexed by that key. A way to use this
 * function would be through {@link CollectionUtil#apply(java.util.Collection,
 * Function)}. After using it the resulting map can be queried with the method
 * {@link #getElementCounts()}.
 *
 * @author eso
 */
public class CountElements<E, K> extends AbstractFunction<E, Void>
{
	//~ Instance fields --------------------------------------------------------

	private Map<K, Integer>		   aCountMap = new HashMap<>();
	private Function<? super E, K> fGetKey;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param fGetElementKey A function that generates the key for the count map
	 */
	public CountElements(Function<? super E, K> fGetElementKey)
	{
		super(CountElements.class.getSimpleName());

		this.fGetKey = fGetElementKey;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see AbstractFunction#evaluate(Object)
	 */
	@Override
	@SuppressWarnings("boxing")
	public Void evaluate(E rElement)
	{
		K	    rKey   = fGetKey.evaluate(rElement);
		Integer rCount = aCountMap.get(rKey);

		aCountMap.put(rKey, rCount != null ? rCount + 1 : 1);

		return null;
	}

	/***************************************
	 * Returns the mappings from keys to the counts of element with that keys.
	 *
	 * @return The countMap value
	 */
	public final Map<K, Integer> getElementCounts()
	{
		return aCountMap;
	}
}
