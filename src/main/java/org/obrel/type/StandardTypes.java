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
package org.obrel.type;

import de.esoco.lib.datatype.ObjectId;
import de.esoco.lib.datatype.Pair;
import de.esoco.lib.datatype.Period;
import de.esoco.lib.datatype.Priority;
import de.esoco.lib.event.ElementEvent.EventType;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypeModifier.FINAL;
import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.core.RelationTypes.newIntType;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * This class contains definitions of standard relation types. It is recommended
 * that applications use these standard types if applicable to increase the
 * interoperability of relation-based code because using standard types allows
 * library code and tools to access and display such relations in a standard
 * way.
 *
 * @author eso
 */
@RelationTypeNamespace(RelationType.DEFAULT_NAMESPACE)
public class StandardTypes
{
	//~ Static fields/initializers ---------------------------------------------

	//- general relation types -------------------------------------------------

	/** The {@link ObjectId} of an object. */
	public static final RelationType<ObjectId<?>> OBJECT_ID = newType(FINAL);

	/** The name of an object. */
	public static final RelationType<String> NAME = newType();

	/**
	 * An additional name of an object, typically used as the first name of a
	 * person.
	 */
	public static final RelationType<String> FIRST_NAME = newType();

	/** An information string */
	public static final RelationType<String> INFO = newType();

	/** An arbitrary text. */
	public static final RelationType<String> TEXT = newType();

	/** A description string. */
	public static final RelationType<String> DESCRIPTION = newType();

	/**
	 * A property that contains a message string; the exact format of this
	 * property depends on the application but it is recommended to use either a
	 * plain text string or a format string that can be processed by methods
	 * like {@link String#format(String, Object...)}.
	 */
	public static final RelationType<String> MESSAGE = newType();

	/**
	 * A property that contains an error message string; the exact format of
	 * this property depends on the application but it is recommended to use
	 * either a plain text string or a format string that can be processed by
	 * methods like {@link String#format(String, Object...)}.
	 */
	public static final RelationType<String> ERROR_MESSAGE = newType();

	/** Holds an exception that occurred in the hierarchy of an object. */
	public static final RelationType<Exception> EXCEPTION = newType();

	/** A generic object reference to the previous value of an element. */
	public static final RelationType<Object> PREVIOUS_VALUE = newType();

	/** A generic filename relation. */
	public static final RelationType<String> FILENAME = newType();

	//- Integer values ---------------------------------------------------------

	/** A size value with an initial value of 0. */
	public static final RelationType<Integer> SIZE = newIntType();

	/** A count value with an initial value of 0. */
	public static final RelationType<Integer> COUNT = newIntType();

	/**
	 * An automatic types that counts the relation updates of a target object
	 * after it has been set on it. Depending on the target it counts the
	 * updates of the relations in a relatable object, of a relation type, or of
	 * a single relation. More specific counters can be created by using other
	 * instances of {@link CounterType}.
	 */
	@SuppressWarnings("boxing")
	public static final RelationType<Integer> UPDATE_COUNTER =
		CounterType.newIntCounter(e -> e.getType() == EventType.UPDATE);

	/** The ordinal number of an object. */
	public static final RelationType<Integer> ORDINAL = newType();

	/** An minimum integer value. */
	public static final RelationType<Integer> MINIMUM = newType();

	/** An maximum integer value. */
	public static final RelationType<Integer> MAXIMUM = newType();

	//- Enumeration properties -------------------------------------------------

	/** A priority value with the initial value {@link Priority#NORMAL}. */
	public static final RelationType<Priority> PRIORITY =
		newInitialValueType(Priority.NORMAL);

	//- Date and time properties -----------------------------------------------

	/** A generic date value. */
	public static final RelationType<Date> DATE = newType();

	/** A generic time value (of type {@link java.util.Date}). */
	public static final RelationType<Date> TIME = newType();

	/** A time duration as a pair of a value and time unit. */
	public static final RelationType<Pair<Long, TimeUnit>> DURATION = newType();

	/**
	 * An active type that always returns the number of milliseconds since it's
	 * creation.
	 */
	public static final RelationType<Long> TIMER = TimerType.newTimer();

	/**
	 * Similar to {@link #TIMER} but with {@link RelationTypeModifier#FINAL} so
	 * that it cannot be modified after creation.
	 */
	public static final RelationType<Long> UPTIME = TimerType.newTimer(FINAL);

	/** The start date from which an element will be valid. */
	public static final RelationType<Date> START_DATE = newType();

	/** The end date until which an element will be valid. */
	public static final RelationType<Date> END_DATE = newType();

	/** The next date on which an element should be processed in some way */
	public static final RelationType<Date> NEXT_DATE = newType();

	/** The period between the occurrence of date-based objects */
	public static final RelationType<Period> PERIOD = newType();

	//- hierarchy types --------------------------------------------------------

	/**
	 * The parent relatable object of an element in a hierarchical object
	 * structure. Applications can either use this type for their parent
	 * reference or define their own relation type with a more specific datatype
	 * and set the meta-flag {@link MetaTypes#PARENT_ATTRIBUTE} on it. Code that
	 * wants to find the parent attribute of a related object should first look
	 * for a relation type with the meta-flag set and if not found, for this
	 * type.
	 */
	public static final RelationType<Relatable> PARENT = newType();

	/**
	 * A list of the child relatable objects of an element in a hierarchical
	 * object structure. Applications can either use this type for the reference
	 * to their children or define their own relation type with a more specific
	 * datatype and set the meta-flag {@link MetaTypes#CHILD_ATTRIBUTE} on it.
	 * Code that wants to find the child attribute of a related object should
	 * first look for a relation type with the meta-flag set and if not found,
	 * for this type.
	 */
	public static final RelationType<List<Relatable>> CHILDREN = newListType();

	//- network types ----------------------------------------------------------
	/** A generic URI. */
	public static final RelationType<URI> URI = newType();

	/** The string of a generic URI. */
	public static final RelationType<String> URI_STRING = newType();

	/** A network URL. */
	public static final RelationType<URL> URL = newType();

	/** The string of a network URL. */
	public static final RelationType<String> URL_STRING = newType();

	/** The name of a host in a network connection. */
	public static final RelationType<String> HOST = newType();

	/** An Internet protocol (IP) address. */
	public static final RelationType<InetAddress> IP_ADDRESS = newType();

	/** A network port. */
	public static final RelationType<Integer> PORT = newType();

	/** A MIME content type string. */
	public static final RelationType<String> MIME_TYPE = newType();

	static
	{
		RelationTypes.init(StandardTypes.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private StandardTypes()
	{
	}
}
