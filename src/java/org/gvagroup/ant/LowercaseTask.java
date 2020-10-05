// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.ant;

import org.apache.tools.ant.Task;

/**
 * A quick and dirty Ant task to lowercase a string.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class LowercaseTask extends Task {

	private String _value;
	private String _property;
	
	public void setProperty(String pName) {
		_property = pName;
	}
	
	public void setValue(String v) {
		_value = String.valueOf(v);
	}
	
	@Override
	public void execute() {
		getProject().setProperty(_property, _value.toLowerCase());
	}
}