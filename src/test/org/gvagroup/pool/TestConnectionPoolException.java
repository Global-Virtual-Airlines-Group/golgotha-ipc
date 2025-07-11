package org.gvagroup.pool;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestConnectionPoolException extends TestCase {
    
    public void testMessage() {
        try {
            throw new ConnectionPoolException("MSG");
        } catch (ConnectionPoolException cpe) {
            assertEquals("MSG", cpe.getMessage());
        }
    }
    
	public void testCause() {
        Exception e = new NullPointerException();
        
        try {
            throw new ConnectionPoolException(e);
        } catch (ConnectionPoolException cpe) {
            assertEquals(e.getMessage(), cpe.getMessage());
            assertEquals(e, cpe.getCause());
        }
    }
}