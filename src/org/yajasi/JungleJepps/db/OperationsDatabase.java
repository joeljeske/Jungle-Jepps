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
        
        public static void main(String[] args) throws DatabaseException{
            System.out.println("This is the main method of the OperationsJdbcCource Class\n");
            SettingsManager settings = SettingsManager.getInstance();
            //settings for mysql
            settings.setValue(Settings.PRIMARY_JDBC_CLASS_PATH, "com.mysql.jdbc.Driver"); 
            settings.setValue(Settings.PRIMARY_JDBC_URI, "jdbc:mysql://localhost/JJDB?user=jepps&password=jepps");
            settings.setValue(Settings.OPERATIONS_JDBC_CLASS_PATH, "com.mysql.jdbc.Driver");
            settings.setValue(Settings.OPERATIONS_JDBC_URI, "jdbc:mysql://localhost/JJDB3?user=jepps&password=jepps");
            settings.setOverrideColumn(Field.AIRCRAFT_IDENTIFIER, "craft");
            settings.setOverrideColumn(Field.RUNWAY_IDENTIFIER, "RUNWAY_ID");
            settings.setOverrideColumn(Field.PDF_PATH, "PDF");
            settings.setValue(Settings.OPERATIONS_TABLE_NAME, "a_id");

            OperationsDatabase db = new OperationsDatabase(settings);
            
            String[] aircraftIds, runwayIds;
            Runway runway;

            ///getting runway and aircraft IDs
            aircraftIds = db.getAllAircraftIds();
            System.out.println("number of aircraft= " + aircraftIds.length);
            runwayIds = db.getAllRunwayIds( aircraftIds[0] );
            System.out.println("number of runway= " + runwayIds.length);
            runway = db.getRunway(runwayIds[0], aircraftIds[0]); 
            
            System.out.print("\nAircraft IDs: ");
            for(String aid : aircraftIds)
                    System.out.print(aid + ", ");

            System.out.print("\nRunway IDs: ");
            for(String rid : runwayIds)
                    System.out.print(rid + ", ");
                       
            ///row display
            runway = db.getRunway(runwayIds[0], aircraftIds[0]);
            System.out.print("\n\n-----" + runwayIds[0] + "---" + aircraftIds[0] + "---Field Printouts-----\n");
            for(Field f : runway.keySet())
                    System.out.println(f.toString() + ": " + runway.get(f) );
            
            db.close();
            
        }

}
