package org.yajasi.JungleJepps.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.Runway;

public class PrimaryJdbcSource implements DatabaseConnection {
	private Connection connection;
	
	public PrimaryJdbcSource(SettingsManager settings) throws ClassNotFoundException, SQLException {
		
		String dbDriverClass = settings.getStringForKey(Settings.PRIMARY_JDBC_CLASS_PATH);
		String dbUrl = settings.getStringForKey(Settings.PRIMARY_JDBC_URI);		
		
		// Load JDBC class into runtime
		Class.forName( dbDriverClass );
		
		// Request class from Driver Manager
		this.connection = DriverManager.getConnection(dbUrl);
	}

	@Override
	public String[] getAllAircraftIds() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String[] getAllRunwayIds(String aircraftId) {
		/*
		String sql = String.format("SELECT %s, %s FROM runways", 
				Field.RUNWAY_IDENTIFIER.toString(), 
				Field.RUNWAY_NAME.toString());
		
		Statement statement;
		ResultSet result;
		Array ids;
		try {
			statement = connection.createStatement();
			result = statement.executeQuery(sql);
			ids = result.getArray(Field.RUNWAY_IDENTIFIER.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		*/
		throw new UnsupportedOperationException();
	}

	@Override
	public Runway getRunway(String runwayId, String aircraftId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean updateRunway(Runway runway) {
		throw new UnsupportedOperationException();
	}
	
	
	public static void runSample() throws SQLException{
		
		//Get a connection from the drivere manager using a driver-specific database URL.
		Connection con = DriverManager.getConnection("jdbc:xls:file:test.xlsx"); //NOTICE==> :sqlite:
		Statement statement = con.createStatement();
		statement.setQueryTimeout(30);  // set timeout to 30 sec.

		
		statement.executeUpdate("drop table if exists person");
		statement.executeUpdate("create table person (id integer, name string)");
		statement.executeUpdate("insert into person values(1, 'leo')");
		statement.executeUpdate("insert into person values(2, 'yui')");
		
		ResultSet rs = statement.executeQuery("select * from person");
		
		while( rs.next() )
		{
	        // read the result set
	        System.out.println("name = " + rs.getString("name"));
	        System.out.println("id = " + rs.getInt("id"));
	     }
	    
		try
		{
			if(con != null)
			    con.close();
		}
		catch(SQLException e)
		{
			// connection close failed.
			System.err.println(e);
		}
		
	}

}
