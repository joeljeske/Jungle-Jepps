package org.yajasi.JungleJepps.jjtp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.db.DatabaseConnection;
import org.yajasi.JungleJepps.db.DatabaseException;
import org.yajasi.JungleJepps.db.DatabaseManager;
import org.yajasi.JungleJepps.db.Settings;
import org.yajasi.JungleJepps.db.SettingsManager;
import org.yajasi.JungleJepps.pdf.Repository;

import com.google.gson.Gson;
import com.sun.net.httpserver.*;


public class Server { 
	private HttpServer server;
	private static Server instance;
	private static boolean started = false;

	private static final File WEB_ROOT;
	
	static {
		String root = DatabaseManager.getSettings().getStringForKey(Settings.WEB_ROOT);
		WEB_ROOT = new File(root);
	}

	public static void main(String[] args) throws IOException{
		start();
	}
	
	public static void start() throws IOException{
		if( !started )
		{
			started = true;
			instance = new Server();
			instance.startServer();
		}
	}
	
	public static void stop(){
		if( started )
		{
			started = false;
			instance.stopServer();
		}
	}
	
	
	private Server(){}
	
	public void startServer() throws IOException{
		DatabaseConnection db = DatabaseManager.getDatabase();
		JJTPHandler handler = new JJTPHandler( db );
		
		server = HttpServer.create( new InetSocketAddress( JungleJeppsmDNS.SERVER_PORT ), 0 );
		server.createContext("/", handler);
		server.start();
		
		JungleJeppsmDNS.startBroadcasting();
	}
	
	public void stopServer(){
		server.stop(0);
	}
	
	/**
	 * Inner class to implement HttpHandler. Enables the seamless handling of
	 * all HTTP Requests and Methods
	 * @author joeljeske14
	 *
	 */
	public class JJTPHandler implements HttpHandler {
		
		private DatabaseConnection db;
		
		public JJTPHandler(DatabaseConnection db){
			this.db = db;
		}

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String method = exchange.getRequestMethod().toUpperCase().trim();
			System.out.println("# HTTP REQUEST ###########################");
			System.out.print(exchange.getRequestMethod() + ' ');
			System.out.print(exchange.getRequestURI().toString() + ' ');
			System.out.println(exchange.getProtocol());
			for(String key : exchange.getRequestHeaders().keySet()) 
				System.out.println(key + ": " + exchange.getRequestHeaders().get(key));
			System.out.println("##########################################\n");


			if( method.equals("GET") )
			{
				doGet( exchange );
			}
			else if( method.equals("POST") ) 
			{
				doPost( exchange );
			}
			else if( method.equals("PATCH") )
			{
				doPatch( exchange );
			}
			else
			{
				//Method not allowed
				templateResponse(exchange, HttpStatus.SC_METHOD_NOT_ALLOWED);
			}
	
		}
		
		/** 
		 *  Handles browser interaction and requests from client
		 *  for strip id list and strip select.
		 * @param HttpExchange exchange
		 * @throws IOException 
		 */
		private void doGet(HttpExchange exchange) throws IOException{
			String path = exchange.getRequestURI().getPath().toLowerCase();
			System.out.println("Handling GET...");
			
			if( path.startsWith("/application/") )
			{
				handleGetDatabaseRequest( exchange );
			}
			else if( path.startsWith("/settings") )
			{
				handleGetSettingsRequest( exchange );
			} 
			else if( path.startsWith("/repository/") )
			{
				handleRepository(exchange);
			}
			else
			{
				templateResponse(exchange, HttpStatus.SC_NOT_FOUND);
			}
		}
		
		
		/**
		 * Handles upsert requests from client
		 * @param exchange
		 */
		private void doPatch(HttpExchange exchange){
			Gson json = new Gson();
			BufferedReader reader;
			Runway runway;
			reader = new BufferedReader( new InputStreamReader( exchange.getRequestBody() ) );
			
			runway = json.fromJson(reader, Runway.class);
			
			try {
				boolean success = db.updateRunway(runway);
				int status = success ? HttpStatus.SC_OK : HttpStatus.SC_CONFLICT;
				exchange.sendResponseHeaders(status, 0L);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DatabaseException e) {
				e.printStackTrace();
			} 
		}	
		
		/**
		 * Handles sync requests from JJ Mobile
		 * @param exchange
		 */
		private void doPost(HttpExchange exchange){
			
		}	
		
		private void handleGetDatabaseRequest(HttpExchange exchange) throws IOException{
			OutputStream os = exchange.getResponseBody();
			String output = null;
			String path = exchange.getRequestURI().getPath().toLowerCase();
			System.out.println("Handling application request...");
			
			if( path.startsWith("/application/runway/") )
			{
				output = getRunwayData(exchange);
			}
			
			else if( path.startsWith( "/application/aircraft/" ) )
			{
				output = getAircraftData(exchange);
			}
			else if( path.startsWith( "/application/aircraft/" ) )
			{
				output = getAircraftData(exchange);
			}
			else 
			{
				templateResponse(exchange, HttpStatus.SC_NOT_FOUND);
			}
			
			
			exchange.sendResponseHeaders(HttpStatus.SC_OK, output.length());
			os.write( output.getBytes() );
			os.close();

		}
		
		/**
		 * This method handles sending the settings to the client.
		 * It serializes all the settings and then sends it. 
		 * @param exchange
		 * @throws IOException
		 */
		private void handleGetSettingsRequest(HttpExchange exchange) throws IOException {
			exchange.getResponseHeaders().add("Content-type", "application/json");
			Long size = SettingsManager.getLength();
			exchange.sendResponseHeaders(HttpStatus.SC_OK, size);
			OutputStream output = exchange.getResponseBody(); 
			InputStream input = SettingsManager.getSettingsStream();
			write(input, output);
		}
		
		private void handleRepository(HttpExchange exchange){
			String repoBase = "/repository/";
			String path = exchange.getRequestURI().getPath();
			
			if( path.equals( repoBase ) )
			{
				// Load repository view
				OutputStream os = exchange.getResponseBody();
				String output;
				output = createHtmlRepoView();
				try {
					exchange.sendResponseHeaders(HttpStatus.SC_OK, output.length());
					os.write( output.getBytes() );
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			else if( path.startsWith( repoBase ) )
			{
				String location = path.substring(repoBase.length(), path.length());
				
				location.replaceAll("/", File.separator); //Make file system independent				
				location += Repository.PUBLISHED_NAME + Repository.DOCUMENT_EXTENSION;
				
				File pdf = new File(Repository.REPOSITORY, location);
								
				exchange.getResponseHeaders().add("Content-type", "application/pdf");
				sendFile(exchange, pdf, HttpStatus.SC_OK);
				
			}
		}
		
		/**
		 * This method handles all requests made to /application/runway/
		 * @param exchange
		 * @return String serialized resultant to respond with
		 */
		private String getRunwayData(HttpExchange exchange){
			exchange.getResponseHeaders().add("Content-type", "application/json");
			Gson json = new Gson(); // Json serializer
			URI uri = exchange.getRequestURI();
			Map<String, String> params;
			String output = null;

			String q = uri.getQuery();
			q = q == null ? "" : q;
			params = parseParameters(q); //Get the parameters in a usable format

			String aircraftId = params.get("aid");
			String runwayId = params.get("rid");

			// Find out what we are supposed do with this request bases on what parameters we have
			// Once we know what to do, we make a db request and serialze the result
			if( aircraftId != null && !aircraftId.isEmpty() ) 
			{
				// GET A SPECIFIC RUNWAY
				if(runwayId != null && !runwayId.isEmpty() ) 
				{
					try {
						Runway runway = db.getRunway(runwayId, aircraftId);
						output = json.toJson(runway);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
				}
				// GET ALL RUNWAY IDS FOR AN AIRCRAFT
				else 
				{
					try {
						String[] runwayIds = db.getAllRunwayIds(aircraftId);
						output = json.toJson(runwayIds);
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
				}
			}
			// GET ALL RUNWAY IDS
			else 
			{
				System.out.println("Getting all runway ids");
				try {
					String[] runwayIds = db.getAllRunwayIds();
					output = json.toJson(runwayIds);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}
			
			// Return the serialized result to send to the client
			return output;
		}
		
		/**
		 * This method handles all requests made to /application/aircraft
		 * @param exchange
		 * @return String the serialized resultant of the request
		 */
		private String getAircraftData(HttpExchange exchange) {
			exchange.getResponseHeaders().add("Content-type", "application/json");
			//We can assume they only want all the aircraft ids until more methods are needed
			Gson json = new Gson();
			String output = null;
			
			try {
				String[] aircraftIds = db.getAllAircraftIds();	// Get all ids
				output = json.toJson(aircraftIds); //Serialize
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			return output;
		}
		
		private String createHtmlRepoView() {
			StringBuilder html = new StringBuilder();
			html.append("<html><head><title>Jungle Jepps Repository</title></head>");
			html.append("<body><h1>Jungle Jepps Repository Viewer</h1>");
			html.append("<table style='width:100%; height:100%'><tr><td style='width: 15%; vertical-align:top;'>");
			html.append("<h2>Runway Charts</h2><br /><div style='text-align:right'");
			
			String[] aids = null;
			try {
				aids = db.getAllAircraftIds();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			for(String aid : aids)
			{
				String[] rids = null;
				try {
					rids = db.getAllRunwayIds(aid);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				
				for(String rid : rids)
				{
					html.append("<div><a href='");
					html.append(aid);
					html.append("/");
					html.append(rid);
					html.append("/");
					html.append("' target='pdf'>");
					html.append(aid);
					html.append(" -> ");
					html.append(rid);
					html.append("</a></div>");
				}
			}
			
			html.append("</div></td><td><iframe style='width:100%; height:100%' name='pdf'></td>");
			html.append("</tr></table></body></html>");
			
			return html.toString();
		}

	
	
		
		/////////////////////////////////////////////////////////////////////////////
		/* HELPER FUNCTIONS*/
		
		private Map<String, String> parseParameters(String query){
			Map<String, String> params = new HashMap<String, String>();
			if(query == null || query.isEmpty())
				return params;
			
			if(query.startsWith("?"))
				query = query.substring(1, query.length());
			
			String[] splits = query.split("&");
			for(String p : splits)
			{
				String[] param = p.split("=");
				params.put(param[0], param[1]);
			}
			return params;
		}
		
		private void templateResponse(HttpExchange exchange, int httpStatusCode){
			exchange.getResponseHeaders().add("Content-type", "text/html");
			File outputFile = new File(WEB_ROOT, httpStatusCode + ".html");
			sendFile( exchange, outputFile, httpStatusCode );
		}
		
		private void sendFile(HttpExchange exchange, File file, int status){
			InputStream is;

			try {
				
				is = new FileInputStream(file); // Setup the input from the file
				exchange.sendResponseHeaders(status, file.length()); //Send headers
				write(is, exchange.getResponseBody()); //Send the body
				
			} catch (FileNotFoundException e){
				templateResponse(exchange, HttpStatus.SC_NOT_FOUND);
				
			} catch (IOException e) {
				templateResponse(exchange, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}
		}
		
		private void write(InputStream is, OutputStream os) throws IOException{
			byte[] data = new byte[1024];
			int length;

			// Read until the amount read is 0
			while( (length = is.read(data) ) > 0 )
				os.write( data, 0, length ); //Write data at offset 0 with length
		}
	}
}
