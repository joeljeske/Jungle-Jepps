package org.yajasi.JungleJepps.db;

public enum Settings {
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
	ALITUDE_UNITS,
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
	DEFAULT_EXPIRATION_PERIOD;
	
	
	public String toString(){
		return super.toString().toLowerCase();
	}
}
