// Copyright 2013, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

import org.apache.catalina.*;

/**
 * An abstract class to handle Tomcat lifecycle events.
 * @author Luke
 * @version 2.2
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
			onStartup(false);
		else if (Lifecycle.AFTER_START_EVENT.equals(t))
			onStartup(true);
		else if (Lifecycle.AFTER_STOP_EVENT.equals(t))
			onShutdown(false);
		else if (Lifecycle.BEFORE_DESTROY_EVENT.equals(t))
			onShutdown(true);
	}

	/**
	 * The startup handler.
	 * @param isAfter TRUE if after startup, FALSE if before
	 */
	abstract void onStartup(boolean isAfter);
		
	/**
	 * The shutdown handler.
	 * @param isAfter TRUE if after startup, FALSE if before
	 */
	abstract void onShutdown(boolean isAfter);
}