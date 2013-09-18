package org.yajasi.JungleJepps.jjtp;


public class JJTP {
	
	// JJTP Characteristics
	public static final int SERVER_PORT = 1777;
	public static final int VERSION_MAJOR = 1;
	public static final int VERSION_MINOR = 0;
	public static final String MDNS_SERVICE_TYPE = "_junglejepps._tcp.local.";

	
	// JJTP Actions 
	/** 
	 * Action used to get information from the server.
	 * Client will use this action to get
	 * <ul>
	 * <li>List of Runway Strips</li>
	 * <li>ONE Runway Strip information set</li>
	 * </ul>
	 */
	public static final String GET 		= "GET";
	
	/** 
	 * Action used to get information change information 
	 * about a runway.
	 */
	public static final String UPDATE 	= "UPDATE";
	
	/**
	 * Action will be used to synchronize a file repository
	 * with the server.
	 */
	public static final String SYNC 	= "SYNC";
	
	// JJTP Response Codes
	/*
		1xx Informational
		2xx Successful
		3xx Redirection
		4xx Client Error
		5xx Server Error
	*/ 
	
	
	

}
