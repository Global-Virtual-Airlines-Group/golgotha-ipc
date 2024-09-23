package org.gvagroup.pool;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import junit.framework.TestCase;

public class TestConnectionPool extends TestCase {

    private JDBCPool _pool;
    private Properties _props;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _props = new Properties();
        _props.load(new FileInputStream("data/jdbc.properties"));
        _pool = new JDBCPool(2, "test");
    }

    @Override
    protected void tearDown() throws Exception {
        _pool.close();
        super.tearDown();
    }
    
    public void testProperties() throws ClassNotFoundException {
        assertEquals(0, _pool.getSize());
        assertEquals(2, _pool.getMaxSize());
        _pool.setProperties(_props);
        _pool.setCredentials(_props.getProperty("user"), _props.getProperty("password"));
        _pool.setDriver(_props.getProperty("driver"));
    }
    
    public void testValidation() throws Exception {
        try {
            _pool.setDriver("java.foo.bar");
            fail("ClassNotFoundException expected");
        } catch (ClassNotFoundException cnfe) { 
        	// empty
        }
        
        try {
            _pool.connect(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
        	// empty
        }
        
        try {
            _pool.connect(4);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
        	// empty
        }
    }
    
    @SuppressWarnings("static-method")
    public void testSemaphore() {
    	Semaphore lock = new Semaphore(1, true);
    	assertTrue(lock.tryAcquire());
    	assertFalse(lock.tryAcquire());
    	lock.release();
    	assertTrue(lock.tryAcquire());
    	lock.release();
    	lock.release();
    }
    
    @SuppressWarnings("resource")
	public void testConnections() throws Exception {
        _pool.setProperties(_props);
        _pool.connect(1);
        assertEquals(1, _pool.getSize());
        Connection c1 = _pool.getConnection();
        assertNotNull(c1);
        Connection c2 = _pool.getConnection();
        assertEquals(2, _pool.getSize());
        assertEquals(_pool.getMaxSize(), _pool.getSize());
        try {
            Connection c3 = _pool.getConnection();
            assertNotNull(c3);
            fail("ConectionPoolException expected");
        } catch (ConnectionPoolException cpe) {
        	// empty
        }
        
        _pool.release(c2);
        assertEquals(2, _pool.getSize());
        Connection c3 = _pool.getConnection();
        c3.close();
        
        _pool.release(c3);
        _pool.close();
    }
}