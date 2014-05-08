// Copyright 2013, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

import java.io.*;

import org.apache.log4j.*;

/**
 * A Tomcat lifecycle listener to load Log4j.
 * @author Luke
 * @version 1.92
 * @since 1.9
 */

public class SharedLog4JLoader extends AbstractLifecycleListener {
	
	private String propFile;
	private Logger log;
	
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
		try (InputStream is = new FileInputStream(propFile)) {
			PropertyConfigurator.configure(is);
			log = Logger.getLogger(SharedLog4JLoader.class);
			log.info("Initialized shared log4j");
		} catch (IOException ie) {
			System.err.println("Cannot init log4j - " + ie.getMessage());
			ie.printStackTrace(System.err);
		}
	}
	
	/**
	 * Shutdown handler. Terminates log4j.
	 */
	@Override
	void onShutdown() {
		log.fatal("Shutting down log4j");
		LogManager.shutdown();
	}
}