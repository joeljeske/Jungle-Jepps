package org.yajasi.JungleJepps.db;

import org.yajasi.JungleJepps.Runway;
import java.sql.*;
import com.sun.rowset.CachedRowSetImpl;

/**
 * Defines abstract connection to a database used for Jungle Jepps.
 * 
 * @author Joel Jeske
 *
 */
public interface DatabaseConnection {

	
	public String[] getAllAircraftIds() throws DatabaseException;
	

	
	/**
	 * Get a list of every runway Id available in the database.
	 * @return String[] Array of runway Ids available to an aircraft
	 */
	public String[] getAllRunwayIds(String aircraftId) throws DatabaseException;
	
        /**
	 * Get a list of every runway Id available in the database.
	 * @return String[] Array of runway Ids
	 */
        public String[] getAllRunwayIds() throws DatabaseException;
	
	/**
	 * Get the runway with the corresponding runway.
	 * Returns null if the runway id is not valid.
	 * @param String runwayId
	 * @return Runway runway from database
	 */
	public Runway getRunway(String runwayId, String aircraftId) throws DatabaseException;
	
	
	/**
	 * Update the runway in the database to reflect the database.
	 * @param Runway the runway to Update
	 * @return boolean true if updated successfully 
	 */
	public boolean updateRunway(Runway runway) throws DatabaseException;
        
      
        
    /**
     * closes the database;
     * @return 1 if data base is closed. 0 if no data base was open
     */
    public boolean close();

}
