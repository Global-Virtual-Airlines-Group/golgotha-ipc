package org.gvagroup.pool;

import java.io.*;
import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

public class TestConnectionPoolEntry extends TestCase {

    private Connection _c;
    private JDBCPoolEntry _cpe;
    private Properties _props;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _props = new Properties();
        _props.load(new FileInputStream("data/jdbc.properties"));
        Class.forName(_props.getProperty("driver"));
        _cpe = new JDBCPoolEntry(1, _props);
        _cpe.connect();
        _c = _cpe.getConnection();
    }

    @Override
    protected void tearDown() throws Exception {
        _cpe.close();
        _c = null;
        _cpe = null;
        _props = null;
        super.tearDown();
    }

    public void testProperties() throws Exception {
        assertFalse(_cpe.inUse());
        assertEquals(1, _cpe.getID());
        assertEquals(_c.hashCode(), _cpe.hashCode());
        assertSame(_c, _cpe.getConnection());
        
        assertFalse(_cpe.isDynamic());
        _cpe.setDynamic(true);
        assertTrue(_cpe.isDynamic());
        _cpe.setDynamic(false);
        assertFalse(_cpe.isDynamic());
        
        Connection c2 = _cpe.reserve(false);
        assertSame(c2, _c);
        assertTrue(_cpe.inUse());
        Thread.sleep(100);
        _cpe.free();
        _cpe.free(); // Should not fail
        
        long useTime = _cpe.getUseTime();
        assertEquals(useTime, _cpe.getTotalUseTime());
    }
    
    public void testSystemConnection() {
        assertFalse(_cpe.inUse());
        assertTrue(_cpe.isDynamic());
        _cpe.setDynamic(false);
        assertTrue(_cpe.isDynamic());
    }
    
    public void testReconnection() throws SQLException {
        _cpe.close();
        _cpe.connect();
        try {
            _cpe.connect();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) {
        	// empty
        }
    }
    
    public void testCloseException() {
    	JDBCPoolEntry cpe2 = new JDBCPoolEntry(2, _props);
        cpe2.close();
    }
    
    @SuppressWarnings("unlikely-arg-type")
	public void testEquality() throws SQLException {
        try (Connection c2 = DriverManager.getConnection(_props.getProperty("url"), _props)) {
        	assertTrue(_cpe.equals(_c));
        	assertFalse(_cpe.equals(c2));
        	assertFalse(_cpe.equals(new Object()));
        	assertFalse(_cpe.equals(null));
        }
    }
    
    public void testIndexOf() {
       JDBCPoolEntry cpe2 = new JDBCPoolEntry(2, _props);
       List<JDBCPoolEntry> l = new ArrayList<JDBCPoolEntry>();
       l.add(_cpe);
       l.add(cpe2);
       assertEquals(0, l.indexOf(_cpe));
       assertEquals(1, l.indexOf(cpe2));
    }
    
    public void testValidation() throws Exception {
        _cpe.reserve(false);
        try {
            _cpe.reserve(false);
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) {
        	// empty
        }
        
        _cpe.free();
    }
}