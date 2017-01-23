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
package de.esoco.lib.event;

/********************************************************************
 * An extended event listener interface that requires from implementations to
 * provide a single {@link #handleEvent(Event)} method.
 *
 * @author eso
 */
@FunctionalInterface
public interface EventHandler<E extends Event<?>> extends EventListener
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * This method must be implemented to handle an event.
	 *
	 * @param rEvent The event that occurred
	 */
	public void handleEvent(E rEvent);
}
