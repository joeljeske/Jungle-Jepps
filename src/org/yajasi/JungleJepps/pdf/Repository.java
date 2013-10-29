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

	private static final File REPOSITORY;
	private static final String PUBLISHED_NAME = "diagram";
	private static final String ARCHIVE_FOLDER = "archive";
	private static final String IMAGE_FOLDER = "photos";
	private static final String DOCUMENT_EXTENSION = ".pdf";
	
	static {
		String path = DatabaseManager.getSettings().getStringForKey(Settings.REPOSITORY_PATH);
		REPOSITORY = new File(path);
	}
	
	/**
	 * Used to get the file where the PDF should be published.
	 * @param runway
	 * @return pdf published location
	 */
	public static File getPublishLocation(Runway runway){
		File runwayBase = getRunwayBaseFolder(runway);
		String name = PUBLISHED_NAME + DOCUMENT_EXTENSION;
		
		return new File(runwayBase, name);
	}
	
	/**
	 * Get the file where the pdf should be stored for archive
	 * @param runway
	 * @return archive pdf location
	 */
	public static File getArchiveLocation(Runway runway){
		File runwayBase = getRunwayBaseFolder(runway); //Base runway folder
		String archiveName = File.pathSeparator + ARCHIVE_FOLDER + File.pathSeparator; // ==> "/archive/"
		String descriptiveName = getDescriptiveName(runway);
		
		File archiveLocation = getNoConflictFile( new File(runwayBase, archiveName + descriptiveName + DOCUMENT_EXTENSION) );
		
		return archiveLocation;
	}
	
	/**
	 * Get the file where the image should be stored for archive
	 * @param runway
	 * @return archive pdf location
	 */
	private static File getPhotoLocation(Runway runway, File image){
		String path = image.getAbsolutePath();
		String extension = path.substring( path.lastIndexOf(".") );
		File runwayBase = getRunwayBaseFolder(runway); // Base runway folder
		String photoName = File.pathSeparator + IMAGE_FOLDER + File.pathSeparator; // ==> "/photos/"
		String descriptiveName = getDescriptiveName(runway);
		
		File newImageFile = getNoConflictFile( new File( runwayBase, photoName + descriptiveName + extension) );
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
		File copy = getPhotoLocation(runway, image);
		copyFileUsingFileChannels(image, copy);
		return copy;
	}
	
	/**
	 * Copy 2 files
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
	private static void copyFileUsingFileChannels(File source, File dest) throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
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
		File noConflict = file;
		String absolutePath = file.getAbsolutePath();
		int index = absolutePath.lastIndexOf("."); // Last period (i.e. extension separator)
		String extension = absolutePath.substring( index );
		String path = absolutePath.substring(0, index);
		
		// Find a place where it DOES NOT ALREADY EXIST
		for(int i = 1; noConflict.exists(); i++)
		{
			String unique = String.format("-%02d", i);
			noConflict = new File( path + unique + extension);
		}
		
		return noConflict;
	}
	
	/**
	 * Build descriptive file name as specified in Design Document v0.1.0 Section 5.1.1 
	 * @param runway
	 * @return
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
	 * @return runway folder in the repository
	 */
	private static File getRunwayBaseFolder(Runway runway){
		String runwayPath;
		
		runwayPath  = File.pathSeparator;
		runwayPath += runway.get(Field.AIRCRAFT_IDENTIFIER);
		runwayPath += File.pathSeparator;
		runwayPath += runway.get(Field.RUNWAY_IDENTIFIER);
		runwayPath += File.pathSeparator;
		
		return new File(REPOSITORY, runwayPath);
	}
}
