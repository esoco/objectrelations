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
package de.esoco.lib.security;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * Contains security-related relation types.
 *
 * @author eso
 */
public class SecurityRelationTypes
{
	//~ Static fields/initializers ---------------------------------------------

	/** A login name for authentication purposes. */
	public static final RelationType<String> LOGIN_NAME = newType();

	/** A string token describing the authentication method to apply. */
	public static final RelationType<String> AUTHENTICATION_METHOD = newType();

	/** An authentication password. */
	public static final RelationType<String> PASSWORD = newType();

	/**
	 * An authentication credential in text form (e.g. the text representation
	 * of a certificate).
	 */
	public static final RelationType<String> CREDENTIAL = newType();

	/**
	 * An authentication credential in binary form (e.g. the binary
	 * representation of a certificate).
	 */
	public static final RelationType<byte[]> BINARY_CREDENTIAL = newType();

	/** A reference to an {@link AuthenticationService}. */
	public static final RelationType<AuthenticationService> AUTHENTICATION_SERVICE =
		RelationTypes.newType();

	static
	{
		RelationTypes.init(SecurityRelationTypes.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private SecurityRelationTypes()
	{
	}
}
