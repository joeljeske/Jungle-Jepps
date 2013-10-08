package org.yajasi.JungleJepps.db;

public class DatabaseManager {
	
	private static DatabaseConnection primaryDB;
	
	public static DatabaseConnection getDatabase(){
		return primaryDB;
	}
}
