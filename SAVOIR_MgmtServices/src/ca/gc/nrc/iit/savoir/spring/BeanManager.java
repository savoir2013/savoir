// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/*
 * BeanManager is a singleton. It holds a reference to the Spring Factory. 
 * Use getContext() to get the ApplicationContext
 * 
 */

public class BeanManager {
	private static BeanManager manager;
	protected ApplicationContext ctx;
	protected final Log log = LogFactory.getLog(getClass());

	/** A private Constructor prevents any other class from instantiating. */
	private BeanManager() {
		log.info("Creating BeanManager Instance.");
		this.ctx = new ClassPathXmlApplicationContext(SpringConfig.CONFIG_FILES);
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
