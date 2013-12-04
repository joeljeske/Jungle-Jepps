/////////////////////////////////////////////////////////////////////////
// Author: Joel Jeske
// File: DatabaseConnection.java
// Class: org.yajasi.JungleJepps.db.DatabaseConnection
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

import org.yajasi.JungleJepps.Runway;

/**
 * This interface is used to define a connection to a database 
 * for use in Jungle Jepps. It is possible to implement this interface
 * and then modify DatabaseManager class to instantiate it based on 
 * appropriate conditions. This modularizes our database connection
 * 
 * @author Joel Jeske
 */
public interface DatabaseConnection {
	
	/**
	 * Get a list of all the aailable aircraft Ids
	 * @return String[] of all the aircraft ids
	 * @throws DatabaseException
	 */
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
	 * @return True if updated successfully 
	 */
	public boolean updateRunway(Runway runway) throws DatabaseException;
        
    /**
     * Closes the database;
     * @return True if data base is closed. False if no data base was open
     */
    public boolean close() throws DatabaseException;

}
