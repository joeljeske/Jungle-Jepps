package org.yajasi.JungleJepps.db;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import org.yajasi.JungleJepps.Field;

import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.db.DatabaseConnection;

public class DataMerge implements DatabaseConnection{
	private DatabaseConnection primaryConnection;
        private DatabaseConnection operationsConnection;
	//private DatabaseConnection primarySource;
	private SettingsManager settingsMgr;

	public DataMerge(SettingsManager settings)throws SQLException, DatabaseException{		
            // Keep reference to primary source to query when necessary
            this.primaryConnection = new PrimaryJdbcSource(settings);
            this.operationsConnection = new OperationsDatabase(settings);

            // Keep reference to settings manager to retrieve overridden field list
            this.settingsMgr = settings;

            //force primaryConnection to have all Keys from operationConnection
            ODPDclosure();
	}
	
	@Override
	public String[] getAllAircraftIds() throws DatabaseException {
            return operationsConnection.getAllAircraftIds();
	}

	@Override
	public String[] getAllRunwayIds() throws DatabaseException{
            return operationsConnection.getAllRunwayIds();
                
	}
	
	@Override
	public String[] getAllRunwayIds(String aircraftId)throws DatabaseException {
            return operationsConnection.getAllRunwayIds(aircraftId);
	}

	@Override
	public Runway getRunway(String runwayId, String aircraftId)throws DatabaseException {
            Runway operationsRunway = new Runway();
            Runway primaryRunway = new Runway();

            operationsRunway = operationsConnection.getRunway(runwayId, aircraftId);
            primaryRunway = primaryConnection.getRunway(runwayId, aircraftId);

            primaryRunway.putAll(operationsRunway);

            return primaryRunway;
	}

	@Override
	public boolean updateRunway(Runway runway)throws DatabaseException {
            primaryConnection.updateRunway(runway);
            return true;
	}
	
	@Override
	public boolean close()throws DatabaseException {
		// TODO Auto-generated method stub
            operationsConnection.close();
            primaryConnection.close();
            return true;
	}
        
        public static void main(String[] args) throws DatabaseException {//DBbuild test
            System.out.println("This is the main method of the datamerge Class\n");
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
                       
            try{
                DataMerge db = new DataMerge(settings);
            
                String[] aircraftIds, runwayIds;
                Runway runway;

                ///getting runway and aircraft IDs
                aircraftIds = db.getAllAircraftIds();
                System.out.println("number of aircraft= " + aircraftIds.length);
                runwayIds = db.getAllRunwayIds( aircraftIds[0] );
                System.out.println("number of runway= " + runwayIds.length);
                runway = db.getRunway("KIW", "PC-6"); 

                System.out.print("\nAircraft IDs: ");
                for(String aid : aircraftIds)
                        System.out.print(aid + ", ");

                System.out.print("\nRunway IDs: ");
                for(String rid : runwayIds)
                        System.out.print(rid + ", ");


                System.out.print("\n\n-----" + "KIW" + "PC-6" + "---Field Printouts-----\n");
                for(Field f : runway.keySet())
                        System.out.println(f.toString() + ": " + runway.get(f) );


                db.close();
            }
            catch(SQLException e){
                System.out.println(e);
            }
        }
        
        private boolean ODPDclosure() throws DatabaseException{
            String[] operationsAID = operationsConnection.getAllAircraftIds();
            String[] operationsRID = operationsConnection.getAllRunwayIds();
            
            Runway operationsRunway = new Runway();
            Runway primaryRunway = new Runway();
            
            for(String R: operationsRID){
                for(String A: operationsAID){
                    operationsRunway = operationsConnection.getRunway(R, A);
                    if(!operationsRunway.isEmpty()){
                        primaryRunway.clear();
                        primaryRunway.put(Field.RUNWAY_IDENTIFIER, R);
                        primaryRunway.put(Field.AIRCRAFT_IDENTIFIER, A);
                        primaryConnection.updateRunway(primaryRunway);
                    }
                }
            }
            return false;
        }
}
