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
	//private DatabaseConnection primaryConnection;
        //private DatabaseConnection operationsConnection;
	//private DatabaseConnection primarySource;
        private DatabaseConnection[] connections;
	private SettingsManager settingsMgr;
        int numOfDbs;

	public DataMerge(SettingsManager settings)throws SQLException, DatabaseException{		
            // Keep reference to primary source to query when necessary
            this.settingsMgr = settings;
            numOfDbs = settingsMgr.get(Settings.OPERATIONS_JDBC_CLASS_PATH).split("$").length+1;
            System.out.println("here: " + numOfDbs);
            connections = new DatabaseConnection[numOfDbs];
            
            this.connections[0] = new PrimaryJdbcSource(settings);
            for(int I = 1; I < numOfDbs; I++){
                this.connections[I] = new OperationsDatabase(settings, I-1); //the -1 is so that the OperationsDatabase does not have to account for the primary database 
            }
            

            // Keep reference to settings manager to retrieve overridden field list
            

            //force primaryConnection to have all Keys from operationConnection
            ODPDclosure();
	}
	
	@Override
	public String[] getAllAircraftIds() throws DatabaseException {
            return connections[0].getAllAircraftIds();
	}

	@Override
	public String[] getAllRunwayIds() throws DatabaseException{
            return connections[0].getAllRunwayIds();
                
	}
	
	@Override
	public String[] getAllRunwayIds(String aircraftId)throws DatabaseException {
            return connections[0].getAllRunwayIds(aircraftId);
	}

	@Override
	public Runway getRunway(String runwayId, String aircraftId)throws DatabaseException {
            //Runway operationsRunway = new Runway();
            Runway results = connections[0].getRunway(runwayId, aircraftId);

            for(int I = numOfDbs-1; I > 0; I--){//this loop is in revers to make sure that the first 3rd party database has primacy on the results
                results.putAll(connections[I].getRunway(runwayId, aircraftId));
            }

            return results;
	}

	@Override
	public boolean updateRunway(Runway runway)throws DatabaseException {
            connections[0].updateRunway(runway);
            return true;
	}
	
	@Override
	public boolean close()throws DatabaseException {
		// TODO Auto-generated method stub
            for(int I = 0; I < numOfDbs; I++){
                connections[I].close();
            }
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
            String[] operationsAID;
            String[] operationsRID;
            
            Runway operationsRunway = new Runway();
            Runway primaryRunway = new Runway();
            for(int I = 1; I < numOfDbs; I ++){
                operationsAID = connections[I].getAllAircraftIds();
                operationsRID = connections[I].getAllRunwayIds();
                for(String R: operationsRID){
                    for(String A: operationsAID){
                        operationsRunway = connections[I].getRunway(R, A);
                        if(!operationsRunway.isEmpty()){
                            primaryRunway.clear();
                            primaryRunway.put(Field.RUNWAY_IDENTIFIER, R);
                            primaryRunway.put(Field.AIRCRAFT_IDENTIFIER, A);
                            connections[0].updateRunway(primaryRunway);
                        }
                    }
                }
            }
            
            return false;
        }
}
