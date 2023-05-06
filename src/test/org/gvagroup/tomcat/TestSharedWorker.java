package org.gvagroup.tomcat;

import java.io.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestSharedWorker extends TestCase implements Thread.UncaughtExceptionHandler {
	
	private Thread _wt;

	@SuppressWarnings("preview")
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Init Log4J
		try (InputStream is = new FileInputStream("data/log4j.test.properties")) {
			PropertyConfigurator.configure(is);
		}
		
		// Start the worker thread
		_wt = Thread.ofVirtual().unstarted(new SharedWorker());
		_wt.setUncaughtExceptionHandler(this);
		_wt.setDaemon(true);
		_wt.start();
		assertTrue(_wt.isAlive());
	}

	@Override
	protected void tearDown() throws Exception {
		_wt.interrupt();
		_wt.join(1250);
		assertFalse(_wt.isAlive());
		LogManager.shutdown();
		super.tearDown();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		throw new RuntimeException(e);
	}
	
	public void testExecution() throws Exception {
		
		DummyTask dt = new DummyTask(50, 5000);
		SharedWorker.register(dt);
		
		Thread.sleep(4000);
		assertEquals(0, dt.getExecCount());
		
		Thread.sleep(1600);
		assertEquals(1, dt.getExecCount());
		
		Thread.sleep(dt.getInterval());
		assertEquals(2, dt.getExecCount());
		
		dt.stop();
		assertTrue(dt.isStopped());
	}
	
	public void testStop() throws Exception {
		
		DummyTask dt = new DummyTask(250, 2500);
		SharedWorker.register(dt);
		
		Thread.sleep(2000);
		assertEquals(0, dt.getExecCount());
		
		Thread.sleep(1000);
		assertEquals(1, dt.getExecCount());
		
		dt.stop();
		assertTrue(dt.isStopped());
		Thread.sleep(dt.getInterval());
		
		assertEquals(1, dt.getExecCount());
	}
	
	public void testExecWarning() throws Exception {
		
		DummyTask dt = new DummyTask(2000, 3000);
		SharedWorker.register(dt);
		
		Thread.sleep(2750);
		assertEquals(0, dt.getExecCount());
		
		Thread.sleep(2000);
		assertEquals(0, dt.getExecCount());
		
		Thread.sleep(500);
		assertEquals(1, dt.getExecCount());

		dt.stop();
		assertTrue(dt.isStopped());
	}
}