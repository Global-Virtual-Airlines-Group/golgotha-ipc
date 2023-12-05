// Copyright 2007, 2010, 2011, 2013, 2016, 2017, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

import java.util.*;
import java.io.Serializable;

import org.apache.logging.log4j.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class to store common data between web applications.
 * @author Luke
 * @version 2.65
 * @since 1.0
 */

public class SharedData {
	
	public static final String ACARS_POOL = "$acarsPool$data";
	public static final String ACARS_DAEMON = "$acarsDaemon$data";
	public static final String ACARS_CACHEINFO = "$acarsDaemon$cacheInfo";
	public static final String ACARS_CMDSTATS = "$acarsDaemon$cmdStats";
	
	public static final String JDBC_POOL="$jdbc$pool";
	public static final String FB_CREDS = "$fb$creds"; 
	public static final String ECON_DATA = "$econ$master"; 
	
	public static final String ELITE_INFO = "$elite$info";

	private static final Logger log = LogManager.getLogger(SharedData.class.getName());

	private static final Collection<String> _appNames = Collections.synchronizedSet(new LinkedHashSet<String>());
	private static final Map<String, Serializable> _data = new ConcurrentHashMap<String, Serializable>();
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
	public static void addData(String key, Serializable value) {
		ClassLoader myLoader = Thread.currentThread().getContextClassLoader();
		ClassLoader cl = _loaders.get(key);
		if ((cl != null) && (cl != myLoader))
			log.warn("Shared data {} already loaded by {}", key, cl);
				
		_data.put(key, value);
		_loaders.put(key, myLoader);
	}
	
	/**
	 * Retrieves a shared data element.
	 * @param key the object key
	 * @return the object, or null if not found
	 */
	public static Serializable get(String key) {
		return _data.get(key);
	}
	
	/**
	 * Purges classloader entries from the current classloader, when a web application is reloaded.
	 * @param appCode the application code
	 */
	public static synchronized void purge(String appCode) {
		int objCount = 0;
		ClassLoader myLoader = Thread.currentThread().getContextClassLoader();
		for (Iterator<Map.Entry<String, ClassLoader>> i = _loaders.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<String, ClassLoader> me = i.next();
			if (myLoader == me.getValue()) {
				if (_data.remove(me.getKey()) == null)
					log.warn("Unable to remove data for {}", me.getKey());
					
				i.remove();
				objCount++;
			}
		}
		
		_appNames.remove(appCode);
		log.info("Removed {} shared objects", Integer.valueOf(objCount));
	}
	
	/**
	 * Returns a string representation of the common data store.
	 * @return the data toString()
	 */
	public static String getData() {
		return _data.toString();
	}
}