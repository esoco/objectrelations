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

import org.obrel.core.Relatable;
import org.obrel.type.StandardTypes;


/********************************************************************
 * Definition of services that perform user authentication based on data in a
 * relatable object.
 *
 * @author eso
 */
public interface AuthenticationService
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Authenticates an entity based on the data in the relatable argument. The
	 * relations of the authentication data object must contain the parameters
	 * necessary to perform the authentication. What exactly these parameters
	 * are depends on the service implementation. For the most basic type of a
	 * password authentication the argument should contain of the relations
	 * {@link StandardTypes#LOGIN_NAME} and {@link StandardTypes#PASSWORD}.
	 * Alternatively the types {@link StandardTypes#CREDENTIAL} or {@link
	 * StandardTypes#BINARY_CREDENTIAL} may be used (either with or without a
	 * login name).
	 *
	 * <p>Implementations may also choose to use different data for the
	 * authentication in which case the required parameters must be documented
	 * for the callers. They can also use the relatable argument to return
	 * addition information if they need to, e.g. a session ID or authentication
	 * token.</p>
	 *
	 * @param  rAuthData The relatable object containing the authentication data
	 *
	 * @return TRUE if the authentication was successful, FALSE if not
	 */
	public boolean authenticate(Relatable rAuthData);
}
