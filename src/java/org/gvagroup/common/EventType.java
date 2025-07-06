// Copyright 2021, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

/**
 * An enumeration of IPC Event types.
 * @version 3.11
 * @since 1.0
 */

public enum EventType {
	AIRPORT_RELOAD, AIRLINE_RELOAD, USER_SUSPEND, USER_INVALIDATE, TZ_RELOAD, CACHE_FLUSH, CACHE_STATS, AIRPORT_RENAME, AIRCRAFT_RENAME, FLIGHT_REPORT;
}