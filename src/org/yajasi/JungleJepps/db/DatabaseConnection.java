package org.yajasi.JungleJepps.db;

import org.yajasi.JungleJepps.Runway;

/**
 * Defines abstract connection to a database used for Jungle Jepps.
 * 
 * @author Joel Jeske
 *
 */
public interface DatabaseConnection {

	public String[] getAllAircraftIds();
	
	/**
	 * Get a list of every runway Id available in the database.
	 * @return String[] Array of runway Ids
	 */
	public String[] getAllRunwayIds(String aircraftId);
	
	
	/**
	 * Get the runway with the corresponding runway.
	 * Returns null if the runway id is not valid.
	 * @param String runwayId
	 * @return Runway runway from database
	 */
	public Runway getRunway(String runwayId, String aircraftId);
	
	
	/**
	 * Update the runway in the database to reflect the database.
	 * @param Runway the runway to Update
	 * @return boolean true if updated successfully 
	 */
	public boolean updateRunway(Runway runway);

}
