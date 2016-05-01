// Copyright 2006, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tile;

/**
 * An interface for Tiles that do not contain raster data.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public interface CompressedTile extends Tile {

	/**
	 * Returns the compressed image data.
	 * @return the image data
	 */
	public byte[] getData();
}