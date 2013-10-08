package org.yajasi.JungleJepps.db;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

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
		String dbDriverClass = settings.getStringForKey("generic-jdbc-driver-classpath");
		String dbUrl = settings.getStringForKey("generic-jdbc-url");
		String dbUsername = settings.getStringForKey("generic-db-username");
		String dbPassword = settings.getStringForKey("generic-db-password");		
		
		// Load JDBC class into runtime
		Class.forName( dbDriverClass );
		
		// Request class from Driver Manager
		this.connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
	}
	
	@Override
	public String[] getAllRunwayIds() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Runway getRunway(String runwayId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean updateRunway(Runway runway) {
		throw new UnsupportedOperationException();
	}

}
