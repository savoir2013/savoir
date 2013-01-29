// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/**
 * Java classes to represent objects in the SAVOIR message specification.
 * {@link ca.gc.nrc.iit.savoir.model.SavoirXml} is an abstract class that all 
 * top-level objects inherit from. {@link ca.gc.nrc.iit.savoir.model.session.Message} 
 * represents an in-session message, which 
 * {@link ca.gc.nrc.iit.savoir.model.profile.ServiceProfile} represents a service 
 * profile message. {@link ca.gc.nrc.iit.savoir.model.MessageTransformer} is a 
 * utility class for converting these objects to and from XML.
 * <p>
 * All the classes in this package have a 
 * <a href="http://en.wikipedia.org/wiki/Fluent_interface">Fluent API</a>. The 
 * convention I have chosen to use for this is, in addition to a 
 * {@code setXxx} method to set property {@code xxx} on an object, there is a 
 * {@code withXxx} method that sets the property and returns a reference to the 
 * object, allowing chained calls (For example, 
 * {@code new Parameter().withId("foo").withValue("bar");}).
 * 
 * @see The SAVOIR message specification
 */
package ca.gc.nrc.iit.savoir.model;
