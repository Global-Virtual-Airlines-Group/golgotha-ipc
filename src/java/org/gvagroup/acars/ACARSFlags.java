// Copyright 2004, 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.acars;

/**
 * An interface to store ACARS Flags constants.
 * @author Luke
 * @author Rahul
 * @version 5.1
 * @since 1.0
 */

public interface ACARSFlags {
   
	// Database Flags
	public static final int FLAG_PAUSED = 0x0001;
	public static final int FLAG_TOUCHDOWN = 0x0002;
	public static final int FLAG_PARKED = 0x0004;
	public static final int FLAG_ONGROUND = 0x0008;
	public static final int FLAG_PUSHBACK = 0x8000;
	public static final int FLAG_STALL = 0x10000;
	public static final int FLAG_OVERSPEED = 0x20000;
	public static final int FLAG_CRASH = 0x40000;
	
	public static final int FLAG_SPARMED = 0x0010;
	public static final int FLAG_GEARDOWN = 0x0020;
	public static final int FLAG_AFTERBURNER = 0x0040;
	public static final int FLAG_REVERSETHRUST = 0x80000;
	
	public static final int FLAG_AP_GPS = 0x0100;
	public static final int FLAG_AP_NAV = 0x0200;
	public static final int FLAG_AP_HDG = 0x0400;
	public static final int FLAG_AP_APR = 0x0800;
	public static final int FLAG_AP_ALT = 0x1000;
	public static final int FLAG_AP_LNAV = 0x100000;
	public static final int FLAG_AP_ANY = 0x101F00;
	
	public static final int FLAG_AT_IAS = 0x2000;
	public static final int FLAG_AT_MACH = 0x4000;
	public static final int FLAG_AT_VNAV = 0x80000;
	public static final int FLAG_AT_ANY = 0x6000;
}