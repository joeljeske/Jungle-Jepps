package org.yajasi.JungleJepps.jjtp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.yajasi.JungleJepps.db.DatabaseConnection;
import org.yajasi.JungleJepps.db.DatabaseManager;

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
			else if( method.equals("PATCH") )
			{
				doPatch( exchange );
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
			String host = exchange.getLocalAddress().getHostName();
			String port = String.valueOf( JungleJeppsmDNS.SERVER_PORT );
			
			String resp = "<html><body><h1>METHOD: " + exchange.getRequestMethod() + "</h1><br /></body>" +
					"<script src='//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js'></script>" +	
					"<script>window.setTimeout(dopost, 2500);" +
						"function dopost(){ $.post('http://" + host + ":" + port + "', function(resp){ $('body').append(resp); }); }"+ 
						"</script>"+
					"</html>";
			
			exchange.sendResponseHeaders(200, resp.length());
			
			OutputStream os = exchange.getResponseBody();
			os.write( resp.getBytes() );
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
		private void doPatch(HttpExchange exchange){
			
		}		
	}
}
