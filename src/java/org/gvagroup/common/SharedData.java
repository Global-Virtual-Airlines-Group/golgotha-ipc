// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class to store common data between web applications.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SharedData {
	
	public static final String ACARS_POOL = "$acarsPool$data";
	
	public static final String ACARS_DAEMON = "$acarsDaemon$data";
	
	private static final Logger log = Logger.getLogger(SharedData.class.getName());
	
	private static final Map<String, Object> _data = new ConcurrentHashMap<String, Object>();
	private static final Map<String, ClassLoader> _loaders = new ConcurrentHashMap<String, ClassLoader>();

	// singleton
	private SharedData() {
		super();
	}
	
	/**
	 * Shares a data element.
	 * @param key the element ID
	 * @param value the element
	 */
	public static void addData(String key, Object value) {
		ClassLoader myLoader = Thread.currentThread().getContextClassLoader();
		ClassLoader cl = _loaders.get(key);
		if ((cl != null) && (cl != myLoader))
			log.warning("Shared data " + key + " already loaded by " + cl.toString());
				
		_data.put(key, value);
		_loaders.put(key, myLoader);
	}
	
	/**
	 * Retrieves a shared data element.
	 * @param key the object key
	 * @return the object, or null if not found
	 */
	public static Object get(String key) {
		return _data.get(key);
	}
	
	/**
	 * Purges classloader entries from the current classloader, when a web application is reloaded.
	 */
	public static void purge() {
		int objCount = 0;
		ClassLoader myLoader = Thread.currentThread().getContextClassLoader();
		for (Iterator<ClassLoader> i = _loaders.values().iterator(); i.hasNext(); ) {
			ClassLoader cl = i.next();
			if (myLoader == cl) {
				i.remove();
				objCount++;
			}
		}
		
		log.info("Removed " + objCount + " shared objects");
	}
	
	/**
	 * Returns a string representation of the common data store.
	 */
	public static String getData() {
		return _data.toString();
	}
}