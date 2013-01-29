// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlSeeAlso( { ExceptionCaught.class, NetworkConnectionConflict.class,
		IncompatibleSchedulingMethod.class, ScenarioConflict.class,
		NoScenarioFoundException.class, UnscheduledSubSession.class,
		ResourceUnavailable.class, ImpossibleReservation.class })
@XmlTransient 
public abstract class SchedulingConflict extends Throwable {

	private static final long serialVersionUID = -2876190873811654079L;

	public SchedulingConflict() {
		super();
	}

	public abstract String toString();

}
