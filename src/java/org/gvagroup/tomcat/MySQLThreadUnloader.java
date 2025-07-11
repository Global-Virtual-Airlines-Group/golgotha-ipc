// Copyright 2017, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

import java.lang.reflect.*;

import org.apache.logging.log4j.*;

/**
 * A Tomcat lifecycle listener to shut down the MySQL abandoned connection listener thread.
 * @author Luke
 * @version 2.60
 * @since 2.2
 */

public class MySQLThreadUnloader extends AbstractLifecycleListener {

	private static final Logger log = LogManager.getLogger(MySQLThreadUnloader.class);

	@Override
	void onStartup(boolean isAfter) {
		// empty
	}

	/**
	 * Shutdown handler. Ensures that the MySQL abandoned connection thread is shut down
	 */
	@Override
	void onShutdown(boolean isAfter) {
		if (isAfter) return;
		
		log.info("Shutting down MySQL abandoned connection thread");
		try {
			Class<?> c = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
			Method m = c.getMethod("checkedShutdown", new Class<?>[] {});
			m.invoke(null, new Object[] {});
			log.info("checkedShutdown()");
			
			// Wait for thread to die
			Field f = c.getDeclaredField("threadRef"); 
			boolean oldAccess = f.canAccess(null); f.setAccessible(true);
			Object o = f.get(null); f.setAccessible(oldAccess);
			if (o != null) {
				Thread t = (Thread) o; int totalTime = 0;
				while (t.isAlive() && (totalTime < 250)) {
					Thread.sleep(50);
					totalTime += 50;
				}
				
				if (t.isAlive()) {
					m = c.getMethod("uncheckedShutdown", new Class<?>[] {});
					m.invoke(null, new Object[] {});
					log.warn("uncheckedShutdown()");
				
					while (t.isAlive() && (totalTime < 1000)) {
						Thread.sleep(50);
						totalTime += 50;
					}
					
					if (t.isAlive())
						throw new IllegalStateException("Thread alive after uncheckedShutdown()");
				}
			}
		} catch (ClassNotFoundException cnfe) {
			log.warn("Cannot load class com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
		} catch (Exception e) {
			log.error(e.getClass().getSimpleName() + " shutting down thread - " + e.getMessage());
		}
	}
}