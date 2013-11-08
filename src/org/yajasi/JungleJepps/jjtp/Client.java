package org.yajasi.JungleJepps.jjtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.db.DatabaseConnection;
import org.yajasi.JungleJepps.db.DatabaseException;

import com.google.gson.Gson;



public class Client implements DatabaseConnection {
	private HttpClient client; 
	
	/**
	 * This example demonstrates the retrieval of runway ids from 
	 * a LAN conected server.
	 * @param args
	 * @throws InterruptedException
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws InterruptedException, DatabaseException{
		Client client = new Client();
		Thread.sleep(1000);
		String aircraftId = "C-5";
		String[] runways = client.getAllRunwayIds(aircraftId);
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
	/////////////////////////////////////////////////////////////////////////////////

	@Override
	public String[] getAllAircraftIds() throws DatabaseException{
		String[] list; // To store all the Ids
		Gson gson = new Gson(); // Google's json parser
		BufferedReader reader; // Reader from server json message

		reader = getApplicationJson("/application/aircraft/", null);
		
		// Build the response data back into a String[] 
		list = gson.fromJson(reader, String[].class);	
		return list;	}
	
	public String[] getAllRunwayIds() throws DatabaseException {
		return getAllRunwayIds("");
	}
	
	/**
	 * This method will return all the runway Ids for this database connection
	 */
	@Override
	public String[] getAllRunwayIds(String aircraftId) throws DatabaseException {
		String[] list; // To store all the Ids
		Gson gson = new Gson(); // Google's json parser
		BufferedReader reader; // Reader from server json message
		
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("aid", aircraftId));
		
		reader = getApplicationJson("/application/runway/", parameters);
		
		// Build the response data back into a String[] 
		list = gson.fromJson(reader, String[].class);	
		return list;
	}

	@Override
	public Runway getRunway(String runwayId, String aircraftId) throws DatabaseException {
		Runway runway; //The runway object to be deserialized into
		Gson gson = new Gson(); // Google's json parser
		BufferedReader reader; // Reader from server json message
		
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("rid", runwayId));
		parameters.add(new BasicNameValuePair("aid", aircraftId));
		
		reader = getApplicationJson("/application/runway/", parameters);
		
		// Build the response data back into a Runway 
		runway = gson.fromJson(reader, Runway.class);	
		return runway;
	}

	@Override
	public boolean updateRunway(Runway runway) throws DatabaseException {
		StatusLine status;
		Gson gson = new Gson(); // Google's json parser
		String json = gson.toJson(runway);
		
		status = sendApplicationJson("/application/runway/update", null, json);
		
		if(status.getStatusCode() != HttpStatus.SC_OK)
			throw new DatabaseException(status.getReasonPhrase());
		
		return true;
	}
	
	public InputStream getSettingsStream(){
		URI uri = null; //The URI that will be built
		HttpResponse response = null; // The response from the Server
		InputStream is = null; // What will read the response from the server 
		HttpGet request = new HttpGet(); // Using HTTP method GET
		
		// Application requests for settings sends it as plain text to load directly into the settings manager
		request.addHeader("Accept", "text/plain");
		
		try {
			uri = new URIBuilder( JungleJeppsmDNS.getProvider() ) // Partial URI containing host and port <host>:<port> 
						.setScheme("http") // Not secure
						.setPath("/settings") // Request to list all runway ids
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
			is = response.getEntity().getContent();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Return the raw InputStream 
		return is;
	}


	@Override
	public boolean close() {
		JungleJeppsmDNS.stopDiscovery();
		client = null;
		return true;
	}

	
	private BufferedReader getApplicationJson(String path, List<NameValuePair> parameters) throws DatabaseException{		
		URI uri = null; //The URI that will be built
		HttpResponse response = null; // The response from the Server
		BufferedReader reader = null; // What will read the response from the server 
		HttpGet request = new HttpGet(); // Using HTTP method GET
		
		// Application requests over LAN will only use JSON
		request.addHeader("Accept", "application/json");
		
		try {
			uri = new URIBuilder( JungleJeppsmDNS.getProvider() ) // Partial URI containing host and port <host>:<port> 
						.setScheme("http") // Not secure
						.setPath(path) // Request path
						.addParameters(parameters) //Add url parameters
						.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new DatabaseException(e);			
		}

		request.setURI(uri); // Use this URI for the request
		
		try {
			response = client.execute(request); // Execute HTTP method 
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new DatabaseException(e);			
		} catch (IOException e) {
			e.printStackTrace();
			throw new DatabaseException(e);			
		}
		
		try {
			// Create reader for the response data
			reader = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
		} catch (IllegalStateException e) {
			e.printStackTrace();
			throw new DatabaseException(e);			
		} catch (IOException e) {
			e.printStackTrace();
			throw new DatabaseException(e);			
		}
		
		return reader;
	}
	
	private StatusLine sendApplicationJson(String path, List<NameValuePair> parameters, String body) throws DatabaseException{
		URI uri = null; //The URI that will be built
		HttpResponse response = null; // The response from the Server
		BufferedReader reader = null; // What will read the response from the server 
		HttpPatch request = new HttpPatch(); // Using HTTP method PATCH
		
		// Application requests over LAN will only use JSON
		request.addHeader("Accept", "application/json");
		
		try {
			uri = new URIBuilder( JungleJeppsmDNS.getProvider() ) // Partial URI containing host and port <host>:<port> 
						.setScheme("http") // Not secure
						.setPath(path) // Request path
						.addParameters(parameters) //Add url parameters
						.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new DatabaseException(e);			
		}

		request.setURI(uri); // Use this URI for the request
		
		try {
			request.setEntity( new StringEntity(body) ); //Set the body of the request
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new DatabaseException(e);			
		}
		
		try {
			response = client.execute(request); // Execute HTTP method 
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new DatabaseException(e);			
		} catch (IOException e) {
			e.printStackTrace();
			throw new DatabaseException(e);
		}
		
		// Return its status
		return response.getStatusLine();
	}


	
}
