package org.yajasi.JungleJepps.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.yajasi.JungleJepps.Runway;

public class PrimaryJdbcSource implements DatabaseConnection {
	private Connection connection;
	
	public PrimaryJdbcSource(SettingsManager settings) throws ClassNotFoundException, SQLException {
		
		String dbDriverClass = settings.getStringForKey("primary-jdbc-driver-classpath");
		String dbUrl = settings.getStringForKey("primary-jdbc-url");
		String dbUsername = settings.getStringForKey("primary-db-username");
		String dbPassword = settings.getStringForKey("primary-db-password");		
		
		// Load JDBC class into runtime
		Class.forName( dbDriverClass );
		
		// Request class from Driver Manager
		this.connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
	}

	@Override
	public String[] getAllRunwayIds() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Runway getRunway(String runwayId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean updateRunway(Runway runway) {
		throw new UnsupportedOperationException();
	}
	
	
	// This method is an example of how to query and work in a JDBC Context
	public static void main(String[] args) throws SQLException{
		
		//Get a connection from the drivere manager using a driver-specific database URL.
		Connection con = DriverManager.getConnection("jdbc:sqlite:sample.db"); //NOTICE==> :sqlite:
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
