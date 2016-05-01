// Copyright 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tile;

import java.util.HashMap;
import java.time.Instant;

/**
 * A bean to store all tile imagery for a particular date/time.  
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class ImageSeries extends HashMap<TileAddress, PNGTile> implements Comparable<ImageSeries> {

	private static final long serialVersionUID = 7441770807204266699L;
	
	private final String _type;
	private final Instant _effDate;
	
	/**
	 * Creates the object.
	 * @param type the image type
	 * @param effDate the effective date/time
	 */
	public ImageSeries(String type, Instant effDate) {
		super();
		_type = type;
		_effDate = effDate;
	}

	/**
	 * Returns the image type.
	 * @return the image type
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * Returns the effective date/time
	 * @return the effective date/time
	 */
	public Instant getDate() {
		return _effDate;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_type).append(':');
		return buf.append(_effDate).toString();
	}
	
	@Override
	public int compareTo(ImageSeries is2) {
		int tmpResult = _effDate.compareTo(is2._effDate);
		return (tmpResult == 0) ? _type.compareTo(is2._type) : tmpResult;
	}
}