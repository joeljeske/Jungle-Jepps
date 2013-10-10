package org.yajasi.JungleJepps.jjtp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.WritableByteChannel;

import org.apache.http.HttpStatus;
import org.xhtmlrenderer.util.IOUtil;
import org.yajasi.JungleJepps.db.DatabaseConnection;
import org.yajasi.JungleJepps.db.DatabaseManager;

import sun.misc.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.sun.net.httpserver.*;


public class Server { 
	private HttpServer server;


	public static void main(String[] args) throws IOException{
		new Server().startServer();
	}
	
	public Server(){
		
	}
	
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
			String type = exchange.getRequestHeaders().getFirst("Accepts");
			System.out.println("Handling GET...");
			
			if( type.equalsIgnoreCase( "application/json" ) )
			{
				handleGetDatabaseRequest( exchange );
			}
			else 
			{
				//handle html and pdf requests
				
			}
			
			if( exchange.getRequestURI().toASCIIString().equals("/") )
				writeDefault(exchange);
			else if( exchange.getRequestURI().toASCIIString().endsWith(".pdf") )
				writePdf(exchange);
				
		}
		
		private void handleGetDatabaseRequest(HttpExchange exchange) throws IOException{
			System.out.println("Handling JSON GET...");
			exchange.getResponseHeaders().add("content-type", "application/json");
			Gson gson = new Gson();
			DatabaseConnection db = DatabaseManager.getDatabase();
			OutputStream os = exchange.getResponseBody();
			
			String path = exchange.getRequestURI().getPath();
			
			System.out.println( path );
			
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
			else
			{
				// Get specific runway
			}
	
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

			int size = (int) pdf.length();
			exchange.getResponseHeaders().add("content-type", "application/pdf");
			exchange.sendResponseHeaders(200, size);
			
			OutputStream os = exchange.getResponseBody();
			byte[] bytes = new byte[ size ];
			input.read(bytes);
			
			os.write(bytes);
			
			os.close();			
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
	}
}
