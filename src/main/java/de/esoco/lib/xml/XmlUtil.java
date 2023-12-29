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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

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
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides static helper functions for the handling of XML documents and
 * files.
 *
 * @author eso
 */
public class XmlUtil {

	private static final String TAG_TEXT_CONTENT_NODE = "#text";

	/**
	 * Private, only static use.
	 */
	private XmlUtil() {
	}

	/**
	 * Puts all child tag elements of a node as properties into a map. The
	 * property key will be the tag name and the value will be retrieved from
	 * the tag's child node of the type "text".
	 *
	 * @param node The node to get the properties from
	 * @param map  The map to place the properties in
	 */
	public static void addTagsAsProperties(Node node,
		Map<String, String> map) {
		if (node instanceof Element) {
			Node child = node.getFirstChild();

			if (child instanceof Text) {
				map.put(node.getNodeName(), ((Text) child).getData());
			}
		}

		NodeList childNodes = node.getChildNodes();
		int count = childNodes.getLength();

		for (int i = 0; i < count; i++) {
			addTagsAsProperties(childNodes.item(i), map);
		}
	}

	/**
	 * Evaluate an XPath expression in the specified context and return the
	 * result as a String.
	 *
	 * @param document        The source document
	 * @param xpathExpression The expression to evaluate
	 * @return The String that is the result of evaluating the expression and
	 * converting the result to a String.
	 * @throws XPathExpressionException If the expression can not be evaluated.
	 */
	public static String evaluateXpathExpression(Document document,
		String xpathExpression) throws XPathExpressionException {
		XPathFactory xpathFactory = XPathFactory.newInstance();

		XPath xpath = xpathFactory.newXPath();

		return xpath.evaluate(xpathExpression, document);
	}

	/**
	 * Formats an XML string into a readable format with one tag per line.
	 *
	 * @param mL              The XML input string
	 * @param indent          The indentation of each XML level
	 * @param withDeclaration TRUE to include the XML declaration, FALSE to
	 *                           omit
	 *                        it
	 * @return The formatted XML string
	 * @throws IllegalArgumentException If the argument string cannot be
	 *                                  formatted
	 */
	public static String format(String mL, int indent,
		boolean withDeclaration) {
		try {
			Transformer transformer =
				SAXTransformerFactory.newInstance().newTransformer();

			Source source = new SAXSource(
				new InputSource(new ByteArrayInputStream(mL.getBytes())));

			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "" + indent);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
				withDeclaration ? "no" : "yes");

			transformer.transform(source, result);

			return writer.toString();
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Adds all child tag elements of a node as properties in a map. The
	 * property key will be the tag name and the value will be retrieved from
	 * the tag's child node of the type "text".
	 *
	 * @param node The node to get the properties from
	 * @return A new {@link LinkedHashMap} containing the node's tags as
	 * properties
	 */
	public static Map<String, String> getTagsAsProperties(Node node) {
		Map<String, String> properties = new LinkedHashMap<>();

		addTagsAsProperties(node, properties);

		return properties;
	}

	/**
	 * Returns the text content of a certain attribute of the first XML
	 * document
	 * node with a particular tag or NULL if either the node or the attribute
	 * doesn't exist. If the format string argument is not NULL it must contain
	 * a '%s' placeholder to insert the attribute value at.
	 *
	 * @param document      The XML document
	 * @param nodeTag       The XML tag of the node to query
	 * @param attributeName The name of the node attribute to query
	 * @param format        An optional format string or NULL for none
	 * @return The text content of the XML node. An empty string if the node is
	 * empty.
	 */
	public static String getXmlAttributeValue(Document document,
		String nodeTag,
		String attributeName, String format) {
		Node node = getXmlNode(document, nodeTag);
		String value = "";

		if (node != null) {
			NamedNodeMap attributes = node.getAttributes();

			if (attributes != null) {
				Node attribute = attributes.getNamedItem(attributeName);

				if (attribute != null) {
					value = attribute.getTextContent();

					if (format != null) {
						value = String.format(format, value);
					}
				}
			}
		}

		return value;
	}

	/**
	 * Returns the first XML document node with a certain tag or NULL if the
	 * node doesn't exist.
	 *
	 * @param document document The XML document
	 * @param nodeTag  The XML tag of the node to query
	 * @return The node or NULL for none
	 */
	public static Node getXmlNode(Document document, String nodeTag) {
		List<Node> nodes = getXmlNodes(document, nodeTag);

		return nodes.size() > 0 ? nodes.get(0) : null;
	}

	/**
	 * Returns the text content of the first XML document node with a certain
	 * tag or NULL if the node doesn't exist.
	 *
	 * @param document The XML document
	 * @param nodeTag  The XML tag of the node to query
	 * @return The node value or an empty string
	 */
	public static String getXmlNodeContent(Document document, String nodeTag) {
		return getXmlNodeContent(document, nodeTag, null);
	}

	/**
	 * Returns the text content of the first XML document node with a certain
	 * tag or NULL if the node doesn't exist. If the format string argument is
	 * not NULL it must contain a '%s' placeholder to insert the node content
	 * at.
	 *
	 * @param document The XML document
	 * @param nodeTag  The XML tag of the node to query
	 * @param format   An optional format string or NULL for none
	 * @return The node value or an empty string
	 */
	public static String getXmlNodeContent(Document document, String nodeTag,
		String format) {
		Node node = getXmlNode(document, nodeTag);
		String value = "";

		if (node != null) {
			value = node.getTextContent();

			if (format != null) {
				value = String.format(format, value);
			}
		}

		return value;
	}

	/**
	 * Returns all XML document nodes with a certain tag.
	 *
	 * @param document The XML document
	 * @param nodeTag  The XML tag of the nodes to query
	 * @return The list of nodes (may be empty)
	 */
	public static List<Node> getXmlNodes(Document document, String nodeTag) {
		NodeList nodeList = document.getElementsByTagName(nodeTag);
		List<Node> nodes = null;

		if (nodeList != null) {
			int size = nodeList.getLength();

			if (size > 0) {
				nodes = new ArrayList<Node>(size);

				for (int i = 0; i < size; i++) {
					nodes.add(nodeList.item(i));
				}
			}
		}

		if (nodes == null) {
			nodes = Collections.emptyList();
		}

		return nodes;
	}

	/**
	 * Returns the text contents of all XML document nodes with a certain tag.
	 *
	 * @param document The XML document
	 * @param nodeTag  The XML tag of the nodes to query
	 * @return A list containing the node values (may be empty)
	 */
	public static List<String> getXmlNodesContent(Document document,
		String nodeTag) {
		List<Node> nodes = getXmlNodes(document, nodeTag);
		List<String> values = new ArrayList<String>(nodes.size());

		for (Node node : nodes) {
			values.add(node.getTextContent());
		}

		return values;
	}

	/**
	 * Converts an XML node and it's hierarchy of child nodes (if available)
	 * into a readable string representation.
	 *
	 * @param node        The root XML node
	 * @param indent      A indentation prefix string or NULL for none
	 * @param includeRoot TRUE to include the root node in the result
	 * @return The string representation of the node hierarchy
	 */
	public static String nodeHierarchyToString(Node node, String indent,
		boolean includeRoot) {
		return nodeHierarchyToString(node, indent, includeRoot, 0);
	}

	/**
	 * Internal method to convert an XML node and it's hierarchy of child nodes
	 * (if available) into a readable string representation.
	 *
	 * @param node        The root XML node
	 * @param indent      A indentation prefix string or NULL for none
	 * @param includeRoot TRUE to include the root node in the result
	 * @param level       The indentation level
	 * @return The string representation of the node hierarchy
	 */
	private static String nodeHierarchyToString(Node node, String indent,
		boolean includeRoot, int level) {
		String nodeName = node.getNodeName();
		NamedNodeMap attributes = node.getAttributes();
		NodeList childNodes = node.getChildNodes();
		StringBuilder result = new StringBuilder();

		int childCount = childNodes != null ? childNodes.getLength() : 0;

		if (includeRoot) {
			if (nodeName != null) {
				nodeName = nodeName.replace(':', '-');
			}

			int attrCount = attributes != null ? attributes.getLength() : 0;

			for (int i = 0; i < level; i++) {
				result.append(indent);
			}

			result.append(nodeName);

			for (int i = 0; i < attrCount; i++) {
				Node attribute = attributes.item(i);

				result.append(" ");
				result.append(attribute.getNodeName());
				result.append("=");
				result.append(attribute.getNodeValue());
			}

			if (childCount == 1 && TAG_TEXT_CONTENT_NODE.equals(
				childNodes.item(0).getNodeName())) {
				String content = node.getTextContent();

				result.append(": ");
				result.append(content);
			} else if (childCount > 0) {
				result.append(": ");
			}

			result.append('\n');
			level++;
		}

		for (int i = 0; i < childCount; i++) {
			Node child = childNodes.item(i);

			if (!TAG_TEXT_CONTENT_NODE.equals(child.getNodeName())) {
				result.append(
					nodeHierarchyToString(child, indent, true, level));
			}
		}

		return result.toString();
	}

	/**
	 * Returns a string representation of an XML node and it's hierarchy.
	 *
	 * @param node The node to convert into a string
	 * @return The string representation of the given node
	 */
	public static String toString(Node node) {
		try {
			Source source = new DOMSource(node);
			StringWriter output = new StringWriter();
			Result result = new StreamResult(output);

			Transformer transformer =
				TransformerFactory.newInstance().newTransformer();

			transformer.transform(source, result);

			return output.toString();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
