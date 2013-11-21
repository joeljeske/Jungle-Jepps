/////////////////////////////////////////////////////////////////////////
// Author: Joel Jeske
// File: Repository.java
// Class: org.yajasi.JungleJepps.pdf.Repository
//
// Target Platform: Java Virtual Machine 
// Development Platform: Apple OS X 10.9
// Development Environment: Eclipse Kepler SDK
// 
// Project: Jungle Jepps - Desktop
// Copyright 2013 YAJASI. All rights reserved. 
// 
// Objective: This class is used to manage the Repository. It handles the 
// location management and file structure of the Repository such as where
// the active PDF document for a runway is stored or where the 
//
/////////////////////////////////////////////////////////////////////////

package org.yajasi.JungleJepps.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;

import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.db.DatabaseManager;
import org.yajasi.JungleJepps.db.Settings;

public class Repository {

	public static final File REPOSITORY;
	public static final String DOCUMENTS_FOLDER = "documents";
	public static final String PUBLISHED_NAME = "diagram";
	public static final String ARCHIVE_FOLDER = "archive";
	public static final String IMAGE_FOLDER = "photos";
	public static final String DOCUMENT_EXTENSION = ".pdf";
	
	//Static block run upon class load
	static {
		//Get the repository base path
		String path = DatabaseManager.getSettings().getStringForKey(Settings.REPOSITORY_PATH);
		
		//Make the File that the repository resides in
		REPOSITORY = new File(path);
	}
	
	
	//Simple changeable testing method
	public static void main(String[] args){
		Runway r = new Runway();
		r.put(Field.RUNWAY_IDENTIFIER, "KIW");
		r.put(Field.AIRCRAFT_IDENTIFIER, "PC-5");

		File bFolder = getRunwayBaseFolder(r);
		System.out.println(bFolder.getAbsolutePath());
	}
	
	
	/**
	 * Used to get the file where the PDF should be published.
	 * @param runway
	 * @return pdf published location
	 */
	public static File getPublishLocation(Runway runway){
		//This is the location of the runway-aircraft data
		File runwayBase = getRunwayBaseFolder(runway);
		
		//The active document always looks like
		// "diagram.pdf"
		String name = PUBLISHED_NAME + DOCUMENT_EXTENSION;
		
		//Make the file and return it
		return new File(runwayBase, name);
	}
	
	/**
	 * Get the file where the pdf should be stored for archive
	 * @param runway
	 * @return archive pdf location
	 */
	public static File getArchiveLocation(Runway runway){
		//Base runway folder
		File runwayBase = getRunwayBaseFolder(runway); 
	
		// ==> "archive"
		File archive = new File(runwayBase, ARCHIVE_FOLDER);
		
		//Get a descritpive name based on the runway and datetime
		String descriptiveName = getDescriptiveName(runway);
		
		//Make sure these folders are created
		archive.mkdirs();
		
		//Get the location for the archived PDF docs for this runway
		File archiveLocation = getNoConflictFile( new File(archive, descriptiveName + DOCUMENT_EXTENSION) );
		
		return archiveLocation;
	}
	
	/**
	 * Used to get a File handle on the image at the path
	 * that is used in the Runway for the Field ENUM, Field.IMAGE_PATH 
	 * @param runway
	 * @return Current photo file for the runway
	 */
	public static File getPhotoFile(Runway runway){
		//Get the runway base folder
		File base = getRunwayBaseFolder(runway);
		
		//Should be similar to 
		// "photos/KIW-Kiwi_20130521-01.png"
		String relPath = IMAGE_FOLDER + File.separator + runway.get(Field.IMAGE_PATH);
		
		//Construct the image file now that we have the base folder and the relative path
		File image = new File(base, relPath);
		
		//Return the current image File
		return image;
	}
	
	/**
	 * Get the file where the image should be stored for archive. The file
	 * returned will not exist, the function using this information will copy
	 * a file to this location
	 * @param runway
	 * @return archive pdf location
	 */
	private static File getPhotoLocation(Runway runway, File image){
		//Get the path of the image 
		String path = image.getAbsolutePath();
		
		//Find what the extension is
		String extension = path.substring( path.lastIndexOf(".") );
		
		//Get the base folder for the runway
		File runwayBase = getRunwayBaseFolder(runway); // Base runway folder
		
		//Get the directory name inside of the runway folder
		// ==> "/photos/"
		String photoName = File.pathSeparator + IMAGE_FOLDER + File.pathSeparator; 
		
		//Get a descriptive file name based on runway info and current datetime
		String descriptiveName = getDescriptiveName(runway);
		
		//Construct an image file that will look similar to (Linux file structure)
		// /User/<user>/JungleJepps/repository/PC-6/KIW/photos/KIW-Kiwi_20130521-01.png
		File newImageFile = getNoConflictFile( new File( runwayBase, photoName + descriptiveName + extension) );
		
		//Return the file where the image should be stored
		return newImageFile;
	}
	
	/**
	 * Used to copy an image file to the repository
	 * @param runway
	 * @param image
	 * @return File the new image in the repository
	 * @throws IOException
	 */
	public static File copyImageFile(Runway runway, File image) throws IOException{
		//Get the location where the imagae should go
		File copy = getPhotoLocation(runway, image);
		
		//Copy the image passed in to the correct destination
		copyFileUsingFileChannels(image, copy);
		
		//Return the location of the newly copied file
		return copy;
	}

	/**
	 * Copies a Document to the documents section of the repository
	 * @param File the docuemnt to copy
	 * @return The copied file
	 */
	public static File addDocumentToRepository(File document) throws IOException {
		//Get the folder */repository/documents/
		File documents = new File(REPOSITORY, DOCUMENTS_FOLDER);
		
		// Make sure the folders are made
		documents.mkdirs(); 
		
		//Get the file to in the repo to store the copy
		File copy = new File(documents, document.getName());
		
		//Copy the file to the repo
		copyFileUsingFileChannels(document, copy); 
		
		//Return the new file location
		return copy; 
	}
	
	/**
	 * Copy file from source to destination
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
	private static void copyFileUsingFileChannels(File source, File dest) throws IOException {
		//Setup Channels for copying
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			//Create channels based on files
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			
			//Call the transferFrom function on the destination file
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			//Responsible housekeeping
			inputChannel.close();
			outputChannel.close();
		}
	}
	
	/**
	 * Tries to find a filename that does not exist by appending numbers to the filename
	 * @param file
	 * @return newFile
	 */
	private static File getNoConflictFile(File file){
		//Will hold the file pointer and point to a non-existant file
		File noConflict = file;
		
		//Get the path of the original file for parsing out information
		String absolutePath = file.getAbsolutePath();
		
		//Get the location of the last period so we can get the file extension
		int index = absolutePath.lastIndexOf("."); 
		
		//Get the original extension
		String extension = absolutePath.substring( index );
		
		//Get the original base path
		String path = absolutePath.substring(0, index);
		
		// Find a place where it DOES NOT ALREADY EXIST
		for(int i = 1; noConflict.exists(); i++)
		{
			String newPath = String.format("%s-%02d%s", path, i, extension);
			noConflict = new File( newPath );
		}

		//We are out of the for loop so now we have a file that does not exist
		return noConflict;
	}
	
	/**
	 * Build descriptive file name as specified in Design Document v0.1.0 Section 5.1.1
	 * <br /><br />
	 * Example: <code>ZZZZ-RunwayName_20130521-01</code>
	 * <br /><br/>
	 * ZZZZ is the 3-4 alphanumeric character unique identifier.<br />
	 * RunwayName is the complete name of the runway in CamelCase with no spaces.<br />
	 * 20130521 is the date the file was created in the YYYYMMDD format.<br/>
	 * 01 is a sequential number for the iteration of that file, should more than one file with the same name be created on the same date. <br />
	 * 
	 * @param runway
	 * @return A Descriptive name based on current time and the runway 
	 */
	private static String getDescriptiveName(Runway runway){
		Calendar today = Calendar.getInstance();
		
		/*
		 * FILENAME:	ZZZZ-RunwayName_20130521-01
		 *
		 * ZZZZ is the 3-4 alphanumeric character unique identifier.
		 * RunwayName is the complete name of the runway in CamelCase with no spaces.
		 * 20130521 is the date the file was created in the YYYYMMDD format.
		 * 01 is a sequential number for the iteration of that file, should more than one file with the same name be created on the same date.
		 */
		return String.format("%s-%s_%d%02d%02d", 
				runway.get(Field.RUNWAY_IDENTIFIER),
				runway.get(Field.RUNWAY_NAME),
				today.get( Calendar.YEAR ),
				today.get( Calendar.MONTH ),
				today.get( Calendar.DAY_OF_MONTH ));
	}
	
	/**
	 * Used to get the base folder for a runway-aircraft in the repository
	 * @param runway
	 * @return Runway-Aircraft folder in the repository
	 */
	private static File getRunwayBaseFolder(Runway runway){
		String runwayPath;
		
		//Create the appropriate path string
		//Example:
		//Aircraft ID: PC-6
		//Runway ID: KIW
		//Platform: Linux
		//runwayPath: /PC-6/KIW/
		runwayPath = String.format("%c%s%c%s%c", 
				File.separatorChar,
				runway.get(Field.AIRCRAFT_IDENTIFIER),
				File.separatorChar,
				runway.get(Field.RUNWAY_IDENTIFIER),
				File.separatorChar
				);

		//Find the correct directory using the Repository as a starting location 
		File base = new File(REPOSITORY, runwayPath);
		
		//Ensure the directories are made before returning the root directory File
		base.mkdirs();
		
		//Return the pointer to the root directory for the Runway-Aircraft relationship
		return base;
	}
}
