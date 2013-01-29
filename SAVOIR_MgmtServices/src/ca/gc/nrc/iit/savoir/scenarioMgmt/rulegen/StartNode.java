// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.gc.nrc.iit.savoir.model.session.Action;
import ca.gc.nrc.iit.savoir.model.session.Service;

/**
 * Represents the rulebase start rule.
 * Triggers when no control node has yet been inserted, starting and passing 
 * control to the services specified by each of its {@code nextNodes}. The 
 * {@code nextNodes} may optionally have customized start commands, set via the 
 * {@code startCommands} mapping of {@code Service}s to {@code Consequence}s.
 * 
 * @author Aaron Moss
 */
public class StartNode extends Node {

	/** Nodes to be started by this rule.
	 *  The key is the node name, the value its associated service. */
	private LinkedHashMap<String, Service> nextNodes;
	/** Custom commands to start a service */
	private Map<Service, List<Consequence>> startCommands;
	
	public StartNode() {
		this.name = "Start";
		nextNodes = new LinkedHashMap<String, Service>();
		startCommands = new HashMap<Service, List<Consequence>>();
	}

	
	public List<String> getNextNodes() {
		return new ArrayList<String>(nextNodes.keySet());
	}
	
	public List<Service> getServices() {
		return new ArrayList<Service>(nextNodes.values());
	}
	
	/**
	 * Get the custom start commands for a service. If this returns null for a 
	 * service set on this start node, the command sent will be simply "start"
	 * 
	 * @param serv		The service to get the start command for
	 * 
	 * @return	The list of custom start commands for that service
	 */
	public List<Consequence> getStartCommands(Service serv) {
		return startCommands.get(serv);
	}
	
	/**
	 * Overrides default behaviour to ignore changes to name
	 */
	public void setName(String name) {}
	
	public void setNextNodes(Map<String, Service> nextNodes) {
		this.nextNodes = 
			(null == nextNodes) ? 
				null : 
				new LinkedHashMap<String, Service>(nextNodes);
	}
	
	public void addNextNode(String nextNode, Service service) {
		this.nextNodes.put(nextNode, service);
	}
	
	/**
	 * Overrides default starting command ("start") with a custom list of 
	 * commands.
	 * 
	 * @param serv		The service to set the commands for
	 * @param cmds		The custom starting behaviour
	 */
	public void setStartCommands(Service serv, List<Consequence> cmds) {
		this.startCommands.put(serv, cmds);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		//rule "Start"
		//	salience 20
		//	
		//	when
		//		not(ControlNode())
		//	then
		//		{#start device <serv>}+
		//		{drools.insert(new ControlNode("<name>"));}+
		//end 
		
		sb.append("rule \"Start\"\n")
				.append("\tsalience 20\n\n")
				.append("\twhen\n")
					.append("\t\tnot(ControlNode())\n")
				.append("\tthen\n");
		for (Service serv : nextNodes.values()) {
			if (null != serv) {
				List<Consequence> cmds = startCommands.get(serv);
				if (cmds != null) {
					//handle custom start commands
					for (Consequence cmd : cmds) {
						sb.append("\t\t").append(cmd.toString());
					}
				} else {
					//default start command
					sb.append("\t\t").append(new UpdateConsequence(
							serv, Action.START, null).toString());
				}
			}
		}
		for (String node : nextNodes.keySet()) {
			//drools.insert(new ControlNode("<name>"));
			sb.append("\t\tdrools.insert(new ControlNode(\"")
				.append(node).append("\"));\n");
		}
		sb.append("end\n");
		
		return sb.toString();
	}
}
