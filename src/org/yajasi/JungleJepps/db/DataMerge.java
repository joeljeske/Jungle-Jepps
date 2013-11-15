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

	public DataMerge(SettingsManager settings, DatabaseConnection primarySource) throws ClassNotFoundException, SQLException, DatabaseException{		
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

            operationsRunway.putAll(primaryRunway);

            return operationsRunway;
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
