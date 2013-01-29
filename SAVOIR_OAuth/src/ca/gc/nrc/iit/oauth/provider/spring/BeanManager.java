// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.oauth.provider.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

public class BeanManager {
	private static BeanManager manager;
	protected ApplicationContext ctx;

	/** A private Constructor prevents any other class from instantiating. */
	private BeanManager() {
		this.ctx = ContextLoader.getCurrentWebApplicationContext();
	}

	public ApplicationContext getContext() {
		return this.ctx;
	}

	public static synchronized BeanManager getBeanManager() {
		if (manager == null) {
			manager = new BeanManager();
		}
		return manager;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
