// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.mgmtUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ca.gc.nrc.iit.savoir.model.MessageTransformer;
import ca.gc.nrc.iit.savoir.model.profile.Activity;
import ca.gc.nrc.iit.savoir.model.profile.ServiceProfile;
import ca.gc.nrc.iit.savoir.model.session.Parameter;
import ca.gc.nrc.iit.savoir.model.session.ParameterFactory;
import ca.gc.nrc.iit.savoir.utils.FileUtils;

/**
 * Utility class for dealing with the profile file repository. 
 * {@code ProfileUtils} keeps a LRU cache of the most recently-used profiles - 
 * the size of this cache can be set using the "cacheSize" bean property (its 
 * default value is {@value #DEFAULT_CACHE_SIZE})
 * 
 * @author Aaron Moss
 */
@SuppressWarnings("serial")
public class ProfileUtils {

	/** local path to the profile file cache */
	private static String filepath = 
		ResourceBundle.getBundle("mgmtservices", Locale.getDefault())
			.getString("repos.profile");
	
	/** LRU cache of active service profiles, by service ID */
	private static LinkedHashMap<String, ServiceProfile> profiles;
	/** default number of service profiles held in cache */
	public static final int DEFAULT_CACHE_SIZE = 40;
	/** number of service profiles held in cache */
	private static int cacheSize = DEFAULT_CACHE_SIZE;
	
	static {
		//set up least-recently-used cache for service profiles
		// this will keep the 35 most recently used service profiles 
		// compiled and in memory, to save the overhead of continually 
		// searching through them to find parameter types
		profiles = new LinkedHashMap<String, ServiceProfile>(
				cacheSize + 1, 0.75f, true){
			protected boolean removeEldestEntry(
					Map.Entry<String, ServiceProfile> e) {
				return size() > cacheSize;
			}
		};
	}
	
	public static int getCacheSize() {
		return ProfileUtils.cacheSize;
	}
	
	public static void setCacheSize(int cacheSize) {
		ProfileUtils.cacheSize = cacheSize;
	}
	
	/**
	 * Stores a new or updated service profile
	 * 
	 * @param serviceId		The ID of the service
	 * @param profile		The new profile
	 * @param xml			The original XML file (optional - to preserve the 
	 * 						source losslessly)
	 */
	public static void updateProfile(String serviceId, ServiceProfile profile, 
			String xml) {
		
		//generate XML, if needed 
		if (xml == null) xml = MessageTransformer.toXml(profile);
		
		//write profile to disk
		String filename = getProfilePath(profile.getId(), profile.getName());
		try {
			FileUtils.writeFile(filename, xml);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		//update profile cache with new profile
		profiles.put(serviceId, profile);
	}
	
	/**
	 * Gets the full file path for a service's cached profile
	 * 
	 * @param serviceId			The service ID
	 * @param serviceName		The service name
	 * 
	 * @return The file path of the resource's cached profile (does not 
	 * 			guarantee that any file exists at that path) 
	 */
	public static String getProfilePath(String serviceId, 
			String serviceName) {
		String name = FileUtils.filenameSafe(serviceName);
		
		String filebase = serviceId + (name == null ? "" : "_" + name);
		if (filebase.length() > 120) filebase = filebase.substring(0, 120);
		
		return filepath + filebase + ".xml";
	}
	
	/**
	 * Gets the profile for a give service
	 * 
	 * @param serviceId		The ID of the service to retrieve for
	 * @param serviceName	The name of the service to retrieve for
	 * 
	 * @return the service profile, null for none such
	 */
	public static ServiceProfile getProfile(String serviceId, 
			String serviceName) {
		//look in cache for profile
		ServiceProfile profile = profiles.get(serviceId);
		if (profile == null && !profiles.containsKey(serviceId)) {
			//not in cache - load from disk
			try {
				String file = FileUtils.readFile(
						getProfilePath(serviceId, serviceName));
				profile = (ServiceProfile)MessageTransformer.fromXml(file);
			} catch (IOException e) {
				profile = null;
			} catch (ParserConfigurationException e) {
				profile = null;
			} catch (SAXException e) {
				profile = null;
			}
			
			profiles.put(serviceId, profile);
		}
		return profile;
	}
	
	/**
	 * Gets a parameter of the appropriate runtime type, with the given id and 
	 * value.
	 * 
	 * @param profile		Service profile to get type from
	 * @param activityId	ID of activity on service
	 * @param id			ID of parameter to get from service profile
	 * @param value			Value of parameter to set
	 * 
	 * @return A parameter, with the ID and value given. Runtime type will be 
	 * 			loaded from profile, if possible, or, if not (profile null, 
	 * 			activity not present in profile, parameter ID not present on 
	 * 			activity), inferred from the value. 
	 */
	public static Parameter getParamter(ServiceProfile profile, 
			String activityId, String id, String value) {
		
		if (profile != null) {
			Activity activity = profile.getActivity(activityId);
			
			if (activity != null) {
				Parameter p = activity.getParameter(id);
				
				if (p != null) {
					//return clone of profile parameter (preserves type)
					// with given value
					return p.clone().withValue(value);
				}
			}
		}
		
		//fallback to inference
		return ParameterFactory.inferParameter(value).withId(id);
	}
	
	/**
	 * Gets a parameter of the appropriate runtime type, with the given id and 
	 * value.
	 * 
	 * @param profile		Service profile to get type from
	 * @param activityId	ID of activity on service
	 * @param id			ID of parameter to get from service profile
	 * 
	 * @return A parameter, with the ID given. Runtime type will be loaded from 
	 * 			profile, if possible, or, if not (profile null, activity not 
	 * 			present in profile, parameter ID not present on activity), 
	 * 			assumed to be String. 
	 */
	public static Parameter getParameter(ServiceProfile profile, 
			String activityId, String id) {
		if (profile != null) {
			Activity activity = profile.getActivity(activityId);
			
			if (activity != null) {
				Parameter p = activity.getParameter(id);
				
				if (p != null) {
					//return clone of profile parameter (preserves type)
					// with given value
					return p.clone();
				}
			}
		}
		
		//fallback to superclass
		return new Parameter().withId(id);
	}
}
