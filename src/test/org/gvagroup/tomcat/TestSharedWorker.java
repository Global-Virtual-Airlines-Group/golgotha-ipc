package org.gvagroup.tomcat;

import java.io.File;

import junit.framework.TestCase;

public class TestSharedWorker extends TestCase implements Thread.UncaughtExceptionHandler {
	
	private Thread _wt;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Setup log4j
		File f = new File("data/log4j2-test.xml");
		assertTrue(f.exists());
		System.setProperty("log4j2.configurationFile", f.getAbsolutePath());

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
		
		SharedWorker.clear(this.getClass().getClassLoader());
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
		
		SharedWorker.clear(this.getClass().getClassLoader());
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
		
		SharedWorker.clear(this.getClass().getClassLoader());
	}
}