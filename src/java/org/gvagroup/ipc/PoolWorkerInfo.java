// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.ipc;

import java.util.Collection;

/**
 * An interface to allow multi-threaded servers to return worker thread information.
 * @author Luke
 * @version 1.4
 * @since 1.4
 */

public interface PoolWorkerInfo {

	/**
	 * Returns information about the worker threads.
	 * @return a Collection of WorkerStatus beans
	 */
	public Collection<WorkerStatus> getWorkers();
}