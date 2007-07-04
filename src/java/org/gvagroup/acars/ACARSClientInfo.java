// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.acars;

import java.util.*;

/**
 * A bean to store ACARS client build information.
 * @author Luke
 * @version 1.1
 * @since 1.1
 */

public class ACARSClientInfo {
	
	private int _latest;
	private final Map<String, Integer> _minBuilds = new TreeMap<String, Integer>();
	
	/**
	 * Returns the latest ACARS client build.
	 * @return the build number
	 */
	public int getLatest() {
		return _latest;
	}
	
	/**
	 * Returns the supported ACARS client versions.
	 * @return a Collection of versions
	 */
	public Collection<String> getVersions() {
		return new LinkedHashSet<String>(_minBuilds.keySet());
	}
	
	/**
	 * Returns the minimum supported build for a client version.
	 * @param ver the client version
	 * @return the build number
	 */
	public int getMinimumBuild(String ver) {
		Integer build = _minBuilds.get(ver);
		return (build == null) ? 0 : build.intValue();
	}
	
	/**
	 * Sets the latest ACARS client build number.
	 * @param build the build number
	 */
	public void setLatest(int build) {
		_latest = build;
	}
	
	/**
	 * Sets the minimum supported build for a client version.
	 * @param ver the client version in major.minor format
	 * @param build the build number
	 */
	public void setMinimumBuild(String ver, int build) {
		_minBuilds.put(ver, new Integer(build));
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder("latest=");
		buf.append(_latest);
		buf.append(", min=");
		buf.append(_minBuilds.toString());
		return buf.toString();
	}
}