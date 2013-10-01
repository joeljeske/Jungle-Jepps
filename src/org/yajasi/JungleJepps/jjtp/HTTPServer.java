package org.yajasi.JungleJepps.jjtp;

import java.io.IOException;
import java.io.OutputStream;
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

			
			String resp = "<html><body><h1>METHOD: " + exchange.getRequestMethod() + "</h1><br /></body>" +
					"<script src='//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js'></script>" +	
					"<script>window.setTimeout(dopost, 2500);" +
						"function dopost(){ $.post('http://localhost:8080', function(resp){ $('body').append(resp); }); }" +
						"function doget(){ $.get('http://localhost:8080', function(resp){ $('body').append(resp); }); }" +

						"</script>"+
					"</html>";
			exchange.sendResponseHeaders(200, resp.length());
			
			OutputStream os = exchange.getResponseBody();
			os.write( resp.getBytes() );
			os.close();
		}
		
	}
}
