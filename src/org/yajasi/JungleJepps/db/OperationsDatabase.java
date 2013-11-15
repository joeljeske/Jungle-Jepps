package org.yajasi.JungleJepps.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
		ArrayList<String> results = new ArrayList<String>();
                String sql = String.format("SELECT %s FROM %s", 
                                           settingsMgr.getOverrideColumn(Field.AIRCRAFT_IDENTIFIER),
                                           settingsMgr.get(Settings.OPERATIONS_TABLE_NAME));
                ResultSet rs; 
                try{
                    rs = connection.createStatement().executeQuery(sql);
                    while(rs.next()){
                        results.add(rs.getString(settingsMgr.getOverrideColumn(Field.AIRCRAFT_IDENTIFIER)));
                    }
                }
                catch(SQLException e){
			e.printStackTrace();
			throw new DatabaseException(e);
		}
                
		return results.toArray(new String[results.size()]);
	}


	@Override
	public String[] getAllRunwayIds(String aircraftId) throws DatabaseException {
		ArrayList<String> results = new ArrayList<String>();
                String sql = String.format("SELECT %s FROM %s "
                                         + "WHERE %s = '%s'", 
                                         settingsMgr.getOverrideColumn(Field.RUNWAY_IDENTIFIER),
                                         settingsMgr.get(Settings.OPERATIONS_TABLE_NAME), 
                                         settingsMgr.getOverrideColumn(Field.AIRCRAFT_IDENTIFIER),
                                         aircraftId);
                ResultSet rs; 
                try{
                    rs = connection.createStatement().executeQuery(sql);
                    while(rs.next()){
                        results.add(rs.getString(settingsMgr.getOverrideColumn(Field.RUNWAY_IDENTIFIER)));
                    }
                }
                catch(SQLException e){
			e.printStackTrace();
			throw new DatabaseException(e);
		}
                
		return results.toArray(new String[results.size()]);
	}


	@Override
	public String[] getAllRunwayIds() throws DatabaseException {
		ArrayList<String> results = new ArrayList<String>();
                String sql = String.format("SELECT %s FROM %s", 
                                         settingsMgr.getOverrideColumn(Field.RUNWAY_IDENTIFIER),
                                         settingsMgr.get(Settings.OPERATIONS_TABLE_NAME));
                ResultSet rs; 
                try{
                    rs = connection.createStatement().executeQuery(sql);
                    while(rs.next()){
                        results.add(rs.getString(settingsMgr.getOverrideColumn(Field.RUNWAY_IDENTIFIER)));
                    }
                }
                catch(SQLException e){
			e.printStackTrace();
			throw new DatabaseException(e);
		}
                
		return results.toArray(new String[results.size()]);
	}


	@Override
	public boolean updateRunway(Runway runway) throws DatabaseException {
		System.out.println("Update on third party Database not permited");
		return false;
	}


	@Override
	public boolean close() throws DatabaseException {
		try{
                    connection.close();
                }
                catch(SQLException e){
			e.printStackTrace();
			throw new DatabaseException(e);
		}
		return false;
	}

}
