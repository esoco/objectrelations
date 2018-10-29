//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
 * An enumeration of modifiers that define certain static aspects of relation
 * types. The following modifiers are currently available:
 *
 * <ul>
 *   <li>{@link #FINAL}: relations with a final modifier cannot be modified or
 *     deleted after they have been created. If the relation type provides an
 *     initial value the final relation will also be created on the first access
 *     to the relation target with the {@link Relatable#get(RelationType)}
 *     method. To avoid this automatic creation an application must either set
 *     the relation first with {@link Relatable#set(RelationType, Object)} or
 *     provide an initial value of NULL for the relation type.</li>
 *   <li>{@link #READONLY}: other than final ones relation types with a readonly
 *     modifier cannot be modified at all, even when they are created (i.e. they
 *     cannot be set with an initial value). Such a type must always return a
 *     value from it's {@link RelationType#initialValue(Relatable)} method
 *     because otherwise the value of relations with that type would always be
 *     NULL. The readonly modifier is typically used for relation types that
 *     generate their target value automatically, e.g. by calculating them from
 *     other relations or by performing a lookup on some kind of resource.</li>
 *   <li>{@link #TRANSIENT}: relations with a transient modifier will be ignored
 *     during the serialization of a {@link SerializableRelatedObject}.</li>
 *   <li>{@link #PRIVATE}: relations with a private modifier will not be visible
 *     in predicate-based relation queries or to relation listeners. They can
 *     only be accessed by methods that use an explicit relation type argument.
 *     Therefore only code to which the type constant is accessible will have
 *     access to the relations with a private type. This modifier can also be
 *     used to prevent write access to certain relations: by internally using a
 *     writable relation type that is non-public and another public type as a
 *     view with {@link Relation#viewAs(RelationType, Relatable,
 *     de.esoco.lib.expression.Function)} (because views are always
 *     readonly).</li>
 * </ul>
 */
public enum RelationTypeModifier
{
	/** Cannot be modified after first initialization. */ FINAL, /** Can never be modified and always returns the initial value. */
	READONLY, /** Will not be serialized. */ TRANSIENT, /** Not visible without access to the relation type. */
	PRIVATE
}
