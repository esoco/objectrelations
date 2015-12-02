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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/********************************************************************
 * Contains ObjectRelations-specific annotations.
 *
 * @author eso
 */
public class Annotations
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private Annotations()
	{
	}

	//~ Annotations ------------------------------------------------------------

	/********************************************************************
	 * An annotation for classes that contains a different namespace for
	 * declared relation types than that of the declaring class. The name must
	 * be a standard Java package name without a trailing dot.
	 *
	 * @author eso
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface RelationTypeNamespace
	{
		String value();
	}

	/********************************************************************
	 * An annotation for relation type fields where the field name should not be
	 * checked for equality with the relation type name. This can be the case if
	 * a relation type is defined in a different context and assigned to a field
	 * which needs to be renamed to prevent name conflicts with other "borrowed"
	 * fields.
	 *
	 * @author eso
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface NoRelationNameCheck
	{
	}
}
