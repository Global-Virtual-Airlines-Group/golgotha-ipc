// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.*;

/**
 * A shared worker thread to schedule low-intensity, regular jobs.
 * @author Luke
 * @version 2.60
 * @since 2.40
 */

public class SharedWorker implements Runnable {
	
	private static final Logger log = LogManager.getLogger(SharedWorker.class);
	
	private static final BlockingQueue<QueueEntry> _tasks = new DelayQueue<QueueEntry>();
	private static final Random _rnd = new Random();

	private static class QueueEntry implements Delayed, java.io.Serializable {
		private static final long serialVersionUID = -4506885278758091086L;
		
		private final SharedTask _t;
		private long _nextExec;
		private int _execCount;
		
		QueueEntry(SharedTask t) {
			super();
			_t = t;
			_nextExec = System.currentTimeMillis() + t.getInterval() + _rnd.nextInt(t.getInterval() / 10);
		}
		
		SharedTask getTask() {
			return _t;
		}
		
		public int getExecutionCount() {
			return _execCount;
		}
		
		void reset() {
			_nextExec = System.currentTimeMillis() + _t.getInterval();
			_execCount++;
		}
		
		@Override
		public int compareTo(Delayed d2) {
			return Long.compare(getDelay(TimeUnit.MILLISECONDS), d2.getDelay(TimeUnit.MILLISECONDS));
		}

		@Override
		public long getDelay(TimeUnit unit) {
			long execDelta = _nextExec - System.currentTimeMillis();
			return unit.convert(execDelta, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * Registers a shared task for periodic execution.
	 * @param t a SharedTask
	 */
	public static void register(SharedTask t) {
		_tasks.add(new QueueEntry(t));
	}
	
	/**
	 * Removes all tasks from a given classloader from the queue.
	 * @param cl the ClassLoader
	 */
	public static void clear(ClassLoader cl) {
		for (Iterator<QueueEntry> i = _tasks.iterator(); i.hasNext(); ) {
			SharedTask t = i.next().getTask();
			if (t.isStopped() || (t.getClass().getClassLoader() == cl)) {
				log.info(String.format("Removed task %s", t));
				i.remove();
			}
		}
	}
	
	/*
	 * Helper method to execute the task if applicable.
	 */
	private static void executeTask(QueueEntry e) {
		final SharedTask t = e.getTask();
		if (t.isStopped()) {
			log.warn(String.format("%s stopped", t));
			return;
		}
		
		if (log.isDebugEnabled()) log.debug(String.format("Executing %s - #%d", t, Integer.valueOf(e.getExecutionCount())));
		long tt = System.currentTimeMillis();
		try {
			t.execute();
			e.reset();
			_tasks.add(e);
		} catch (Exception ex) {
			log.error(String.format("%s executing %s", ex.getMessage(), t), ex);
		} finally {
			long execTime = System.currentTimeMillis() - tt;
			if (execTime > 2500)
				log.warn(String.format("Execess execution time for %s - %d", t, Long.valueOf(execTime)));
		}
	}
	
	@Override
	public void run() {
		log.info("Starting");
		while (!Thread.currentThread().isInterrupted()) {
			QueueEntry qe = null;
			try {
				qe = _tasks.poll(20, TimeUnit.SECONDS);
				if (qe != null)
					executeTask(qe);
			} catch (InterruptedException ie) {
				log.info("Interrupted");
				Thread.currentThread().interrupt();
			}
		}
		
		_tasks.clear();
		log.info("Stopped");
	}
}