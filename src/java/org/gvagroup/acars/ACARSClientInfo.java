// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.acars;

import java.util.*;

/**
 * A bean to store ACARS client build information.
 * @author Luke
 * @version 2.1
 * @since 1.1
 */

public class ACARSClientInfo {
	
	private int _latest;
	private final Map<String, Integer> _minBuilds = new TreeMap<String, Integer>();
	private final Map<Integer, Integer> _betaBuilds = new TreeMap<Integer, Integer>();
	
	/**
	 * Returns the latest ACARS client build.
	 * @return the build number
	 * @see ACARSClientInfo#setLatest(int)
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
	 * Returns the supported builds with betas.
	 * @return a Collection of builds
	 */
	public Collection<Integer> getBetas() {
		return new TreeSet<Integer>(_betaBuilds.keySet());
	}
	
	/**
	 * Returns the minimum supported build for a client version.
	 * @param ver the client version
	 * @return the build number
	 * @see ACARSClientInfo#setMinimumBuild(String, int)
	 */
	public int getMinimumBuild(String ver) {
		Integer build = _minBuilds.get(ver);
		return (build == null) ? 0 : build.intValue();
	}
	
	/**
	 * Returns the minimum supported beta release for a build.
	 * @param build the build number
	 * @return the minimum beta release
	 */
	public int getMinimumBetaBuild(int build) {
		Integer beta = _betaBuilds.get(Integer.valueOf(build));
		return (beta == null) ? 0 : beta.intValue();
	}
	
	/**
	 * Sets the latest ACARS client build number.
	 * @param build the build number
	 * @see ACARSClientInfo#getLatest()
	 */
	public void setLatest(int build) {
		_latest = build;
	}
	
	/**
	 * Sets the minimum supported build for a client version.
	 * @param ver the client version in major.minor format
	 * @param build the build number
	 * @see ACARSClientInfo#getMinimumBuild(String)
	 */
	public void setMinimumBuild(String ver, int build) {
		_minBuilds.put(ver, Integer.valueOf(build));
	}
	
	/**
	 * Sets the minimum supported beta release for a build.
	 * @param build the build number
	 * @param beta the beta number
	 */
	public void setMinimumBetaBuild(int build, int beta) {
		if (build > 0)
			_betaBuilds.put(Integer.valueOf(build), Integer.valueOf(beta));
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder("latest=");
		buf.append(_latest);
		buf.append(", min=");
		buf.append(_minBuilds.toString());
		buf.append(", beta=");
		buf.append(_betaBuilds.toString());
		return buf.toString();
	}
}