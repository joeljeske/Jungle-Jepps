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
            String[] urlInfo = new String[3];
            String[] dbTablesFound = new String[3];
            String[] dbTablesNeeded = new String[]{"Runway","Aircraft","Log"};
            String dbname;
            int tablesCorrect;
            
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
            if(dbInfo[1].compareTo("mysql") == 0 || dbInfo[1].compareTo("sqlserver") == 0){
                try{
                    tablesCorrect = 0;
                    this.connection = DriverManager.getConnection(dbUrl);
                                        
                    dbTablesFound = printDbTables();
                    for(String N:dbTablesNeeded){//checks to see if the table schema matches the needed schema
                        System.out.println("N: " + N.toUpperCase());
                        for(String F: dbTablesFound){
                            System.out.println("    F: " + F.toUpperCase());
                            if(F != null && N.toUpperCase().compareTo(F.toUpperCase()) == 0){
                                tablesCorrect++;//number of table that are correct.
                            }
                        }
                    }
                    
                    switch(tablesCorrect){
                        case 0: System.out.println("No Database Tables not found. Building schema now.");setupRelationships(); break;//all tables are not found so a new schema can be setup with not conflicts
                        case 1: System.out.println("Two Database Tables were not found. Unable to repaire. Please check Database"); System.exit(1);
                        case 2: System.out.println("One Database Tables was not found. Unable to repaire. Please check Database"); System.exit(1);
                        case 3: System.out.println("Database schema is correct. Making connection."); break;//no need to setup the schema...hoping that the tables are setup correctly
                    }
                }
                catch(java.sql.SQLException e){
                    System.out.println(e.toString() + ": Trying to fix...");
                    dbname = dbInfo[2].split("[/?;]+")[2];//this line pulls the desired database name out of the url for mysql and mssql...i think
                    System.out.println(dbname);
                    urlInfo = dbInfo[2].split(dbname);
                    this.connection = DriverManager.getConnection(dbInfo[0]+":"+dbInfo[1]+":"+urlInfo[0] + urlInfo[1]);
                    connection.createStatement().executeUpdate("CREATE DATABASE " + dbname);
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
            String updateSet = new String();
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
                if(f != Field.RUNWAY_IDENTIFIER || f != Field.AIRCRAFT_IDENTIFIER ){
                    updateSet = updateSet + "," + f + " = '" + runway.get(f) + "' ";
                }
            }
            statement = connection.prepareStatement("UPDATE Runway LEFT JOIN Aircraft "
                                                    + "ON Runway."+Field.RUNWAY_IDENTIFIER+" = Aircraft.RUNWAY_ID "
                                                    + "SET LAST_UPDATE = ? " + updateSet
                                                    + "WHERE Runway."+Field.RUNWAY_IDENTIFIER+" = ? AND Aircraft."+Field.AIRCRAFT_IDENTIFIER+" = ?");
                                                    statement.setTimestamp(1, new Timestamp(new Date().getTime()));
                                                    statement.setString(2, runway.get(Field.RUNWAY_IDENTIFIER));
                                                    statement.setString(3, runway.get(Field.AIRCRAFT_IDENTIFIER));
                                                    statement.execute();
            statement = connection.prepareStatement("INSERT INTO log("+Field.RUNWAY_IDENTIFIER+","+Field.AIRCRAFT_IDENTIFIER+",user,field_updated,time)"
                                                    + "VALUES(?,?,?,?,?)");
                                                    statement.setString(1, runway.get(Field.RUNWAY_IDENTIFIER));
                                                    statement.setString(2, runway.get(Field.AIRCRAFT_IDENTIFIER));
                                                    statement.setString(3, System.getProperty("user.name"));
                                                    statement.setString(4, updateSet);
                                                    statement.setTimestamp(5, new Timestamp(new Date().getTime()));
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
	public String[] printDbTables() throws SQLException {
		
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet res = meta.getTables(null, null, null, new String[]{"TABLE"});
		String[] results = new String[3];
                int I = 0;
		
                System.out.println("\n\nList of Tables in Database");
                while (res.next()) {
                    results[I] = res.getString("TABLE_NAME");
                    System.out.println( results[I] );
                    I++;
		}
                System.out.println("\n\n");
                return results;
		
	}
	
	// This method is an example of how to query and work in a JDBC Context
	public static void main(String[] args) throws ClassNotFoundException, SQLException {//DBbuild test
            System.out.println("This is the main method of the PrimaryJdbcCource Class\n");
            //PrimaryJdbcSource db = new PrimaryJdbcSource("org.sqlite.JDBC", "jdbc:sqlite:JJDB.db");
            PrimaryJdbcSource db = new PrimaryJdbcSource("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/JJDB?user=jepps&password=jepps");
            db.printDbTables();
            
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
                        + "ELEVATION_HL                  INTEGER     default 0  ,"
                        + "LENGTH                       VARCHAR(255)             ,"
                        + "LENGTH_HL                     INTEGER     default 0  ,"
                        + "WIDTH_TEXT                   VARCHAR(255)             ,"
                        + "WIDTH_TEXT_HL                 INTEGER     default 0  ,"
                        + "TDZ_SLOPE                    VARCHAR(255)             ,"
                        + "TDZ_SLOPE_HL                  INTEGER     default 0  ,"
                        + "RUNWAY_A                     VARCHAR(255)             ,"
                        + "RUNWAY_A_HL                   INTEGER     default 0  ,"
                        + "RUNWAY_B                     VARCHAR(255)             ,"
                        + "RUNWAY_B_HL                   INTEGER     default 0  "
                        + ");"
                        );
                
                System.out.println("setup Aircraft");
                statement.executeUpdate(
                        "CREATE TABLE Aircraft"
                        +"(AIRCRAFT_IDENTIFIER          VARCHAR(255)            ,"
                        + "RUNWAY_ID                    VARCHAR(255)            ,"
                        + "IAS_ADJUSTMENT               VARCHAR(255)            ,"
                        + "IAS_ADJUSTMENT_HL             INTEGER     default 0,"
                        + "PRECIPITATION_ON_SCREEN      VARCHAR(255)            ,"
                        + "PRECIPITATION_ON_SCREEN_HL    INTEGER     default 0,"
                        + "A_TAKEOFF_RESTRICTION        VARCHAR(255)            ,"
                        + "A_TAKEOFF_RESTRICTION_HL      INTEGER     default 0,"
                        + "A_TAKEOFF_NOTE               VARCHAR(255)            ,"
                        + "A_TAKEOFF_NOTE_HL             INTEGER     default 0,"
                        + "A_LANDING_RESTRICTION        VARCHAR(255)            ,"
                        + "A_LANDING_RESTRICTION_HL      INTEGER     default 0,"
                        + "A_LANDING_NOTE               VARCHAR(255)            ,"
                        + "A_LANDING_NOTE_HL             INTEGER     default 0,"
                        + "B_TAKEOFF_RESTRICTION        VARCHAR(255)            ,"
                        + "B_TAKEOFF_RESTRICTION_HL      INTEGER     default 0,"
                        + "B_TAKEOFF_NOTE               VARCHAR(255)            ,"
                        + "B_TAKEOFF_NOTE_HL             INTEGER     default 0,"
                        + "B_LANDING_RESTRICTION        VARCHAR(255)            ,"
                        + "B_LANDING_RESTRICTION_HL      INTEGER     default 0,"
                        + "B_LANDING_NOTE               VARCHAR(255)            ,"
                        + "B_LANDING_NOTE_HL             INTEGER     default 0,"
                        + "PDF_PATH                     VARCHAR(1000)            ,"
                        + "IMAGE_PATH                   VARCHAR(1000)            ,"
                        + "P1_TEXT_1                    VARCHAR(1000)            ,"
                        + "P1_TEXT_2                    VARCHAR(1000)            ,"
                        + "P1_TEXT_3                    VARCHAR(1000)            ,"
                        + "P1_TEXT_4                    VARCHAR(1000)            ,"
                        + "P1_TEXT_5                    VARCHAR(1000)            ,"
                        + "P1_TEXT_6                    VARCHAR(1000)            ,"
                        + "P1_TEXT_7                    VARCHAR(1000)            ,"
                        + "P2_TEXT_1                    VARCHAR(1000)            ,"
                        + "P2_TEXT_2                    VARCHAR(1000)            ,"
                        + "P2_TEXT_3                    VARCHAR(1000)            ,"
                        + "P2_TEXT_4                    VARCHAR(1000)            ,"
                        + "P2_TEXT_5                    VARCHAR(1000)            ,"
                        + "P2_TEXT_6                    VARCHAR(1000)            ,"
                        + "P2_TEXT_7                    VARCHAR(1000)            ,"
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
                        + "field_updated                        VARCHAR(2400),"
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
