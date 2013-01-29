// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * File: ClientException.java
 *
 * Created on 19 mars 2008, 20:25
 *
 */

package ca.gc.nrc.iit.savoir.mgmtUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ClientException extends java.lang.Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Exception originalException=null;
	
	/** Creates a new instance of ClientException */
	public ClientException() {
		super();
	}
	
	/** Creates a new instance of ClientException */
	public ClientException(Exception e) {
		super();
		this.originalException=e;
	}
	
	/** Creates a new instance of ClientException */
	public ClientException(String fault) {
		super(fault);
		this.originalException= null;
	}
	
	/** Creates a new instance of ClientException */
	public ClientException(String fault, Exception e) {
		super(fault);
		this.originalException=e;
	}
	
	public String getFaultTrace() {
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    this.printStackTrace(printWriter);
	    if(originalException!=null){
		    String ls = System.getProperty("line.separator");
		    printWriter.print(ls+"Original fault caused by:"+ls);
		    originalException.printStackTrace(printWriter);    
	    }
	    return result.toString();
	}
	
	public static String getFaultTrace(Throwable t) {
		String resultTrace="";
		if(t instanceof ClientException){
			if (((ClientException) t).getOriginalException() != null) {
				ClientException cle = (ClientException) t;
				resultTrace = cle.getFaultTrace();
			}
		}else{
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			t.printStackTrace(printWriter);
			resultTrace = result.toString();
		}
		return resultTrace;
	}
	
	public static String getFaultTrace(Exception e) {
		String resultTrace="";
		if(e instanceof ClientException){
			if (((ClientException) e).getOriginalException() != null) {
				ClientException cle = (ClientException) e;
				resultTrace = cle.getFaultTrace();
			}
		}else{
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			resultTrace = result.toString();
		}
		return resultTrace;
	}
	
	public Exception getOriginalException() {
		return originalException;
	}
}
