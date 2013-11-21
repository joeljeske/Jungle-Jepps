/////////////////////////////////////////////////////////////////////////
// Author: Joel Jeske
// File: Settings.java
// Class: org.yajasi.JungleJepps.db.Settings
//
// Target Platform: Java Virtual Machine 
// Development Platform: Apple OS X 10.9
// Development Environment: Eclipse Kepler SDK
// 
// Project: Jungle Jepps - Desktop
// Copyright 2013 YAJASI. All rights reserved. 
// 
// Objective: This file represents an ENUM for every setting
// needed for Jungle Jepps Desktop configuration. This is used to as the
// key in a key-value persistence storage system in the SettingsManager.
//
/////////////////////////////////////////////////////////////////////////


package org.yajasi.JungleJepps.db;

import org.yajasi.JungleJepps.ValueByEnum;

public enum Settings implements ValueByEnum.JungleJeppsEnum {
	IS_PRIMARY,
	REPOSITORY_PATH,
	PRIMARY_JDBC_URI,
	PRIMARY_JDBC_CLASS_PATH,
	IS_OPERATIONS_DB,
	OPERATIONS_JDBC_URI,
	OPERATIONS_JDBC_CLASS_PATH,
	OPERATIONS_TABLE_NAME,
	ADMIN_PASSWORD,
	LAST_RUNWAY,
	LAST_AIRCRAFT,
	ADMIN_EMAIL,
	ALTITUDE_UNITS,
	WEIGHT_UNITS,
	DISTANCE_UNITS,
	DIMENSION_UNITS,
	DISTANCE_CONVERT_FACTOR,
	LONG_LAT_FORMAT,
	IS_TRUE_COURSE,
	MAGNETIC_VARIATION,
	HOME_RUNWAY,
	SECONDARY_RUNWAY,
	PAGE_1_DISCLAIMER,
	PAGE_2_DISCLAIMER,
	DEFAULT_EXPIRATION_PERIOD,
	WEB_ROOT;
	
	/**
	 * This method converts the literal value of the ENUM to 
	 * a String. The string is returned in lower case and should be 
	 * used whenever a persistent or portable field identifier is 
	 * needed.
	 * @return Literal value of the ENUM in lower case
	 */
	public String toString(){
		return super.toString().toLowerCase();
	}
}
