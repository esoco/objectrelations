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
package de.esoco.lib.xml;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.InputSource;


/********************************************************************
 * Provides static helper functions for the handling of XML documents and files.
 *
 * @author eso
 */
public class XmlUtil
{
	//~ Static fields/initializers ---------------------------------------------

	private static final String TAG_TEXT_CONTENT_NODE = "#text";

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private XmlUtil()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Puts all child tag elements of a node as properties into a map. The
	 * property key will be the tag name and the value will be retrieved from
	 * the tag's child node of the type "text".
	 *
	 * @param rNode The node to get the properties from
	 * @param rMap  The map to place the properties in
	 */
	public static void addTagsAsProperties(Node				   rNode,
										   Map<String, String> rMap)
	{
		if (rNode instanceof Element)
		{
			Node rChild = rNode.getFirstChild();

			if (rChild instanceof Text)
			{
				rMap.put(rNode.getNodeName(), ((Text) rChild).getData());
			}
		}

		NodeList rChildNodes = rNode.getChildNodes();
		int		 nCount		 = rChildNodes.getLength();

		for (int i = 0; i < nCount; i++)
		{
			addTagsAsProperties(rChildNodes.item(i), rMap);
		}
	}

	/***************************************
	 * Evaluate an XPath expression in the specified context and return the
	 * result as a String.
	 *
	 * @param  rDocument        The source document
	 * @param  sXpathExpression The expression to evaluate
	 *
	 * @return The String that is the result of evaluating the expression and
	 *         converting the result to a String.
	 *
	 * @throws XPathExpressionException If the expression can not be evaluated.
	 */
	public static String evaluateXpathExpression(
		Document rDocument,
		String   sXpathExpression) throws XPathExpressionException
	{
		XPathFactory aXpathFactory = XPathFactory.newInstance();

		XPath aXpath = aXpathFactory.newXPath();

		return aXpath.evaluate(sXpathExpression, rDocument);
	}

	/***************************************
	 * Formats an XML string into a readable format with one tag per line.
	 *
	 * @param  sXML             The XML input string
	 * @param  nIndent          The indentation of each XML level
	 * @param  bWithDeclaration TRUE to include the XML declaration, FALSE to
	 *                          omit it
	 *
	 * @return The formatted XML string
	 *
	 * @throws IllegalArgumentException If the argument string cannot be
	 *                                  formatted
	 */
	public static String format(String  sXML,
								int		nIndent,
								boolean bWithDeclaration)
	{
		try
		{
			Transformer aTransformer =
				SAXTransformerFactory.newInstance().newTransformer();

			Source aSource =
				new SAXSource(new InputSource(new ByteArrayInputStream(sXML.getBytes())));

			StringWriter aWriter = new StringWriter();
			StreamResult aResult = new StreamResult(aWriter);

			aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
			aTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
										   "" + nIndent);
			aTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
										   bWithDeclaration ? "no" : "yes");

			aTransformer.transform(aSource, aResult);

			return aWriter.toString();
		}
		catch (TransformerException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	/***************************************
	 * Adds all child tag elements of a node as properties in a map. The
	 * property key will be the tag name and the value will be retrieved from
	 * the tag's child node of the type "text".
	 *
	 * @param  rNode The node to get the properties from
	 *
	 * @return A new {@link LinkedHashMap} containing the node's tags as
	 *         properties
	 */
	public static Map<String, String> getTagsAsProperties(Node rNode)
	{
		Map<String, String> aProperties = new LinkedHashMap<>();

		addTagsAsProperties(rNode, aProperties);

		return aProperties;
	}

	/***************************************
	 * Returns the text content of a certain attribute of the first XML document
	 * node with a particular tag or NULL if either the node or the attribute
	 * doesn't exist. If the format string argument is not NULL it must contain
	 * a '%s' placeholder to insert the attribute value at.
	 *
	 * @param  rDocument      The XML document
	 * @param  sNodeTag       The XML tag of the node to query
	 * @param  sAttributeName The name of the node attribute to query
	 * @param  sFormat        An optional format string or NULL for none
	 *
	 * @return The text content of the XML node. An empty string if the node is
	 *         empty.
	 */
	public static String getXmlAttributeValue(Document rDocument,
											  String   sNodeTag,
											  String   sAttributeName,
											  String   sFormat)
	{
		Node   rNode  = getXmlNode(rDocument, sNodeTag);
		String sValue = "";

		if (rNode != null)
		{
			NamedNodeMap rAttributes = rNode.getAttributes();

			if (rAttributes != null)
			{
				Node rAttribute = rAttributes.getNamedItem(sAttributeName);

				if (rAttribute != null)
				{
					sValue = rAttribute.getTextContent();

					if (sFormat != null)
					{
						sValue = String.format(sFormat, sValue);
					}
				}
			}
		}

		return sValue;
	}

	/***************************************
	 * Returns the first XML document node with a certain tag or NULL if the
	 * node doesn't exist.
	 *
	 * @param  rDocument rDocument The XML document
	 * @param  sNodeTag  The XML tag of the node to query
	 *
	 * @return The node or NULL for none
	 */
	public static Node getXmlNode(Document rDocument, String sNodeTag)
	{
		List<Node> rNodes = getXmlNodes(rDocument, sNodeTag);

		return rNodes.size() > 0 ? rNodes.get(0) : null;
	}

	/***************************************
	 * Returns the text content of the first XML document node with a certain
	 * tag or NULL if the node doesn't exist.
	 *
	 * @param  rDocument The XML document
	 * @param  sNodeTag  The XML tag of the node to query
	 *
	 * @return The node value or an empty string
	 */
	public static String getXmlNodeContent(Document rDocument, String sNodeTag)
	{
		return getXmlNodeContent(rDocument, sNodeTag, null);
	}

	/***************************************
	 * Returns the text content of the first XML document node with a certain
	 * tag or NULL if the node doesn't exist. If the format string argument is
	 * not NULL it must contain a '%s' placeholder to insert the node content
	 * at.
	 *
	 * @param  rDocument The XML document
	 * @param  sNodeTag  The XML tag of the node to query
	 * @param  sFormat   An optional format string or NULL for none
	 *
	 * @return The node value or an empty string
	 */
	public static String getXmlNodeContent(Document rDocument,
										   String   sNodeTag,
										   String   sFormat)
	{
		Node   rNode  = getXmlNode(rDocument, sNodeTag);
		String sValue = "";

		if (rNode != null)
		{
			sValue = rNode.getTextContent();

			if (sFormat != null)
			{
				sValue = String.format(sFormat, sValue);
			}
		}

		return sValue;
	}

	/***************************************
	 * Returns all XML document nodes with a certain tag.
	 *
	 * @param  rDocument The XML document
	 * @param  sNodeTag  The XML tag of the nodes to query
	 *
	 * @return The list of nodes (may be empty)
	 */
	public static List<Node> getXmlNodes(Document rDocument, String sNodeTag)
	{
		NodeList   rNodeList = rDocument.getElementsByTagName(sNodeTag);
		List<Node> rNodes    = null;

		if (rNodeList != null)
		{
			int nSize = rNodeList.getLength();

			if (nSize > 0)
			{
				rNodes = new ArrayList<Node>(nSize);

				for (int i = 0; i < nSize; i++)
				{
					rNodes.add(rNodeList.item(i));
				}
			}
		}

		if (rNodes == null)
		{
			rNodes = Collections.emptyList();
		}

		return rNodes;
	}

	/***************************************
	 * Returns the text contents of all XML document nodes with a certain tag.
	 *
	 * @param  rDocument The XML document
	 * @param  sNodeTag  The XML tag of the nodes to query
	 *
	 * @return A list containing the node values (may be empty)
	 */
	public static List<String> getXmlNodesContent(
		Document rDocument,
		String   sNodeTag)
	{
		List<Node>   rNodes  = getXmlNodes(rDocument, sNodeTag);
		List<String> aValues = new ArrayList<String>(rNodes.size());

		for (Node rNode : rNodes)
		{
			aValues.add(rNode.getTextContent());
		}

		return aValues;
	}

	/***************************************
	 * Converts an XML node and it's hierarchy of child nodes (if available)
	 * into a readable string representation.
	 *
	 * @param  rNode        The root XML node
	 * @param  sIndent      A indentation prefix string or NULL for none
	 * @param  bIncludeRoot TRUE to include the root node in the result
	 *
	 * @return The string representation of the node hierarchy
	 */
	public static String nodeHierarchyToString(Node    rNode,
											   String  sIndent,
											   boolean bIncludeRoot)
	{
		return nodeHierarchyToString(rNode, sIndent, bIncludeRoot, 0);
	}

	/***************************************
	 * Returns a string representation of an XML node and it's hierarchy.
	 *
	 * @param  rNode The node to convert into a string
	 *
	 * @return The string representation of the given node
	 */
	public static String toString(Node rNode)
	{
		try
		{
			Source		 aSource = new DOMSource(rNode);
			StringWriter aOutput = new StringWriter();
			Result		 aResult = new StreamResult(aOutput);

			Transformer aTransformer =
				TransformerFactory.newInstance().newTransformer();

			aTransformer.transform(aSource, aResult);

			return aOutput.toString();
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e);
		}
	}

	/***************************************
	 * Internal method to convert an XML node and it's hierarchy of child nodes
	 * (if available) into a readable string representation.
	 *
	 * @param  rNode        The root XML node
	 * @param  sIndent      A indentation prefix string or NULL for none
	 * @param  bIncludeRoot TRUE to include the root node in the result
	 * @param  nLevel       The indentation level
	 *
	 * @return The string representation of the node hierarchy
	 */
	private static String nodeHierarchyToString(Node    rNode,
												String  sIndent,
												boolean bIncludeRoot,
												int		nLevel)
	{
		String		  sNodeName   = rNode.getNodeName();
		NamedNodeMap  rAttributes = rNode.getAttributes();
		NodeList	  rChildNodes = rNode.getChildNodes();
		StringBuilder aResult     = new StringBuilder();

		int nChildCount = rChildNodes != null ? rChildNodes.getLength() : 0;

		if (bIncludeRoot)
		{
			if (sNodeName != null)
			{
				sNodeName = sNodeName.replace(':', '-');
			}

			int nAttrCount = rAttributes != null ? rAttributes.getLength() : 0;

			for (int i = 0; i < nLevel; i++)
			{
				aResult.append(sIndent);
			}

			aResult.append(sNodeName);

			for (int i = 0; i < nAttrCount; i++)
			{
				Node rAttribute = rAttributes.item(i);

				aResult.append(" ");
				aResult.append(rAttribute.getNodeName());
				aResult.append("=");
				aResult.append(rAttribute.getNodeValue());
			}

			if (nChildCount == 1 &&
				TAG_TEXT_CONTENT_NODE.equals(rChildNodes.item(0).getNodeName()))
			{
				String sContent = rNode.getTextContent();

				aResult.append(": ");
				aResult.append(sContent);
			}
			else if (nChildCount > 0)
			{
				aResult.append(": ");
			}

			aResult.append('\n');
			nLevel++;
		}

		for (int i = 0; i < nChildCount; i++)
		{
			Node rChild = rChildNodes.item(i);

			if (!TAG_TEXT_CONTENT_NODE.equals(rChild.getNodeName()))
			{
				aResult.append(nodeHierarchyToString(rChild,
													 sIndent,
													 true,
													 nLevel));
			}
		}

		return aResult.toString();
	}
}
