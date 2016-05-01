// Copyright 2006, 2007, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tile;

import java.awt.image.*;

/**
 * An abstract class to support common tile functions.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public abstract class AbstractTile implements Tile, java.io.Serializable {
	
	private static final long serialVersionUID = -7740652028060955205L;
	
	protected final TileAddress _addr;
	
	/**
	 * Creates a new Tile.
	 * @param addr the Tile address
	 */
	protected AbstractTile(TileAddress addr) {
		super();
		_addr = addr;
	}
	
	/**
	 * Returns the Tile address.
	 * @return the address
	 */
	@Override
	public final TileAddress getAddress() {
		return _addr;
	}
	
	/**
	 * Returns the Tile name.
	 * @return the name
	 */
	@Override
	public final String getName() {
		return _addr.getName();
	}
	
	/**
	 * Updates the Tile image.
	 * @param img the Tile image
	 */
	@Override
	public abstract void setImage(BufferedImage img);

	/**
	 * Compares two tiles by comparing their addresses.
	 */
	@Override
	public int compareTo(Tile t2) {
		return _addr.compareTo(t2.getAddress());
	}

	@Override
	public int hashCode() {
		return _addr.hashCode();
	}
}