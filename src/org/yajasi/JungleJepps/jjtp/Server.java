package org.yajasi.JungleJepps.jjtp;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.WritableByteChannel;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.xhtmlrenderer.util.IOUtil;
import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.db.DatabaseConnection;
import org.yajasi.JungleJepps.db.DatabaseManager;
import org.yajasi.JungleJepps.db.Settings;
import org.yajasi.JungleJepps.db.SettingsManager;

import sun.misc.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.sun.net.httpserver.*;


public class Server { 
	private HttpServer server;
	private static Server instance;

	private static final File WEB_ROOT = new File("/src/xhtml/");

	public static void main(String[] args) throws IOException{
		start();
	}
	
	public static void start() throws IOException{
		instance = new Server();
		instance.startServer();
	}
	
	public static void stop(){
		instance.stopServer();
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
			/*
			if(db == null)
				throw new IllegalArgumentException();
			*/
			
			this.db = db;
		}

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String method = exchange.getRequestMethod().toUpperCase().trim();
			
			System.out.print(exchange.getRequestMethod() + ' ');
			System.out.print(exchange.getRequestURI().toString() + ' ');
			System.out.println(exchange.getProtocol());
			for(String key : exchange.getRequestHeaders().keySet()) 
				System.out.println(key + ": " + exchange.getRequestHeaders().get(key));


			if( method.equals("GET") )
			{
				doGet( exchange );
			}
			else if( method.equals("POST") ) 
			{
				doPost( exchange );
			}
			else if( method.equals("PUT") )
			{
				doPut( exchange );
			}
			else
			{
				String err405 = "<html><head><title>405 Method Not Allowed</title></head><body><h1>Method not allowed</h1><p>The requested HTTP method is not allowed on this server.</p><hr /></body></html>";
				OutputStream os;

				//HTTP Response Code 405: Method not allowed
				exchange.sendResponseHeaders(405, err405.length()); 
				
				os = exchange.getResponseBody();
				os.write( err405.getBytes() );
				os.close();
			}
	
		}
		
		/** 
		 *  Handles browser interaction and requests from client
		 *  for strip id list and strip select.
		 * @param HttpExchange exchange
		 * @throws IOException 
		 */
		private void doGet(HttpExchange exchange) throws IOException{
			String type = exchange.getRequestHeaders().getFirst("Accept");
			System.out.println("Handling GET...");
			
			if( type.equalsIgnoreCase( "application/json" ) )
			{
				handleGetDatabaseRequest( exchange );
			}
			else if(type.equalsIgnoreCase( "text/plain" ) && 
					exchange.getRequestURI().toASCIIString().equalsIgnoreCase("/settings") )
			{
				handleGetSettingsRequest( exchange );
			} 
			else 
			{
				// handle general http html/pdf requests
			}
			
		/*	if(  )
				writeDefault(exchange);
			else if( exchange.getRequestURI().toASCIIString().endsWith(".pdf") )
			*/	
		}
		
		private void handleGetDatabaseRequest(HttpExchange exchange) throws IOException{
			System.out.println("Handling JSON GET...");
			exchange.getResponseHeaders().add("content-type", "application/json");
			Gson gson = new Gson();
			DatabaseConnection db = DatabaseManager.getDatabase();
			OutputStream os = exchange.getResponseBody();
			
			String path = exchange.getRequestURI().getPath();
			
			if( path.equals( "/*" ) )
			{
				System.out.println("Getting Runway Names...");
				// Get all runway names 
				String[] runwayIds;
				//runwayIds = db.getAllRunwayIds();
				runwayIds = new String[]{"KIWI", "AMA","JFK"};
				String json = gson.toJson(runwayIds);
				
				System.out.println(json);

				exchange.sendResponseHeaders(HttpStatus.SC_OK, json.length());
				os.write( json.getBytes() );
				os.close();
			}
			else if( path.startsWith( "/runway" ) )
			{
				String runwayId = null; 
				List<NameValuePair> params = new URIBuilder(exchange.getRequestURI()).getQueryParams();
				for(NameValuePair param : params)
					if( param.getName().equalsIgnoreCase("rid") )
						runwayId = param.getValue();
				if(runwayId == null)
				System.out.println("Getting Runway: " + runwayId);
				// Get specific runway 
				Runway runway;
				//runway = db.getRunway(runwayId);
				runway = new Runway();
				String json = gson.toJson(runway);
				
				System.out.println(json);

				exchange.sendResponseHeaders(HttpStatus.SC_OK, json.length());
				os.write( json.getBytes() );
				os.close();
			}
			else {
				templateResponse(exchange, HttpStatus.SC_NOT_FOUND);
			}
		}
		
		private void handleGetSettingsRequest(HttpExchange exchange) throws IOException {
			//SettingsManager.sentToOutputStream(stream);
			Long size = SettingsManager.getLength();
			exchange.sendResponseHeaders(HttpStatus.SC_OK, size);
			OutputStream os = exchange.getResponseBody(); 
			InputStream is = SettingsManager.getSettingsStream();
			
			write( is, os, size.intValue() );
			
			os.close();
		}
		
		
		private void writeDefault(HttpExchange exchange) throws IOException{
			File outputHtml = new File("src/xhtml/pdf-test.html");
			int size = (int) outputHtml.length();
			InputStream input = new FileInputStream(outputHtml);

			exchange.getResponseHeaders().add("content-type", "text/html");
			exchange.sendResponseHeaders(200, size);
			
			OutputStream os = exchange.getResponseBody();
			byte[] bytes = new byte[ size ];
			input.read(bytes);
			
			os.write(bytes);
			
			//new FileInputStream( outputHtml ).getChannel().transferTo( Long.valueOf(0), (Long) outputHtml.length(), (WritableByteChannel) os);
			os.close();
		}
		
		private void writePdf(HttpExchange exchange) throws IOException{
	
			
			File pdf = new File("pdf-repo/output.pdf");
			InputStream input = new FileInputStream(pdf);

			Long size = pdf.length();
			exchange.getResponseHeaders().add("content-type", "application/pdf");
			exchange.sendResponseHeaders(200, size);
			
			write(input, exchange.getResponseBody(), size.intValue() );
			
			exchange.getResponseBody().close();			
		}
		
		private void write(InputStream is, OutputStream os, int size){
			byte[] bytes = new byte[ size ];
			try {
				is.read(bytes);
				os.write(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		
		/**
		 * Handles Sync requests from JJ Mobile 
		 * @param exchange
		 * @throws IOException 
		 */
		private void doPost(HttpExchange exchange) throws IOException{
			String resp = "<h1>METHOD: POST</h1>";
			
			exchange.sendResponseHeaders(200, resp.length());
			
			OutputStream os = exchange.getResponseBody();
			os.write( resp.getBytes() );
			os.close();
		}
		
		/**
		 * Handles upsert requests from client
		 * @param exchange
		 */
		private void doPut(HttpExchange exchange){
			
		}		
		
		private void templateResponse(HttpExchange exchange, int httpStatusCode){
			File outputFile = new File(WEB_ROOT, httpStatusCode + ".html");
			sendFile( exchange, outputFile, httpStatusCode );
		}
		
		private void sendFile(HttpExchange exchange, File file, int status){
			OutputStream os;
			InputStream is;
			Long size = file.length();
			byte[] data = new byte[ size.intValue() ];

			try {
				is = new FileInputStream(file);
				is.read( data );

				exchange.sendResponseHeaders(status, size);
				
				os = exchange.getResponseBody();
				os.write( data );
				
				os.close();
			} catch (FileNotFoundException e){
				templateResponse(exchange, HttpStatus.SC_NOT_FOUND);
			} catch (IOException e) {
				templateResponse(exchange, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}
}
