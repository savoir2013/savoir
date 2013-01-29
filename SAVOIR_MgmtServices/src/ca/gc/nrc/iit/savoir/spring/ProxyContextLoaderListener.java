// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.spring;

import javax.servlet.ServletContextEvent;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

public class ProxyContextLoaderListener extends ContextLoaderListener {
	private static WebApplicationContext ctx;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		super.contextInitialized(event);
		ctx = ContextLoader.getCurrentWebApplicationContext();
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		super.contextDestroyed(event);
		ctx = null;
	}

	public static WebApplicationContext getCtx() {
		return ctx;
	}
}
