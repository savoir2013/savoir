// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/**
 * Manages SAVOIR resources (edge services).
 * The resource manager is responsible for all direction and long-term state of 
 * resources.
 * 
 * <h2>Message Sequencing Rules</h2>
 * To ensure proper ordering of messages, the resource manager maintains a 
 * general set of rules governing the states in which it is proper to send 
 * certain kinds of messages. The 
 * message sequencing state machine document describes these rules in detail.
 * <p>
 * These rules are scoped to a single session. That is, each and every SAVOIR 
 * session gets an instance of this rulebase to manage its message sequencing. 
 * A reference to this session's ID is provided as a global for this knowledge 
 * base.
 * <p>
 * Within a session, there are two types of facts asserted into this knowledge 
 * base. The first is an 
 * {@link ca.gc.nrc.iit.savoir.resourceMgmt.InstanceState} - this represents 
 * the running state of a single instance of an edge service (so, for instance, 
 * different activities on the same device get different {@code InstanceState} 
 * objects). There will be exactly one {@code InstanceState} object per 
 * resource instance in the session.
 * <p>
 * The second type of fact is a 
 * {@link ca.gc.nrc.iit.savoir.resourceMgmt.ResourceMessage} - this represents 
 * a message that is queued to send. The class documentation on 
 * {@code ResourceMessage} has more details, but note that one field of 
 * {@code ResourceMessage} is {@code resource}, a reference to the unique 
 * {@code InstanceState} object representing the state of the message's 
 * destination.
 */
package ca.gc.nrc.iit.savoir.resourceMgmt;
