package org.yajasi.JungleJepps.jjtp;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.*;


public class HTTPServer { 
	

	public static void main(String[] args) throws IOException{
		
		HttpServer server;
		
		JungleJeppsmDNS.startBroadcasting();
		
		server = HttpServer.create(new InetSocketAddress(JungleJeppsmDNS.SERVER_PORT), 0); 
		server.createContext("/", new Handler());
		server.start();
	}
	
	public static class Handler implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			
			System.out.print(exchange.getRequestMethod() + ' ');
			System.out.print(exchange.getRequestURI().toString() + ' ');
			System.out.println(exchange.getProtocol());
			for(String key : exchange.getRequestHeaders().keySet()) 
				System.out.println(key + ": " + exchange.getRequestHeaders().get(key));
			
			//System.out.println(exchange.getRequestBody().);
		}
		
		
		
	}
}
