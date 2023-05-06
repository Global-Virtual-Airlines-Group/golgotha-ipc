// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

import org.apache.log4j.Logger;

/**
 * A Tomcat context listener to manage the Shared Worker thread.
 * @author Luke
 * @version 2.51
 * @since 2.40
 */

public class SharedWorkerListener extends AbstractLifecycleListener implements Thread.UncaughtExceptionHandler {
	
	private Logger log;
	private Thread _wt;

	@SuppressWarnings("preview")
	@Override
	void onStartup(boolean isAfter) {
		if (isAfter) return;
		log = Logger.getLogger(SharedWorker.class);
		
		_wt = Thread.ofVirtual().unstarted(new SharedWorker());
		_wt.setUncaughtExceptionHandler(this);
		_wt.setDaemon(true);
		_wt.start();
	}

	@Override
	void onShutdown(boolean isAfter) {
		if (isAfter) return;
		_wt.interrupt();
		try {
			_wt.join(1250);
		} catch (InterruptedException ie) {
			log.warn("Timed out waiting for SharedWorker termination");
		}
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (t != _wt) {
			log.error(String.format("Unknown thread - %s", t.getName()), e);
			return;
		}
		
		_wt = new Thread(new SharedWorker(), "SharedWorker");
		_wt.setUncaughtExceptionHandler(this);
		_wt.setDaemon(true);
		_wt.start();
		log.error(String.format("Restarted %s", t.getName()), e);
	}
}