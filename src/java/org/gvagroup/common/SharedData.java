// Copyright 2007, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class to store common data between web applications.
 * @author Luke
 * @version 1.4
 * @since 1.0
 */

public class SharedData {
	
	public static final String ACARS_POOL = "$acarsPool$data";
	public static final String ACARS_DAEMON = "$acarsDaemon$data";
	public static final String ACARS_CLIENT_BUILDS = "$acarsClient$data";
	
	public static final String JDBC_POOL="$jdbc$pool";
	
	public static final String MVS_POOL = "$mvsPool$data";
	public static final String MVS_DAEMON = "$mvsDaemon$data";

	private static final Logger log = Logger.getLogger(SharedData.class.getName());

	private static final Collection<String> _appNames = Collections.synchronizedSet(new LinkedHashSet<String>());
	private static final Map<String, Object> _data = new ConcurrentHashMap<String, Object>();
	private static final Map<String, ClassLoader> _loaders = new ConcurrentHashMap<String, ClassLoader>();

	// singleton
	private SharedData() {
		super();
	}
	
	/**
	 * Marks an application as running.
	 * @param appCode the application code
	 * @see SharedData#purge(String)
	 */
	public static void addApp(String appCode) {
		_appNames.add(appCode);
	}

	/**
	 * Returns the running application codes.
	 * @return the application codes
	 */
	public static Collection<String> getApplications() {
		return new LinkedHashSet<String>(_appNames);
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
	 * @param appCode the application code
	 */
	public static void purge(String appCode) {
		int objCount = 0;
		ClassLoader myLoader = Thread.currentThread().getContextClassLoader();
		for (Iterator<ClassLoader> i = _loaders.values().iterator(); i.hasNext(); ) {
			ClassLoader cl = i.next();
			if (myLoader == cl) {
				i.remove();
				objCount++;
			}
		}
		
		_appNames.remove(appCode);
		log.info("Removed " + objCount + " shared objects");
	}
	
	/**
	 * Returns a string representation of the common data store.
	 */
	public static String getData() {
		return _data.toString();
	}
}