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
package de.esoco.lib.event;

/**
 * An listener interface for objects that need to be notified of editing events.
 */
public interface EditListener<T> {

	/**
	 * Enumeration of the standard actions for the editing of objects.
	 */
	enum EditAction {SAVE, CANCEL}

	/**
	 * Will be invoked after the editing of an object has been finished with a
	 * certain action.
	 *
	 * @param object       The edited object
	 * @param finishAction The edit action that caused the finishing
	 */
	void editFinished(T object, EditAction finishAction);

	/**
	 * Will be invoked when the editing of a TLD begins.
	 *
	 * @param object The edited object
	 */
	void editStarted(T object);
}
