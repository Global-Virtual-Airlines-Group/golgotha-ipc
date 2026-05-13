// Copyright 2022, 2023, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

import org.apache.logging.log4j.*;

/**
 * A Tomcat context listener to manage the Shared Worker thread.
 * @author Luke
 * @version 3.15
 * @since 2.40
 */

public class SharedWorkerListener extends AbstractLifecycleListener implements Thread.UncaughtExceptionHandler {
	
	private static final String THREAD_NAME = "Golgotha Shared Worker";
	
	private Logger log;
	private Thread _wt;

	@Override
	void onStartup(boolean isAfter) {
		if (isAfter) return;
		log = LogManager.getLogger(SharedWorker.class);
		_wt = createThread();
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
	
	private Thread createThread() {
		Thread t = Thread.ofVirtual().name(THREAD_NAME).unstarted(new SharedWorker());
		t.setUncaughtExceptionHandler(this);
		t.setDaemon(true);
		return t;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (t != _wt) {
			log.atError().withThrowable(e).log("Unknown thread - {}", t.getName());
			return;
		}
		
		_wt = createThread();
		log.atError().withThrowable(e).log("Restarted {}", t.getName());
	}
}