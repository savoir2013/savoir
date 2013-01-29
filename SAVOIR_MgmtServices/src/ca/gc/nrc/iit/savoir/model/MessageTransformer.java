// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ca.gc.iit.nrc.savoir.domain.PersonInfo;
import ca.gc.nrc.iit.savoir.model.profile.Activity;
import ca.gc.nrc.iit.savoir.model.profile.Choice;
import ca.gc.nrc.iit.savoir.model.profile.Choice.ChoiceType;
import ca.gc.nrc.iit.savoir.model.profile.Widget.ImgType;
import ca.gc.nrc.iit.savoir.model.profile.Choices;
import ca.gc.nrc.iit.savoir.model.profile.Option;
import ca.gc.nrc.iit.savoir.model.profile.ServiceProfile;
import ca.gc.nrc.iit.savoir.model.profile.Widget;
import ca.gc.nrc.iit.savoir.model.registration.AuthenticationInfo;
import ca.gc.nrc.iit.savoir.model.registration.DeviceInfo;
import ca.gc.nrc.iit.savoir.model.registration.JmsEndpoint;
import ca.gc.nrc.iit.savoir.model.registration.NetworkInfo;
import ca.gc.nrc.iit.savoir.model.registration.ProtocolEndpoint;
import ca.gc.nrc.iit.savoir.model.registration.RegistrationTicket;
import ca.gc.nrc.iit.savoir.model.registration.ServiceInfo;
import ca.gc.nrc.iit.savoir.model.registration.TcpEndpoint;
import ca.gc.nrc.iit.savoir.model.registration.AuthenticationInfo.AuthenticationType;
import ca.gc.nrc.iit.savoir.model.registration.ProtocolEndpoint.ProtocolType;
import ca.gc.nrc.iit.savoir.model.session.Action;
import ca.gc.nrc.iit.savoir.model.session.FloatParameter;
import ca.gc.nrc.iit.savoir.model.session.IntParameter;
import ca.gc.nrc.iit.savoir.model.session.Message;
import ca.gc.nrc.iit.savoir.model.session.Notification;
import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.model.session.ParameterFactory;
import ca.gc.nrc.iit.savoir.model.session.Service;

import static ca.gc.nrc.iit.savoir.utils.XmlUtils.addAttr;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.addTextNode;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.escapeXml;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.evalBoolean;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.evalExists;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.evalInt;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.evalNode;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.evalNodeList;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.evalString;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.OPTIONAL;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.REQUIRED;
import static ca.gc.nrc.iit.savoir.utils.XmlUtils.safeEvalString;

/**
 * Utility class for transforming SAVOIR Messages to and from XML.
 * Uses XPath to parse values out of the message, using the 
 * {@link ca.gc.nrc.iit.savoir.utils.XmlUtils} utility class, and the 
 * standard Java {@link StringBuilder} class for XML generation.
 * 
 * @author Aaron Moss
 * 
 * @see The SAVOIR message specification of message formats.
 */
public class MessageTransformer {
	
	/** Map of available {@code Action} string values to {@code Action}s */
	private static final Map<String, Action> actionMap = 
		toStringMap(Action.class);
	/** Map of available {@code ChoiceType} string values to 
	 *  {@code ChoiceType}s */
	private static final Map<String, ChoiceType> choiceTypeMap = 
		toStringMap(ChoiceType.class);
	/** Map of available {@code ProtocolType} string values to 
	 *  {@code ProtocolType}s */
	private static final Map<String, ProtocolType> protocolTypeMap = 
		toStringMap(ProtocolType.class);
	/** Map of available {@code ImgType} string values to {@code ImgType}s */
	private static final Map<String, ImgType> imgTypeMap = 
		toStringMap(ImgType.class);
	/** Map of available {@code AuthenticationType} string values to 
	 *  {@code AuthenticationType}s */
	private static final Map<String, AuthenticationType> authnTypeMap = 
		toStringMap(AuthenticationType.class);
	
	/**
	 * Provides a reverse lookup map for an enum type.
	 *  
	 * @param <E>		The enum type
	 * @param clazz		The class of the enum type
	 * 
	 * @return an unmodifiable map which maps a string value (derived from the 
	 * enum type's {@code toString()} method) to the corresponding enum 
	 * constant (Or null for no such constant).
	 */
	private static final <E extends Enum<E>> Map<String, E> toStringMap(
			Class<E> clazz) {
		
		Map<String, E> tmp = new LinkedHashMap<String, E>();
		for (E e : clazz.getEnumConstants()) {
			tmp.put(e.toString(), e);
		}
		
		return Collections.unmodifiableMap(tmp);
	}
	
	/* Profile message timestamp defined by SAVOIR spec */
	private static DateFormat timestampFormat = 
		new SimpleDateFormat("ddMMMyyyy:mm:HH");
	
	/**
	 * Transforms a {@link Message} object to XML
	 * 
	 * @param message		The {@code Message} object
	 * 
	 * @return a normalized XML form of the {@code Message} object
	 */
	public static String toXml(Message message) {
		StringBuilder sb = new StringBuilder();
		String s; Action a; Boolean b;
		
		sb.append("<message ");
			a = message.getAction();
			addAttr(sb, "action", (a == null ? null : a.toString()), REQUIRED);
			addAttr(sb, "ID", message.getId(), REQUIRED);
			addAttr(sb, "sessionID", message.getSessionId(), REQUIRED);
		sb.append(">");
			Service service = message.getService();
			if (service != null) {
				sb.append("<service ");
					addAttr(sb, "ID", service.getId(), REQUIRED);
					addAttr(sb, "name", service.getName(), OPTIONAL);
					addAttr(sb, "activityID", service.getActivityId(), REQUIRED);
					addAttr(sb, "activityName", service.getActivityName(), OPTIONAL);
					s = service.getServiceUserId();
					if (s == null) {
						//do nothing
					} else if (s.isEmpty()) {
						sb.append("serviceUserID=\"\" ");	//append empty value
					} else {
						addAttr(sb, "serviceUserID", s, REQUIRED);	//append attribute value
					}
					s = service.getServicePassword();
					if (s == null) {
						//do nothing
					} else if (s.isEmpty()) {
						sb.append("servicePassword=\"\" ");	//append empty value
					} else {
						addAttr(sb, "servicePassword", s, REQUIRED);	//append attribute value
					}
					addAttr(sb, "path", service.getPath(), OPTIONAL);
				sb.append(">");
					List<Parameter> parameters = service.getParameters();
					if (parameters != null) {
						sb.append("<activityParameters>");
						for (Parameter parameter : parameters) {
							sb.append("<activityParameter ");
							addAttr(sb, "ID", parameter.getId(), REQUIRED);
							addAttr(sb, "value", parameter.getValue(), REQUIRED);
							addAttr(sb, "name", parameter.getName(), OPTIONAL);
							sb.append("/>");
						}
						sb.append("</activityParameters>");
					}
					Notification notification = service.getNotification();
					if (notification != null) {
						sb.append("<notification ");
							addAttr(sb, "messageID", notification.getMessageId(), REQUIRED);
							a = notification.getMessageAction();
							addAttr(sb, "messageAction", (a == null ? null : a.toString()), REQUIRED);
							b = notification.isSuccess();
							addAttr(sb, "success", (b == null ? "0" : (b ? "1" : "0")), REQUIRED);
						s = notification.getMessage();
						if (s != null) {
							sb.append(">").append(escapeXml(s)).append("</notification>");
						} else {
							sb.append("/>");
						}
					}
				sb.append("</service>");
			}
		sb.append("</message>");
		
		return sb.toString();
	}
	
	/**
	 * Transforms a {@link ServiceProfile} object to XML
	 * 
	 * @param profile		The {@code ServiceProfile} object
	 * 
	 * @return a normalized XML form of the {@code ServiceProfile} object
	 */
	public static String toXml(ServiceProfile profile) {
		StringBuilder sb = new StringBuilder();
		Date d; Widget w;
		
		sb.append("<profileMessage>");
		sb.append("<service ");
			addAttr(sb, "ID", profile.getId(), REQUIRED);
			addAttr(sb, "name", profile.getName(), OPTIONAL);
			addAttr(sb, "description", profile.getDescription(), REQUIRED);
			d = profile.getTimestamp();
			if (d == null) d = new Date();	//allocates new date to current time
			addAttr(sb, "timestamp", timestampFormat.format(d), REQUIRED);
		sb.append(">");
			w = profile.getWidget();
			if (w != null) emitWidget(sb, w);
			sb.append("<activities>");
			List<Activity> activities = profile.getActivities();
			if (activities != null) for (Activity activity : activities) {
				sb.append("<activity ");
					addAttr(sb, "ID", activity.getId(), REQUIRED);
					addAttr(sb, "name", activity.getName(), REQUIRED);
					addAttr(sb, "paramId", activity.getParamId(), OPTIONAL);
					addAttr(sb, "paramValue", activity.getParamValue(), OPTIONAL);
				List<Parameter> params = activity.getParameters();
				if (params == null) {
					sb.append("/>");
				} else {
					sb.append(">");
					sb.append("<activityParameters>");
					for (Parameter param : params) {
						sb.append("<activityParameter ");
							addAttr(sb, "ID", param.getId(), REQUIRED);
							addAttr(sb, "name", param.getName(), REQUIRED);
							addAttr(sb, "dataType", param.getDataType(), REQUIRED);
						sb.append("/>");
					}
					sb.append("</activityParameters>");
					sb.append("</activity>");
				}
			}
			sb.append("</activities>");
			List<Parameter> globals = profile.getGlobalParameters();
			if (globals != null) {
				sb.append("<globalParameters>");
				for (Parameter global : globals) {
					sb.append("<globalParameter ");
						addAttr(sb, "ID", global.getId(), REQUIRED);
						addAttr(sb, "name", global.getName(), REQUIRED);
						addAttr(sb, "dataType", global.getDataType(), REQUIRED);
					sb.append("/>");
				}
				sb.append("</globalParameters>");
			}
		sb.append("</service>");
		sb.append("</profileMessage>");
		
		return sb.toString();
	}
	
	/**
	 * Transforms a {@link RegistrationTicket} object to XML
	 * 
	 * @param ticket		The {@code RegistrationTicket} object
	 * 
	 * @return a normalized XML form of the {@code RegistrationTicket} object
	 */
	public static String toXml(RegistrationTicket ticket) {
		StringBuilder sb = new StringBuilder();
		String s; PersonInfo p; AuthenticationType at;
		
		sb.append("<newService>");
		ServiceInfo service = ticket.getService();
		if (service != null) {
			sb.append("<service ");
				addAttr(sb, "ID", service.getId(), OPTIONAL);
				addAttr(sb, "name", service.getName(), REQUIRED);
				addAttr(sb, "type", service.getType(), OPTIONAL);
			sb.append(">");
				addTextNode(sb, "description", service.getDescription(), REQUIRED);
				sb.append("<contact ");
				s = service.getContactUser();
				p = service.getContactInfo();
				if (s != null && !s.isEmpty()) {
					addAttr(sb, "savoirUser", s, REQUIRED);
				} else if (p != null) {
					addAttr(sb, "title", p.getHonorific(), OPTIONAL);
					addAttr(sb, "firstName", p.getFName(), REQUIRED);
					addAttr(sb, "lastName", p.getLName(), REQUIRED);
					addAttr(sb, "phone", p.getWorkPhone(), OPTIONAL);
					addAttr(sb, "email", p.getEmail1(), REQUIRED);
					addAttr(sb, "organization", p.getOrganization(), OPTIONAL);
					addAttr(sb, "region", p.getRegion(), OPTIONAL);
					addAttr(sb, "country", p.getCountry(), OPTIONAL);
					addAttr(sb, "postalCode", p.getPostal(), OPTIONAL);
				}
				sb.append("/>");
			sb.append("</service>");
		}
		NetworkInfo network = ticket.getNetwork();
		if (network != null) {
			sb.append("<network>");
			emitProtocolEndpoint(sb, network.getToService(), "toService");
			emitProtocolEndpoint(sb, network.getToSavoir(), "toSavoir");
			sb.append("</network>");
		}
		DeviceInfo device = ticket.getDevice();
		if (device != null) {
			sb.append("<device ");
				addAttr(sb, "maxSimultaneousUsers", 
						Integer.toString(device.getMaxSimultaneousUsers()), REQUIRED);
			sb.append(">");
				AuthenticationInfo authn = device.getAuthentication();
				if (authn != null) {
					sb.append("<authentication ");
						addAttr(sb, "required", Boolean.toString(authn.isRequired()), REQUIRED);
						at = authn.getType();
						addAttr(sb, "type", (at == null ? null : at.toString()), REQUIRED);
					sb.append("/>");
				}
			sb.append("</device>");
		}
		sb.append("</newService>");
		
		return sb.toString();
	}
	
	/**
	 * Transforms a {@link Widget} object to XML
	 * 
	 * @param widget		The {@code Widget} object
	 * 
	 * @return a normalized XML form of the {@code Widget} object
	 */
	public static String toXml(Widget widget) {
		StringBuilder sb = new StringBuilder();
		emitWidget(sb, widget);
		return sb.toString();
	}
	
	/**
	 * Transforms a {@link ProtocolEndpoint} object to XML
	 * 
	 * @param sb			The string builder to add the XML to
	 * @param endpoint		The {@code ProtocolEndpoint} object
	 * @param name			The name of the XML element containing the 
	 * 						{@code ProtocolEndpoint}
	 */
	private static void emitProtocolEndpoint(StringBuilder sb, 
			ProtocolEndpoint endpoint, String name) {
		
		ProtocolType protocol;
		
		if (endpoint == null) return;
		
		protocol = endpoint.getProtocol();
		
		sb.append("<").append(name).append(" ");
			addAttr(sb, "protocol", (protocol == null ? null : protocol.toString()), REQUIRED);
		sb.append(">");
			switch (protocol) {
			case TCP_SOCKET:
				try {
					TcpEndpoint tcp	= (TcpEndpoint)endpoint;
					
					sb.append("<tcpSocket>");
					addTextNode(sb, "ipAddress", tcp.getIpAddress(), REQUIRED);
					addTextNode(sb, "portNumber", Integer.toString(tcp.getPortNumber()), REQUIRED);
					sb.append("</tcpSocket>");
				} catch (ClassCastException e) {}
				break;
			case JMS:
				try {
					JmsEndpoint jms = (JmsEndpoint)endpoint;
					
					sb.append("<jms><jmsTransportConnector ");
					addAttr(sb, "method", jms.getConnectionMethod(), REQUIRED);
					sb.append(">");
					addTextNode(sb, "uri", jms.getJmsUri(), REQUIRED);
					addTextNode(sb, "topic", jms.getJmsTopic(), REQUIRED);
					sb.append("</jmsTransportConnector></jms>");
				} catch (ClassCastException e) {}
				break;
			}
		sb.append("</").append(name).append(">");
	}
	
	/**
	 * Transforms a {@link Widget} object to XML
	 * 
	 * @param sb			The string builder to add the XML to
	 * @param widget		The {@code Widget} object
	 */
	private static void emitWidget(StringBuilder sb, Widget widget) {
		sb.append("<widget ");
			addAttr(sb, "title", widget.getTitle(), OPTIONAL);
		sb.append(">");
			addTextNode(sb, "description", widget.getDescription(), REQUIRED);
			sb.append("<icon ");
				addAttr(sb, "name", widget.getIconName(), OPTIONAL);
				ImgType it = widget.getIconType();
				addAttr(sb, "format", (it == null ? null : it.toString()), REQUIRED);
			sb.append("/>");
			sb.append("<choices ");
			Choices choices = widget.getChoices();
			if (choices == null) {
				addAttr(sb, "baseUri", "", REQUIRED);
				sb.append("/>");
			} else {
				addAttr(sb, "baseUri", choices.getBaseUri(), REQUIRED);
				List<Choice> cs = choices.getChoices();
				if (cs == null || cs.isEmpty()) {
					sb.append("/>");
				} else {
					sb.append(">");
					for (Choice choice : cs) {
						sb.append("<choice ");
							addAttr(sb, "id", choice.getId(), OPTIONAL);
							addAttr(sb, "label", choice.getLabel(), OPTIONAL);
							ChoiceType t = choice.getType();
							addAttr(sb, "type", (t == null ? null : t.toString()), REQUIRED);
							addAttr(sb, "paramId", choice.getParamId(), OPTIONAL);
						List<Option> options = choice.getOptions();
						if (options == null || options.isEmpty()) {
							sb.append("/>");
						} else {
							sb.append(">");
							for (Option option : options) {
								sb.append("<option ");
									addAttr(sb, "name", option.getName(), REQUIRED);
									addAttr(sb, "paramId", option.getParamId(), OPTIONAL);
									addAttr(sb, "paramValue", option.getParamValue(), REQUIRED);
								sb.append("/>");
							}
							sb.append("</choice>");
						}
					}
					sb.append("</choices>");
				}
			}
		sb.append("</widget>");
	}
	
	/**
	 * Parses an object of one of the {@link SavoirXml} subclasses from XML.
	 * 
	 * @param xml		The XML string to parse the object from
	 * 
	 * @return a {@link Message}, {@link ServiceProfile}, or 
	 * 			{@link RegistrationTicket} object, or {@code null} on error.
	 */
	public static SavoirXml fromXml(String xml) 
			throws ParserConfigurationException, SAXException, IOException {
		//set up XML parser
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(xml)));
		doc.getDocumentElement().normalize();
		
		return fromXml(doc);
	}
	
	/**
	 * Parses an object of one of the {@link SavoirXml} subclasses from XML.
	 * 
	 * @param doc		The XML document to parse the object from
	 * 
	 * @return a {@link Message}, {@link ServiceProfile}, or 
	 * 			{@link RegistrationTicket} object, or {@code null} on error.
	 */
	public static SavoirXml fromXml(Document doc) {
		XPath xpath = XPathFactory.newInstance().newXPath();

		if (evalExists(xpath, "/message", doc)) {
			//build message
			return parseMessage(evalNode(xpath, "/message", doc), xpath);
		} else if (evalExists(xpath, "/profileMessage", doc)) {
			//build profile message
			return parseProfile(
					evalNode(xpath, "/profileMessage/service", doc), xpath);
		} else if (evalExists(xpath, "/newService", doc)) {
			return parseRegistration(
					evalNode(xpath, "/newService", doc), xpath);
		} else {
			//unrecognized
			return null;
		}
	}
	
	/**
	 * Parses a {@link Message} from XML.
	 * 
	 * @param node		The {@code <message>} node of the XML object
	 * @param xpath		The XPath parser to use
	 * 
	 * @return a {@code Message} object corresponding to the XML
	 */
	private static Message parseMessage(Node node, XPath xpath) {
		Message message = new Message();
		
		if (node != null) {
			//build message attributes
			String action = evalString(xpath, "@action", node);
			if (action != null && !action.isEmpty()) 
				message.setAction(actionMap.get(action));
			
			String id = evalString(xpath, "@ID", node);
			if (id != null && !id.isEmpty()) message.setId(id);
			
			String sessionId = evalString(xpath, "@sessionID", node);
			if (sessionId != null && !sessionId.isEmpty()) 
				message.setSessionId(sessionId);
			
			//build message children (service elements)
			Node serviceNode = evalNode(xpath, "service", node);
			if (serviceNode != null) {
				Service service = new Service();
				message.setService(service);
				
				//build service attributes
				String serviceId = evalString(xpath, "@ID", serviceNode);
				if (serviceId != null && !serviceId.isEmpty()) 
					service.setId(serviceId);
				
				String serviceName = evalString(xpath, "@name", serviceNode);
				if (serviceName != null && !serviceName.isEmpty())
					service.setName(serviceName);
				
				String activityId = evalString(xpath, "@activityID", serviceNode);
				if (activityId != null && !activityId.isEmpty())
					service.setActivityId(activityId);
				
				String activityName = evalString(xpath, "@activityName", serviceNode);
				if (activityName != null && !activityName.isEmpty())
					service.setActivityName(activityName);
				
				String userId = evalString(xpath, "@serviceUserID", serviceNode);
				if (userId != null && !userId.isEmpty()) 
					service.setServiceUserId(userId);
				
				String password = evalString(xpath, "@servicePassword", serviceNode);
				if (password != null && !password.isEmpty())
					service.setServicePassword(password);
				
				String path = evalString(xpath, "@path", serviceNode);
				if (path != null && !path.isEmpty()) 
					service.setPath(path);
				
				//build service children
				NodeList actParams = evalNodeList(
						xpath, "activityParameters/activityParameter", serviceNode);
				if (actParams != null && actParams.getLength() > 0) {
					List<Parameter> parameters = 
						new ArrayList<Parameter>(actParams.getLength());
					
					for (int i = 0; i < actParams.getLength(); i++) {
						Node actParam = actParams.item(i);
						Parameter parameter;
						
						//build parameter
						String value = evalString(xpath, "@value", actParam);
						if (value != null && !value.isEmpty()) {
							try {
								//check for integer parameter
								int intVal = Integer.parseInt(value);
								parameter = new IntParameter().withValue(intVal);
							} catch (NumberFormatException e) {
								try {
									//check for floating point parameter
									double floatVal = Double.parseDouble(value);
									parameter = new FloatParameter().withValue(floatVal);
								} catch (NumberFormatException x) {
									//string format parameter
									parameter = new Parameter().withValue(value);
								}
							}
						} else {
							parameter = new Parameter();
						}
						
						//set parameter on service
						service.addParameter(parameter);
						
						//build parameter attributes
						String paramId = evalString(xpath, "@ID", actParam);
						if (paramId != null && !paramId.isEmpty()) 
							parameter.setId(paramId);
						else {
							//handle spec ambiguity
							paramId = evalString(xpath, "@id", actParam);
							if (paramId != null && !paramId.isEmpty())
								parameter.setId(paramId);
						}
						
						String paramName = evalString(xpath, "@name", actParam);
						if (paramName != null && !paramName.isEmpty())
							parameter.setName(paramName);
						
						parameters.add(parameter);
					}
					
					service.setParameters(parameters);
				}
				
				Node notificationNode = evalNode(xpath, "notification", serviceNode);
				if (notificationNode != null) {
					Notification notification = new Notification();
					service.setNotification(notification);
					
					String ackedId = evalString(xpath, "@messageID", notificationNode);
					if (ackedId != null && !ackedId.isEmpty())
						notification.setMessageId(ackedId);
					
					String ackedAct = evalString(xpath, "@messageAction", notificationNode);
					if (ackedAct != null && !ackedAct.isEmpty())
						notification.setMessageAction(actionMap.get(ackedAct));
					
					Boolean success = evalBoolean(xpath, "@success", notificationNode);
					if (success != null) notification.setSuccess(success);
					
					String text = notificationNode.getTextContent();
					if (text != null) notification.setMessage(text);
				}
			}
			
		}
		
		return message;
	}
	
	/**
	 * Parses a {@link ServiceProfile} from XML.
	 * 
	 * @param node		The {@code <profileMessage>/<service>} node of the XML 
	 * 					object
	 * @param xpath		The XPath parser to use
	 * 
	 * @return a {@code ServiceProfile} object corresponding to the XML
	 */
	private static ServiceProfile parseProfile(Node node, XPath xpath) {
		ServiceProfile profile = new ServiceProfile();
		
		String id = evalString(xpath, "@ID", node);
		if (id != null && !id.isEmpty()) profile.setId(id);
		
		String name = evalString(xpath, "@name", node);
		if (name != null && !name.isEmpty()) profile.setName(name);
		
		String description = evalString(xpath, "@description", node);
		if (description != null && !description.isEmpty())
			profile.setDescription(description);
		
		String timeStr = evalString(xpath, "@timestamp", node);
		if (timeStr != null) {
			try {
				profile.setTimestamp(timestampFormat.parse(timeStr));
			} catch (ParseException e) {
			}
		}
		
		Node widget = evalNode(xpath, "widget", node);
		if (widget != null) profile.setWidget(parseWidget(widget, xpath));
		
		NodeList activities = evalNodeList(xpath, "activities/activity", node);
		List<Activity> as = new ArrayList<Activity>();
		
		if (activities != null && activities.getLength() > 0) {
			
			for (int i = 0; i < activities.getLength(); i++) {
				Node activity = activities.item(i);
				Activity a = new Activity();
				
				String activityId = evalString(xpath, "@ID", activity);
				if (activityId != null && !activityId.isEmpty())
					a.setId(activityId);
				
				String activityName = evalString(xpath, "@name", activity);
				if (activityName != null && !activityName.isEmpty()) 
					a.setName(activityName);
				
				String paramId = evalString(xpath, "@paramId", activity);
				if (paramId != null && !paramId.isEmpty())
					a.setParamId(paramId);
				
				String paramValue = evalString(xpath, "@paramValue", activity);
				if (paramValue != null && !paramValue.isEmpty())
					a.setParamValue(paramValue);
				
				NodeList parameters = evalNodeList(xpath, 
						"activityParameters/activityParameter", activity);
				if (parameters != null && parameters.getLength() > 0) {
					for (int j = 0; j < parameters.getLength(); j++) {
						a.addParameter(parseParameter(parameters.item(j), xpath));
					}
				}
				
				as.add(a);
			}
		}
		profile.setActivities(as);
		
		NodeList globals = 
			evalNodeList(xpath, "globalParameters/globalParameter", node);
		if (globals != null && globals.getLength() > 0) {
			List<Parameter> ps = new ArrayList<Parameter>(globals.getLength());
			
			for (int i = 0; i < globals.getLength(); i++) {
				ps.add(parseParameter(globals.item(i), xpath));
			}
			
			profile.setGlobalParameters(ps);
		}
		
		return profile;
	}
	
	/**
	 * Parses a {@link RegistrationTicket} from XML.
	 * 
	 * @param node		The {@code <newService>} node of the XML object
	 * @param xpath		The XPath parser to use
	 * 
	 * @return a {@code RegistrationTicket} object corresponding to the XML
	 */
	private static RegistrationTicket parseRegistration(Node node, 
			XPath xpath) {
		
		RegistrationTicket ticket = new RegistrationTicket();
		
		Node service = evalNode(xpath, "service", node);
		if (service != null) {
			ServiceInfo si = new ServiceInfo();
			
			String id = evalString(xpath, "@ID", service);
			if (id != null && !id.isEmpty()) si.setId(id);
			
			String name = evalString(xpath, "@name", service);
			if (name != null && !name.isEmpty()) si.setName(name);
			
			String type = evalString(xpath, "@type", service);
			if (type != null && !type.isEmpty()) si.setType(type);
			
			Node description = evalNode(xpath, "description", service);
			if (description != null) si.setDescription(description.getTextContent());
			
			Node contact = evalNode(xpath, "contact", service);
			if (contact != null) {
				String savoirUser = safeEvalString(xpath, "@savoirUser", contact);
				if (savoirUser != null) {
					si.setContactUser(savoirUser);
				} else {
					PersonInfo pi = new PersonInfo();
					
					String title = evalString(xpath, "@title", contact);
					if (title != null && !title.isEmpty()) pi.setHonorific(title);
					
					String fName = evalString(xpath, "@firstName", contact);
					if (fName != null && !fName.isEmpty()) pi.setFName(fName);
					
					String lName = evalString(xpath, "@lastName", contact);
					if (lName != null && !lName.isEmpty()) pi.setLName(lName);
					
					String phone = evalString(xpath, "@phone", contact);
					if (phone != null && !phone.isEmpty()) pi.setWorkPhone(phone);
					
					String email = evalString(xpath, "@email", contact);
					if (email != null && !email.isEmpty()) pi.setEmail1(email);
					
					String org = evalString(xpath, "@organization", contact);
					if (org != null && !org.isEmpty()) pi.setOrganization(org);
					
					String addr = evalString(xpath, "@streetAddress", contact);
					if (addr != null && !addr.isEmpty()) pi.setStreetAddress(addr);
					
					String city = evalString(xpath, "@city", contact);
					if (city != null && !city.isEmpty()) pi.setCity(city);
					
					String region = evalString(xpath, "@region", contact);
					if (region != null && !region.isEmpty()) pi.setRegion(region);
					
					String country = evalString(xpath, "@country", contact);
					if (country != null && !country.isEmpty()) pi.setCountry(country);
					
					String postal = evalString(xpath, "@postalCode", contact);
					if (postal != null && !postal.isEmpty()) pi.setPostal(postal);
					
					si.setContactInfo(pi);
				}
			}
			
			ticket.setService(si);
		}
		
		Node network = evalNode(xpath, "network", node);
		if (network != null) {
			NetworkInfo ni = new NetworkInfo();
			
			Node toService = evalNode(xpath, "toService", network);
			if (toService != null) ni.setToService(parseProtocolEndpoint(toService, xpath));
			
			Node toSavoir = evalNode(xpath, "toSavoir", network);
			if (toSavoir != null) ni.setToSavoir(parseProtocolEndpoint(toSavoir, xpath));
			
			ticket.setNetwork(ni);
		}
		
		Node widget = evalNode(xpath, "widget", node);
		if (widget != null) ticket.setWidget(parseWidget(widget, xpath));
		
		Node device = evalNode(xpath, "device", node);
		if (device != null) {
			DeviceInfo di = new DeviceInfo();
			
			Integer i = evalInt(xpath, "@maxSimultaneousUsers", device);
			if (i != null) di.setMaxSimultaneousUsers(i);
			
			Node authn = evalNode(xpath, "authentication", device);
			if (authn != null) {
				AuthenticationInfo ai = new AuthenticationInfo();
				
				Boolean b = evalBoolean(xpath, "@required", authn);
				if (b != null) ai.setRequired(b);
				
				AuthenticationType authnType = 
					authnTypeMap.get(evalString(xpath, "@type", authn));
				if (authnType != null) ai.setType(authnType);
				
				di.setAuthentication(ai);
			}
			
			ticket.setDevice(di);
		}
		
		return ticket;
	}
	
	/**
	 * Parses a {@link Widget} from XML.
	 * 
	 * @param node		The {@code <widget>} node of the XML object
	 * @param xpath		The XPath parser to use
	 * 
	 * @return a {@code Widget} object corresponding to the XML
	 */
	private static Widget parseWidget(Node widget, XPath xpath) {
		Widget w = new Widget();
		
		String title = evalString(xpath, "@title", widget);
		if (title != null && !title.isEmpty()) w.setTitle(title);
		
		Node description = evalNode(xpath, "description", widget);
		if (description != null) w.setDescription(description.getTextContent());
		
		String iconName = evalString(xpath, "icon/@name", widget);
		if (iconName != null && !iconName.isEmpty()) w.setIconName(iconName);
		
		ImgType iconType = imgTypeMap.get(evalString(xpath, "icon/@format", widget));
		if (iconType != null) w.setIconType(iconType);
		
		Node choices = evalNode(xpath, "choices", widget);
		if (choices != null) {
			Choices cs = new Choices();
			
			String baseUri = evalString(xpath, "@baseUri", choices);
			if (baseUri != null && !baseUri.isEmpty()) 
				cs.setBaseUri(baseUri);
			
			NodeList choiceNodes = evalNodeList(xpath, "choice", choices);
			if (choiceNodes != null && choiceNodes.getLength() > 0) {
				for (int i = 0; i < choiceNodes.getLength(); i++) {
					cs.addChoice(parseChoice(choiceNodes.item(i), xpath));
				}
			}
			
			w.setChoices(cs);
		}
		
		return w;
	}
	
	/**
	 * Parses a {@link Choice} from XML.
	 * 
	 * @param node		The {@code <choice>} node of the XML object
	 * @param xpath		The XPath parser to use
	 * 
	 * @return a {@code Choice} object corresponding to the XML
	 */
	private static Choice parseChoice(Node choice, XPath xpath) {
		Choice c = new Choice();
		
		String id = evalString(xpath, "@id", choice);
		if (id != null && !id.isEmpty()) c.setId(id);
		
		String label = evalString(xpath, "@label", choice);
		if (label != null && !label.isEmpty()) c.setLabel(label);
		
		String type = evalString(xpath, "@type", choice);
		if (type != null && !type.isEmpty()) c.setType(choiceTypeMap.get(type));
		
		String choiceParamId = evalString(xpath, "@paramId", choice);
		if (choiceParamId != null && !choiceParamId.isEmpty())
			c.setParamId(choiceParamId);
		
		NodeList options = evalNodeList(xpath, "option", choice);
		if (options != null && options.getLength() > 0) {
			for (int i = 1; i < options.getLength(); i++) {
				Node option = options.item(i);
				Option o = new Option();
				
				String name = evalString(xpath, "@name", option);
				if (name != null && !name.isEmpty()) o.setName(name);
				
				String optParamId = evalString(xpath, "@paramId", option);
				if (optParamId != null && !optParamId.isEmpty())
					o.setParamId(optParamId);
				
				String paramValue = evalString(xpath, "@paramValue", option);
				if (paramValue != null && !paramValue.isEmpty())
					o.setParamValue(paramValue);
				
				c.addOption(o);
			}
		}
		
		return c;
	}
	
	/**
	 * Parses a {@link Parameter} from XML.
	 * 
	 * @param node		The {@code <activityParameter>} or 
	 * 					{@code <globalParameter>} node of the XML object
	 * @param xpath		The XPath parser to use
	 * 
	 * @return a {@code Parameter} object corresponding to the XML
	 */
	private static Parameter parseParameter(Node parameter, XPath xpath) {
		String dataType = evalString(xpath, "@dataType", parameter);
		Parameter p = ParameterFactory.newParameter(dataType);
		
		String paramId = evalString(xpath, "@ID", parameter);
		if (paramId != null && !paramId.isEmpty()) 
			p.setId(paramId);
		
		String paramName = evalString(xpath, "@name", parameter);
		if (paramName != null && !paramName.isEmpty())
			p.setName(paramName);
		
		return p;
	}
	
	/**
	 * Parses a {@link ProtocolEndpoint} from XML.
	 * 
	 * @param node		The {@code <toService>} or {@code <toSavoir>} node of 
	 * 					the XML object
	 * @param xpath		The XPath parser to use
	 * 
	 * @return a {@code ProtocolEndpoint} object corresponding to the XML
	 */
	private static ProtocolEndpoint parseProtocolEndpoint(Node endpoint, 
			XPath xpath) {

		ProtocolType protocol = protocolTypeMap.get(
				evalString(xpath, "@protocol", endpoint));
		if (protocol == null) return null;
		
		switch (protocol) {
		case TCP_SOCKET:
			TcpEndpoint tcp = 
				new TcpEndpoint().withProtocol(ProtocolType.TCP_SOCKET);
			
			Node ipAddr = evalNode(xpath, "tcpSocket/ipAddress", endpoint);
			if (ipAddr != null) tcp.setIpAddress(ipAddr.getTextContent());
			
			Node portNum = evalNode(xpath, "tcpSocket/portNumber", endpoint);
			if (portNum != null) {
				try {
					tcp.setPortNumber(
							Integer.parseInt(portNum.getTextContent()));
				} catch (NumberFormatException e) {}
			}
			
			return tcp;
			
		case JMS:
			JmsEndpoint jms = new JmsEndpoint().withProtocol(ProtocolType.JMS);
			
			String method = evalString(
					xpath, "jms/jmsTransportConnector/@method", endpoint);
			if (method != null) jms.setConnectionMethod(method);
			
			Node uri = evalNode(
					xpath, "jms/jmsTransportConnector/uri", endpoint);
			if (uri != null) jms.setJmsUri(uri.getTextContent());
			
			Node topic = evalNode(
					xpath, "jms/jmsTransportConnector/topic", endpoint);
			if (topic != null) jms.setJmsTopic(topic.getTextContent());
			
			return jms;
			
		default:
			return null;
		}
	}
}
