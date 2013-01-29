// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.registration;

import ca.gc.nrc.iit.savoir.model.SavoirXml;
import ca.gc.nrc.iit.savoir.model.profile.Widget;

/**
 * Represents a service registration ticket, as defined in the "<b>Edge Service 
 * Registration</b>" section of the SAVOIR message spec.
 * 
 * @author Aaron Moss
 */
public class RegistrationTicket extends SavoirXml {

	/** Basic information about the service, and contact details for the 
	 *  owner */
	private ServiceInfo service;
	/** Transport-level parameters for communication with the device */
	private NetworkInfo network;
	/** Specification to generate the device widget */
	private Widget widget;
	/** Device-specific parameters required to manage the device */
	private DeviceInfo device;
	
	
	public RegistrationTicket() {}


	//Java bean API
	public ServiceInfo getService() {
		return service;
	}

	public NetworkInfo getNetwork() {
		return network;
	}

	public Widget getWidget() {
		return widget;
	}

	public DeviceInfo getDevice() {
		return device;
	}


	public void setService(ServiceInfo service) {
		this.service = service;
	}

	public void setNetwork(NetworkInfo network) {
		this.network = network;
	}

	public void setWidget(Widget widget) {
		this.widget = widget;
	}

	public void setDevice(DeviceInfo device) {
		this.device = device;
	}
	
	
	//Fluent API
	public RegistrationTicket withService(ServiceInfo service) {
		this.service = service;
		return this;
	}

	public RegistrationTicket withNetwork(NetworkInfo network) {
		this.network = network;
		return this;
	}

	public RegistrationTicket withWidget(Widget widget) {
		this.widget = widget;
		return this;
	}

	public RegistrationTicket withDevice(DeviceInfo device) {
		this.device = device;
		return this;
	}
	
}
