// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility methods for doing XPath parsing more concisely.
 * Suggested usage of this class is a static import (to save typing the class 
 * name: {@code import static ca.gc.nrc.iit.savoir.utils.XmlUtils.*;})
 * 
 * @author Aaron Moss
 */
public class XmlUtils {

	private static boolean debug = false;
	
	public static void setDebug(boolean debug) {
		XmlUtils.debug = debug;
	}
	
	/**
	 * Checks is there exist nodes that match the given expression
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	true for expression matches, false otherwise
	 */
	public static final boolean evalExists(
			XPath xpath, String expr, Object ctx) {
		
		try {
			return evalExists(xpath.compile(expr), ctx);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Checks is there exist nodes that match the given expression
	 * 
	 * @param expr		The XPath expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	true for expression matches, false otherwise
	 */
	public static final boolean evalExists(XPathExpression expr, Object ctx) {
		try {
			NodeList list = 
				(NodeList)expr.evaluate(ctx, XPathConstants.NODESET);
			
			return (list == null) ? false : list.getLength() > 0; 
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Evaluates the specified expression as a String
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The string value of the expression (the empty string for no 
	 * 		match)
	 */
	public static final String evalString(
			XPath xpath, String expr, Object ctx) {
		
		try {			
			return evalString(xpath.compile(expr), ctx);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as a String
	 * 
	 * @param expr		The XPath expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The string value of the expression (the empty string for no 
	 * 		match)
	 */
	public static final String evalString(XPathExpression expr, Object ctx) {
		try {
			return (String)expr.evaluate(ctx, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Equivalent to {@link #evalString()} except returns null instead of 
	 * empty string if expression does not exist (or exists multiple times)
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return The string value of the expression (null for no match)
	 */
	public static final String safeEvalString(
			XPath xpath, String expr, Object ctx) {
		
		try {
			return safeEvalString(xpath.compile(expr), ctx);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Equivalent to {@link #evalString()} except returns null instead of 
	 * empty string if expression does not exist
	 * 
	 * @param expr		The XPath expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return The string value of the expression (null for no match)
	 */
	public static final String safeEvalString(
			XPathExpression expr, Object ctx) {
		
		try {
			NodeList nodes = 
				(NodeList)expr.evaluate(ctx, XPathConstants.NODESET);
			if (nodes.getLength() > 0) {
				return nodes.item(0).getTextContent();
			} else {
				return null;
			}	
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as a double
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The floating-point value of the expression (null for error)
	 */
	public static final Double evalDouble(
			XPath xpath, String expr, Object ctx) {
		
		try {
			return evalDouble(xpath.compile(expr), ctx);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as a double
	 * 
	 * @param expr		The XPath expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The floating-point value of the expression (null for error)
	 */
	public static final Double evalDouble(XPathExpression expr, Object ctx) {
		try {
			return (Double)expr.evaluate(ctx, XPathConstants.NUMBER);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as an integer
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The integer value of the expression (null for error)
	 */
	public static final Integer evalInt(XPath xpath, String expr, Object ctx) {
		try {
			return evalInt(xpath.compile(expr), ctx);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as an integer
	 * 
	 * @param expr		The XPath expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The integer value of the expression (null for error)
	 */
	public static final Integer evalInt(XPathExpression expr, Object ctx) {
		try {
			Double d = (Double)expr.evaluate(ctx, XPathConstants.NUMBER);
			return (d == null) ? null : d.intValue();
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as a boolean, using standard XML 
	 * rules (that is, "true" or "1" is true, "false" or "0" is false, any 
	 * other value is erroneous)
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The boolean value of the expression (null for error)
	 */
	public static final Boolean evalBoolean(
			XPath xpath, String expr, Object ctx) {
		
		try {
			return evalBoolean(xpath.compile(expr), ctx);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as a boolean, using standard XML 
	 * rules (that is, "true" or "1" is true, "false" or "0" is false, any 
	 * other value is erroneous)
	 * 
	 * @param expr		The XPath expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The boolean value of the expression (null for error)
	 */
	public static final Boolean evalBoolean(XPathExpression expr, Object ctx) {
		String s = evalString(expr, ctx);		
		
		if (s == null || s.isEmpty()) return null;
		else if ("true".equals(s) || "1".equals(s)) return true;
		else if ("false".equals(s) || "0".equals(s)) return false;
		else return null;
	}
	
	/**
	 * Evaluates the specified expression as a value of an enum type. If the 
	 * string is not a value of the enum type, or another error occurs, returns 
	 * null.
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param clazz		The enum class to evaluate as
	 * @param ctx		The context to match it in
	 * 
	 * @return	The enum value of the expression (null for error)
	 */
	public static final <E extends Enum<E>> E evalEnum(
			XPath xpath, String expr, Class<E> clazz, Object ctx) {
		
		try {
			return evalEnum(xpath.compile(expr), clazz, ctx);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as a value of an enum type (that is, 
	 * any key of the enum type (case-sensitive, though allowing extra 
	 * whitespace) is that enum value, any other value is erroneous)
	 * 
	 * @param expr		The XPath expression to match against
	 * @param clazz		The enum class to evaluate against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The enum value of the expression (null for error)
	 */
	public static final <E extends Enum<E>> E evalEnum(
			XPathExpression expr, Class<E> clazz, Object ctx) {
		
		String s = evalString(expr, ctx);		
		
		if (s == null || s.isEmpty()) return null;
		s = s.trim();
		try {
			return Enum.valueOf(clazz, s);
		} catch (IllegalArgumentException e) {
			if (debug) System.err.println(e.getMessage());
			return null;
		} catch (NullPointerException e) {
			if (debug) System.err.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as an XML date, using standard XML 
	 * date/time parsing rules.
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The date value of the expression (null for error)
	 */
	public static final Date evalDate(XPath xpath, String expr, Object ctx) {
		try {
			return evalDate(xpath.compile(expr), ctx);
		} catch (XPathExpressionException e) {
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as an XML date, using standard XML 
	 * date/time parsing rules.
	 * 
	 * @param expr		The XPath expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The date value of the expression (null for error)
	 */
	public static final Date evalDate(XPathExpression expr, Object ctx) {
		String s = evalString(expr, ctx);
		if (s == null) return null;
		
		XMLGregorianCalendar cal = null;
		try {
			cal = 
				DatatypeFactory.newInstance().newXMLGregorianCalendar(s.trim());
		} catch (Exception ignored) {
		}
		if (cal == null) return null;
		
		return cal.toGregorianCalendar().getTime();
	}
	
	/**
	 * Evaluates the specified expression as a DOM Node
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The DOM Node matching the expression (null for error)
	 */
	public static final Node evalNode(XPath xpath, String expr, Object ctx) {
		try {
			return evalNode(xpath.compile(expr), ctx);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as a DOM Node
	 * 
	 * @param expr		The XPath expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The DOM Node matching the expression (null for error)
	 */
	public static final Node evalNode(XPathExpression expr, Object ctx) {
		try {
			return (Node)expr.evaluate(ctx, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Sets an attribute on a node.
	 * If an attribute already exists on this node with the given name, 
	 * overwrites it.
	 * 
	 * @param node		The node to set the attribute on (will fail if this is 
	 * 					not an element node)
	 * @param name		The name of the attribute to set
	 * @param value		The value to set the attribute to
	 * 
	 * @return {@code true} for success, {@code false} for failure
	 */
	public static final boolean setAttribute(
			Node node, String name, String value) {
		
		if (node instanceof Element) {
			try {
				((Element)node).setAttribute(name, value);
				return true;
			} catch (DOMException e) {
				return false;
			}
		} else return false;
	}
	
	/**
	 * Evaluates the specified expression as a list of DOM Nodes
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The DOM NodeList of nodes matching the expression (null for 
	 * 		error)
	 */
	public static final NodeList evalNodeList(
			XPath xpath, String expr, Object ctx) {
		
		try {
			return evalNodeList(xpath.compile(expr), ctx);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Evaluates the specified expression as a list of DOM Nodes
	 * 
	 * @param xpath		The XPath evaluator to use
	 * @param expr		The expression to match against
	 * @param ctx		The context to match it in
	 * 
	 * @return	The DOM NodeList of nodes matching the expression (null for 
	 * 		error)
	 */
	public static final NodeList evalNodeList(
			XPathExpression expr, Object ctx) {
		
		try {
			return (NodeList)expr.evaluate(ctx, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}
	
	/** Transforms DOM nodes back to XML */
	private static Transformer nodeXform = null;
	/** Transforms DOM documents back to XML */
	private static Transformer docXform = null;
	static {
		try {
			nodeXform = TransformerFactory.newInstance().newTransformer();
			nodeXform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			docXform = TransformerFactory.newInstance().newTransformer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes a DOM node to the XML string it represents
	 * 
	 * @param node	The DOM node to write
	 * 
	 * @return a String representing the XML of that node (not necessarily 
	 * 		identical to the one that created it), null on error
	 */
	public static final String toXmlString(Node node) {
		//wrap source node
		Source src = new DOMSource(node);
		
		//wrap result string
		StringWriter out = new StringWriter();
		Result res = new StreamResult(out);
		
		try {
			nodeXform.transform(src, res);
		} catch (TransformerException e) {
			return null;
		}
		
		//return result string
		return out.toString();
	}
	
	/**
	 * Writes a DOM document to the XML string it represents
	 * 
	 * @param doc	The DOM document to write
	 * 
	 * @return a String representing the XML of that document (not necessarily 
	 * 		identical to the one that created it), null on error
	 */
	public static final String toXmlString(Document doc) {
		//wrap source node
		Source src = new DOMSource(doc);
		
		//wrap result string
		StringWriter out = new StringWriter();
		Result res = new StreamResult(out);
		
		try {
			docXform.transform(src, res);
		} catch (TransformerException e) {
			return null;
		}
		
		//return result string
		return out.toString();
	}
	
	/**
	 * Converts an XML-formatted string to a DOM document
	 * 
	 * @param xml		The String to convert
	 * 
	 * @return The document corresponding to that String
	 * 
	 * @throws ParserConfigurationException  
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static final Document toDocument(String xml) 
			throws ParserConfigurationException, SAXException, IOException {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(xml)));
		doc.getDocumentElement().normalize();
		return doc;
	}
	
	/**
	 * Converts a DOM Node to an DOM document (will wrap the node as the root 
	 * of a new document, if necessary) 
	 * 
	 * @param node		The node to convert or wrap
	 * 
	 * @return An DOM document rooted at the node, null for error 
	 * 
	 * @throws ParserConfigurationException 
	 */
	public static final Document toDocument(Node node) 
			throws ParserConfigurationException {
		
		//if already a document, cast and return
		if (node instanceof Document) return (Document)node;
		
		//otherwise, create a new document, and attempt to import the node
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().newDocument();
		
		//import node, and add to document
		Node imported = doc.importNode(node, true);
		doc.appendChild(imported);
		
		return doc;
	}
	
	/** Denotes a required attribute */
	public static final boolean REQUIRED = true;
	/** Denote an optional attribute */
	public static final boolean OPTIONAL = false;
	
	/**
	 * Adds an XML attribute to a string builder. The attribute will be omitted 
	 * if it is optional and its value is {@code null} or empty.
	 * 
	 * @param sb			The string builder to add the attribute to
	 * @param key			The key of the attribute
	 * @param value			The value of the attribute
	 * @param required		{@code true} for required, 
	 * 						{@code false} for optional
	 */
	public static final void addAttr(StringBuilder sb, String key,	
			String value, boolean required) {
		
		if (required == true || (value != null && !value.isEmpty())) {
			sb.append(key).append("=\"").append(escapeXml(value)).append("\" ");
		}
	}
	
	/**
	 * Adds an XML attribute to a string builder. The attribute will be omitted 
	 * if it is optional and its value is {@code null}.
	 * 
	 * @param sb			The string builder to add the attribute to
	 * @param key			The key of the attribute
	 * @param value			The value of the attribute
	 * @param required		{@code true} for required, 
	 * 						{@code false} for optional
	 */
	public static final void addAttr(StringBuilder sb, String key,	
			Integer value, boolean required) {
		
		if (value != null) {
			sb.append(key).append("=\"").append(value.toString()).append("\" ");
		} else if (required == true) {
			sb.append(key).append("=\"\" ");
		}
	}
	
	/**
	 * Adds an XML text node to a string builder. The node will be omitted 
	 * if it is optional and its value is {@code null}.
	 * 
	 * @param sb			The string builder to add the node to
	 * @param name			The name of the node to add
	 * @param value			The value of the text node
	 * @param required		{@code true} for required, 
	 * 						{@code false} for optional
	 */
	public static final void addTextNode(StringBuilder sb, String name, 
			String value, boolean required) {
		
		if (required == true || (value != null && !value.isEmpty())) {
			sb.append("<").append(name).append(">")
				.append(escapeXml(value))
				.append("</").append(name).append(">");
		}
	}
	
	/**
	 * Replaces &, <, >, ", and ' with their XML escape codes.
	 * If s is null, replaces it with the empty string.
	 */
	public static final String escapeXml(String s) {
		if (s == null) return "";

		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			switch (c) {
			case '&':	sb.append("&amp;");		break;
			case '<':	sb.append("&lt;");		break;
			case '>':	sb.append("&gt;");		break;
			case '\"':	sb.append("&quot;");	break;
			case '\'':	sb.append("&apos;");	break;
			default:	sb.append(c);			break;
			}
		}
		return sb.toString();
	}
}
