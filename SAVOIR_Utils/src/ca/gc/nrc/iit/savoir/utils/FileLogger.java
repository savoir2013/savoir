// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

/**
 * Logs output to a file. Basic capabilities, designed to provide a reliable 
 * way to get file output.
 * 
 * @author Aaron Moss
 */
public class FileLogger {

	/** Log file to write to */
	private PrintStream logFile;
	/** Name of this log */
	private String name;
	
	/**
	 * Generate a logger that appends output to the given file.
	 * 
	 * @param name		Name of this logger
	 * @param filename	The name of the file to output to (will use 
	 * 					{@code System.err} if this fails)
	 */
	public FileLogger(String name, String filename) {
		this(name, filename, true);
	}
	
	/**
	 * Generate a logger that outputs to the given file.
	 * 
	 * @param name		Name of this logger
	 * @param filename	The name of the file to output to (will use 
	 * 					{@code System.err} if this fails)
	 * @param append	True for append to file, false for overwrite
	 */
	public FileLogger(String name, String filename, boolean append) {
		this.name = name;
		
		try {
			logFile = new PrintStream(new FileOutputStream(filename, append));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logFile = System.err;
		}
	}
	
	protected void finalize() throws Throwable {
		if (logFile != System.err) logFile.close();
		super.finalize();
	}
	
	public String getName() {
		return name;
	}

	/**
	 * Writes a message to the log with a timestamp
	 * @param message	The message to write
	 */
	public final void log(String message) {
		logFile.printf("%n[%tc]\t%s%n", Calendar.getInstance(), message);
	}

	public static final String toString(Throwable t) {
		StringWriter w = new StringWriter();
		t.printStackTrace(new PrintWriter(w));
		return w.toString();
	}
}
