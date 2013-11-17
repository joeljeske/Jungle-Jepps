package org.yajasi.JungleJepps.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;

import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.jjtp.Client;
import org.yajasi.JungleJepps.jjtp.JungleJeppsmDNS;
import org.yajasi.JungleJepps.jjtp.Server;

public class DatabaseManager {
	
	private static DatabaseConnection primaryDB;
	
	/**
	 * Must be called prior to using database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	static {
		// By having a static block to initialize the database, it removes the need for 
		// each method to handle the exceptions that could occur from loading the DB.

		try {
			
			primaryDB = createDatabaseConnection();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/* Start up Server if primary instance */
		boolean isPrimary = getSettings().getBooleanForKey(Settings.IS_PRIMARY);
		if(isPrimary)
		{
			try {
				Server.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
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
	 * @throws SQLException
	 * @throws DatabaseException 
	 */
	private static DatabaseConnection createDatabaseConnection() throws ClassNotFoundException, SQLException, DatabaseException{
		System.out.println("Making initial connection to database...");
		DatabaseConnection newConnection;
		SettingsManager settings = getSettings();
		

		boolean isPrimary = settings.getBooleanForKey(Settings.IS_PRIMARY);
		boolean isUsingThirdParty = settings.getBooleanForKey(Settings.IS_OPERATIONS_DB);

		// Should this instance connect to db directly or through primary instance?		
		if( isPrimary )
		{
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
			Client client = new Client();
			newConnection = (DatabaseConnection) client;
			
			JungleJeppsmDNS.registerChangeListener(new JungleJeppsmDNS.ProviderStatusListener() {

				@Override
				public void newPrimaryProvider(URI newProvider) {
					Client client = (Client) getDatabase();
					InputStream stream = client.getSettingsStream();
					SettingsManager.loadFromInputStream(stream);
				}
			});
			
		}
		
		return newConnection;
	}

	
}
