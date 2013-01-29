// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import ca.gc.nrc.iit.savoir.model.profile.ServiceProfile;
import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.model.session.Service;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.ActivityNode;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.ActivityRule;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.EndNode;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.JoinNode;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.Node;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.RuleFile;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.SplitNode;
import ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.StartNode;
import ca.gc.nrc.iit.savoir.mgmtUtils.ProfileUtils;

/**
 * Output of parsing a scenario XML file.
 * Includes references to rule objects, as well as other fields not included in 
 * rule file.
 * 
 * @author Aaron Moss
 */
public class ScenarioParseState {
	
	/**
	 * Represents a variable in a scenario XML file 
	 */
	public static class Variable {
		
		/** Name of this variable */
		public String name;
		/** Service this variable is associated with */
		public Service service;
		/** ID of the parameter this variable binds to */
		public String parameterId;
		/** Type of parameter this variable represents */
		public Class<? extends Parameter> type;
		/** keep update attribute of variable */
		public boolean keepUpdate;
	}
	
	/** Document parsed */
	private Document document;
	
	/** Unique integer ID of this scenario */
	private Integer scenarioId;
	/** Name of the scenario */
	private String scenarioName;
	/** Time that this date was last modified */
	private Date lastModified;
	/** SAVOIR username of scenario author */
	private String authorId;
	/** Name of scenario author */
	private String authorName;
	/** A textual description of the scenario, can be used for the UI */
	private String description;
	
	/** APN sites in this scenario. */
	private ApnReservation apn;
	/** Nodes in this scenario, indexed by node ID */
	private LinkedHashMap<Integer, Node> nodes;
	/** Services in this scenario, indexed by node ID */
	private LinkedHashMap<Integer, Service> nodeServices;
	/** Services in this scenario, indexed by service ID */
	private LinkedHashMap<String, LinkedHashMap<String, Service>> services;
	/** Profiles of services in this scenario, indexed by service ID */
	private Map<String, ServiceProfile> profiles;
	/** Variables in this scenario, indexed by name */
	private Map<String, Variable> vars;
	
	
	public ScenarioParseState() {}
	
	
	public Document getDocument() {
		return document;
	}
	
	public Integer getScenarioId() {
		return scenarioId;
	}
	
	public String getScenarioName() {
		return scenarioName;
	}
	
	public Date getLastModified() {
		return this.lastModified;
	}
	
	public String getAuthorId() {
		return this.authorId;
	}
	
	public String getAuthorName() {
		return this.authorName;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public ApnReservation getSites() {
		return apn;
	}
	
	public List<Node> getNodes() {
		return (nodes == null) ? null : new ArrayList<Node>(nodes.values());
	}
	
	/**
	 * Gets a node from the list
	 * @param id	The ID of the node to get
	 * @return the node with that ID, null for none such
	 */
	public Node getNode(int id) {
		return (nodes == null) ? null : nodes.get(id);
	}
	
	public List<Service> getServices() {
		if (services == null) return null;
		
		ArrayList<Service> output = new ArrayList<Service>();
		for (Map<String, Service> activities : services.values()) {
			output.addAll(activities.values());
		}
		
		return output;
	}
	
	/**
	 * Gets a service from the list
	 * 
	 * @param id	The ID of the service to get
	 * @param aId	The activity ID of the service to get
	 * 
	 * @return	the service with that ID and activity ID, (if aId is null, will 
	 * 			select a service with ID id arbitrarily), null for none such
	 */
	public Service getService(String id, String aId) {
		if (services == null) return null;
		Map<String, Service> activities = services.get(id);
		if (activities == null) return null;
		if (aId == null) {
			if (activities.isEmpty()) return null;
			else return activities.values().iterator().next();
		} else {
			return activities.get(aId);
		}
	}
	
	/**
	 * Gets the service profile for the service with the given ID
	 * @param id	The service ID
	 * @return the service profile for the service with that ID, null for 
	 *  		none such
	 */
	public ServiceProfile getProfile(String id) {
		return profiles == null ? null : profiles.get(id);
	}
	
	public Map<String, Variable> getVars() {
		return this.vars;
	}
	
	/**
	 * Gets a variable by name
	 * @param name	The name of the variable
	 * @return	The variable, null for none such
	 */
	public Variable getVar(String name) {
		return (vars == null) ? null : vars.get(name);
	}
	
	/**
	 * @return the rulefile represented by this scenario file
	 */
	public RuleFile getRules() {
		RuleFile rf = new RuleFile();
		
		if (nodes != null) {
			rf.addNodes(nodes.values());
		}
		
		return rf;
	}
	
	
	public void setDocument(Document document) {
		this.document = document;
	}
	
	public void setScenarioId(Integer scenarioId) {
		this.scenarioId = scenarioId;
	}
	
	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}
	
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setApn(ApnReservation apn) {
		this.apn = apn;
	}
	
	/**
	 * Adds a resource node to the end of the list of nodes, assigning it an 
	 * unique name as well.
	 * @param nodeId	The ID of the node
	 * @param service	The service on that node
	 */
	public void addResourceNode(int nodeId, Service service) {
		String serviceId = service.getId();
		String activityId = service.getActivityId();
		
		if (this.nodeServices == null) {
			this.nodeServices = new LinkedHashMap<Integer, Service>();
		}
		if (this.services == null) {
			this.services = 
				new LinkedHashMap<String, LinkedHashMap<String, Service>>();
		}
		if (this.profiles == null) {
			this.profiles = new HashMap<String, ServiceProfile>();
		}
		
		this.nodeServices.put(nodeId, service);
		LinkedHashMap<String, Service> activities = 
			this.services.get(serviceId);
		if (activities == null) {
			activities = new LinkedHashMap<String, Service>();
			this.services.put(serviceId, activities);
		}
		activities.put(activityId, service);
		this.profiles.put(serviceId, 
				ProfileUtils.getProfile(serviceId, service.getName()));
		
		ActivityNode node = new ActivityNode();
		node.setName(service.getName() + "-" + nodeId);
		addNode(nodeId, node);
	}
	
	/**
	 * Adds the start node to the list of nodes
	 * @param id	The ID of the start node
	 */
	public void addStartNode(int id) {
		addNode(id, new StartNode());
	}
	
	/**
	 * Adds the end node to the list of nodes
	 * @param id	The ID Of the end node
	 */
	public void addEndNode(int id) {
		addNode(id, new EndNode());
	}
	
	/**
	 * Adds a split node to the list of nodes, assigning it an unique name as 
	 * well.
	 * @param id	The ID of the split node
	 */
	public void addSplitNode(int id) {
		SplitNode node = new SplitNode();
		node.setName("Split-" + id);
		addNode(id, node);
	}
	
	/**
	 * Adds a join node to the list of nodes, assigning it an unique name as 
	 * well.
	 * 
	 * @param id	The ID of the join node
	 */
	public void addJoinNode(int id) {
		JoinNode node = new JoinNode();
		node.setName("Join-" + id);
		addNode(id, node);
	}
	
	/**
	 * Adds a node to the list of nodes
	 * 
	 * @param id		The id of the node
	 * @param node		The node to add
	 */
	private void addNode(int id, Node node) {
		if (this.nodes == null) this.nodes = new LinkedHashMap<Integer, Node>();
		this.nodes.put(id, node);
	}
	
	/**
	 * Adds a variable
	 * @param var	The variable to add (will not add if null)
	 */
	public void addVar(Variable var) {
		if (var == null) return;
		if (vars == null) vars = new HashMap<String, Variable>();
		
		vars.put(var.name, var);
	}
	
	/**
	 * Adds a variable
	 * 
	 * @param name			The name of the variable
	 * @param serv			The service this variable is bound to
	 * @param paramId		The ID of the parameter this variable is bound to
	 * @param keepUpdate	The keepUpdate attribute of this variable
	 */
	public void addVar(String name, Service serv, String paramId, 
			boolean keepUpdate) {
		Variable var = new Variable();
		
		var.name = name;
		var.service = serv;
		var.parameterId = paramId;
		var.keepUpdate = keepUpdate;
		
		addVar(var);
	}
	
	/**
	 * Adds a link to the rulefile, handling correctly the outgoing effects on 
	 * the "from" node.
	 * 
	 * @param fromId	The "from" node for the link this rule is on
	 * @param toId		The "to" node for the link this rule is on
	 * @param rule		The rule to add
	 */
	public void addLink(int fromId, int toId, ActivityRule rule) {
		if (null == nodes) return;
		
		Node from = nodes.get(fromId);
		Node to = nodes.get(toId);
		
		if (null == from || null == to) return;
		
		String toName;
		if (to instanceof JoinNode) {
			toName = ((JoinNode)to).getNewName();
		} else {
			toName = to.getName();
		}
		
		if (to instanceof EndNode && rule == null) {
			//no added rule for empty link to endNode
			return;
		}
		
		if (from instanceof StartNode) {
			StartNode snFrom = (StartNode)from;
			Service toService = 
				(null == nodeServices) ? null : nodeServices.get(toId);
			snFrom.addNextNode(toName, toService);
			if (rule != null) {
				//set custom start commands for this service
				snFrom.setStartCommands(toService, rule.getConsequences());
			}
		
		} else if (from instanceof SplitNode) {
			((SplitNode)from).addNextNode(toName);
		
		} else if (from instanceof JoinNode) {
			((JoinNode)from).setNextNode(toName);
		
		} else if (from instanceof ActivityNode) {
			//add rule to node
			((ActivityNode)from).addRule(rule);
		}
	}

}
