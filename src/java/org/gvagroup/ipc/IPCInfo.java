// Copyright 2007, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.ipc;

import java.util.Collection;

/**
 * An interface to define classes that can dump info via serialization.
 * @author Luke
 * @version 2.10
 * @since 1.0
 * @param <T> the serialized output type
 */

public interface IPCInfo<T extends java.io.Serializable> {

	/**
	 * Returns the data in a serialized fashion, suitable for transfer between virtual machines and class loaders. Each element
	 * is a byte array which can be fed into an ObjectInputStream for deserialization.
	 * @return a Collection of byte arrays
	 */
	public Collection<byte[]> getSerializedInfo();
}