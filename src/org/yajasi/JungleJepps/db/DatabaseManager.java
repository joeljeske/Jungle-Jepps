package org.yajasi.JungleJepps.db;

import java.sql.SQLException;

import org.yajasi.JungleJepps.jjtp.Client;

public class DatabaseManager {
	
	private static DatabaseConnection primaryDB;
	
	/**
	 * Must be called prior to using database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void initalizeDatabase() throws ClassNotFoundException, SQLException {
		// By having a different method to initialize the database, it removes the need for 
		// each method to handle the exceptions that could occur from loading the DB.

		primaryDB = createDatabaseConnection();
	}
	
	/**
	 * Used to retrieve handle from the database.
	 * @return
	 */
	public static DatabaseConnection getDatabase()  {
		return primaryDB;
	}
	
	/**
	 * Used to retrieve handle to Settings Manager
	 * @return SettingsManager
	 */
	public static SettingsManager getSettings(){
		return SettingsManager.getInstance();
	}
	
	
	private static DatabaseConnection createDatabaseConnection() throws ClassNotFoundException, SQLException{
		DatabaseConnection newConnection;
		SettingsManager settings = getSettings();
		

		boolean isPrimary = settings.getBooleanForKey("is-primary-instance");
		boolean isUsingThirdParty = settings.getBooleanForKey("using-third-party-source");

		// Should this instance connect to db directly or through primary instance?		
		if( isPrimary )
		{
			newConnection = new PrimaryJdbcSource( settings );
			
			// Should this instance connect to a third party db and merge or only the primary 
			if( isUsingThirdParty )
			{
				newConnection = new DataMerge(settings, newConnection);
			}			
		}
		else
		{
			newConnection = new Client();
		}
		

		return newConnection;
	}

	
}
