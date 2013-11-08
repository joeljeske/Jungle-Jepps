
package org.yajasi.JungleJepps;

import java.io.IOException;
import java.sql.SQLException;

import org.yajasi.JungleJepps.db.DatabaseConnection;
import org.yajasi.JungleJepps.db.DatabaseException;
import org.yajasi.JungleJepps.db.DatabaseManager;

public class Main {

	public static void main(String[] args) throws SQLException, DatabaseException {
		System.out.println("Running main...");

		
		DatabaseConnection db = DatabaseManager.getDatabase();
		
		String[] aids = db.getAllAircraftIds();
		String[] rids = db.getAllRunwayIds( aids[0] );
		

		
		Runway runway = db.getRunway(rids[0], aids[0]); 
		
		System.out.println("Print runway...");
		for(Field f : Field.values())
			System.out.println( f.toString() + ": " + runway.get(f) );
		
		try {
			runway.publish();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
