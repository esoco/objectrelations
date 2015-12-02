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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/********************************************************************
 * A related object subclass that implements the {@link Serializable} interface
 * so that it can be serialized to a stream. Applications that need serializable
 * related objects should use or extend this class.
 *
 * <p>When serializing a related object all of it's relations will be serialized
 * (with the exception of transient relations that have a type with the modifier
 * {@link RelationTypeModifier#TRANSIENT} set). This means that all target
 * objects of these relations must be serializable too, or else an exception
 * will be thrown.</p>
 *
 * <p>If target objects are (serializable) related objects or if relations are
 * set on the relations, these sub-relations will be serialized too. Trying to
 * serialize very complex relation structures may result in large serialization
 * output or even cause stack overflow errors. Therefore it is necessary to
 * monitor the complexity of the objects to be serialized and to use transient
 * relation types if necessary.</p>
 *
 * @author eso
 */
public class SerializableRelatedObject extends RelatedObject
	implements Serializable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see RelatedObject#RelatedObject()
	 */
	public SerializableRelatedObject()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Deserializes this instance by reading it's state from the given input
	 * stream.
	 *
	 * @param  rIn The source input stream
	 *
	 * @throws IOException            If reading data fails
	 * @throws ClassNotFoundException If the class couldn't be found
	 */
	private void readObject(ObjectInputStream rIn) throws IOException,
														  ClassNotFoundException
	{
		rIn.defaultReadObject();
		readRelations(rIn);
	}

	/***************************************
	 * Serializes this instance by writing it's fields to the given output
	 * stream.
	 *
	 * @param      rOut The target output stream
	 *
	 * @throws     IOException If writing data fails
	 *
	 * @serialData First writes the name of the object space and then all
	 *             relations in the order in which they have been added to this
	 *             instance
	 */
	private void writeObject(ObjectOutputStream rOut) throws IOException
	{
		rOut.defaultWriteObject();
		writeRelations(rOut);
	}
}
