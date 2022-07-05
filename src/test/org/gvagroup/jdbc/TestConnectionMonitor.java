package org.gvagroup.jdbc;

import java.util.ArrayList;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestConnectionMonitor extends TestCase {

    private ConnectionMonitor _cm;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _cm = new ConnectionMonitor("test", 3, new ConnectionPool(1, "test"));
    }

    @Override
    protected void tearDown() throws Exception {
        _cm = null;
        super.tearDown();
    }

    public void testBasicProperties() {
        assertEquals(0, _cm.size());

        ArrayList<Object> pool1 = new ArrayList<Object>();
        pool1.add(new Object());
        assertEquals(pool1.size(), _cm.size());
        
        // Check to ensure that we have cloned pool1
        pool1.clear();
        assertEquals(0, pool1.size());
        assertEquals(1, _cm.size());
    }
    
    public void testMultiplePools() {
        ArrayList<Object> pool1 = new ArrayList<Object>();
        pool1.add(new Object());
        
        ArrayList<Object> pool2 = new ArrayList<Object>();
        pool2.add(new Object());
        pool2.add(new Object());
    }
}