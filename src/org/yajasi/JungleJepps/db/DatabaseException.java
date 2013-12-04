/////////////////////////////////////////////////////////////////////////
// Author: Joel Jeske
// File: DatabaseException.java
// Class: org.yajasi.JungleJepps.db.DatabaseException
//
// Target Platform: Java Virtual Machine 
// Development Platform: Apple OS X 10.9
// Development Environment: Eclipse Kepler SDK
// 
// Project: Jungle Jepps - Desktop
// Copyright 2013 YAJASI. All rights reserved. 
// 
/////////////////////////////////////////////////////////////////////////


package org.yajasi.JungleJepps.db;

/**
 * This class an exception used to abstract all possible errors
 * in using the database connection.
 * @author Joel Jeske
 *
 */
public class DatabaseException extends Exception{

	/**
	 * Serial version
	 */
	private static final long serialVersionUID = 6380880501417806301L;

	/**
	 * Standard empty constructor
	 */
	public DatabaseException() {
		//Use the super constructor
		super();
	}
	
	/**
	 * Construct exception based on error message
	 * @param message
	 */
	public DatabaseException(String message){
		//Use the super constructor
		super(message);
	}
	
	/**
	 * This constructor makes an exception from the message 
	 * in the passed in exception
	 * @param Exception 
	 */
	public DatabaseException(Exception excp){
		//Use the message for this constructor 
		this(excp.getMessage());
	}
	
}
