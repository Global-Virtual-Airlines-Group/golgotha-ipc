// Copyright 2006, 2007, 2012, 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.tile;

import java.util.*;

/**
 * A class to store quadtree Tile addresses.
 * @author Luke
 * @version 2.61
 * @since 2.1
 */

public class TileAddress implements  java.io.Serializable, Comparable<TileAddress> {

	private static final long serialVersionUID = 33734107647171797L;

	private static final int[] MASKS = { 0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288 };
	
	private final int _x;
	private final int _y;
	private final int _level;

	/**
	 * Creates a new Tile Address from cartesian coordinates.
	 * @param x the zero-offset X coordinate
	 * @param y the zero-offset Y coordinate
	 * @param level the level
	 */
	public TileAddress(int x, int y, int level) {
		super();
		_x = x;
		_y = y;
		_level = level;
	}
	
	/**
	 * Creates a new Tile Address from a tile filename.
	 * @param name the tile filename
	 * @throws NullPointerException if name is null
	 */
	public TileAddress(String name) {
		super();
		String n = (name.indexOf('.') != -1) ? name.substring(0, name.indexOf('.')) : name; 

		// Parse the numbers
		_level = n.length(); int X = 0; int Y = 0;
		for (int x = 0; x < _level; x++) {
			switch (n.charAt(x)) {
				case '1':
					X += MASKS[_level - x];
					break;

				case '2':
					Y += MASKS[_level - x];
					break;

				case '3':
					X += MASKS[_level - x];
					Y += MASKS[_level - x];
					break;
					
				case '0':
				default:
					break;
			}
		}
		
		_x = X; _y = Y;
	}
	
	/**
	 * Creates a new Tile Address from pixel coordinates.
	 * @param px the zero-offset X coordinate
	 * @param py the zero-offset Y coordinate
	 * @param zoom the zoom level
	 * @return the new TileAddress
	 */
	public static TileAddress fromPixel(int px, int py, int zoom) {
		return new TileAddress(px / Tile.WIDTH, py / Tile.HEIGHT, zoom);
	}

	/**
	 * Returns the Tile level.
	 * @return the level
	 */
	public int getLevel() {
		return _level;
	}

	/**
	 * Returns the X-coordinate of this Tile.
	 * @return the zero-offset X coordinate
	 */
	public int getX() {
		return _x;
	}
	
	/**
	 * Returns the Y-coordinate of this Tile.
	 * @return the zero-offset Y coordinate
	 */
	public int getY() {
		return _y;
	}
	
	/**
	 * Returns the X-coordinate of this tile within the global image.
	 * @return the zero-offset pixel X coordinate
	 */
	public int getPixelX() {
		return _x * Tile.WIDTH;
	}
	
	/**
	 * Returns the Y-coordinate of this tile within the global image.
	 * @return the zero-offset pixel Y coordinate
	 */
	public int getPixelY() {
		return _y * Tile.HEIGHT;
	}

	/**
	 * Returns the base filename of this Tile.
	 * @return the base filename
	 */
	public String getName() {
		final StringBuilder buf = new StringBuilder();
		for (int i = _level; i > 0; i--) {
			int digit1 = ((MASKS[i] & _x) == 0) ? 0 : 1;
			int digit2 = ((MASKS[i] & _y) == 0) ? 0 : 2;
			buf.append(digit1 + digit2);
		}

		return buf.toString();
	}
	
	/**
	 * Returns the coordinates and level of the Tile.
	 */
	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder("(");
		buf.append(_x);
		buf.append(',');
		buf.append(_y);
		buf.append(',');
		buf.append(_level);
		buf.append(')');
		return buf.toString();
	}
	
	/**
	 * Compares two addresses by comparing their levels, Y and X coordinates.
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(TileAddress addr2) {
		// Compare the level
		int tmpResult = Integer.compare(_level, addr2._level) * -1;
		if (tmpResult != 0)
			return tmpResult;
		
		// Compare the Y coordinates
		tmpResult = Integer.compare(_y, addr2._y);
		if (tmpResult != 0)
			return tmpResult;
		
		// Compare the X coordinates
		return Integer.compare(_x, addr2._x);
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof TileAddress ta2) && (compareTo(ta2) == 0);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	/**
	 * Returns this Tile's parent address.
	 * @return the Tile's parent address
	 * @see TileAddress#getParents()
	 */
	public TileAddress getParent() {
		return (_level < 1) ? null : new TileAddress(_x >> 1, _y >> 1, _level - 1);
	}
	
	/**
	 * Returns the addresses of all of this Tile's parents.
	 * @return a Collection of TileAddress beans
	 * @see TileAddress#getParent()
	 * @see TileAddress#getChildren()
	 */
	public Collection<TileAddress> getParents() {
		Collection<TileAddress> results = new ArrayList<TileAddress>(_level - 1);
		
		// Work our way backwards
		TileAddress parent = getParent();
		while (parent != null) {
			results.add(parent);
			parent = parent.getParent();
		}
		
		return results;
	}
	
	/**
	 * Returns the addresses of all of this Tile's children.
	 * @return a Collection of TileAddress beans
	 * @see TileAddress#getParent()
	 */
	public Collection<TileAddress> getChildren() {
		return getChildren(_level + 1);
	}
	
	/**
	 * Returns the addresses of all of this Tile's descendents at a particular zoom level.
	 * @param level the new zoom level
	 * @return a Collection of TileAddress beans
	 */
	public Collection<TileAddress> getChildren(int level) {
		int levelChange = (level - _level);
		if (levelChange < 1)
			return Collections.emptyList();
		
		// Build the base address and number of descendants in tile
		int baseX = _x << levelChange;
		int baseY = _y << levelChange;
		int childTiles = 1 << levelChange;
		
		// Build the addresses
		Collection<TileAddress> results = new ArrayList<TileAddress>(childTiles);
		for (int x = baseX; x < (baseX + childTiles); x++) {
			for (int y = baseY; y < (baseY + childTiles); y++)
				results.add(new TileAddress(x, y, level));
		}
		
		return results;
	}
	
	/**
	 * Returns the address of this Tile when zooming to another level. If zooming to a higher level, then the address
	 * of the top-left child will be returned.
	 * @param newLevel the new zoom level
	 * @return the Address of the tile
	 */
	public TileAddress zoomTo(int newLevel) {
		if (newLevel < 1)
			throw new IllegalArgumentException("Invalid Zoom level - " + newLevel);
		
		// Convert the address
		if (newLevel < _level) {
			int levelChange = (_level - newLevel);
			return new TileAddress(_x >> levelChange, _y >> levelChange, newLevel);
		} else if (newLevel > _level) {
			int levelChange = (newLevel - _level);
			return new TileAddress(_x << levelChange, _y << levelChange, newLevel);
		} else
			return new TileAddress(_x, _y, _level);
	}
}