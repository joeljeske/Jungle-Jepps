package org.yajasi.JungleJepps.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.io.File;
import com.sun.rowset.CachedRowSetImpl;

import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.Field;

public class PrimaryJdbcSource implements DatabaseConnection {
	
        private Connection connection;
	
	public PrimaryJdbcSource(SettingsManager settings) throws ClassNotFoundException, SQLException {
		
                
                File dbfile = new File("JJDB.db");
                String dbDriverClass = "org.sqlite.JDBC"; //settings.getStringForKey("primary-jdbc-driver-classpath");
		String dbUrl = "jdbc:sqlite:JJDB.db";//settings.getStringForKey("primary-jdbc-url");
		String dbUsername = null;//settings.getStringForKey("primary-db-username");
		String dbPassword = null;//settings.getStringForKey("primary-db-password");		
		
		// Load JDBC class into runtime
		Class.forName( dbDriverClass );
		
		// Request class from Driver Manager
		
                
                if(dbfile.exists() == false)
                {
                    this.connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                    System.out.println("here");
                    setupRelationships();
                }
                else{
                    this.connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                }        
	}
        @Override
        public String[] getAllAircraftIds()throws SQLException{
                CachedRowSetImpl crs = new CachedRowSetImpl();
                String[] results;
                int I = 0;
                
                crs = SQLquery("SELECT DISTINCT AIRCRAFT_IDENTIFIER FROM Aircraft");
                
                results = new String[crs.size()];
                while(crs.next()){
                    results[I] = crs.getString("AIRCRAFT_IDENTIFIER");
                    I++;
                }
                
                for(I = 0; I < results.length; I++){
                    System.out.println(results[I]);
                }
                
                return results;
        }

	@Override
	public String[] getAllRunwayIds()throws SQLException{
		CachedRowSetImpl crs = new CachedRowSetImpl();
                String[] results;
                int I = 0;
                
                crs = SQLquery("SELECT RUNWAY_IDENTIFIER FROM Runway");
                
                results = new String[crs.size()];
                while(crs.next()){
                    results[I] = crs.getString("RUNWAY_IDENTIFIER");
                    I++;
                }
                
                for(I = 0; I < results.length; I++){
                    System.out.println(results[I]);
                }
                
                return results;
	}

	@Override
	public Runway getRunway(String runwayId, String aircraftId)throws SQLException{
		CachedRowSetImpl crs = new CachedRowSetImpl();
                Runway results = new Runway();
                
                crs = SQLquery("SELECT * "
                            + "FROM Runway JOIN Aircraft "
                            + "ON Runway.RUNWAY_IDENTIFIER = Aircraft.RUNWAY_IDENTIFIER "
                            + "WHERE Aircraft.RUNWAY_IDENTIFIER = \"KIW\" AND Aircraft.AIRCRAFT_IDENTIFIER = \"PC-6\" "
                            );
                
                if(crs.next()){
                    results.setField(Field.RUNWAY_IDENTIFIER, crs.getString("RUNWAY_IDENTIFIER"));
                    results.setField(Field.RUNWAY_NAME, crs.getString("RUNWAY_NAME"));
                    results.setField(Field.AIRCRAFT_IDENTIFIER, crs.getString("AIRCRAFT_IDENTIFIER"));
                    results.setField(Field.LONGITUDE, crs.getString("LONGITUDE"));
                    results.setField(Field.LATITUDE, crs.getString("LATITUDE"));
                    results.setField(Field.INSPECTION_NA, crs.getString("INSPECTION_NA"));
                    results.setField(Field.INSPECTION_DATE, crs.getString("INSPECTION_DATE"));
                    results.setField(Field.INSPECTOR_NAME, crs.getString("INSPECTOR_NAME"));
                    results.setField(Field.INSPECTION_DUE, crs.getString("INSPECTION_DUE"));
                    results.setField(Field.CLASSIFICATION, crs.getString("CLASSIFICATION"));
                    results.setField(Field.FREQUENCY_1, crs.getString("FREQUENCY_1"));
                    results.setField(Field.FREQUENCY_2, crs.getString("FREQUENCY_2"));
                    results.setField(Field.LANGUAGE_GREET, crs.getString("LANGUAGE_GREET"));
                    results.setField(Field.ELEVATION, crs.getString("ELEVATION"));
                    results.setField(Field.LENGTH, crs.getString("LENGTH"));
                    results.setField(Field.WIDTH_TEXT, crs.getString("WIDTH_TEXT"));
                    results.setField(Field.TDZ_SLOPE, crs.getString("TDZ_SLOPE"));
                    results.setField(Field.IAS_ADJUSTMENT, crs.getString("IAS_ADJUSTMENT"));
                    results.setField(Field.PRECIPITATION_ON_SCREEN, crs.getString("PRECIPITATION_ON_SCREEN"));
                    results.setField(Field.RUNWAY_A, crs.getString("RUNWAY_A"));
                    results.setField(Field.A_TAKEOFF_RESTRICTION, crs.getString("A_TAKEOFF_RESTRICTION"));
                    results.setField(Field.A_TAKEOFF_NOTE, crs.getString("A_TAKEOFF_NOTE"));
                    results.setField(Field.A_LANDING_RESTRICTION, crs.getString("A_LANDING_RESTRICTION"));
                    results.setField(Field.A_LANDING_NOTE, crs.getString("A_LANDING_NOTE"));
                    results.setField(Field.RUNWAY_B, crs.getString("RUNWAY_B"));
                    results.setField(Field.B_TAKEOFF_RESTRICTION, crs.getString("B_TAKEOFF_RESTRICTION"));
                    results.setField(Field.B_TAKEOFF_NOTE, crs.getString("B_TAKEOFF_NOTE"));
                    results.setField(Field.B_LANDING_RESTRICTION, crs.getString("B_LANDING_RESTRICTION"));
                    results.setField(Field.B_LANDING_NOTE, crs.getString("B_LANDING_NOTE"));
                    results.setField(Field.PDF_PATH, crs.getString("PDF_PATH"));
                    results.setField(Field.IMAGE_PATH, crs.getString("IMAGE_PATH"));
                    results.setField(Field.P1_TEXT_1, crs.getString("P1_TEXT_1"));
                    results.setField(Field.P1_TEXT_2, crs.getString("P1_TEXT_2"));
                    results.setField(Field.P1_TEXT_3, crs.getString("P1_TEXT_3"));
                    results.setField(Field.P1_TEXT_4, crs.getString("P1_TEXT_4"));
                    results.setField(Field.P1_TEXT_5, crs.getString("P1_TEXT_5"));
                    results.setField(Field.P1_TEXT_6, crs.getString("P1_TEXT_6"));
                    results.setField(Field.P1_TEXT_7, crs.getString("P1_TEXT_7"));
                    results.setField(Field.IMAGE_PATH, crs.getString("IMAGE_PATH")); 
                }
                                
                return results;
	}

	@Override
	public boolean updateRunway(Runway runway)throws SQLException{
		
                ///this currently does nothing.
                ///CachedRowSetImpl crs = new CachedRowSetImpl();
                throw new UnsupportedOperationException();
	}
	
	
	// This method is an example of how to query and work in a JDBC Context
	public static void main(String[] args) throws SQLException, ClassNotFoundException{//DBbuild test
	    //Class.forName("rssbus.jdbc.excel.ExcelDriver");
            DatabaseConnection DB = new PrimaryJdbcSource(SettingsManager.getInstance());
            //DB.runSample();
            DB.getAllRunwayIds();
            DB.getAllAircraftIds();
            DB.getRunway("KIW", "PC-6");
        }
	
	public void setupRelationships() throws SQLException{
		System.out.println("setup... ");
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30);  // set timeout to 30 sec.

		System.out.println("setup Runway");
                statement.executeUpdate(
                        "CREATE TABLE Runway"
                        +"(RUNWAY_IDENTIFIER            String  PRIMARY KEY,"
                        + "RUNWAY_NAME                  String             ,"
                        + "LONGITUDE                    Float              ,"
                        + "LATITUDE                     Float              ,"
                        + "INSPECTION_NA                Bool               ,"
                        + "INSPECTION_DATE              Date               ,"
                        + "INSPECTOR_NAME               String             ,"
                        + "INSPECTION_DUE               Date               ,"
                        + "CLASSIFICATION               String             ,"
                        + "FREQUENCY_1                  Float              ,"
                        + "FREQUENCY_2                  Float              ,"
                        + "LANGUAGE_GREET               String             ,"
                        + "ELEVATION                    Int                ,"
                        + "ELEVATIONHL                  Int     default 0  ,"
                        + "LENGTH                       Int                ,"
                        + "LENGTHHL                     Int     default 0  ,"
                        + "WIDTH                        String             ,"
                        + "WIDTH_TEXT                   String             ,"
                        + "WIDTH_TEXTHL                 Int     default 0  ,"
                        + "TDZ_SLOPE                    Int                ,"
                        + "TDZ_SLOPEHL                  Int     default 0  ,"
                        + "RUNWAY_A                     Int                ,"
                        + "RUNWAY_AHL                   Int     default 0  ,"
                        + "RUNWAY_B                     Int                ,"
                        + "RUNWAY_BHL                   Int     default 0  "
                        + ");"
                        );
                System.out.println("setup Aircraft");
                statement.executeUpdate(
                        "CREATE TABLE Aircraft"
                        +"(AIRCRAFT_IDENTIFIER          String           ,"
                        + "RUNWAY_IDENTIFIER            String           ,"
                        + "IAS_ADJUSTMENT               String           ,"
                        + "IAS_ADJUSTMENTHL             Int     default 0,"
                        + "PRECIPITATION_ON_SCREEN      Int              ,"
                        + "PRECIPITATION_ON_SCREENHL    Int     default 0,"
                        + "A_TAKEOFF_RESTRICTION        String           ,"
                        + "A_TAKEOFF_RESTRICTIONHL      Int     default 0,"
                        + "A_TAKEOFF_NOTE               String           ,"
                        + "A_TAKEOFF_NOTEHL             Int     default 0,"
                        + "A_LANDING_RESTRICTION        String           ,"
                        + "A_LANDING_RESTRICTIONHL      Int     default 0,"
                        + "A_LANDING_NOTE               String           ,"
                        + "A_LANDING_NOTEHL             Int     default 0,"
                        + "B_TAKEOFF_RESTRICTION        String           ,"
                        + "B_TAKEOFF_RESTRICTIONHL      Int     default 0,"
                        + "B_TAKEOFF_NOTE               String           ,"
                        + "B_TAKEOFF_NOTEHL             Int     default 0,"
                        + "B_LANDING_RESTRICTION        String           ,"
                        + "B_LANDING_RESTRICTIONHL      Int     default 0,"
                        + "B_LANDING_NOTE               String           ,"
                        + "B_LANDING_NOTEHL             Int     default 0,"
                        + "PDF_PATH                     String           ,"
                        + "IMAGE_PATH                   String           ,"
                        + "P1_TEXT_1                    String           ,"
                        + "P1_TEXT_2                    String           ,"
                        + "P1_TEXT_3                    String           ,"
                        + "P1_TEXT_4                    String           ,"
                        + "P1_TEXT_5                    String           ,"
                        + "P1_TEXT_6                    String           ,"
                        + "P1_TEXT_7                    String           ,"
                        + "PRIMARY KEY(AIRCRAFT_IDENTIFIER, RUNWAY_IDENTIFIER)"
                        + ")"
                        );
                System.out.println("setup Note");
                statement.executeUpdate(
                        "CREATE TABLE Note"
                        +"(NoteID                   INTEGER PRIMARY KEY,"
                        + "AIRCRAFT_IDENTIFIER               String,"
                        + "RUNWAY_IDENTIFIER                 String,"
                        + "NameSoft                 String,"
                        + "NoteText                 String"//this needs to be Rich Text in end form
                        + ")"
                        );
                
                System.out.println("populate example runway");
                statement.executeUpdate(
                        "INSERT INTO Runway (RUNWAY_IDENTIFIER,RUNWAY_NAME,LONGITUDE,LATITUDE,INSPECTION_NA,INSPECTION_DATE,INSPECTOR_NAME,INSPECTION_DUE,CLASSIFICATION,FREQUENCY_1,FREQUENCY_2,LANGUAGE_GREET,ELEVATION,LENGTH,WIDTH,WIDTH_TEXT,TDZ_SLOPE,RUNWAY_A,RUNWAY_B)"
                        + "VALUES('KIW','Kiwi',044185,1404332,1,2013-10-25,"
                        + "'Jesse','2013-10-25','SP',123.45,1,'Ngalum/Yepmum',"
                        + "3500,419,31,'Ditch',2,31,13)"
                        );
                
                
                System.out.println("populate example aircraft");
                statement.executeUpdate(
                        "INSERT INTO Aircraft (AIRCRAFT_IDENTIFIER,RUNWAY_IDENTIFIER,IAS_ADJUSTMENT,PRECIPITATION_ON_SCREEN,A_TAKEOFF_RESTRICTION,A_TAKEOFF_NOTE,B_TAKEOFF_RESTRICTION,B_TAKEOFF_NOTE,PDF_PATH)"
                        + "VALUES('PC-6','KIW','KIAS +8',0,'-100k//2700GW','Reduced due to airstrip lenght','-200k//2600GW','Temp untill further experience','TBD')"
                        );
	
		statement.close();
                return;
	}
	
        @Override
	public void runSample() throws SQLException{
		
		
                System.out.println("sample...");
                Statement statement = connection.createStatement();
		
		ResultSet rs = statement.executeQuery("SELECT * FROM runway");
		System.out.println("Test of runway table:");
		while( rs.next() )
		{
	        // read the result set
                    System.out.println("ID = " + rs.getString("RUNWAY_IDENTIFIER"));
                    System.out.println("LENGTH = " + rs.getInt("LENGTH"));
                    System.out.println("WIDTH = " + rs.getInt("WIDTH"));
                    System.out.println("ELEVATION = " + rs.getInt("LENGTH"));
                }
                
                rs = statement.executeQuery("SELECT * FROM Aircraft");
		
		System.out.println("Test of Aircraft table:");
                while( rs.next() )
		{
	        // read the result set
                    System.out.println("Runway = " + rs.getString("RUNWAY_IDENTIFIER"));
                    System.out.println("Aircraft = " + rs.getString("AIRCRAFT_IDENTIFIER"));
                }
	    	
	}
        
        @Override
        public CachedRowSetImpl SQLquery(String SQLstatement)throws SQLException{
            CachedRowSetImpl crs = new CachedRowSetImpl();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(SQLstatement);
            crs.populate(rs);
            return crs;
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
			System.err.println(e);
                        return false;
		}
        }

}
