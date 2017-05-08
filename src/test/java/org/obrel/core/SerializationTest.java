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

import de.esoco.lib.text.TextUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import org.obrel.type.StandardTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.filter.RelationFilters.ALL_RELATIONS;


/********************************************************************
 * Tests serialization of object-relation classes.
 *
 * @author eso
 */
public class SerializationTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final RelationType<List<String>> TEST_STRINGS =
		newListType();

	private static final byte[] NAME_PROPERTY_TYPE_BYTES =
	{
		(byte) 0xAC, (byte) 0xED, 0x00, 0x05, 0x73, 0x72, 0x00, 0x1B, 0x6F,
		0x72, 0x67, 0x2E, 0x6F, 0x62, 0x72, 0x65, 0x6C, 0x2E, 0x63, 0x6F, 0x72,
		0x65, 0x2E, 0x52, 0x65, 0x6C, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x54, 0x79,
		0x70, 0x65, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x00,
		0x01, 0x4C, 0x00, 0x05, 0x73, 0x4E, 0x61, 0x6D, 0x65, 0x74, 0x00, 0x12,
		0x4C, 0x6A, 0x61, 0x76, 0x61, 0x2F, 0x6C, 0x61, 0x6E, 0x67, 0x2F, 0x53,
		0x74, 0x72, 0x69, 0x6E, 0x67, 0x3B, 0x78, 0x70, 0x74, 0x00, 0x04, 0x4E,
		0x41, 0x4D, 0x45
	};

	static
	{
		RelationTypes.init(SerializationTest.class);
	}

	//~ Instance fields --------------------------------------------------------

	private ByteArrayOutputStream aByteOut;

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Helper method to create the 'NAME_PROPERTY_TYPE_BYTES' array. Only needed
	 * when the serialized form of the {@link StandardTypes#NAME} relation type
	 * changes. Remove the comment of the {@link BeforeClass} annotation to
	 * print the serialized data to the console, then copy it into the array.
	 */
//	@BeforeClass
	public static void createNameRelationTypeBytes()
	{
		try
		{
			ByteArrayOutputStream aOut    = new ByteArrayOutputStream();
			ObjectOutputStream    aObjOut = new ObjectOutputStream(aOut);

			aObjOut.writeObject(StandardTypes.NAME);
			aObjOut.close();
			aOut.close();

			byte[] aBytes = aOut.toByteArray();

			System.out.printf("0x%s\n", TextUtil.hexString(aBytes, ", 0x"));
		}
		catch (IOException e)
		{
			System.out.println("ERR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Reads the relation type instance {@link StandardTypes#NAME} from the
	 * 'NAME_PROPERTY_TYPE_BYTES' array. The name of this test method starts
	 * with an underscore because it must be run first to test the
	 * deserialization of a type that hasn't been defined yet by accessing a
	 * standard type. This will test the automatic relation type registration
	 * mechanism that is implemented by the class {@link RelationType} which in
	 * turn is based on {@link ObjectRelations#registerRelationTypes(Class[])}.
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void _deserializeNameType() throws IOException,
											  ClassNotFoundException
	{
		ObjectInputStream in =
			new ObjectInputStream(new ByteArrayInputStream(NAME_PROPERTY_TYPE_BYTES));

		in.readObject();
	}

	/***************************************
	 * Test serialization of alias relations.
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void serializeAliasRelations() throws IOException,
												 ClassNotFoundException
	{
		RelatedObject	   obj = new SerializableRelatedObject();
		ObjectOutputStream out = createOutputStream();

		obj.set(StandardTypes.NAME, "TESTNAME")
		   .viewAs(RelationTest.TEST_ID, obj);
		obj.set(StandardTypes.DESCRIPTION, "TESTDESC")
		   .aliasAs(StandardTypes.INFO, obj);

		out.writeObject(obj);

		ObjectInputStream in = createInputStream();

		obj = (RelatedObject) in.readObject();

		assertEquals(4, obj.getRelationCount(ALL_RELATIONS));
		assertEquals("TESTNAME", obj.get(StandardTypes.NAME));
		assertEquals("TESTNAME", obj.get(RelationTest.TEST_ID));
		assertEquals("TESTDESC", obj.get(StandardTypes.DESCRIPTION));
		assertEquals("TESTDESC", obj.get(StandardTypes.INFO));

		obj.set(StandardTypes.INFO, "NEWDESC");
		assertEquals("NEWDESC", obj.get(StandardTypes.DESCRIPTION));
		assertEquals("NEWDESC", obj.get(StandardTypes.INFO));

		try
		{
			obj.set(RelationTest.TEST_ID, "NEWNAME");
			assertTrue(false);
		}
		catch (UnsupportedOperationException e)
		{
			// This is expected because ID is a view which is readonly
		}
	}

	/***************************************
	 * Test serialization of relations that contain (meta-)relations.
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void serializeRelationRelations() throws IOException,
													ClassNotFoundException
	{
		RelatedObject	   obj = new SerializableRelatedObject();
		ObjectOutputStream out = createOutputStream();
		Relation<String>   rel = obj.set(StandardTypes.NAME, "TESTNAME");

		rel.set(StandardTypes.DESCRIPTION, "TESTDESC");
		rel.get(TEST_STRINGS).add("TEST1");
		rel.get(TEST_STRINGS).add("TEST2");

		out.writeObject(obj);

		ObjectInputStream in = createInputStream();

		obj = (RelatedObject) in.readObject();
		rel = obj.getRelation(StandardTypes.NAME);

		assertEquals(2, rel.getRelationCount(ALL_RELATIONS));
		assertEquals("TESTDESC", rel.get(StandardTypes.DESCRIPTION));

		List<String> aTestStrings = rel.get(TEST_STRINGS);

		assertEquals(2, aTestStrings.size());
		assertEquals("TEST1", aTestStrings.get(0));
		assertEquals("TEST2", aTestStrings.get(1));
	}

	/***************************************
	 * Test serialization of simple relations.
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void serializeRelations() throws IOException, ClassNotFoundException
	{
		RelatedObject	   obj = new SerializableRelatedObject();
		ObjectOutputStream out = createOutputStream();

		obj.set(StandardTypes.NAME, "TESTNAME");
		obj.set(StandardTypes.DESCRIPTION, "TESTDESC");
		obj.get(TEST_STRINGS).add("TEST1");
		obj.get(TEST_STRINGS).add("TEST2");

		out.writeObject(obj);

		ObjectInputStream in = createInputStream();

		obj = (RelatedObject) in.readObject();

		assertEquals(3, obj.getRelationCount(ALL_RELATIONS));
		assertEquals("TESTNAME", obj.get(StandardTypes.NAME));
		assertEquals("TESTDESC", obj.get(StandardTypes.DESCRIPTION));

		List<String> aTestStrings = obj.get(TEST_STRINGS);

		assertEquals(2, aTestStrings.size());
		assertEquals("TEST1", aTestStrings.get(0));
		assertEquals("TEST2", aTestStrings.get(1));
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void serializeRelationTypes() throws IOException,
												ClassNotFoundException
	{
		ObjectOutputStream out = createOutputStream();

		out.writeObject(StandardTypes.NAME);

		ObjectInputStream in = createInputStream();

		assertEquals(StandardTypes.NAME, in.readObject());
	}

	/***************************************
	 * Returns a new {@link ObjectInputStream} that reads from the byte array
	 * that had been created by {@link #createOutputStream()}.
	 *
	 * @return The new object input stream
	 *
	 * @throws IOException On errors
	 */
	ObjectInputStream createInputStream() throws IOException
	{
		return new ObjectInputStream(new ByteArrayInputStream(aByteOut
															  .toByteArray()));
	}

	/***************************************
	 * Returns a new {@link ObjectOutputStream} that writes to a byte array
	 * through {@link ByteArrayOutputStream}.
	 *
	 * @return The new stream
	 *
	 * @throws IOException On errors
	 */
	ObjectOutputStream createOutputStream() throws IOException
	{
		aByteOut = new ByteArrayOutputStream();

		return new ObjectOutputStream(aByteOut);
	}
}
