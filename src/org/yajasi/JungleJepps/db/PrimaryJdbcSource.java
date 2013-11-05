package org.yajasi.JungleJepps.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;

import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.Field;

public class PrimaryJdbcSource implements DatabaseConnection {
	
	private Connection connection;
	
	/**
         * Public constructor 
         * @param settings DB location and type info
         * @throws ClassNotFoundException
         * @throws SQLException 
         */
        public PrimaryJdbcSource(SettingsManager settings) throws ClassNotFoundException, SQLException {
                this(settings.getStringForKey(Settings.PRIMARY_JDBC_CLASS_PATH), settings.getStringForKey(Settings.PRIMARY_JDBC_URI));
	}
        
        /**
         * Private constructor: do not use directly except when needing to test this class in isolation
         * @param dbDriverClass drive/db type
         * @param dbUrl location
         * @throws ClassNotFoundException
         * @throws SQLException 
         */
        private PrimaryJdbcSource(String dbDriverClass, String dbUrl) throws ClassNotFoundException, SQLException {
            File dbfile;
            String[] dbInfo = new String[3];
            String[] urlInfo = new String[2];
            // Load JDBC class into runtime
            Class.forName( dbDriverClass );
            // Request class from Driver Manager
            dbInfo = dbUrl.split(":");
            dbfile = new File(dbInfo[2]);//checks to see if the file is open
                        
            if(dbInfo[1].compareTo("sqlite") == 0){//makes connection to a sqlite DB
                if(dbfile.exists() == false)//if it does not exist, this builds a new DB
                {
                    
                    this.connection = DriverManager.getConnection(dbUrl);
                    setupRelationships();
                }
                else{
                    this.connection = DriverManager.getConnection(dbUrl);
                }
            }
            if(dbInfo[1].compareTo("mysql") == 0){
                try{
                    
                    this.connection = DriverManager.getConnection(dbUrl);
                }
                catch(java.sql.SQLException e){
                    urlInfo = dbInfo[2].split("JJDB");
                    this.connection = DriverManager.getConnection(dbInfo[0]+":"+dbInfo[1]+":"+urlInfo[0] + urlInfo[1]);
                    connection.createStatement().executeUpdate("CREATE DATABASE JJDB");
                    this.connection = DriverManager.getConnection(dbUrl);
                    setupRelationships();
                    
                }
            }          
        }
	
        /**
         * returns all Aircraft IDs
         * @return array of Aircraft IDs in String format
         * @throws SQLException 
         */
        @Override
        public String[] getAllAircraftIds()throws SQLException{
            ResultSet rs ;
            ArrayList<String> results = new ArrayList<String>();
            int I = 0;

            rs = connection.createStatement().executeQuery("SELECT DISTINCT " + Field.AIRCRAFT_IDENTIFIER + " FROM Aircraft");
            
            while(rs.next()){
                results.add(rs.getString(Field.AIRCRAFT_IDENTIFIER.toString()));
                I++;
            }
            
            return results.toArray(new String[results.size()]);
        }

	/**
         * Gets only the runways that aircraftId can use
         * @param aircraftId aircraft of interest
         * @return array of Runway IDs in String format
         * @throws SQLException 
         */
        @Override
        public String[] getAllRunwayIds(String aircraftId) throws SQLException{
            
            ResultSet rs ;
            ArrayList<String> results = new ArrayList<String>();
            int I = 0;

            rs = connection.createStatement().executeQuery("SELECT " + Field.RUNWAY_IDENTIFIER + " FROM Runway JOIN Aircraft "
                                                        + "ON Runway." + Field.RUNWAY_IDENTIFIER + " = Aircraft.RUNWAY_ID "
                                                        + "WHERE Aircraft." + Field.AIRCRAFT_IDENTIFIER + " = '" + aircraftId +"'");
            
            while(rs.next()){
                results.add(rs.getString(Field.RUNWAY_IDENTIFIER.toString()));
                I++;
            }
            
            return results.toArray(new String[results.size()]);
        }
        
        /**
         * Gets all runways IDs in the DataBase
         * @return array of Runway IDs in String format
         * @throws SQLException 
         */
        @Override
        public String[] getAllRunwayIds() throws SQLException{
            ResultSet rs;
            ArrayList<String> results = new ArrayList<String>();
            int I = 0;

            rs = connection.createStatement().executeQuery("SELECT " + Field.RUNWAY_IDENTIFIER + " FROM Runway");

            
            while(rs.next()){
                results.add(rs.getString(Field.RUNWAY_IDENTIFIER.toString()));
                I++;
            }

            return results.toArray(new String[results.size()]);
            
        }
        
        /**
         * Gets all Fields from A Runway Aircraft combination
         * @param runwayId
         * @param aircraftId
         * @return Runway containing all info needed to build a PDF
         * @throws SQLException 
         */
	@Override
	public Runway getRunway(String runwayId, String aircraftId)throws SQLException{
            ResultSet rs;
            Runway results = new Runway();

            rs = connection.createStatement().executeQuery("SELECT * "
                                                        + "FROM Runway JOIN Aircraft "
                                                        + "ON Runway." + Field.RUNWAY_IDENTIFIER + " = Aircraft.RUNWAY_ID "
                                                        + "WHERE Runway." + Field.RUNWAY_IDENTIFIER + " ='" + runwayId + "' AND " + Field.AIRCRAFT_IDENTIFIER + " = '" + aircraftId + "' "
                                                        );
            if(rs.next()){
                for(Field f: Field.values()){
                    results.put(f,rs.getString(f.toString()));
                }
            }
            
        return results;
	}
        
        /**
         * Makes changes to the database both new rows and row updates
         * @param runway
         * @return pointless return value. returns true unless it crashes
         * @throws SQLException 
         */
	@Override
	public boolean updateRunway(Runway runway)throws SQLException{
            PreparedStatement statement = null; // if time, consider having all sql statements use this
            ResultSet rs = connection.createStatement().executeQuery("SELECT " + Field.RUNWAY_IDENTIFIER + " "
                                                        + "FROM Runway "
                                                        + "WHERE " + Field.RUNWAY_IDENTIFIER + " = '" + runway.get(Field.RUNWAY_IDENTIFIER) + "' ");
            
            if(rs.next() == false){//if row is not found in Runway table, build the row
                
                connection.createStatement().executeUpdate("INSERT INTO Runway "
                                                        + "("+ Field.RUNWAY_IDENTIFIER.toString() +") "
                                                        + "VALUES('" + runway.get(Field.RUNWAY_IDENTIFIER) + "')");
            }
            
            rs = connection.createStatement().executeQuery("SELECT Aircraft." + Field.AIRCRAFT_IDENTIFIER.toString() + " "
                                                        + "FROM Runway JOIN Aircraft "
                                                        + "ON Runway." + Field.RUNWAY_IDENTIFIER.toString() + " = Aircraft.RUNWAY_ID "
                                                        + "WHERE Runway."+ Field.RUNWAY_IDENTIFIER.toString() + " = '" + runway.get(Field.RUNWAY_IDENTIFIER) +"' "
                                                        + "AND Aircraft."+ Field.AIRCRAFT_IDENTIFIER.toString() +" = '" + runway.get(Field.AIRCRAFT_IDENTIFIER) + "'");
            
            if(rs.next() == false){//if row is not found in Aircraft table, build the row
                connection.createStatement().executeUpdate("INSERT INTO Aircraft "
                                                        + "(RUNWAY_ID,"+ Field.AIRCRAFT_IDENTIFIER.toString() +") "
                                                        + "VALUES ('"+ runway.get(Field.RUNWAY_IDENTIFIER)+"','" + runway.get(Field.AIRCRAFT_IDENTIFIER)+ "')");
            }
            
            for(Field f: runway.keySet()){
                int table = 0;//0 = skip item, 1 = item goes in the aircraft table, 2 = item goes in the runway table. NO other number should be set.
                switch(f){
                    case RUNWAY_IDENTIFIER: table = 0; break;
                    case RUNWAY_NAME: table = 2; break;
                    case AIRCRAFT_IDENTIFIER: table = 0; break;
                    case LONGITUDE: table = 2; break;
                    case LATITUDE: table = 2; break;
                    case INSPECTION_NA: table = 2; break;
                    case INSPECTION_DATE: table = 2; break;
                    case INSPECTOR_NAME: table = 2; break;
                    case INSPECTION_DUE: table = 2; break;
                    case CLASSIFICATION: table = 2; break;
                    case FREQUENCY_1: table = 2; break;
                    case FREQUENCY_2: table = 2; break;
                    case LANGUAGE_GREET: table = 2; break;
                    case ELEVATION: table = 2; break;
                    case LENGTH: table = 2; break;
                    case WIDTH_TEXT: table = 2; break;
                    case TDZ_SLOPE: table = 2; break;
                    case IAS_ADJUSTMENT: table = 1; break;
                    case PRECIPITATION_ON_SCREEN: table = 1; break;
                    case RUNWAY_A: table = 2; break;
                    case A_TAKEOFF_RESTRICTION: table = 1; break;
                    case A_TAKEOFF_NOTE: table = 1; break;
                    case A_LANDING_RESTRICTION: table = 1; break;
                    case A_LANDING_NOTE: table = 1; break;
                    case RUNWAY_B: table = 2; break;
                    case B_TAKEOFF_RESTRICTION: table = 1; break;
                    case B_TAKEOFF_NOTE: table = 1; break;
                    case B_LANDING_RESTRICTION: table = 1; break;
                    case B_LANDING_NOTE: table = 1; break;
                    case P1_TEXT_1: table = 1; break;
                    case P1_TEXT_2: table = 1; break;
                    case P1_TEXT_3: table = 1; break;
                    case P1_TEXT_4: table = 1; break;
                    case P1_TEXT_5: table = 1; break;
                    case P1_TEXT_6: table = 1; break;
                    case P1_TEXT_7: table = 1; break;
                    case P2_TEXT_1: table = 1; break;
                    case P2_TEXT_2: table = 1; break;
                    case P2_TEXT_3: table = 1; break;
                    case P2_TEXT_4: table = 1; break;
                    case P2_TEXT_5: table = 1; break;
                    case P2_TEXT_6: table = 1; break;
                    case P2_TEXT_7: table = 1; break;
                    case PDF_PATH: table = 1; break;
                    case IMAGE_PATH: table = 1; break;
                }
                
                switch(table){
                    case 0: break;
                    case 1:connection.createStatement().executeUpdate("UPDATE Aircraft "
                                                                    + "SET "+ f + " = '" + runway.get(f) + "' "
                                                                    + "WHERE Aircraft.RUNWAY_ID = '" + runway.get(Field.RUNWAY_IDENTIFIER) + "' "
                                                                    + "AND Aircraft.AIRCRAFT_IDENTIFIER = '" + runway.get(Field.AIRCRAFT_IDENTIFIER) + "' ");
                           statement = connection.prepareStatement("INSERT INTO log("+Field.RUNWAY_IDENTIFIER+","+Field.AIRCRAFT_IDENTIFIER+",user,field_updated,time)"
                                                                    + "VALUES('"+ runway.get(Field.RUNWAY_IDENTIFIER) +"','"+runway.get(Field.AIRCRAFT_IDENTIFIER) +"','"+System.getProperty("user.name")+"','"+ f +"',?)");
                           statement.setTimestamp(1, new Timestamp(new Date().getTime()));
                           statement.execute();
                           break;
                    case 2:connection.createStatement().executeUpdate("UPDATE Runway "
                                                                    + "SET "+ f + " = '" + runway.get(f) + "' "
                                                                    + "WHERE Runway." + Field.RUNWAY_IDENTIFIER + " = '" + runway.get(Field.RUNWAY_IDENTIFIER) + "' ");
                           statement = connection.prepareStatement("INSERT INTO log("+Field.RUNWAY_IDENTIFIER+",user,field_updated,time)"
                                                                    + "VALUES('"+ runway.get(Field.RUNWAY_IDENTIFIER) +"','"+System.getProperty("user.name")+"','"+ f +"',?)");
                           statement.setTimestamp(1, new Timestamp(new Date().getTime()));
                           statement.execute();
                           break;
                }
                
            }
            statement = connection.prepareStatement("UPDATE Aircraft "//sets time of last update
                                    + "SET LAST_UPDATE = ? "
                                    + "WHERE Aircraft.RUNWAY_ID = '" + runway.get(Field.RUNWAY_IDENTIFIER) + "' "
                                    + "AND Aircraft.AIRCRAFT_IDENTIFIER = '" + runway.get(Field.AIRCRAFT_IDENTIFIER) + "' ");
            
            statement.setTimestamp(1, new Timestamp(new Date().getTime()));
            statement.execute();
            return true;
        }
	
	/**
	 * Added by Joel.
	 * Possible example of testing added tables to provide cross
	 * Database type portability. 
	 * If the DB was tested here, we could find if we needed to run our setup
	 * function or not. 
	 * Also, we can always assume with Server DBs (mysql, mssql) that the 
	 * Database Name given in the JDBC URI 
	 * (i.e. JJDB in the URI jdbc:mysql://localhost/JJDB?user=jepps&password=jepps")
	 * already exists, we do not need to create it which should help.  
	 * @param db
	 * @throws SQLException
	 */
	public static void printDbTables(PrimaryJdbcSource db) throws SQLException {
		
		DatabaseMetaData meta = db.connection.getMetaData();
		ResultSet res = meta.getTables(null, null, null, new String[]{"TABLE"});
		
		while (res.next()) {
		     System.out.println( res.getString("TABLE_NAME") ); 
		  }
		
	}
	
	// This method is an example of how to query and work in a JDBC Context
	public static void mafin(String[] args) throws ClassNotFoundException, SQLException {//DBbuild test
            System.out.println("This is the main method of the PrimaryJdbcCource Class\n");
            PrimaryJdbcSource db = new PrimaryJdbcSource("org.sqlite.JDBC", "jdbc:sqlite:JJDB.db");
            //PrimaryJdbcSource db = new PrimaryJdbcSource("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/JJDB?user=jepps&password=jepps");
            printDbTables(db);
            
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
            
            ///updating a row
            System.out.println("Updating database");
            runway.clear();
            runway.put(Field.AIRCRAFT_IDENTIFIER, "PC-6");
            runway.put(Field.RUNWAY_IDENTIFIER, "KIW");
            runway.put(Field.INSPECTOR_NAME, "Sam");
            
            db.updateRunway(runway);
            
            ///row display
            runway = db.getRunway(runwayIds[0], aircraftIds[0]);
            System.out.print("\n\n-----" + runwayIds[0] + "---" + aircraftIds[0] + "---Field Printouts-----\n");
            for(Field f : runway.keySet())
                    System.out.println(f.toString() + ": " + runway.get(f) );
            
            
            System.out.println("un-Updating database");
            runway.clear();
            runway.put(Field.AIRCRAFT_IDENTIFIER, "PC-6");
            runway.put(Field.RUNWAY_IDENTIFIER, "KIW");
            runway.put(Field.INSPECTOR_NAME, "Jesse");
            
            db.updateRunway(runway);
            
            ///adding a new row
            System.out.println("adding row to database");
            
            runway.clear();
            for(Field f: Field.values()){
                runway.put(f, ""+(int)(Math.random()*100));
            }
            
            db.updateRunway(runway);
            
            db.close();
        }
	/**
         * This method does all the work of setting up the schema. This will not run if database is already built
         * @throws SQLException 
         */
	public void setupRelationships() throws SQLException{
		System.out.println("setup... ");
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30);  // set timeout to 30 sec.

		System.out.println("setup Runway");
                statement.executeUpdate(
                        "CREATE TABLE Runway"
                        +"(RUNWAY_IDENTIFIER            VARCHAR(255)  PRIMARY KEY,"
                        + "RUNWAY_NAME                  VARCHAR(255)             ,"
                        + "LONGITUDE                    Float              ,"//Float so that we can calc distance from home
                        + "LATITUDE                     Float              ,"//Float so that we can calc distance from home
                        + "INSPECTION_NA                VARCHAR(255)             ,"
                        + "INSPECTION_DATE              VARCHAR(255)             ,"
                        + "INSPECTOR_NAME               VARCHAR(255)             ,"
                        + "INSPECTION_DUE               VARCHAR(255)             ,"
                        + "CLASSIFICATION               VARCHAR(255)             ,"
                        + "FREQUENCY_1                  VARCHAR(255)             ,"
                        + "FREQUENCY_2                  VARCHAR(255)             ,"
                        + "LANGUAGE_GREET               VARCHAR(255)             ,"
                        + "ELEVATION                    VARCHAR(255)             ,"
                        + "ELEVATIONHL                  INTEGER     default 0  ,"
                        + "LENGTH                       VARCHAR(255)             ,"
                        + "LENGTHHL                     INTEGER     default 0  ,"
                        + "WIDTH_TEXT                   VARCHAR(255)             ,"
                        + "WIDTH_TEXTHL                 INTEGER     default 0  ,"
                        + "TDZ_SLOPE                    VARCHAR(255)             ,"
                        + "TDZ_SLOPEHL                  INTEGER     default 0  ,"
                        + "RUNWAY_A                     VARCHAR(255)             ,"
                        + "RUNWAY_AHL                   INTEGER     default 0  ,"
                        + "RUNWAY_B                     VARCHAR(255)             ,"
                        + "RUNWAY_BHL                   INTEGER     default 0  "
                        + ");"
                        );
                
                System.out.println("setup Aircraft");
                statement.executeUpdate(
                        "CREATE TABLE Aircraft"
                        +"(AIRCRAFT_IDENTIFIER          VARCHAR(255)            ,"
                        + "RUNWAY_ID                    VARCHAR(255)            ,"
                        + "IAS_ADJUSTMENT               VARCHAR(255)            ,"
                        + "IAS_ADJUSTMENTHL             INTEGER     default 0,"
                        + "PRECIPITATION_ON_SCREEN      VARCHAR(255)            ,"
                        + "PRECIPITATION_ON_SCREENHL    INTEGER     default 0,"
                        + "A_TAKEOFF_RESTRICTION        VARCHAR(255)            ,"
                        + "A_TAKEOFF_RESTRICTIONHL      INTEGER     default 0,"
                        + "A_TAKEOFF_NOTE               VARCHAR(255)            ,"
                        + "A_TAKEOFF_NOTEHL             INTEGER     default 0,"
                        + "A_LANDING_RESTRICTION        VARCHAR(255)            ,"
                        + "A_LANDING_RESTRICTIONHL      INTEGER     default 0,"
                        + "A_LANDING_NOTE               VARCHAR(255)            ,"
                        + "A_LANDING_NOTEHL             INTEGER     default 0,"
                        + "B_TAKEOFF_RESTRICTION        VARCHAR(255)            ,"
                        + "B_TAKEOFF_RESTRICTIONHL      INTEGER     default 0,"
                        + "B_TAKEOFF_NOTE               VARCHAR(255)            ,"
                        + "B_TAKEOFF_NOTEHL             INTEGER     default 0,"
                        + "B_LANDING_RESTRICTION        VARCHAR(255)            ,"
                        + "B_LANDING_RESTRICTIONHL      INTEGER     default 0,"
                        + "B_LANDING_NOTE               VARCHAR(255)            ,"
                        + "B_LANDING_NOTEHL             INTEGER     default 0,"
                        + "PDF_PATH                     VARCHAR(700)            ,"
                        + "IMAGE_PATH                   VARCHAR(700)            ,"
                        + "P1_TEXT_1                    VARCHAR(700)            ,"
                        + "P1_TEXT_2                    VARCHAR(700)            ,"
                        + "P1_TEXT_3                    VARCHAR(700)            ,"
                        + "P1_TEXT_4                    VARCHAR(700)            ,"
                        + "P1_TEXT_5                    VARCHAR(700)            ,"
                        + "P1_TEXT_6                    VARCHAR(700)            ,"
                        + "P1_TEXT_7                    VARCHAR(700)            ,"
                        + "P2_TEXT_1                    VARCHAR(700)            ,"
                        + "P2_TEXT_2                    VARCHAR(700)            ,"
                        + "P2_TEXT_3                    VARCHAR(700)            ,"
                        + "P2_TEXT_4                    VARCHAR(700)            ,"
                        + "P2_TEXT_5                    VARCHAR(700)            ,"
                        + "P2_TEXT_6                    VARCHAR(700)            ,"
                        + "P2_TEXT_7                    VARCHAR(700)            ,"
                        + "LAST_UPDATE                  TIMESTAMP             ,"
                        + "PRIMARY KEY(AIRCRAFT_IDENTIFIER, RUNWAY_ID)"
                        + ")"
                        );
                System.out.println("setup Note");
                statement.executeUpdate(
                        "CREATE TABLE Log"
                        +"(LogID                   INTEGER AUTO_INCREMENT PRIMARY KEY,"
                        + "AIRCRAFT_IDENTIFIER                  VARCHAR(255),"
                        + "RUNWAY_IDENTIFIER                    VARCHAR(255),"
                        + "user                                 VARCHAR(255),"
                        + "field_updated                        VARCHAR(255),"//if multi update the will be the last field to be updated.
                        + "time                                 TIMESTAMP" 
                        + ")"
                        );

                System.out.println("populate example runway");//default dataset
                statement.executeUpdate(
                        "INSERT INTO Runway (RUNWAY_IDENTIFIER,RUNWAY_NAME,LONGITUDE,LATITUDE,INSPECTION_NA,INSPECTION_DATE,INSPECTOR_NAME,INSPECTION_DUE,CLASSIFICATION,FREQUENCY_1,FREQUENCY_2,LANGUAGE_GREET,ELEVATION,LENGTH,WIDTH_TEXT,TDZ_SLOPE,RUNWAY_A,RUNWAY_B)"
                        + "VALUES('KIW','Kiwi',044185,1404332,1,2013-10-25,"
                        + "'Jesse','2013-10-25','SP',123.45,1,'Ngalum/Yepmum',"
                        + "3500,419,'31/Ditch',2,31,13)"
                       );


                System.out.println("populate example aircraft");//default dataset
                statement.executeUpdate(
                        "INSERT INTO Aircraft (AIRCRAFT_IDENTIFIER,RUNWAY_ID,IAS_ADJUSTMENT,PRECIPITATION_ON_SCREEN,A_TAKEOFF_RESTRICTION,A_TAKEOFF_NOTE,B_TAKEOFF_RESTRICTION,B_TAKEOFF_NOTE,PDF_PATH,LAST_UPDATE,P1_TEXT_1,P1_TEXT_2,P1_TEXT_3,P1_TEXT_4,P1_TEXT_5,P1_TEXT_6,P1_TEXT_7,P2_TEXT_1,P2_TEXT_2,P2_TEXT_3,P2_TEXT_4,P2_TEXT_5,P2_TEXT_6,P2_TEXT_7)"
                        + "VALUES("
                        + "'PC-6',"
                        + "'KIW',"
                        + "'KIAS +8',"
                        + "0,"
                        + "'-100k//2700GW',"
                        + "'Reduced due to airstrip lenght','-200k//2600GW',"
                        + "'Temp untill further experience','TBD','2013-10-30',"
                        + "'Before the River at 5050 ft','Right turn down valley',"
                        + "'Describe location/procedure for least damage/injury',"
                        + "'Grass over packed gravel, smooth even surface.',"
                        + "'WIND CURFEW 9:00am. WAIVER Rwy for TDZ slope. If other than CALM winds, DO NOT LAND! CALM winds defined as Indicated by \"dead\" or slight movements of the WS. Sudden Verry Strong winds possible including tailwinds, updrafts, down drafts and rolling action on medium to short final. Other wind indicators at KWI include; turbulence at Kivi pass, flag at KP, smoke in the vally, and winds at KWR.',"
                        + "'50m into the takeoff roll. At 2/3 the Rwy. turn left into the embankment. DO NOT GO OFF THE END.',"
                        + "'Description of pre-determined emergency landing options.',"
                        + "'Ngalum/Yepmum',"
                        + "'<B>Non Typical</B> Deastern highland weather. WINDS funneled up vally or down vally past airstrip. Winds come up and change QUICKLEY!',"
                        + "'<B>9:00 WIND CURFEW.</B> With reliable CURRENT(while overhead) readio report of CALM winds, the wind time restriction may be waived. WAIVER AIRSTRIP DUE TO TDZ SLOPE',"
                        + "'DO NOT LAND with winds other than calm as defined by POH600D. Windsock DOES NOT always indicate winds accurately. Also look for flag movement on Keypoint ridge, and other wind indications. Agreement of Indicators required BEFORE committal point Airspeed 63(-0+5)kts, WSI 500-700, (Helio Power 10-12 inches)[PC6 7-9PSI]3 Landing Accidents and Multiple Landing scares over 40 years due to winds.',"
                        + "'Lots of text',"
                        + "'Surface should be smooth and free of ruts. Grass Height should not obstruct the clear view of each side marker',"
                        + "'Lots of text')"
                        );
	
		statement.close();
	}
        
    @Override
    public boolean close(){
        try
		{
			if(connection != null)
			    connection.close();
			return true;              
		}
		catch(SQLException e)
		{
			// connection close failed.
			e.printStackTrace();
            return false;
		}
    }

}