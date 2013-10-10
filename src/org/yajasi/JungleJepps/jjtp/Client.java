package org.yajasi.JungleJepps.jjtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.db.DatabaseConnection;

import com.google.gson.Gson;



public class Client implements DatabaseConnection{
	private HttpClient client; 
	
	
	public Client() {
		client = HttpClientBuilder.create().build();
		JungleJeppsmDNS.startDiscovery();
	} 
	
	
	public static void main(String[] args) throws InterruptedException{
		Client client = new Client();
		Thread.sleep(1000);
		String[] runways = client.getAllRunwayIds();
		for(String runway : runways){
			System.out.println(runway);
		}
	}
	
	
	@Override
	public String[] getAllRunwayIds() {
		String[] list;
		HttpResponse response = null;
		Gson gson = new Gson();
		BufferedReader reader = null;

		HttpGet get = new HttpGet();
		get.addHeader("Accepts", "application/json");
		
		URI provider = getProvider();

		URI uri = null;
		try {
			uri = new URIBuilder(provider)
						.setScheme("http")
						.setPath("/*")
						.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
					
		get.setURI(uri);
		
		try {
			response = client.execute(get);
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
		
		list = gson.fromJson(reader, String[].class);	
		return list;
	}

	@Override
	public Runway getRunway(String runwayId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean updateRunway(Runway runway) {
		throw new UnsupportedOperationException();
	}

	private URI getProvider(){
		URI[] providers = JungleJeppsmDNS.getProviders();
		if( providers.length > 0 )
			return providers[0];
		return null;
	}
	
}
