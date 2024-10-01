// Copyright 2021, 2022, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import java.io.*;
import java.net.Socket;

import org.newsclub.net.unix.*;

import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * A Jedis socket factory to create domain socket connections.  
 * @author Luke
 * @version 11.3
 * @since 10.0
 */

public class JedisDomainSocketFactory implements JedisSocketFactory {
	
	private final String _socketFile;

	/**
	 * Creates the socket factory.
	 * @param addr the domain socket path
	 */
	public JedisDomainSocketFactory(String addr) {
		_socketFile = addr;
	}
	
	/**
	 * Creates a socket to the given endpoint.
	 * @throws JedisConnectionException if the socket cannot be found or connected to
	 */
	@Override
	public Socket createSocket() throws JedisConnectionException {
		try {
			File f = new File(_socketFile);
			if (!f.exists())
				throw new FileNotFoundException(f.getAbsolutePath());
		
			AFUNIXSocket sock = AFUNIXSocket.newInstance();
			sock.connect(AFUNIXSocketAddress.of(f));
			return sock;
		} catch (IOException ie) {
			throw new JedisConnectionException(ie);
		}
	}
}