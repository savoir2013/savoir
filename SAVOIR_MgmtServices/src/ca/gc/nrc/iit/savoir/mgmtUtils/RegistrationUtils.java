// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.mgmtUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ca.gc.nrc.iit.savoir.model.MessageTransformer;
import ca.gc.nrc.iit.savoir.model.registration.RegistrationTicket;
import ca.gc.nrc.iit.savoir.utils.FileUtils;

/**
 * Utility class for dealing with the profile file cache.
 * 
 * @author Aaron Moss
 */
public class RegistrationUtils {


	private static ResourceBundle resources = 
		ResourceBundle.getBundle("mgmtservices", Locale.getDefault());
		
	/** local path to the registration ticket file cache */
	private static String ticketFilepath = 
		resources.getString("repos.registration");
	/** local path to the icon file cache */
	private static String iconFilepath = 
		resources.getString("repos.icon");
	/** Prefix of paths to local cache that represents the web server root */
	private static String localPathPrefix;
	/** Prefix of web URIs to local files */
	private static String webUriPrefix;
	
	static {
		localPathPrefix = resources.getString("repos.filePrefix");
		if (localPathPrefix == null) localPathPrefix = "";
		
		webUriPrefix = resources.getString("repos.webPrefix");
		if (webUriPrefix == null) webUriPrefix = "";
	}
	
	/**
	 * Gets the full file path for a service's cached registration ticket
	 * 
	 * @param serviceId			The service ID
	 * @param serviceName		The service name
	 * 
	 * @return The file path of the resource's cached registration ticket (does 
	 * 			not guarantee that any file exists at that path) 
	 */
	public static String getTicketPath(String serviceId, 
			String serviceName) {
		String name = FileUtils.filenameSafe(serviceName);
		
		String filebase = serviceId + (name == null ? "" : "_" + name);
		if (filebase.length() > 120) filebase = filebase.substring(0, 120);
		
		return ticketFilepath + filebase + ".xml";
	}
	
	/** Code for the small icon */
	public static final int SMALL_ICON = 1;
	/** Code for the large icon */
	public static final int LARGE_ICON = 2;
	
	/**
	 * Gets the full file path for a service's cached icon
	 * 
	 * @param serviceId			The service ID
	 * @param serviceName		The service name
	 * @param size				The size of icon to get. If 
	 * 							{@value #SMALL_ICON}, the small icon; if 
	 * 							{@value #LARGE_ICON}, the large icon; otherwise 
	 * 							a default value
	 * 
	 * @return The file path of the resource's cached icon (does not guarantee 
	 * 			that any file exists at that path)
	 */
	public static String getIconPath(String serviceId, String serviceName, 
			int size) {
		
		String name = FileUtils.filenameSafe(serviceName);
		
		String filebase = serviceId + (name == null ? "" : "_" + name);
		if (filebase.length() > 120) filebase = filebase.substring(0, 120);
		
		String sizeSuffix;
		switch (size) {
		case SMALL_ICON:
			sizeSuffix = "_small";
			break;
		case LARGE_ICON:
			sizeSuffix = "_large";
			break;
		default:
			sizeSuffix = "";
			break;
		}
		
		return iconFilepath + filebase + sizeSuffix + ".png";
	}
	
	/**
	 * Gets the web URI accessing the given local path
	 * 
	 * @param localPath		The local filesystem path to a file
	 * 
	 * @return the web URI of the same file (does not guarantee file exists)
	 */
	public static String getWebUri(String localPath) {
		
		if (localPath == null || !localPath.startsWith(localPathPrefix)) {
			return localPath;
		} else {
			return webUriPrefix + localPath.substring(localPathPrefix.length());
		}
	}
	
	/**
	 * Gets the registration ticket for a given service
	 * 
	 * @param serviceId		The ID of the service to retrieve for
	 * @param serviceName	The name of the service to retrieve for
	 * 
	 * @return the registration ticket, null for none such
	 */
	public static RegistrationTicket getTicket(String serviceId, 
			String serviceName) {
		try {
			String file = FileUtils.readFile(
					getTicketPath(serviceId, serviceName));
			return (RegistrationTicket)MessageTransformer.fromXml(file);
		} catch (IOException e) {
			return null;
		} catch (ParserConfigurationException e) {
			return null;
		} catch (SAXException e) {
			return null;
		}
	}
	
	/**
	 * Stores a registration ticket in the repository
	 * 
	 * @param serviceId			The ID of the service to store the ticket for
	 * @param serviceName		The name of the service to store the ticket for
	 * @param xml				The XML value of the ticket to store
	 */
	public static void storeTicket(String serviceId, String serviceName, 
			String xml) {
		
		String filename = getTicketPath(serviceId, serviceName);
		
		try {
			FileUtils.writeFile(filename, xml);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
