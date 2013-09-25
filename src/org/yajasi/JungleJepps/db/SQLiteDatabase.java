package org.yajasi.JungleJepps.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class SQLiteDatabase implements DatabaseConnection {
	
	public static void main(String[] args) throws SQLException{
		
		Connection con = null;
		
		con = DriverManager.getConnection("jdbc:sqlite:sample.db");
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
	
	} //main
	
}//class
	