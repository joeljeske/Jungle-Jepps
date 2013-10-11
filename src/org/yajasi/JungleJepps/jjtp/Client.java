package org.yajasi.JungleJepps.jjtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.db.DatabaseConnection;

import com.google.gson.Gson;



public class Client implements DatabaseConnection{
	private HttpClient client; 
	
	/**
	 * This example demonstrates the retrieval of runway ids from 
	 * a LAN conected server.
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException{
		Client client = new Client();
		Thread.sleep(1000);
		String[] runways = client.getAllRunwayIds();
		for(String runway : runways){
			System.out.println(runway);
		}
	}
	
	
	/**
	 * Constructor to create a new JungleJepps DatabaseConnection 
	 * to access the database on a primary instance over the LAN
	 */
	public Client() {
		client = HttpClientBuilder.create().build();
		JungleJeppsmDNS.startDiscovery();
	} 
	
	/////////////////////////////////////////////////////////////////////////////////
	// START DatabaseConnection Implementation
	//
	//
	// 	public String[] getAllRunwayIds()
	//
	// 	public Runway getRunway(String runwayId)
	//
	// 	public boolean updateRunway(Runway runway) 
	/////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will return all the runway Ids for this database connection
	 */
	@Override
	public String[] getAllRunwayIds() {
		String[] list; // To store all the Ids
		URI uri = null; //The URI that will be built
		HttpResponse response = null; // The response from the Server
		BufferedReader reader = null; // What will read the response from the server 
		Gson gson = new Gson(); // Googles JSON parser
		HttpGet request = new HttpGet(); // Using HTTP method GET
		
		// Application requests over LAN will only use JSON
		request.addHeader("Accepts", "application/json");
		
		try {
			uri = new URIBuilder( JungleJeppsmDNS.getProvider() ) // Partial URI containing host and port <host>:<port> 
						.setScheme("http") // Not secure
						.setPath("/*") // Request to list all runway ids
						.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
					
		request.setURI(uri); // Use this URI for the request
		
		try {
			response = client.execute(request); // Execute HTTP method 
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			// Create reader for the response data
			reader = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Build the response data back into a String[] 
		list = gson.fromJson(reader, String[].class);	
		return list;
	}

	@Override
	public Runway getRunway(String runwayId) {
		Runway runway;
		HttpResponse response = null;
		BufferedReader reader = null;
		Gson gson = new Gson();
		HttpGet request = new HttpGet();
		
		request.addHeader("Accepts", "application/json");

		URI uri = null;
		try {
			uri = new URIBuilder( JungleJeppsmDNS.getProvider() )
						.setScheme("http")
						.setPath("/runway")
						.addParameter("rid", runwayId)
						.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
					
		request.setURI(uri);
		
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		try {
			reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		runway = gson.fromJson(reader, Runway.class);	
		return runway;
	}

	@Override
	public boolean updateRunway(Runway runway) {
		HttpPatch request = new HttpPatch();
		request.addHeader("Accepts", "application/json");

		throw new UnsupportedOperationException();
	}

	
}
