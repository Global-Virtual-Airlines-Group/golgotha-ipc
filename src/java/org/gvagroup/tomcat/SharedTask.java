// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

/**
 * An interface for SharedWorker tasks.
 * @author Luke
 * @version 2.40
 * @since 2.40
 */

public interface SharedTask extends java.io.Serializable {
	
	/**
	 * Returns the execution interval.
	 * @return the execution interval in milliseconds
	 */
	public int getInterval();
	
	/**
	 * Returns if the task has been marked for termiantion.
	 * @return TRUE if stopped, otherwise FALSE
	 */
	public boolean isStopped();
	
	/**
	 * Prevents the task from executing further.
	 */
	public void stop();
	
	/**
	 * Executes the Task.
	 */
	public void execute();
}