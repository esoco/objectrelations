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

/********************************************************************
 * Runtime exception that signals an unresolvable relation.
 *
 * @author eso
 */
public class UnresolvableRelationException extends RuntimeException
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Default constructor.
	 */
	public UnresolvableRelationException()
	{
	}

	/***************************************
	 * Creates an instance with an error message.
	 *
	 * @param rMessage The error message
	 */
	public UnresolvableRelationException(String rMessage)
	{
		super(rMessage);
	}

	/***************************************
	 * Creates an instance with a causing exception.
	 *
	 * @param rCause The causing exception
	 */
	public UnresolvableRelationException(Throwable rCause)
	{
		super(rCause);
	}

	/***************************************
	 * Creates an instance with an error message and a causing exception.
	 *
	 * @param rMessage The error message
	 * @param rCause   The causing exception
	 */
	public UnresolvableRelationException(String rMessage, Throwable rCause)
	{
		super(rMessage, rCause);
	}
}
