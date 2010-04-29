// Copyright 2005, 2006, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

/**
 * An exception thrown when a Connection Pool error occurs.
 * @author Luke
 * @version 1.4
 * @since 1.0
 */

public class ConnectionPoolException extends Exception {
	
    /**
     * Creates a new exception with a given message.
     * @param msg the error message
     */
    public ConnectionPoolException(String msg) {
        super(msg);
    }
    
    /**
     * Creates a new exception from an existing exception.
     * @param t the nested exception
     */
    public ConnectionPoolException(Throwable t) {
        super(t);
    }
}