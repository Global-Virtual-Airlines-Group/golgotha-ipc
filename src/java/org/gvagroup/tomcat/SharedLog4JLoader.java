// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

import org.apache.log4j.*;

/**
 * A Tomcat lifecycle listener to load Log4j.
 * @author Luke
 * @version 1.9
 * @since 1.9
 */

public class SharedLog4JLoader extends AbstractLifecycleListener {
	
	private String propFile;
	
	/**
	 * Sets the properties filename.
	 * @param fileName the properties filename
	 */
	public void setProperties(String fileName) {
		propFile = fileName;
	}

	/**
	 * Startup handler. Initializes log4j.
	 */
	@Override
	void onStartup() {
		PropertyConfigurator.configure(propFile);
	}
	
	/**
	 * Shutdown handler. Terminates log4j.
	 */
	@Override
	void onShutdown() {
		LogManager.shutdown();
	}
}