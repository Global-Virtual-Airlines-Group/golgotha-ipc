// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tomcat;

public class DummyTask implements SharedTask {
	private static final long serialVersionUID = -5282264848894446765L;
	
	private final int _execTime;
	private final int _execInterval;
	
	private int _execCount;
	private boolean _isStopped;

	DummyTask(int execTime, int execInterval) {
		super();
		_execTime = Math.max(0, execTime);
		_execInterval = Math.max(500, execInterval);
	}

	@Override
	public int getInterval() {
		return _execInterval;
	}

	@Override
	public boolean isStopped() {
		return _isStopped;
	}
	
	int getExecCount() {
		return _execCount;
	}

	@Override
	public void stop() {
		_isStopped = true;
	}

	@Override
	public void execute() {
		try {
			Thread.sleep(_execTime);
			_execCount++;
		} catch (InterruptedException ie) {
			System.err.println("Interrupted!");
		}
	}
}