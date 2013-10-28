package org.yajasi.JungleJepps.db;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.db.DatabaseConnection;

public class DataMerge implements DatabaseConnection{
	private Connection connection; 
	private DatabaseConnection primarySource;
	private SettingsManager settings;

	public DataMerge(SettingsManager settings, DatabaseConnection primarySource) throws ClassNotFoundException, SQLException{		
		// Keep reference to primary source to query when necessary
		this.primarySource = primarySource;
		
		// Keep reference to settings manager to retrieve overridden field list
		this.settings = settings;
 
		// Load third party and set to merge with existing source
		String dbDriverClass = settings.getStringForKey(Settings.OPERATIONS_JDBC_CLASS_PATH);
		String dbUrl = settings.getStringForKey(Settings.OPERATIONS_JDBC_URI);
		
		// Load JDBC class into runtime
		Class.forName( dbDriverClass );
		
		// Request class from Driver Manager
		this.connection = DriverManager.getConnection(dbUrl);
	}
	
	@Override
	public String[] getAllAircraftIds() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String[] getAllRunwayIds(String aircraftId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Runway getRunway(String runwayId, String aircraftId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean updateRunway(Runway runway) {
		throw new UnsupportedOperationException();
	}
	
	private boolean isFieldOutsourced(Field field){
		return settings.getOverrideColumn(field).isEmpty();
	}
	
}
