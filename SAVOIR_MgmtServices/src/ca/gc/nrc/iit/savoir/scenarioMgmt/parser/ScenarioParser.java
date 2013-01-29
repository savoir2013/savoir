// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ca.gc.nrc.iit.savoir.model.profile.ServiceProfile;
import ca.gc.nrc.iit.savoir.model.session.Action;
import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.model.session.Service;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ApnReservation.ApnConnection;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ApnReservation.ApnReservationMethod;
import ca.gc.nrc.iit.savoir.scenarioMgmt.parser.ScenarioParseState.Variable;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.ActivityRule;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.AndCondition;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.Comparison;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.Condition;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.ControlflowRule;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.NotCondition;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.OrCondition;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.UpdateConsequence;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.VarBinding;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.WhenCondition;
import ca.gc.nrc.iit.savoir.mgmtUtils.ProfileUtils;
import ca.gc.nrc.iit.savoir.utils.XmlUtils;

import static ca.gc.nrc.iit.savoir.utils.XmlUtils.*;

/**
 * Parses a scenario XML file, generating a rule file.
 * 
 * Has two global properties, "{@code debug}" and "{@code validation}". 
 * "{@code debug}" is a boolean property, which, if true, will send a record of 
 * every element and attribute found to stdout; its default value is false. 
 * "{@code validation}" is a string property which takes one of the following 
 * values: "{@value #VALIDATION_LENIENT}" (the default), 
 * "{@value #VALIDATION_STRICT}", and "{@value #VALIDATION_NONE}". 
 * "{@value #VALIDATION_LENIENT}" logs validation errors to the error log, but 
 * does not fail, "{@value #VALIDATION_STRICT}" fails on error, and 
 * "{@value #VALIDATION_NONE}" does not perform validation at all. Unrecognized 
 * values of "{@code validation}" are treated the same as 
 * "{@value #VALIDATION_LENIENT}"
 * 
 * @author Aaron Moss
 */
public class ScenarioParser {

	private static boolean debug = false;

	/** 
	 * Bean value for lenient validation. Logs all validation errors. "{@value}" 
	 */
	public static final String VALIDATION_LENIENT = "lenient";
	
	/** 
	 * Bean value for strict validation. Throws exception on all validation 
	 * errors. "{@value}" 
	 */
	public static final String VALIDATION_STRICT = "strict";
	
	/**
	 * Bean value for no validation. "{@value}"
	 */
	public static final String VALIDATION_NONE = "none";
	
	private static String validation = VALIDATION_LENIENT;
	
	public static void setDebug(boolean debug) {
		ScenarioParser.debug = debug;
		XmlUtils.setDebug(debug);
	}
	
	public static void setValidation(String validation) {
		ScenarioParser.validation = validation;
	}
	
	private static Schema scenarioSchema = null;
	static {
		try {
			scenarioSchema = 
				SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
				.newSchema(new SAXSource(new InputSource(
						ScenarioParser.class.getClassLoader()
						.getResourceAsStream("scenario.xsd"))));
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses a scenario file.
	 * @param xml		The scenario file to parse
	 * @param warnings	A list to store error warnings.
	 * @return	The output of parsing the file
	 * @throws ParserConfigurationException on parser configuration failure 
	 * 			(shouldn't happen)
	 * @throws IOException on I/O error (shouldn't happen)
	 * @throws SAXException on invalid or malformed XML
	 */
	public static ScenarioParseState parseXml(String xml, List<String> warnings) 
			throws ParserConfigurationException, SAXException, IOException {
		
		//validate XML
		if (warnings != null && null == scenarioSchema) {
			if (!VALIDATION_NONE.equals(validation)) {
				warnings.add(
					"Scenario schema failed to load. No validation performed.");
			}
		} else if (VALIDATION_LENIENT.equals(validation)) {
			//set up lenient validation
			Validator val = scenarioSchema.newValidator();
			val.setErrorHandler(new LenientErrorHandler(warnings));
			val.validate(new SAXSource(new InputSource(new StringReader(xml))));
		} else if (VALIDATION_STRICT.equals(validation)) {
			//set up strict validation
			Validator val = scenarioSchema.newValidator();
			val.validate(new SAXSource(new InputSource(new StringReader(xml))));
		} else if (VALIDATION_NONE.equals(validation)) {
			//do nothing
		} else /* unrecognized value of validation property */{
			//set up lenient validation
			Validator val = scenarioSchema.newValidator();
			val.setErrorHandler(new LenientErrorHandler(warnings));
			val.validate(new SAXSource(new InputSource(new StringReader(xml))));
		}
		
		//set up XML parser
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(xml)));
		doc.getDocumentElement().normalize();
		XPath xpath = XPathFactory.newInstance().newXPath();
		//start parsing
		ScenarioParseState state = null;
		Node node = evalNode(xpath, "/scenario", doc);
		
		if (node != null) {
			if (debug) System.out.println("\t<scenario>");
			
			state = new ScenarioParseState();
			state.setDocument(doc);
			parseScenario(node, state, warnings, xpath);
		} else {
			if (warnings != null) warnings.add("<scenario> node not found");
		}
		
		return state;
	}
	
	/**
	 * Parses a "scenario" element of a scenario XML file
	 * @param node		The DOM node representing a scenario element
	 * @param state		The current parse state (will be updated)
	 * @param warnings	List to store warnings
	 * @param xpath		The XPath evaluator to use
	 */
	private static void parseScenario(Node node, ScenarioParseState state, 
			List<String> warnings, XPath xpath) {
		
		//parse attributes
		Integer scenarioId = evalInt(xpath, "@scenarioId", node);
		if (debug && scenarioId != null)
			System.out.println("\tscenarioId " + scenarioId.toString());
		if (warnings != null && scenarioId == null)
			warnings.add("scenarioID not found");
		state.setScenarioId(scenarioId);
		
		String scenarioName = evalString(xpath, "@scenarioName", node);
		if (debug && scenarioName != null)
			System.out.println("\tscenarioName `" + scenarioName + "'");
		if (warnings != null && 
				(scenarioName == null || scenarioName.isEmpty()))
			warnings.add("scenarioName not found");
		state.setScenarioName(scenarioName);
		
		Date lastModified = evalDate(xpath, "@lastModified", node);
		if (debug && lastModified != null)
			System.out.println("\tlastModified " + lastModified.toString());
		state.setLastModified(lastModified);
		
		String authorId = evalString(xpath, "@authorId", node);
		if (debug && authorId != null)
			System.out.println("\tauthorId `" + authorId + "'");
		if (warnings != null && (authorId == null || authorId.isEmpty()))
			warnings.add("authorId not found");
		state.setAuthorId(authorId);
		
		String authorName = evalString(xpath, "@authorName", node);
		if (debug && authorName != null)
			System.out.println("\tauthorName `" + authorName + "'");
		state.setAuthorName(authorName);
		
		//parse elements
		String description = evalString(xpath, "description/text()", node);
		if (debug && description != null)
			System.out.println("\tdescription `" + description + "'");
		state.setDescription(description);
		
		Node apn = evalNode(xpath, "apn", node);
		if (apn != null) {
			if (debug) System.out.println("\t\t<apn>");
			
			parseApn(apn, state, warnings, xpath);
		}
		
		NodeList nodes = evalNodeList(xpath, "nodes/*", node);
		if (nodes != null && nodes.getLength() > 0) {
			for (int i = 0; i < nodes.getLength(); i++) {
				parseNode(nodes.item(i), state, warnings, xpath);
			}
		} else {
			if (warnings != null) warnings.add("No nodes found");
		}
		
		NodeList links = evalNodeList(xpath, "link", node);
		if (links != null && links.getLength() > 0) {
			for (int i = 0; i < links.getLength(); i++) {
				if (debug) System.out.println("\t\t<link>");
				
				parseLink(links.item(i), state, warnings, xpath);
			}
		} else {
			if (warnings != null) warnings.add("No links found");
		}
	}
	
	/**
	 * Parses an "{@code apn}" element of a scenario XML file
	 * 
	 * @param node		The DOM node representing an "{@code apn}" element
	 * @param state		The current parse state (will be updated)
	 * @param warnings	List of parser warnings
	 * @param xpath		The XPath evaluator to use
	 */
	private static void parseApn(Node node, ScenarioParseState state, 
			List<String> warnings, XPath xpath) {
		
//		Integer id = null; String name = "";
//		
//		id = evalInt(xpath, "id/text()", node);
//		if (debug && id != null) 
//			System.out.println("\t\tid " + id.toString());
//		if (warnings != null && id == null)
//			warnings.add("No ID found for <apn-site>");
//		
//		name = evalString(xpath, "name/text()", node);
//		if (debug && name != null) 
//			System.out.println("\t\tname `" + name + "'");
//				
//		if (id != null) state.addSite(id, name);
		
		String resMethodStr = evalString(xpath, "@reservationMethod", node);
		ApnReservationMethod resMethod = null; 
		try {
			resMethod = ApnReservationMethod.valueOf(resMethodStr);
		} catch (Exception e) {
			warnings.add("invalid apn reservationMethod `" + resMethodStr 
					+ "'");
			return;
		}
		
		NodeList connections = evalNodeList(xpath, "connection", node);
		if (connections == null || connections.getLength() == 0) {
			warnings.add("no connections on <apn> node");
			return;
		}
		
		ApnReservation reservation = new ApnReservation();
		reservation.setReservationMethod(resMethod);
		
		for (int i = 0; i < connections.getLength(); i++) {
			Node connection = connections.item(i);
			
			Integer connectionId = evalInt(xpath, "@id", connection);
			if (connectionId == null) {
				warnings.add("no id on <apn> <connection>");
				continue;
			}
			
			String maxBandwidth = null;
			if (evalExists(xpath, "@maxBandwidth", connection)) {
				maxBandwidth = evalString(xpath, "@maxBandwidth", connection);
			} else {
				warnings.add("no maxBandwidth on <apn> <connection> " 
						+ connectionId.toString());
				continue;
			}
			
			String minBandwidth = null;
			if (evalExists(xpath, "@minBandwidth", connection)) {
				minBandwidth = evalString(xpath, "@minBandwidth", connection);
			} else {
				warnings.add("no minBandwidth on <apn> <connection>" 
						+ connectionId.toString());
				continue;
			}
			
			Integer sourceId = evalInt(xpath, "sourceSite/@id", connection);
			if (sourceId == null) {
				warnings.add("no id on <apn> <connection> " 
						+ connectionId.toString() + " <sourceSite>");
				continue;
			}
			
			String sourceName = null;
			if (evalExists(xpath, "sourceSite/@name", connection)) {
				sourceName = evalString(xpath, "sourceSite/@name", connection);
			} else {
				warnings.add("no name on <apn> <connection> " 
						+ connectionId.toString() + " <sourceSite> " 
						+ sourceId.toString());
				continue;
			}
			
			Integer destId = evalInt(xpath, "destSite/@id", connection);
			if (destId == null) {
				warnings.add("no id on <apn> <connection> " 
						+ connectionId.toString() + " <destSite>");
				continue;
			}
			
			String destName = null;
			if (evalExists(xpath, "destSite/@name", connection)) {
				destName = evalString(xpath, "destSite/@name", connection);
			} else {
				warnings.add("no name on <apn> <connection> " 
						+ connectionId.toString() + " <destSite> " 
						+ destId.toString());
				continue;
			}
			
			reservation.addConnnection(new ApnConnection(connectionId, 
					minBandwidth, maxBandwidth, sourceId, sourceName, destId, 
					destName));
		}
		
		state.setApn(reservation);
	}
	
	/**
	 * Parses a "startNode", "resourceNode", or "endNode" element of a scenario 
	 * XML file.
	 * @param node		The DOM node representing the xxxNode element
	 * @param state		The current parse state (will be updated)
	 * @param warnings	List of parser warnings
	 * @param xpath		The XPath evaluator to use
	 */
	private static void parseNode(Node node, ScenarioParseState state, 
			List<String> warnings, XPath xpath) {
		Integer nodeId = evalInt(xpath, "@nodeId", node);
		if (warnings != null && nodeId == null) {
			warnings.add("no nodeId found for <" + node.getLocalName() + ">");
			return;
		}
				
		if ("startNode".equals(node.getLocalName())) {
			if (debug) {
				System.out.println("\t\t<startNode>");
				System.out.println("\t\tid " + nodeId.toString());
			}
			
			state.addStartNode(nodeId);
		
		} else if ("endNode".equals(node.getLocalName())) {
			if (debug) {
				System.out.println("\t\t<endNode>");
				System.out.println("\t\tid " + nodeId.toString());
			}
			
			state.addEndNode(nodeId);
		
		} else if ("resourceNode".equals(node.getLocalName())) {
			if (debug) {
				System.out.println("\t\t<resourceNode>");
				System.out.println("\t\tid " + nodeId.toString());
			}
			
			NodeList resources = evalNodeList(xpath, "resource", node);
			if (resources != null && resources.getLength() > 0) {
				for (int i = 0; i < resources.getLength(); i++) {
					if (debug) System.out.println("\t\t\t<resource>");
					
					Node resource = resources.item(i);
					Service s = new Service();
					
					String id = evalString(xpath, "@id", resource);
					if (debug && id != null)
						System.out.println("\t\t\tid `" + id + "'");
					if (warnings != null && (null == id || id.isEmpty()))
						warnings.add("no id found for <resource> on " +
								"<resourceNode> with id " + nodeId);
					s.setId(id);
					
					String name = evalString(xpath, "@name", resource);
					if (debug && name != null)
						System.out.println("\t\t\tname `" + name + "'");
					s.setName(name);
					
					String aId = null;
					Node activity = evalNode(xpath, "activity", resource);
					if (activity != null) {
						if (debug) 
							System.out.println("\t\t\t\t<activity>");
						
						aId = evalString(xpath, "@id", activity);
						if (debug && aId != null)
							System.out.println("\t\t\t\tid `" + aId + "'");
						if (warnings != null && (null == aId || aId.isEmpty()))
							warnings.add("no id found for <activity> on " +
									"<resource> with id " + id);
						s.setActivityId(aId);
						
						String aName = evalString(xpath, "@name", activity);
						if (debug && aName != null)
							System.out.println("\t\t\t\tname `" + aName + "'");
						s.setActivityName(aName);
						
						//NOTE I didn't see the point of parsing activity 
						// parameters, so I didn't - that would go here.
					}
					
					NodeList variables = 
						evalNodeList(xpath, "variable", resource);
					if (variables != null && variables.getLength() > 0) {
						ServiceProfile profile = 
							ProfileUtils.getProfile(id, name);
						
						for (int j = 0; j < variables.getLength(); j++) {
							if (debug)
								System.out.println("\t\t\t\t<variable>");
							
							Node variable = variables.item(j);
							Variable v = new Variable();
							v.service = s;
							
							String vName = evalString(xpath, "@name", variable);
							if (warnings != null && 
									(null == vName || vName.isEmpty()))
								warnings.add("no name for <variable> on " +
										"<resource> with id " + id);
							if (vName != null) {
								if (debug)
									System.out.println("\t\t\t\tname `" + vName 
											+ "'");
								v.name = varName(vName);
							}
							
							if (null != state.getVar(v.name)) {
								//already exists a variable with this name
								if (warnings != null)
									warnings.add("variable with name " + vName 
											+ " already exists. New variable " +
											"not bound.");
								continue;
							}
							
							String parameter = 
								evalString(xpath, "@parameter", variable);
							if (debug && parameter != null)
								System.out.println("\t\t\t\tparameter `" + 
										parameter + "'");
							if (warnings != null && 
									(null == parameter || parameter.isEmpty()))
								warnings.add("no parameter for <variable> " +
										"with name " + vName);
							v.parameterId = parameter;
							
							//get parameter type
							v.type = ProfileUtils.getParameter(
									profile, aId, parameter).getClass();
							
							Boolean keepUpdate = 
								evalBoolean(xpath, "@keepUpdate", variable);
							if (keepUpdate != null) {
								if (debug)
									System.out.println("\t\t\t\tkeepUpdate " + 
											keepUpdate.toString());
								v.keepUpdate = keepUpdate;
							}
							
							state.addVar(v);
						}
					}
					
					state.addResourceNode(nodeId, s);
				}
			} else /* no <resource> in resourceNode */ {
				if (warnings != null) 
					warnings.add("no <resource> found in <resourceNode> with " +
							"id " + nodeId);
			}
		} else if ("splitNode".equals(node.getLocalName())) {
			if (debug) {
				System.out.println("\t\t<splitNode>");
				System.out.println("\t\tid " + nodeId.toString());
			}
			
			state.addSplitNode(nodeId);
		
		} else if ("joinNode".equals(node.getLocalName())) {
			if (debug) {
				System.out.println("\t\t<joinNode>");
				System.out.println("\t\tid " + nodeId.toString());
			}
			
			state.addJoinNode(nodeId);
		
		} else /* unrecognized node type */ {
			if (warnings != null) 
				warnings.add("unrecognized node type <" + node.getLocalName() + 
					">");
		}
	}
	
	/* Characters to be stripped in variable names */
	private static Pattern unsafeChars = Pattern.compile("[^A-Za-z0-9_]");
	
	/**
	 * Gets a normalized variable name from one given
	 * @param name	The given variable name
	 * @return	the given variable name, with all non alpha-numeric characters 
	 * stripped, and a '$' prepended.
	 */
	private static String varName(String name) {
		if (name == null) return null;
		return "$" + unsafeChars.matcher(name).replaceAll("");
	}
	
	/**
	 * Parses a "link" element of a scenario XML file
	 * @param node		The DOM node representing a link element
	 * @param state		The current parse state (will be updated)
	 * @param warnings	Parser warnings list
	 * @param xpath		The XPath evaluator to use
	 */
	private static void parseLink(Node node, ScenarioParseState state,
			List<String> warnings, XPath xpath) {
		int from = 0, to = 0;
		
		Integer aFrom = evalInt(xpath, "@from", node);
		if (aFrom != null) {
			if (debug) System.out.println("\t\tfrom " + aFrom.toString());
			
			from = aFrom;
		} else {
			if (warnings != null) warnings.add("no from attribute on <link>");
			return;
		}
		
		Integer aTo = evalInt(xpath, "@to", node);
		if (aTo != null) {
			if (debug) System.out.println("\t\tto " + aTo.toString());
			
			to = aTo;
		} else {
			if (warnings != null) warnings.add("no to attribute on <link>");
			return;
		}
		
		NodeList rules = evalNodeList(xpath, "rule", node);
		if (rules != null && rules.getLength() > 0) {
			for (int i = 0; i < rules.getLength(); i++) {
				if (debug) System.out.println("\t\t\t<rule>");
				
				Node rule = rules.item(i);
				ControlflowRule r = new ControlflowRule();
				r.setNextNode(state.getNode(to).getName());
				
				NodeList parts = rule.getChildNodes();
//				NodeList parts = evalNodeList(xpath, "*", rule);
				
				if (parts == null || parts.getLength() <= 0) {
					//incomplete rule
					state.addLink(from, to, null);
					if (warnings != null) 
						warnings.add("incomplete rule on <link> from " + from + 
								" to " + to);
					return;
				}
				
				List<Condition> conds = new ArrayList<Condition>();
				
				int j = 0;
				Node part = null;
				Condition cond;
				while (j < parts.getLength()) { 
					part = parts.item(j);
					
					//skip non-element children
					if (null == part.getLocalName()) {
						j++;
						continue;
					}
					
					//handle "then" element later
					if ("then".equals(part.getLocalName())) break;
					
					if ("when".equals(part.getLocalName())) {
						cond = parseWhen(part, state, warnings, xpath);
						if (cond != null) conds.add(cond);

					} else if ("or".equals(part.getLocalName())) {
						cond = parseOr(part, state, warnings, xpath);
						if (cond != null) conds.add(cond);
					
					} else if ("not".equals(part.getLocalName())) {
						cond = parseWhen(part, state, warnings, xpath);
						if (cond != null) conds.add(cond);
					
					} else if ("and".equals(part.getLocalName())) {
						//violation of schema, but unambiguous one
						cond = parseAnd(part, state, warnings, xpath);
						if (cond != null) 
							conds.addAll(((AndCondition)cond).getConditions());
					} else /* unrecognized condition */ {
						if (warnings != null)
//							warnings.add("unrecognized condition <" + 
//									part.getLocalName() + "> on <rule> on " +
//									"<link> from " + from + " to " + to);
//							warnings.add("unrecognized condition <" + 
//									part.getLocalName() + "> on `" + 
//									toXmlString(rule));
							warnings.add("unrecognized <rule> element `"
									+ toXmlString(part) + "'");
					}
					
					j++;
				}
				
				r.setConditions(conds);
				
				if ("then".equals(part.getLocalName())) {
					parseThen(part, state, r, warnings, xpath);
				} else {
					if (warnings != null)
						warnings.add("no consequence found on <rule> on " +
								"<link> from " + from + " to " + to);
					return;
				}
				
				state.addLink(from, to, r);
			}
		} else {
			//no rule on link
			state.addLink(from, to, null);
		}
	}
	
	/* map of operator names to Comparison enums */
	private static Map<String, Comparison> comps = 
		new HashMap<String, Comparison>();
	static {
		comps.put("eq", Comparison.EQ);
		comps.put("ge", Comparison.GE);
		comps.put("gt", Comparison.GT);
		comps.put("le", Comparison.LE);
		comps.put("lt", Comparison.LT);
		comps.put("ne", Comparison.NE);
	}
	
	/**
	 * Parses a "when" element of a scenario XML file
	 * @param node		A DOM node representing a "when" element
	 * @param state		State used to get needed variables (read-only)
	 * @param warnings	Parser warnings list
	 * @param xpath		The XPath evaluator to use
	 * @return The corresponding WhenCondition 
	 */
	private static WhenCondition parseWhen(Node node, ScenarioParseState state, 
			List<String> warnings, XPath xpath) {		
		
		if (debug) System.out.println("\t\t\t\t<when>");
		
		Node resource = evalNode(xpath, "resource", node);
		if (resource == null) {
			if (warnings != null) 
				warnings.add("no <resource> on <when>, when condition not " +
						"added");
			return null;
		}

		if (debug) System.out.println("\t\t\t\t\t<resource>");
		
		Integer id = evalInt(xpath, "@id", resource);
		if (id == null) {
			if (warnings != null)
				warnings.add("no id on <resource> on <when>, when condition " +
						"not added");
			return null;
		}
		
		if (debug) System.out.println("\t\t\t\t\tid " + id.toString());
		
		String aId = null;
		if (evalExists(xpath, "@activityID", resource)) {
			aId = evalString(xpath, "@activityID", resource);
			
			if (debug) System.out.println("\t\t\t\t\tactivityID " + aId);
		}
		
		Service serv = state.getService(id.toString(), aId);
		if (serv == null) {
			if (warnings != null) 
				warnings.add("no resource defined with id " + id + ", when " +
						"condition not added");
			return null;
		}
		
		String paramId = evalString(xpath, "parameter/text()", node);
		if (paramId == null) {
			if (warnings != null)
				warnings.add("no parameter found on <when> with <resource> id " 
						+ id + ", when condition not added");
			return null;
		}
		if (debug) System.out.println("\t\t\t\tparameter `" + paramId + "'");
		
		String op = evalString(xpath, "operator/text()", node);
		if (debug && op != null) 
			System.out.println("\t\t\t\toperator `" + op + "'");
		Comparison comp = comps.get(op);
		if (comp == null) {
			if (warnings != null)
				warnings.add("no comparision found for <operator> " + op + 
						" on <when> with <resource> id " + id + ", when " +
						"condition not added");
			return null;
		}
		
		String value = evalString(xpath, "value/text()", node);
		if (value == null) {
			if (warnings != null)
				warnings.add("no value found on <when> with <resource> id " + 
						id + ", when condition not added");
			return null;
		}
		if (debug) System.out.println("\t\t\t\tvalue `" + value + "'");
		
		//gets parameter for this condition.
		//attempts to load proper type from profile, falls back to inference
		Parameter param = ProfileUtils.getParamter(
				state.getProfile(id.toString()), serv.getActivityId(), 
				paramId, value);
		
		return new WhenCondition(serv, param, comp);
	}
	
	/**
	 * Parses an "or" element of a scenario XML file
	 * @param node		A DOM node representing an "or" element
	 * @param state		State used to get needed variables (read-only)
	 * @param warnings	Parser warnings list
	 * @param xpath		The XPath evaluator to use
	 * @return The corresponding OrCondition
	 */
	private static OrCondition parseOr(Node node, ScenarioParseState state,
			List<String> warnings, XPath xpath) {
		
		if (debug) System.out.println("\t\t\t\t<or>");
		
		List<Condition> conds = new ArrayList<Condition>();
		Condition cond;
		
		NodeList nodes = node.getChildNodes();
		if (nodes.getLength() > 0) for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			
			if ("when".equals(node.getLocalName())) {
				cond = parseWhen(node, state, warnings, xpath);
				if (cond != null) conds.add(cond);
			
			} else if ("and".equals(node.getLocalName())) {
				cond = parseAnd(node, state, warnings, xpath);
				if (cond != null) conds.add(cond);
				
			} else if ("not".equals(node.getLocalName())) {
				cond = parseNot(node, state, warnings, xpath);
				if (cond != null) conds.add(cond);
				
			} else if ("or".equals(node.getLocalName())) {
				//violation of schema, but unambiguous one
				if (warnings != null) warnings.add("<or> embedded in <or>");

				cond = parseOr(node, state, warnings, xpath);
				if (cond != null)
					conds.addAll(((OrCondition)cond).getConditions());
			}
		}
		
		if (conds.isEmpty()) {
			if (warnings != null) warnings.add("no conditions found on <or>");
			return null;
		}
		
		return new OrCondition(conds);
	}
	
	/**
	 * Parses an "and" element of a scenario XML file
	 * @param node		A DOM node representing an "and" element
	 * @param state		State used to get needed variables (read-only)
	 * @param warnings	Parser warnings list
	 * @param xpath		The XPath evaluator to use
	 * @return The corresponding AndCondition
	 */
	private static AndCondition parseAnd(Node node, ScenarioParseState state, 
			List<String> warnings, XPath xpath) {
		
		if (debug) System.out.println("\t\t\t\t<and>");
		
		List<Condition> conds = new ArrayList<Condition>();
		Condition cond;
		
		NodeList nodes = node.getChildNodes();
		if (nodes.getLength() > 0) for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			
			if ("when".equals(node.getLocalName())) {
				cond = parseWhen(node, state, warnings, xpath);
				if (cond != null) conds.add(cond);
				
			} else if ("or".equals(node.getLocalName())) {
				cond = parseOr(node, state, warnings, xpath);
				if (cond != null) conds.add(cond);
				
			} else if ("not".equals(node.getLocalName())) {
				cond = parseNot(node, state, warnings, xpath);
				if (cond != null) conds.add(cond);
				
			} else if ("and".equals(node.getLocalName())) {
				//violation of schema, but unambiguous one
				if (warnings != null) warnings.add("<and> embedded in <and>");
				
				cond = parseAnd(node, state, warnings, xpath);
				if (cond != null) 
					conds.addAll(((AndCondition)cond).getConditions());
			}
		} 
		
		if (conds.isEmpty()) {
			if (warnings != null) warnings.add("no conditions found on <and>");
			return null;
		}
		
		return new AndCondition(conds);
	}
	
	/**
	 * Parses a "not" element of a scenario XML file
	 * @param node		A DOM node representing a "not" element
	 * @param state		State used to get needed variables (read-only)
	 * @param warnings	Parser warnings list
	 * @param xpath		The XPath evaluator to use
	 * @return The corresponding NotCondition
	 */
	private static NotCondition parseNot(Node node, ScenarioParseState state, 
			List<String> warnings, XPath xpath) {
		
		if (debug) System.out.println("\t\t\t\t<not>");
		
		List<Condition> conds = new ArrayList<Condition>();
		Condition cond;
		
		NodeList nodes = node.getChildNodes();
		if (nodes.getLength() > 0) for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			
			if ("when".equals(node.getLocalName())) {
				cond = parseWhen(node, state, warnings, xpath);
				if (cond != null) conds.add(cond);
				
			} else if ("and".equals(node.getLocalName())) {
				cond = parseAnd(node, state, warnings, xpath);
				if (cond != null) conds.add(cond);
				
			} else if ("or".equals(node.getLocalName())) {
				cond = parseOr(node, state, warnings, xpath);
				if (cond != null) conds.add(cond);
				
			} else if ("not".equals(node.getLocalName())) {
				cond = parseNot(node, state, warnings, xpath);
				if (cond != null) conds.add(cond);
			}
		} 
		
		if (conds.isEmpty()) {
			if (warnings != null) warnings.add("no conditions found on <not>");
			return null;
		}
		
		return new NotCondition(conds);
	}
	
	/* map of action names to Action enums */
	private static Map<String, Action> acts = new HashMap<String, Action>();
	static {
		for (Action act : Action.values()) {
			acts.put(act.toString(), act);
		}
	}
	
	/**
	 * Parses a "then" element of scenario XML
	 * @param node		A DOM node representing the then 
	 * @param state		State used to get needed variables (read-only)
	 * @param rule		Rule this "then" condition applies on (will be changed)
	 * @param warnings	Parser warnings list
	 * @param xpath		The XPath evaluator to use
	 */
	private static void parseThen(Node node, ScenarioParseState state, 
			ActivityRule rule, List<String> warnings, XPath xpath) {
		
		if (debug) System.out.println("\t\t\t\t<then>");
		
		NodeList resources = evalNodeList(xpath, "resource", node);
		if (resources == null || resources.getLength() <= 0) {
			if (warnings != null) 
				warnings.add("no consequences found on <then>");
			return; 
		}
			
		for (int i = 0; i < resources.getLength(); i++) {
			Node resource = resources.item(i);			
			
			if (debug) System.out.println("\t\t\t\t\t<resource>");
			
			Integer id = evalInt(xpath, "@id", resource);
			if (id == null) {
				if (warnings != null) 
					warnings.add("no id found on <resource> on <then>, " +
							"consequence skipped");
				continue;
			}
			
			if (debug) System.out.println("\t\t\t\t\tid " + id.toString()); 
			
			String aId = null;
			if (evalExists(xpath, "@activityID", resource)) {
				aId = evalString(xpath, "@activityID", resource);
				
				if (debug) System.out.println("\t\t\t\t\tactivityID " + aId);
			}
			
			Service serv = state.getService(id.toString(), aId);
			if (serv == null) {
				if (warnings != null)
					warnings.add("no resource defined with id " + id + ", " +
							"consequence skipped");
				continue;
			}
			
			
			NodeList actions = evalNodeList(xpath, "action", resource);
			if (actions == null || actions.getLength() <= 0) {
				if (warnings != null)
//					warnings.add("no <action> found for <resource> with id " + 
//							id + " on <then>, consequence skipped");
					warnings.add("no <action> found on <then>: `" + 
							toXmlString(resource) + "`");
				continue;
			}
			
			for (int j = 0; j < actions.getLength(); j++) {
				Node action = actions.item(j);
				if (debug) System.out.println("\t\t\t\t\t\t<action>");
				
				String actionName = evalString(xpath, "@actionName", action);
				if (actionName == null) {
					if (warnings != null)
						warnings.add("no actionName found for <action> on " +
								"<resource> with id " + id + " on <then>, " +
								"action skipped");
					continue;
				}
				if (debug) 
					System.out.println("\t\t\t\t\t\tactionName `" + actionName 
							+ "'");
				
				Action act = acts.get(actionName);
				if (act == null) {
					if (warnings != null)
						warnings.add("no action matches actionName " + 
								actionName + " on <action> on <resource> " +
								"with id " + id + " on <then>, action skipped");
					continue;
				}
				
				List<Parameter> params;
				
				NodeList parameters = evalNodeList(xpath, "parameter", action);
				
				if (parameters != null && parameters.getLength() > 0) {
					params = new ArrayList<Parameter>(parameters.getLength());
					
					for (int k = 0; k < parameters.getLength(); k++) {
						if (debug) 
							System.out.println("\t\t\t\t\t\t\t<parameter>");
						
						Node parameter = parameters.item(k);
						Parameter p = new Parameter();
						
						String pid = evalString(xpath, "@id", parameter);
						if (pid == null) {
							if (warnings != null)
								warnings.add("no id on <parameter> on " +
										"<action> on <resource> with id " + id 
										+ " on <then>, parameter skipped");
							continue;
						}
						if (debug) 
							System.out.println("\t\t\t\t\t\t\tid `" + pid + 
									"'");
						p.setId(pid);
						
						if (evalExists(xpath, "@value", parameter)) {
							String value = 
								evalString(xpath, "@value", parameter);
							
							if (debug) 
								System.out.println("\t\t\t\t\t\t\tvalue `" + 
										value + "'");
							
							//quote value to ensure that it is treated literally
							p.setValue(mungeValue(value));
							
						} else if (evalExists(xpath, "@variable", parameter)) {
							String variable = 
								evalString(xpath, "@variable", parameter);
							if (debug) 
								System.out.println("\t\t\t\t\t\t\tvariable `" 
										+ variable + "'");
							
							//get already defined variable
							Variable v = state.getVar("$" + variable);
							if (v == null) {
								if (warnings != null)
									warnings.add("no variable with name " + 
											variable + " found, <parameter> " +
											"with name " + pid + " on " +
											"<action> on <resource> with id " +
											id + " on <then>, parameter " +
											"skipped");
								continue;
							}
							
							//add to rule
							rule.addVar(new VarBinding(v.name, v.service, 
									v.parameterId, v.type));
							//add to parameter
							p.setValue(mungeVariable(v.name));
						
						} else if (evalExists(xpath, "@expression", parameter)) {
							String expression = 
								evalString(xpath, "@expression", parameter);
							
							if (debug) 
								System.out.println("\t\t\t\t\t\t\texpression `" 
										+ expression + "'");
							
							//find variables in expression
							List<String> variables = getVars(expression);
							
							for (String variable : variables) {
								//get already defined variable
								Variable v = state.getVar("$" + variable);
								if (v == null) {
									if (warnings != null)
										warnings.add("no variable with name " + 
												variable + " found, <parameter> " +
												"with name " + pid + " on " +
												"<action> on <resource> with id " +
												id + " on <then>, parameter " +
										"skipped");
									continue;
								}

								//add to rule
								rule.addVar(new VarBinding(v.name, v.service, 
										v.parameterId, v.type));
							}
							
							
							//add to parameter
							p.setValue(mungeExpression(expression));
							
						} else {
							if (warnings != null)
								warnings.add("neither <value>, <variable>, nor " 
										+ "<expression> found for <parameter> " 
										+ "with name " + pid + " on <action> " 
										+ "on <resource> with id " + id  
										+ " on <then>, parameter skipped");
							continue;
						}
						
						params.add(p);
					}
				} else params = null;
				
				rule.addConsequence(new UpdateConsequence(serv, act, params));
			}
		}
	}
	
	private static Pattern findVars = 
		Pattern.compile("\\$([A-Za-z][A-Za-z0-9_]*?)\\$");
	
	/**
	 * Gets all the variables used in an expression
	 * 
	 * @param expression		The expression to get the variables out of.
	 * 							Variables will be listed in the expression with 
	 * 							their names surrounded by '$'. 
	 * 							(i.e. {@code $foo$ + 5})
	 * 							If you want to use a literal '$' in the 
	 * 							expression, please double it. 
	 * 							(i.e. {@code "$$" + $price$} would give you the 
	 * 							string {@code "$1.00"} when {@code price} is 
	 * 							bound to {@code "1.00"})
	 * 
	 * @return a list of variables used in the expression
	 */
	private static List<String> getVars(String expression) {
		List<String> vars = new ArrayList<String>();
		
		//escape hatch for null or empty expression
		if (expression == null || expression.isEmpty()) return vars;
		
		Matcher matcher = findVars.matcher(expression);
		
		while (matcher.find()) {
			vars.add(matcher.group(1));
		}
		
		return vars;
	}
	
	/**
	 * Reformats a value as a string literal
	 * 
	 * @param value			The value to reformat - will be surrounded with 
	 * 						quotes, and have all special characters inside 
	 * 						escaped.
	 * 
	 * @return the reformatted value
	 */
	private final static String mungeValue(String value) {
		
		StringBuilder sb = new StringBuilder(value.length() + 3);
		sb.append('"');		//start string
		for (char c : value.toCharArray()) {
			switch (c) {	//escape characters
			case '"':				// "
				sb.append("\\\"");	// \"
				break;
			case '\\':				// \
				sb.append("\\\\");	// \\
				break;
			case '\t':				// <tab>
				sb.append("\\t");	// \t
				break;
			case '\n':				// <newline>
				sb.append("\\n");	// \n
				break;
			case '\r':				// <carriage return>
				sb.append("\\r");	// \r
				break;
			default:
				sb.append(c);
				break;
			}
		}
		sb.append('"');		//end string
		return sb.toString();
	}
	
	/**
	 * Reformats a variable as a string
	 * 
	 * @param variable		The value to reformat - will be surrounded with a 
	 * 						conversion to String
	 * 
	 * @return the reformated variable
	 */
	private final static String mungeVariable(String variable) {
		return "String.valueOf(" + variable + ")";
	}
	
	/**
	 * Reformats an expression for use as a parameter value.
	 * 
	 * @param expression		The expression to reformat.
	 * 							Will be surrounded with a conversion to String, 
	 * 							and have all variable references normalized to 
	 * 							standard Drools format (name prepended with $)
	 * 
	 * @return the reformatted expression
	 */
	private static String mungeExpression(String expression) {
		//escape hatch for null or empty expression
		if (expression == null || expression.isEmpty()) return expression;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("String.valueOf(");
		
		Matcher matcher = findVars.matcher(expression);
		int firstIndex = 0;		//index of first character not added to sb
		while (matcher.find()) {
			//add unadded bits of expression
			sb.append(expression.substring(firstIndex, matcher.start()));
			//add variable
			sb.append("$").append(matcher.group(1));
			//update last index added
			firstIndex = matcher.end();
		}
		//add bits after last match
		sb.append(expression.substring(firstIndex));
		
		sb.append(")");
	
		return sb.toString();
	}
	
	/**
	 * Takes a list of filenames as arguments, parses them and generates 
	 * rulefiles.
	 * The rulefile will be saved under the same name, with its extension (if 
	 * such exists) replaced with ".drl"
	 */
	public static void main(String[] args) {
		for (String name : args) {
			System.out.println("Parsing `" + name + "'");
			
			StringBuilder sb = new StringBuilder();
			try {
				BufferedReader in = new BufferedReader(new FileReader(name));
				String s = in.readLine();
				while (s != null) {
					sb.append(s).append('\n');
					
					s = in.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("\tFailure");
				continue;
			}
			
			ScenarioParseState ps = null;
			try {
				List<String> warnings = new ArrayList<String>();
				ps = parseXml(sb.toString(), warnings);
				for (String w : warnings) {
					System.out.println("\tWarning: " + w);
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				System.out.println("\tFailure");
				continue;
			} catch (SAXException e) {
				e.printStackTrace();
				System.out.println("\tFailure");
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("\tFailure");
				continue;
			}
			
			//replace extension with .drl
			int extInd = name.lastIndexOf('.');
			String ruleName = (extInd == -1) ? 
					name + ".drl"
					: name.substring(0, extInd) + ".drl";
			
			try {
				FileWriter fw = new FileWriter(ruleName);
				fw.write(ps.getRules().toString());
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("\tFailure");
				continue;
			}
			
			System.out.println("\tSuccessfully written to `" + ruleName + "'");			
		}
	}
}
