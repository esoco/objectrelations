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

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;


/********************************************************************
 * A token string format subclass for relatable objects. It allows to use the
 * names of RelationTypes as tokens to access the corresponding relations.
 *
 * @author eso
 */
public class RelationTokenFormat extends TokenStringFormat<Relatable>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain token string.
	 *
	 * @param sTokenString The token string this format is based on
	 */
	public RelationTokenFormat(String sTokenString)
	{
		super(sTokenString);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see TokenStringFormat#getToken(String)
	 */
	@Override
	protected Token<? super Relatable> getToken(String sToken)
	{
		RelationType<?>			 rType   = RelationType.valueOf(sToken);
		Token<? super Relatable> rResult;

		if (rType != null)
		{
			rResult = new RelationToken(this, rType);
		}
		else
		{
			rResult = super.getToken(sToken);
		}

		return rResult;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A token subclass that allows to access relations. The relation type is
	 * stored in the token instance.
	 *
	 * @author eso
	 */
	public static class RelationToken extends Token<Relatable>
	{
		//~ Instance fields ----------------------------------------------------

		private final RelationType<?> rRelationType;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance for a certain relation type.
		 *
		 * @param rParent The parent relation token format
		 * @param rType   The relation type for this instance
		 */
		protected RelationToken(
			RelationTokenFormat rParent,
			RelationType<?>		rType)
		{
			super(rParent, rType.getName(), null);

			rRelationType = rType;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the value of the relation with this token's relation type.
		 *
		 * @see de.esoco.lib.expression.function.TokenStringFormat.Token#extractValue(Object)
		 */
		@Override
		protected Object extractValue(Relatable rInput)
		{
			return rInput.get(rRelationType);
		}
	}
}
