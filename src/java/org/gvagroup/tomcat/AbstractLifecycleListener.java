// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

import org.apache.catalina.*;

/**
 * An abstract class to handle Tomcat lifecycle events.
 * @author Luke
 * @version 1.9
 * @since 1.9
 */

abstract class AbstractLifecycleListener implements LifecycleListener {

	/**
	 * Tomcat lifecycle event handler.
	 * @param e the LifecycleEvent
	 */
	@Override
	public final void lifecycleEvent(LifecycleEvent e) {
		String t = e.getType();
		if (Lifecycle.BEFORE_START_EVENT.equals(t))
			onStartup();
		else if (Lifecycle.AFTER_STOP_EVENT.equals(t))
			onShutdown();
	}

	/**
	 * The startup handler.
	 */
	abstract void onStartup();
		
	/**
	 * The shutdown handler.
	 */
	abstract void onShutdown();
}