// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/**
 * Parses scenario XML files, generating rulebases.
 * Will take scenario files corresponding to 
 * the scenario schema, and generate a rulebase using 
 * {@linkplain ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen the rule generator}.
 * 
 * <h2> Scenario Definition File Overview </h2>
 * The principle parts of a scenario definition file are the list of 
 * {@code <nodes>}, defining the resources (as well as control flow elements, 
 * like start, end, split and join), and the {@code <link>}s connecting them.
 * 
 * <h3> Nodes </h3>
 * The first node defined in a scenario file is the {@code <startNode>}. This 
 * node will be triggered when an authored session based off this scenario is 
 * run. When this happens, all nodes having an outgoing link from this node 
 * will be given control, and any containing resources will be started.
 * <p>
 * The most common type of node in a scenario is a {@code <resourceNode>}. The 
 * resource node contains a reference to a specific instance of an edge 
 * service, defined by its {@code <resource>} child. The resource has 
 * references to the {@code id} and (optionally) {@code name} of the resource, 
 * as well as an {@code <activity>} child which defines the activity {@code id} 
 * and (optionally) activity {@code name}. In addition to these, a resource may 
 * contain one or more {@code <variable>} children, defining variable bindings 
 * on this resource that can be used later. The {@code name} of a variable is 
 * used to later reference it, and should be unique, and composed entirely of 
 * alpha-numeric characters. The {@code parameter} attribute of a variable 
 * references the name of the activity parameter on the resource that should be 
 * bound to the variable.
 * <p>
 * {@code <splitNode>}s and {@code <joinNode>}s control branching and merging 
 * of control flow in the scenario. When a split node is reached, all nodes 
 * having an outgoing link from that split node will be given focus 
 * automatically, while a join node will only propagate focus on to the next 
 * node when all nodes having an incoming link to that join node have passed 
 * focus to it.
 * <p>
 * Finally, an {@code <endNode>}, the last node in a scenario file, will end 
 * the authored session when control is passed to it.
 * 
 * <h3> Links </h3>
 * {@code <link>} elements define the flow of control between nodes. A link 
 * leads out of the node referenced in its {@code from} attribute, and to the 
 * node referenced in its {@code to} attribute. Start and split nodes can have 
 * any number of links leading from them, where the node on the other end of 
 * each link is triggered automatically when the start or split node is 
 * reached. Join nodes also trigger the node on the other end of their link 
 * automatically once they are reached, but should only have one link leading 
 * out. Links leading out of start, split, or join nodes should not have rules 
 * defined on them. Also, as should be obvious, there should be no links 
 * leading out of end nodes, as they will never be used.
 * <p>
 * Links leading out of resource nodes are somewhat more complex. These links 
 * define the rule triggers to tranfer control away from that resource. Thus, 
 * there can be any number of links leading out of a resource node, but they 
 * should all have at least one rule defined on them.
 * 
 * <h3> Rules </h3>
 * A {@code <rule>} is divided into two parts, conditions and consequences. 
 * The conditions are composed primarily of {@code <when>} elements, 
 * optionally combined with {@code <and>}s, {@code <or>}s, and {@code <not>}s. 
 * The consequences are included in the {@code <then>} child of the rule.
 * <p>
 * A when condition is a condition of the form "when PARAMETER of RESOURCE 
 * COMPARES TO VALUE". Its {@code <resource>} child defines the resource, with 
 * similar syntax to the resource definition on a {@code <resourceNode>} 
 * (though not defining variables), while its {@code <parameter>} child gives 
 * the name of the parameter to consider. The {@code <value>} child gives the 
 * value to compare the parameter against, while the {@code <operator>} child 
 * dictates which comparison operator to use, as in the following table:
 * 	<h4> Comparison Operators </h4>
 * 	<table>
 * 	<tr><th align="left">Value&nbsp;&nbsp;</th><th align="left">Meaning</th></tr>
 * 	<tr>	<td>{@code eq}</td>	<td>Equals (==)</td>					</tr>
 * 	<tr>	<td>{@code ne}</td>	<td>Not Equal (!=)</td>					</tr>
 * 	<tr>	<td>{@code lt}</td>	<td>Less Than (&lt;)</td>				</tr>
 * 	<tr>	<td>{@code le}</td>	<td>Less Than or Equal (&lt;=)</td>		</tr>
 * 	<tr>	<td>{@code gt}</td>	<td>Greater Than (&gt;)</td>			</tr>
 * 	<tr>	<td>{@code ge}</td>	<td>Greater Than or Equal (&gt;=)</td>	</tr>
 * 	</table>
 * <p>
 * Compound conditions can be constructed with the {@code <and>}, {@code <or>}, 
 * and {@code <not>} elements wrapping other conditions. An AND condition is 
 * true only when all of its subcondtions are true. There is an implicit AND 
 * wrapping all the conditions in a rule, so there is no need for an 
 * {@code <and>} element immediately under a (@code <rule>} element. An OR 
 * condition is true if any of its subconditions are true, and false otherwise. 
 * A NOT condition is actually defined to be a compound condition, true when 
 * none of its subconditions are true (logically, this is more a NOT-OR than a 
 * simple NOT). AND, OR, and NOT condtions are allowed to wrap other AND, OR, 
 * NOT, and WHEN conditions to arbitrary depth.
 * <p>
 * The consequence of the rule (the actions that will take place when the 
 * conditions are all true) is held in the {@code <then>} element of the rule. 
 * The {@code <then>} can hold multiple {@code <resource>} children, defined 
 * similarly to how they are defined inside a {@code <resourceNode>}, though 
 * without variable definitions. Each {@code <resource>} should also hold one 
 * or more {@code <action>} children, which define actions to take on that 
 * resource. Each {@code <action>} has an {@code actionName} attribute, which 
 * is the name of one of the actions defined in the SAVOIR messaging spec, 
 * limited to the set of actions sensible for SAVOIR to send mid-session (these 
 * permissible actions are enumerated in the list below). A 
 * {@code setParameter} action should also have a set of {code <parameter>} 
 * children defining the parameters to set. The {@code id} attribute of a 
 * parameter gives the parameter ID, while a {@code value} attribute can be 
 * used to set a parameter to a fixed value, or a {@code variable} attribute is 
 * used to set the parameter to the value of an earlier-defined variable.
 * 	<h4> Valid in-session actions</h4>
 * 	<ul>
 * 	<li>start
 * 	<li>stop
 * 	<li>pause
 * 	<li>resume
 * 	<li>getStatus
 * 	<li>setParameter
 * 	<li>getProfile
 * 	</ul>
 * 
 * @see The scenario schema
 */
package ca.gc.nrc.iit.savoir.scenarioMgmt.parser;
