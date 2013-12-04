/////////////////////////////////////////////////////////////////////////
// Author: Joel Jeske
// File: DatabaseManager.java
// Class: org.yajasi.JungleJepps.db.DatabaseManager
//
// Target Platform: Java Virtual Machine 
// Development Platform: Apple OS X 10.9
// Development Environment: Eclipse Kepler SDK
// 
// Project: Jungle Jepps - Desktop
// Copyright 2013 YAJASI. All rights reserved. 
//
/////////////////////////////////////////////////////////////////////////


package org.yajasi.JungleJepps.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.jjtp.Client;
import org.yajasi.JungleJepps.jjtp.JungleJeppsmDNS;
import org.yajasi.JungleJepps.jjtp.Server;

/**
 * This class is used to manage the database connections and 
 * the settings connection. It retrieves the settings instance and 
 * constructs the various database connection types based on if this is
 * the primary or secondary connections and the status of the 3rd party
 * database.
 * @author Joel Jeske
 *
 */
public class DatabaseManager {
	
	/**
	 * Will hold the primary database connection. <br />
	 * This could be a networked LAN client, JDBC connection
	 * or a DataMerge connection using two JDBC connections
	 */
	private static DatabaseConnection primaryDB;
	
	/**
	 * This class is used as static only so we have a private and empty constructor.
	 */
	private DatabaseManager(){}

	static {
		// By having a static block to initialize the database, it removes the need for 
		// each method to handle the exceptions that could occur from loading the DB.

		try {
			
			primaryDB = createDatabaseConnection();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		/* Start up Server if primary instance */
		boolean isPrimary = getSettings().getBooleanForKey(Settings.IS_PRIMARY);
		
		if(isPrimary)
		{
			try {
				//Server starts the mDNS broadcast and 
				//initializes its services in a separate thread
				Server.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//Simple test method creates the database and calls various methods on it.
	public static void main(String[] args) throws DatabaseException{
		// By running this static function, the static block has already run
		// and therefore the database connection has been made and the settings have 
		// been initialized. If the instance is primary, the Server has been started a well. 
		
        DatabaseConnection db = DatabaseManager.getDatabase();
        String[] aircraftIds, runwayIds;
        Runway runway;
        
        aircraftIds = db.getAllAircraftIds();
        runwayIds = db.getAllRunwayIds( aircraftIds[0] );
        runway = db.getRunway(runwayIds[0], aircraftIds[0]);
        
        System.out.print("\nAircraft IDs: ");
        for(String aid : aircraftIds)
        	System.out.print(aid + ", ");

        System.out.print("\nRunway IDs: ");
        for(String rid : runwayIds)
        	System.out.print(rid + ", ");
        
    	System.out.print("\n\n-----Field Printouts-----\n");
        for(Field f : runway.keySet())
        	System.out.println(f.toString() + ": " + runway.get(f) );
		
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
	
	/**
	 * This method is used to initialize a database connection.
	 * It uses the settings to load the database type and initiates 
	 * the merge class with a 3rd party source if need be.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws DatabaseException 
	 */
	private static DatabaseConnection createDatabaseConnection() throws ClassNotFoundException, DatabaseException{
		//Verbose output
		System.out.println("Making initial connection to database...");
		
		//Will hold the two data source connections
		DatabaseConnection newConnection;
		SettingsManager settings = getSettings();
		
		//True if this instance is the primary connection
		boolean isPrimary = settings.getBooleanForKey(Settings.IS_PRIMARY);
		boolean isUsingThirdParty = settings.getBooleanForKey(Settings.IS_OPERATIONS_DB);

		// Should this instance connect to db directly or through primary instance?		
		if( isPrimary )
		{
			//Get a database connection from the primary JDBC source
			newConnection = new PrimaryJdbcSource( settings );
			
			// Should this instance connect to a third party db and merge or only the primary 
			if( isUsingThirdParty )
			{
				throw new UnsupportedOperationException("Data merge construction needs fixing in DatabaseManager class");
				//newConnection = new DataMerge(settings, newConnection);
			}			
		}
		else
		{
			//Create a LAN client to connect to the primary instance
			//This will be our primary connection
			newConnection = (DatabaseConnection) new Client(); 
			
			//Create a new listener to wait until we have a primary instance IP and port
			JungleJeppsmDNS.registerChangeListener(new JungleJeppsmDNS.ProviderStatusListener() {

				//This block will run async whenever we have a primary instance
				@Override
				public void newPrimaryProvider(URI newProvider) {
					//We know the database will be a client becuase this block only runs 
					//if we are a secondary instance
					Client client = (Client) getDatabase();
					
					//Get a stream to load the settings from
					InputStream stream = client.getSettingsStream();
					
					//Load the settings from the primary source using a LAN stream
					SettingsManager.loadFromInputStream(stream);
				}
			});
			
		}
		
		//We now have a good connection whether it is a JDBC source, DataMerge, or a Client
		return newConnection;
	}

	
}
