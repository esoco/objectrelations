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
package org.obrel.core;

/********************************************************************
 * An interface for object that provide access to relation-based configuration
 * values.
 *
 * @author eso
 */
@FunctionalInterface
public interface ProvidesConfiguration {
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns a configuration relation value from this instance or a default if
	 * no relation with the given type exists.
	 *
	 * @param  rType         The relation type
	 * @param  rDefaultValue The default value to return if no relation exists
	 *
	 * @return The relation value or the default
	 */
	<T> T getConfigValue(RelationType<T> rType, T rDefaultValue);

	/***************************************
	 * Sets a configuration value. The default implementation always throws an
	 * unsupported operation exception. If a mutable configuration is needed
	 * this method must be overridden by a subclass.
	 *
	 * @param rType  The relation type
	 * @param rValue The value to set
	 */
	default <T> void setConfigValue(RelationType<T> rType, T rValue) {
		throw new UnsupportedOperationException("Modifications not supported");
	}
}
