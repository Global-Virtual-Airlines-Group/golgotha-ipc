// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.ant;

import java.lang.reflect.Field;

import org.apache.tools.ant.*;

/**
 * A quick and dirty Ant Task to extract version info constants.  
 * @author Luke
 * @version 2.64
 * @since 2.64
 */

public class VersionInfoTask extends Task {
	
	private String _className;
	private String _fieldName;
	private String _property;
	
	public void setClassName(String name) {
		_className = name;
	}
	
	public void setFieldName(String name) {
		_fieldName = name;
	}

	public void setProperty(String pName) {
		_property = pName;
	}
	
	@Override
	public void execute() {
		try {
			Class<?> c = Class.forName(_className);
			Field f = c.getDeclaredField(_fieldName);
			if (!f.canAccess(null))
				f.setAccessible(true);
				
			Object v = f.get(null);
			getProject().setProperty(_property, String.valueOf(v));
		} catch (Exception e) {
			getProject().log(String.format("%s - %s", e.getClass().getSimpleName(), e.getMessage()), e, Project.MSG_ERR);
			throw new RuntimeException();
		}
	}
}