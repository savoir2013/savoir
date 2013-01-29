// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.service.factory.DefaultServiceConfiguration;
import org.apache.cxf.service.model.MessagePartInfo;

//this custom service configuration is used to customise the generated WSDL 
public class ServiceConfiguration extends DefaultServiceConfiguration {

	protected final static Log log = LogFactory
			.getLog(ServiceConfiguration.class);

	public Long getWrapperPartMinOccurs(MessagePartInfo mpi) {
		if (super.getWrapperPartMaxOccurs(mpi) != Long.MAX_VALUE)
			return super.getWrapperPartMaxOccurs(mpi);
		else
			return super.getWrapperPartMinOccurs(mpi);

	}

}
