// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/**
 * Object graph representing the rule file needed to run an authored session. 
 * The classes in this package are used by 
 * {@linkplain ca.gc.nrc.iit.savoir.scenarioMgmt.parser the scenario parser} to 
 * generate rulefiles.
 * 
 * <h2> Rulefile Operation </h2>
 * The structure of the rulefiles generated from this class can be thought of 
 * as a directed graph, with edge device instances as the nodes, and rules as 
 * the edges. Control is passed between nodes of the graph by inserting a 
 * {@link ca.gc.nrc.iit.savoir.scenarioMgmt.ControlNode ControlNode} as a fact 
 * in the knowledge base, where the name of the {@code ControlNode} is derived 
 * from the node ID in the defining scenario XML, and, for better 
 * human-readability, the type of node, or the name of the resource on a 
 * resource node. Branching control flow is acheived with a 
 * {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.SplitNode}, which inserts 
 * multiple {@code ControlNode}s when it is reached, while joining branched 
 * flows uses a {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.JoinNode}, 
 * which waits for multiple incoming {@code ControlNode}s before it triggers. 
 * Standard {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.ActivityNode}s 
 * trigger when certain conditions hold (the rules governing the scenario), 
 * and, in the case of 
 * {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.ControlflowRule}s, transfer 
 * control to the subsequent node. With the exception of 
 * {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.StartNode}s (which trigger 
 * when there is no {@code ControlNode}), each rule requires the presence of 
 * the {@code ControlNode} corresponding to its node to trigger. Rules that 
 * transfer control to other nodes will bind this {@code ControlNode} to a 
 * variable so that it can later be retracted.
 * <p>
 * The facts inserted into the knowledge base are 
 * {@link ca.gc.nrc.iit.savoir.model.session.Service Service}s and 
 * {@link ca.gc.nrc.iit.savoir.model.session.Parameter Parameter}s from the message bus 
 * model (normalized for better comparison). To match a rule, the rule engine 
 * first checks for the presence of the appropriate {@code Service} facts, then 
 * for {@code Parameter} facts whose values correspond to those specified in 
 * the scenario definition. The various typed subclasses of {@code Parameter} 
 * are used here to ensure that the comparison is performed correctly (for 
 * example, {@code "042" != "42"} (as strings), but {@code 042 == 42} (as 
 * integers)). The consequence of the rule makes hardcoded calls to the  
 * {@link ca.gc.nrc.iit.savoir.scenarioMgmt.ScenarioMgrImpl.MgmtProxy mgmtProxy}, 
 * an object which wraps the management services. The most typical call made, 
 * to {@code controlDevice()}, sends a message to that device. As the rule's 
 * consequences are known at generation time, they are possible to hardcode 
 * (with the exception of variable values to set parameters to - those are 
 * accomplished by using a 
 * {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.VarBinding} to ensure that 
 * the rule engine currently has a value for that variable. This becomes a 
 * condition of the rule, as it doesn't make sense to set a parameter to an 
 * unknown value, and binds the value of that parameter to a variable that is 
 * used in the rule consequence).
 * 
 * <h2> Package Structure </h2>
 * <ul>
 * 	<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.RuleFile} 
 * 		holds a list of {@code Node}s, wrapping them with the package 
 * 		declaration, imports, and globals. </li>
 * 	<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.Node} 
 * 		is an abstract class, representing a workflow node. Each node has a 
 * 		"name" property, which should be unique. Node is extended by the 
 * 		following:
 * 	<ul>
 * 		<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.StartNode}, 
 * 			a scenario start rule, holding a map of node names of nodes 
 * 			({@code String}) to the services ({@code Service}) they correspond 
 * 			to. Each of these services will be started and have control passed 
 * 			to it at scenario start. (the start node's name is "Start") </li>
 * 		<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.EndNode}, 
 * 			a scenario end rule, taking no parameters (its node name is "End") 
 * 			</li>
 * 		<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.SplitNode}, 
 * 			which branches the flow of control to a list of "nextNodes" 
 * 			(identified by their names) </li>
 * 		<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.JoinNode}, 
 * 			which merges a branched flow of control from "index" separate input 
 * 			streams (the {@code JoinNode} should be addressed by 
 * 			"&lt;name&gt;-&lt;index&gt;" instead of "&lt;name&gt;") </li>
 * 		<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.ActivityNode} 
 * 			represents a control flow node, holding a set of 
 * 			{@code ActivityRule}s
 * 		<ul>
 * 			<li> Adding a rule to this node will cause the node to give it its 
 * 				own activity ID and an activity-unique index </li>
 * 			<li> An idiom would be to add rules to an appropriate 
 * 				{@code ActivityNode}, then call 
 * 				{@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.RuleFile#addNode(Node)}, 
 * 				ensuring an unique name for all rules </li>
 * 		</ul> </li> 
 * 	</ul> </li> 
 * 	<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.Rule} 
 * 		is an abstract class, extended by the following:
 * 	<ul>
 * 		<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.ActivityRule}, 
 * 			a rule on an activity
 * 		<ul>
 * 			<li> Has a list of {@code Condition}s to trigger on, as well as a 
 * 				list of {@code Consequence}s to perform when those conditions 
 * 				are met. </li>
 * 			<li> Has an "{@code activity}" (name for this node) and 
 * 				"{@code index}" (unique rule number within the node) </li>
 * 			<li> Also keeps a set of {@code VarBinding}, variables needed by 
 * 				this rule, as well as a set of {@code ServiceBinding} services 
 * 				needed by the conditions and variables of this rule. </li>
 * 			<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.ControlflowRule}, 
 * 				an {@code ActivityRule} that transfers control to the next node 
 * 				(given as a {@code String}) </li>
 * 			<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.DataflowRule}, 
 * 				an {@code ActivityRule} that does not pass control to the next 
 * 				node </li>
 * 		</ul> </li> 
 * 	</ul> </li> 
 * 	<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.Condition} 
 * 		is an abstract class representing a rule condition. Its subclasses are 
 * 		responsible for implementing 
 * 		{@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.Condition#requiredServices()}, 
 * 		which gives a list of {@code Service} objects this condition 
 * 		requires bound in its rule.
 * 	<ul>
 * 		<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.WhenCondition} 
 * 			is a {@code Condition} representing the "when" part of a rule - 
 * 			when a {@code Parameter} "{@code param}" on a {@code Service} 
 * 			"{@code service}" compared to a value derived from "{@code param}" 
 * 			using {@code Comparison} "{@code comp}" returns true </li>
 * 		<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.CompoundCondition} 
 * 			is an abstract class representing {@code Condition}s composed of 
 * 			multiple other conditions
 * 		<ul>
 * 			<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.AndCondition} 
 * 				is a {@code CompoundCondition} representing the intersection of 
 * 				its subconditions (true when all of its subconditions are 
 * 				true). </li>
 * 			<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.OrCondition} 
 * 				is a {@code CompoundCondition} representing the union of its 
 * 				subconditions (true when any of its subconditions are true). 
 * 				</li>
 * 			<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.NotCondition} 
 * 				is a {@code CompoundCondition} representing the complement of 
 * 				the union of its subconditions (true when none of its 
 * 				subconditions are true). </li>
 * 		</ul> </li> 
 * 	</ul> </li> 
 * 	<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.Consequence} 
 * 		is an abstract class representing an action taken in the "then" part of 
 * 		a rule
 * 	<ul>
 * 		<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.UpdateConsequence} 
 * 			is a {@code Consequence} which sends a message with {@code Action} 
 * 			"{@code action}" to {@code Service} "{@code service}" with 
 * 			{@code Parameter}s "{@code params}" </li>
 * 	</ul> </li> 
 * 	<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.Comparison} 
 * 		is an enumeration of the available comparisons </li>
 * 	<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.VarBinding} 
 * 		represents a condition needed to bind a variable </li>
 * 	<li> {@link ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen.ServiceBinding} 
 * 		represents a condition needed to bind a service </li>
 * </ul>
 */
package ca.gc.nrc.iit.savoir.scenarioMgmt.rulegen;
