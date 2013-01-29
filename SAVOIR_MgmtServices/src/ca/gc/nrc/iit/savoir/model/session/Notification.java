// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.session;

/**
 * Java representation of a notification element of a Service
 */
public class Notification {

	/** associated service */
	private Service service;
	
	/** ID of message being responded to */
	private String messageId;
	/** action of message being responded to */
	private Action messageAction;
	/** was this message successfully acted upon */
	private boolean success;
	/** the message of this notification */
	private String message;
	
	public Notification() {
	}

	//Java bean API for Notification construction
	
	public Service getService() {
		return service;
	}

	public String getMessageId() {
		return messageId;
	}

	public Action getMessageAction() {
		return messageAction;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public void setService(Service service) {
		if (this.service == service) {
			//break infinite loop
			return;
		}
		
		if (this.service != null) {
			this.service.setNotification(null);
		}
		this.service = service;
		if (service != null && service.getNotification() == null) {
			service.setNotification(this);
		}
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public void setMessageAction(Action messageAction) {
		this.messageAction = messageAction;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	//Fluent API for Notification construction
	
	public Notification withMessageId(String messageId) {
		this.messageId = messageId;
		return this;
	}

	public Notification withMessageAction(Action messageAction) {
		this.messageAction = messageAction;
		return this;
	}

	public Notification withSuccess(boolean success) {
		this.success = success;
		return this;
	}

	public Notification withMessage(String message) {
		this.message = message;
		return this;
	}
	
	public Service end() {
		return this.service;
	}
}
