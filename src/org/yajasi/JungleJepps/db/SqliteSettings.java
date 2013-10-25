package org.yajasi.JungleJepps.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.yajasi.JungleJepps.Field;

public class SqliteSettings {
	
	private static final String JDBC_DRIVER_CLASSPATH = "org.sqlite.JDBC";
	private static final String JDBC_PREFIX = "jdbc:sqlite:";
	private static final String DB_NAME = "settings.db";
	private static final String LABEL_PREFIX = "label_";
	private static final String GENERAL_SETTINGS_TABLE = "gen_settings";
	private static final String KEY_COLUMN = "key";
	private static final String VALUE_COLUMN = "value";
	
	/**
	 * The static reference to the instance. Used to keep only one 
	 * instance during runtime.
	 */
	private static SqliteSettings instance;
	
	private Connection connection; 
	
	/**
	 * Singleton model. Use to retrieve an instance of this class. 
	 * Used to prevent multiple instances trying to modify or read from a single file.
	 * @return SettingsManager instance
	 */
	public static SqliteSettings getInstance(){
		if( instance == null)
			instance = new SqliteSettings();
		return instance;
	}
	
	/**
	 * Private constructor sets all property readers
	 */
	private SqliteSettings(){
		try {
			Class.forName( JDBC_DRIVER_CLASSPATH );
			
			connection = DriverManager.getConnection(JDBC_PREFIX + DB_NAME);
			initialize();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void initialize(){
		Statement statement;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(5);
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + GENERAL_SETTINGS_TABLE + "(" + KEY_COLUMN + "string, " + VALUE_COLUMN + "string)" );
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the field label for a given field 
	 * @param field
	 * @return field label
	 */
	public String getLabel(Field field){
		return getLabel( field.toString() );
	}
	
	/**
	 * Get the field label for a given field 
	 * @param field
	 * @return field label
	 */
	public String getLabel(String field){
		return getStringForKey( LABEL_PREFIX + field );
	}
	
	public int getIntegerForKey(String key){
		Object value = getValue(key);
		
		if(value instanceof String)
			return Integer.parseInt( (String) value );
		
		throw new ClassCastException("Cannot create int from a java.lang.Object that is not of type java.lang.String.");	
	}

	public boolean getBooleanForKey(String key){
		Object value = getValue(key);
		
		if(value instanceof String)
			return Boolean.parseBoolean( (String) value);
		
		throw new ClassCastException("Cannot create boolean from a java.lang.Object that is not of type java.lang.String.");
	}
	
	public float getFloatForKey(String key){
		Object value = getValue(key);
		
		if(value instanceof String)
			return Float.parseFloat( (String) value );
		
		throw new ClassCastException("Cannot create float from a java.lang.Object that is not of type java.lang.String.");	
	}
	
	public String getStringForKey(String key){
		Object value = getValue(key);
		
		if(value instanceof String)
			return (String) value;
		
		return null;
	}
	
	private Object getValue(String key) {
		String sql = "SELECT * FROM " + GENERAL_SETTINGS_TABLE + "  WHERE " + KEY_COLUMN + " = '" + key + "'";
		Statement statement;
		ResultSet result;
		
		try {
			statement = connection.createStatement();
			result = statement.executeQuery(sql);
			return result.getObject(VALUE_COLUMN);
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		return null;
	}
	


}
