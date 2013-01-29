// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class ResourceBundleFactory {

	private static Map<String, ResourceBundle> map = new HashMap<String, ResourceBundle>();

	public static Object getProperty(String resourceBundle, String key) {
		checkAndLoad(resourceBundle);
		return map.get(resourceBundle).getObject(key);
	}
	
	public static String getString(String resourceBundle, String key) {
		checkAndLoad(resourceBundle);
		return map.get(resourceBundle).getString(key);
	}
	
	public static boolean containsKey(String resourceBundle, String key) {
		checkAndLoad(resourceBundle);
		return map.get(resourceBundle).containsKey(key);
	}
	
	public static Set<String> keySet(String resourceBundle) {
		checkAndLoad(resourceBundle);
		return map.get(resourceBundle).keySet();
	}

	private static void checkAndLoad(String resourceBundle) {
		if (!map.containsKey(resourceBundle)) {
			map.put(resourceBundle, ResourceBundle.getBundle(resourceBundle));
		}

		//the file name should be specific and end with the resourceBundle: clearResourceBundle
		File file = new File("clearCache");
		if (file.exists()) {
			ResourceBundle.clearCache();
			map.remove(resourceBundle);
			map.put(resourceBundle, ResourceBundle.getBundle(resourceBundle));
			file.delete();
		}
	}	

}
