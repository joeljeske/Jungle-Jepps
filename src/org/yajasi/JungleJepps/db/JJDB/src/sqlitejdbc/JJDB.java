/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlitejdbc;

import java.sql.*;
import java.io.*;

public class JJDB
{
    private static JJDB database = null;
    String activeDBDriver;// = "org.sqlite.JDBC";
    String activeConnectionName;// = "jdbc:sqlite:JJDB.db";
  
    
    public static void main( String args[] )
    {
        getDBsingleton().SQL("SELECT * FROM STRIP;");        
    }
  
    private JJDB()
    {
        File dbfile = new File("JJDB.db");
        String DBname = "JJDB.db";
        activeDBDriver = "org.sqlite.JDBC";
        activeConnectionName = "jdbc:sqlite:" + DBname;
        if(dbfile.exists() == false)
        {
            BuildDB(activeDBDriver,activeConnectionName);
        }
    }
    
    private JJDB(String driver, String databaseName)
    {
        File dbfile = new File("JJDB.db");
        String DBname = "JJDB.db";
        activeDBDriver = driver;
        activeConnectionName = databaseName;
        if(dbfile.exists() == false)
        {
            BuildDB(driver,databaseName);
        }
    }
  
  public static JJDB getDBsingleton()
  {
      if(database == null){
          database = new JJDB();
      }
      return database;
  }
  
  public static JJDB rebuildDBSingleton(String driver, String databaseName)
  {
      if(database == null){
          database = new JJDB(driver, databaseName);
      }
      return database;
  }
  
  private static void BuildDB(String driver, String database)
  {
      System.out.println("This Database does not exist. Creating now...");
      Connection c = null;
      Statement stmt = null;
      String sql = null;  
      try
        {
            Class.forName(driver);
            c = DriverManager.getConnection(database);
            //c.setAutoCommit(false);
            stmt = c.createStatement();
            System.out.println("Database has been crated.");
                    
            sql =     "CREATE TABLE STRIP"
                    + "(STRIPID TEXT PRIMARY KEY  NOT NULL,"
                    + "NAME         TEXT    NOT NULL,"
                    + "AGENCY       TEXT    NOT NULL,"
                    + "CLASS        TEXT    NOT NULL,"
                    + "LATITUDE     INT     NOT NULL,"
                    + "LONGITUDE    INT     NOT NULL,"
                    + "VHRFREQ      FLOAT   NOT NULL,"
                    + "LENGTH       INT     NOT NULL,"
                    + "WIDTH        INT     NOT NULL,"
                    + "ELEVATION    INT     NOT NULL,"
                    + "SLOPE        INT     NOT NULL,"
                    + "SURFACE      TEXT    NOT NULL,"
                    + "BOUNDARY     TEXT    NOT NULL,"
                    + "GPS          TEXT    NOT NULL)";
            
            stmt.executeUpdate(sql);
            
            sql =     "CREATE TABLE AIRCRAFTRELATED"
                    + "(STRIPID         TEXT    NOT NULL,"
                    + "CRAFTID          TEXT    NOT NULL,"
                    + "COMMITPOINT           TEXT    NOT NULL,"
                    + "GOAROUND         TEXT    NOT NULL,"
                    + "NOSTOP           TEXT    NOT NULL,"
                    + "HAZARDS          TEXT    NOT NULL,"
                    + "NOTAKEOFF        TEXT    NOT NULL,"
                    + "ENGINEFAIL       TEXT    NOT NULL,"
                    + "WEIGHTA          INT     NOT NULL,"
                    + "WEIGHTB          INT,"                    
                    + "WEIGHTREASONA    TEXT,"
                    + "WEIGHTREASONB    TEST,"
                    + "PRECIPITATION    BOOLEAN NOT NULL,"
                    + "PDF              TEXT     NOT NULL,"
                    + "PRIMARY KEY(STRIPID, CRAFTID))";
            
            stmt.executeUpdate(sql);
                        
            sql =     "CREATE TABLE NOTES"
                    + "(STRIPID TEXT PRIMARY KEY  NOT NULL,"
                    + "TITLE         TEXT,"
                    + "MESSAGE       TEXT    NOT NULL)";
                    
             stmt.executeUpdate(sql);
             
             sql = "INSERT INTO STRIP (STRIPID,NAME,AGENCY,CLASS,LATITUDE,LONGITUDE,"
                     + "VHRFREQ,LENGTH,WIDTH,ELEVATION,SLOPE,SURFACE,BOUNDARY,GPS) "
                     + "VALUES ('KWI', 'Kiwi', 'Yajasi', 'SP', 'S 04:41.85', 'E 140:43.32',"
                     + "122.5, 419, 31, 3500, 2, 'Grass over packed gravel, smooth even surface.',"
                     + "'Ditch', 'KIAS +8' );";
             
             stmt.executeUpdate(sql);
             
             sql = "INSERT INTO AIRCRAFTRELATED (STRIPID,CRAFTID,COMMITPOINT,GOAROUND,NOSTOP,"
                     + "HAZARDS,NOTAKEOFF,ENGINEFAIL,WEIGHTA,WEIGHTB,WEIGHTREASONA,WEIGHTREASONB,"
                     + "PRECIPITATION,PDF)"
                     + "VALUES ('KWI','PC-6', "
                     + "'Before the River at 5050 ft', "
                     + "'Right turn down valley', "
                     + "'Describe location/procedure for least damage/injury', "
                     + "'WIND CURFEW 9:00am. WAIVER Rwy for TDZ slope. If other "
                     + "than CALM winds, DO NOT LAND! CALM winds defined as "
                     + "Indicated by \"dead\" or slight movements of the WS. "
                     + "Sudden Verry Strong winds possible including tailwinds, "
                     + "updrafts, down drafts and rolling action on medium to "
                     + "short final. Other wind indicators at KWI include; "
                     + "turbulence at Kivi pass, flag at KP, smoke in the vally, "
                     + "and winds at KWR.', "
                     + "'50m into the takeoff roll. At 2/3 the Rwy. turn left "
                     + "into the embankment. DO NOT GO OFF THE END.', "
                     + "'Description of pre-determined emergency landing options.', "
                     + "-100, 'Reduced due to airstrip length', -200, "
                     + "'Temp until further experience',"
                     + "'NO','TBA');";
             
             stmt.executeUpdate(sql);

             stmt.close();
             c.close();
            
        }
        catch(Exception e)
        {
            System.out.println(e.getClass().getName() + ": " + e.getMessage()+" A");
        }
  }
  
  private ResultSet SQL(String SQLstatment)
  {
      Connection c = null;
      Statement stmt = null;
      String sql = null;  
      ResultSet rs;
      try {
      Class.forName(activeDBDriver);
      c = DriverManager.getConnection(activeConnectionName);
      
      c.setAutoCommit(false);
      System.out.println("Opened database successfully");

      stmt = c.createStatement();
      rs = stmt.executeQuery( SQLstatment );
      /*while ( rs.next() ) {
         String STRIPID = rs.getString("STRIPID");
         String NAME = rs.getString("NAME");
         String AGENCY = rs.getString("AGENCY");
         String CLASS = rs.getString("CLASS");
         String LATITUDE = rs.getString("LATITUDE");
         String LONGITUDE = rs.getString("LONGITUDE");
         float VHRFREQ = rs.getFloat("VHRFREQ");
         int LENGTH = rs.getInt("LENGTH");
         int WIDTH = rs.getInt("WIDTH");
         int ELEVATION = rs.getInt("ELEVATION");
         int SLOPE = rs.getInt("SLOPE");
         String GPS = rs.getString("GPS");
                                 
         System.out.println( "STRIPIDID = " + STRIPID );
         System.out.println( "NAME = " + NAME );
         System.out.println( "AGENCY = " + AGENCY );
         System.out.println( "CLASS = " + CLASS );
         System.out.println( "LATITUDE = " + LATITUDE );
         System.out.println( "LONGITUDE = " + LONGITUDE );
         System.out.println( "VHRFREQ = " + VHRFREQ );
         System.out.println( "LENGTH = " + LENGTH );
         System.out.println( "WIDTH = " + WIDTH );
         System.out.println( "ELEVATION = " + ELEVATION );
         System.out.println( "SLOPE = " + SLOPE );
         System.out.println( "GPS = " + GPS );
         System.out.println();
      }*/
      //rs.get
      stmt.close();
      c.close();
      System.out.println("Operation done successfully");
      return rs;
    } catch ( Exception e ) 
    {
        System.err.println( e.getClass().getName() + ": " + e.getMessage() +" B" );
        System.exit(0);
    }
    return null;
  }
}
