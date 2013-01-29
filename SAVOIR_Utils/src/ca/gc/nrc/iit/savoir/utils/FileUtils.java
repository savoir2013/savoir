// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Simple utilities for file I/O.
 * 
 * @author Aaron Moss
 */
public class FileUtils {

	/**
	 * Reads the specified file
	 * @param filename		The name of the file
	 * @return The contents of the file, as a String
	 * @throws IOException 
	 */
	public static String readFile(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		StringBuilder sb = new StringBuilder();
		
		String s = in.readLine();
		while (s != null) {
			sb.append(s);
			s = in.readLine();
		}
		
		return sb.toString();
	}
	
	/**
	 * Writes a file to disk
	 * @param filename		The filename to save the file under
	 * @param file			The contents of the file
	 * @throws IOException 
	 */
	public static void writeFile(String filename, String file) 
			throws IOException {
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(filename);
			writer.write(file);
		} finally {
			if (writer != null) writer.close();
		}
	}
	
	/* Filename-unsafe characters (to be replaced with underscores) */
	private static Pattern unsafeChars = Pattern.compile("[^a-zA-Z0-9]+");
	
	/**
	 * Makes a string that is very conservative about the characters it uses,
	 * suitable for a filename.
	 * @param str	The base string to use 
	 * @return str, where all strings of characters not in the numbers 0-9 or 
	 * 			the letters a-z or A-Z are replaced by an underscore
	 */
	public static String filenameSafe(String str) {
		if (str == null) return null;
		return unsafeChars.matcher(str).replaceAll("_");
	}
}
