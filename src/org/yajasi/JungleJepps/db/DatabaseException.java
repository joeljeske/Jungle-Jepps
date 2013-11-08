package org.yajasi.JungleJepps.db;

public class DatabaseException extends Exception{

	public DatabaseException() {}
	
	public DatabaseException(String message){
		super(message);
	}
	
	public DatabaseException(Exception excp){
		this(excp.getMessage());
	}
	
}
