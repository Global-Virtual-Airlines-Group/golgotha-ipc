// Copyright 2013, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

import java.lang.reflect.Method;

/**
 * A Tomcat lifecycle listener to block AWT memory leaks.
 * @author Luke
 * @version 2.2
 * @since 1.9
 */

public class AWTMemoryLeakGuard extends AbstractLifecycleListener {

	/**
	 * Startup handler. Ensures that sun.awt.AppContext.getContext() has the root classloader
	 * as its context classloader. 
	 */
	@Override
	void onStartup(boolean isAfter) {
		if (isAfter) return;
		
		final Thread t = Thread.currentThread();
		final ClassLoader cl = t.getContextClassLoader(); 
		try {
			ClassLoader root = cl;
			while (root.getParent() != null)
				root = root.getParent();
			
			// Temporarily make the root class loader the active class loader
			t.setContextClassLoader(root);
			Class<?> c = Class.forName("sun.awt.AppContext");
			Method m = c.getDeclaredMethod("getAppContext", new Class<?>[] {});
			m.invoke(null, new Object[] {});
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			t.setContextClassLoader(cl);
		}
	}

	/**
	 * Shutdown handler.
	 */
	@Override
	void onShutdown(boolean isAfter) {
		// empty
	}
}