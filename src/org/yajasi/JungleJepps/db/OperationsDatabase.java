package org.yajasi.JungleJepps.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.Runway;

public class OperationsDatabase implements DatabaseConnection {
	
	private SettingsManager settingsMgr;
	private Connection connection;
	
	public OperationsDatabase(SettingsManager settings) throws DatabaseException{
		this.settingsMgr = settings;
		String dbDriverClass = settingsMgr.get(Settings.OPERATIONS_JDBC_CLASS_PATH);
		String dbUrl = settingsMgr.get(Settings.OPERATIONS_JDBC_URI);
		
		try{
	        // Load JDBC class into runtime
			Class.forName( dbDriverClass );
	
			// Request class from Driver Manager
			this.connection = DriverManager.getConnection(dbUrl);
			
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new DatabaseException(e); //Rethrow exception using e
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e); //Rethrow exception using e
		}
		
	}
	
	
	public Runway getRunway(String runwayId, String aircraftId) throws DatabaseException{
		Runway runway = new Runway();
		ResultSet rs;
		
		String sql = String.format("SELECT * FROM %s WHERE %s = '%s' AND %s = '%s'", 
				settingsMgr.get(Settings.OPERATIONS_TABLE_NAME),
				settingsMgr.getOverrideColumn(Field.AIRCRAFT_IDENTIFIER),
				aircraftId,
				settingsMgr.getOverrideColumn(Field.RUNWAY_IDENTIFIER),
				runwayId);

		try{
			rs = connection.createStatement().executeQuery(sql);
	
			while(rs.next()){
				for(Field f : Field.values()){
					if( settingsMgr.isFieldOverridden(f) ){
						runway.put(f, rs.getString( settingsMgr.getOverrideColumn(f) ));
					}
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
			throw new DatabaseException(e);
		}
		
		return runway;
	}


	@Override
	public String[] getAllAircraftIds() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String[] getAllRunwayIds(String aircraftId) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String[] getAllRunwayIds() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean updateRunway(Runway runway) throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean close() throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

}
