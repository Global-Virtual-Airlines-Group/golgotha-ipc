// Copyright 2007, 2008, 2013, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

import java.util.*;
import java.util.concurrent.*;

/**
 * A utility class to dispatch events between web applications.
 * @author Luke
 * @version 1.95
 * @since 1.0
 */

public class EventDispatcher {
	
	private static final Map<Thread, Queue<SystemEvent>> _events = new HashMap<Thread, Queue<SystemEvent>>();
	
	// singleton
	private EventDispatcher() {
		super();
	}

	/**
	 * Registers a thread to listen for system events.
	 * @throws InterruptedException if the thread is interrupted while waiting for the event
	 */
	public synchronized static void waitForEvent() throws InterruptedException {
		if (!_events.containsKey(Thread.currentThread()))
			_events.put(Thread.currentThread(), new ConcurrentLinkedQueue<SystemEvent>());
		
		// Wait for an event
		EventDispatcher.class.wait();
	}

	/**
	 * Notifies waiting threads of a particular system event.
	 * @param e the event to send
	 */
	public synchronized static void send(SystemEvent e) {
		for (Iterator<Map.Entry<Thread, Queue<SystemEvent>>> i = _events.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<Thread, Queue<SystemEvent>> me = i.next();
			if (me.getKey().isAlive())
				me.getValue().add(e);
			else
				i.remove();
		}
		
		// Notify threads
		EventDispatcher.class.notifyAll();
	}
	
	/**
	 * Retrieves any pending events for the current Thread.
	 * @return a Collection of SystemEvents
	 */
	public static synchronized Collection<SystemEvent> getEvents() {
		Queue<SystemEvent> events = _events.get(Thread.currentThread());
		if (events == null)
			return Collections.emptySet();
		
		// Get our events and reset the list
		Collection<SystemEvent> results = new ArrayList<SystemEvent>(events);
		events.clear();
		return results;
	}
	
	public static synchronized void unregister() {
		_events.remove(Thread.currentThread());
	}
	
	/**
	 * Shuts down the dispatcher.
	 */
	public static synchronized void shutDown() {
		for (Iterator<Map.Entry<Thread, Queue<SystemEvent>>> i = _events.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<Thread, Queue<SystemEvent>> me = i.next();
			if (me.getKey().isAlive())
				me.getKey().interrupt();
			
			me.getValue().clear();
			i.remove();
		}
	}
}