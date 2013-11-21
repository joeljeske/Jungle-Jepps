/////////////////////////////////////////////////////////////////////////
// Author: Joel Jeske
// File: JungleJeppsmDNS.java
// Class: org.yajasi.JungleJepps.jjtp.JungleJeppsmDNS
//
// Target Platform: Java Virtual Machine 
// Development Platform: Apple OS X 10.9
// Development Environment: Eclipse Kepler SDK
// 
// Project: Jungle Jepps - Desktop
// Copyright 2013 YAJASI. All rights reserved. 
// 
// Objective: This class is used to manage the broadcasting and discovery 
// of a custom Jungle Jepps mDNS service. Depending on if the instance is
// primary or secondary, this class is used to start broadcasting or to 
// return a IP address and port number so the client can connect to the 
// primary instance. 
//
/////////////////////////////////////////////////////////////////////////


package org.yajasi.JungleJepps.jjtp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceListener;

import org.apache.http.client.utils.URIBuilder;

/**
 * This class manages the client-side and server-side of multicast DNS. 
 * It is capable of broadcasting its services and finding providers 
 * that match its service critera.
 * @author Joel Jeske
 */
public class JungleJeppsmDNS {
	
	// mDNS service values 
	public static final String PROPRIETARY_MDNS_SERVICE_TYPE = "_junglejepps._tcp.local.";
	public static final String HTTP_MDNS_SERVICE_TYPE = "_http._tcp.local.";
	public static final int SERVER_PORT = 6545;
	private static final String MDNS_SERVICE_NAME = "JJTP service";
	private static final String MDNS_SERVICE_NAME_PRETTY = "Jungle Jepps Desktop Service";

	// Must have 2 different objects for use when discovering and broadcasting
	// Specifically when testing and broadcasting and discovering from the same runtime 
	private static JmDNS broadcasting, discovery;
	private static Listener listener;
	
	// Client requested servers
	private static Queue<URI> providers = new ArrayDeque<URI>();
	
	// Set of listeners to notify of changes
	private static Set<ProviderStatusListener> listeners = new HashSet<ProviderStatusListener>();

	
	// Demonstration test function
	public static void main(String[] args) throws InterruptedException {
		 JungleJeppsmDNS.startBroadcasting();
		 Thread.sleep(8000);
		 JungleJeppsmDNS.stopBroadcasting();
		 /*
		 JungleJeppsmDNS.startDiscovery();
		 Thread.sleep(1000);
		 
		 for(URI provider : providers)
			 System.out.println(provider);
		 
		 JungleJeppsmDNS.stopDiscovery();
		 
		 
		 JungleJeppsmDNS.stopBroadcasting();
		 */
	 }
	
	/**
	 * Get the oldest providers found that is still available. 
	 * Returns null is there is no providers available.
	 * @return
	 */
	public static URI getProvider(){
		return providers.peek();
	}
	
	/**
	 * Starts searching for providers for Jungle Jepps
	 */
	public static void startDiscovery(){
		 if(discovery == null)
			try {
				discovery = JmDNS.create();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		 
		 System.out.println("Listening for service changes...");
		 
		 listener = new Listener();
		 discovery.addServiceListener(PROPRIETARY_MDNS_SERVICE_TYPE, listener);
	}
	
	/**
	 * Stops searching a listening for changes for service providers
	 */
	public static void stopDiscovery(){
		System.out.println("Stop listening for service changes...");
		
		// Clean up
		discovery.removeServiceListener(PROPRIETARY_MDNS_SERVICE_TYPE, listener);
		
		try {
			discovery.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	 }
	
	 /**
	  * This method starts a new thread to broadcast JungleJepps service 
	  * name over multicast DNS (mDNS). 
	  */
	 public static void startBroadcasting(){
		 //To create an instance of this abstract class,
		 //we must define its one needed function, run().
		 Thread startBroadcast = new Thread(){
			 public void run(){
				 //When we have a new thread ready, we want to start the time consuming
				 //process to broadcast our services to the world.
				 _startBroadcasting();
			 }
		 };
		 
		 //Once we have this anonymous class ready, we can start this thread. 
		 startBroadcast.start();
	 }
	 
	 /**
	  * Start the broadcasting services 
	  */
	 private static void _startBroadcasting(){
		 //We will broadcast two different services:
		 //A junglejepps service and an http service.
		 //This way our client application can connect uniquely to it
		 //and other http service discoveries can use it to see our public 
		 //http site, "http://<host>:<port>/repository/"
		 ServiceInfo jjtpService, httpService;

		 //If we do not have a JmDNS created yet, make one
		 if(broadcasting == null)
			try {
				broadcasting = JmDNS.create();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}		 
		 
		try 
		{
			// Assign service name and info
			jjtpService = ServiceInfo.create(PROPRIETARY_MDNS_SERVICE_TYPE, MDNS_SERVICE_NAME_PRETTY, SERVER_PORT, MDNS_SERVICE_NAME);
			httpService = ServiceInfo.create(HTTP_MDNS_SERVICE_TYPE, MDNS_SERVICE_NAME_PRETTY, SERVER_PORT, MDNS_SERVICE_NAME);

			// Register service and broadcast over LAN
			System.out.println("Registering service and starting broadcast...");
			broadcasting.registerService( jjtpService );
			broadcasting.registerService( httpService );
		}
		catch(IOException e){
			e.printStackTrace();
		}
	 }
	 
	 /**
	  * This method unregisters broadcaster
	  */
	 public static void stopBroadcasting(){
		System.out.println("Unregistering service and killing broadcast...");
		 
		try
		{
			//Stop all services
			broadcasting.unregisterAllServices();
			
			//Close the mDNS connectin 
			broadcasting.close();

			//Verbose
			System.out.println("Broadcast finished.");
		} 
		catch (IOException e) {
			System.out.println("Broadcast had error in stop command");
			e.printStackTrace();	 
		}
	 }
	 
	 /**
	  * This class implements the JmDNS Service listener and modifies
	  * the providers queue and notifies any listeners of changes
	  */
	 private static class Listener implements ServiceListener {
			
			/**
			 * Callback function to handle adding of services.
			 */
			public void serviceAdded(ServiceEvent serviceEvent) {
				//We dont care when they are added, only when resolved
			}
			
			/**
			 * Callback function to handle removal of services from list
			 */
			public void serviceRemoved(ServiceEvent serviceEvent) {
				ServiceInfo info = serviceEvent.getInfo();
				boolean primaryChange = false;
				
				for(String addr : info.getHostAddresses())
				{
					
					URI toRemove = buildAddress( addr, info.getPort() ) ;
					
					if( toRemove.equals( providers.peek() ) )
						primaryChange = true;
						
					providers.remove( toRemove );
				}
				
				if( primaryChange ) // Need to notify listeners
					notifyListeners();	
			}
			
			/**
			 * Callback function to handle addition (after resolution)
			 * of services to list
			 */
			public void serviceResolved(ServiceEvent serviceEvent) {
				//Get the info from the service
				ServiceInfo info = serviceEvent.getInfo();
				
				//Do we need to notify any one? 
				//We do if there are not providers to begin with, making this
				//provider a primary provider
				boolean notifyListeners = providers.isEmpty(); 
				
				//Get all the available providers from this service message
				for(String addr : info.getHostAddresses())
					//Providers is based on address and port
					providers.add( buildAddress( addr, info.getPort() ) );
				
				// Need to notify listeners?
				if( notifyListeners  && !providers.isEmpty())
					//Notify
					notifyListeners();				
			}
			
			/**
			 * This method builds an address URI based solely on the address and port
			 * @param address
			 * @param port
			 * @return URI the constructed provider
			 */
			private URI buildAddress(String address, int port){
				URI uri= null;
				
				try {
					//Use a handy URI builder and give it our information
					uri = new URIBuilder().setHost(address).setPort(port).build();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				
				return uri; 
			}
			
			private void notifyListeners(){
				//Get but do not remove our primary URI, the one at the front of our provider queue
				URI newProvider = providers.peek();
				
				//Tell all of our listeners of this new provider
				for(ProviderStatusListener listener : listeners)
					listener.newPrimaryProvider(newProvider);
			}
	}
	 
	
	 /**
	  * Register a listener to be notified of changes in providers
	  * @param listener
	  */
	 public static void registerChangeListener(ProviderStatusListener listener){
		 //Ignore invalid requests
		 if(listener == null) return;
		 
		 //Add it to our list
		 listeners.add(listener);
		 
		 //If we have a primary provider
		 if( !providers.isEmpty() )
			 //Notify this new listener immediately
			 listener.newPrimaryProvider( providers.peek() );
	 }
	 
	 /**
	  * Unregister a listener to stop receiving notifications of changes
	  * @param listener
	  */
	 public static void unregisterChangeListener(ProviderStatusListener listener){
		 //Simply remove this listener from our list of listeners
		 //Note: this uses address uniqueness so callers of this function
		 //must pass the exact instance previously registered
		 listeners.remove(listener);
	 }
	 
	 /**
	  * This is a listener interface used to allow sub-clients to listen on
	  * the statuses of providers.
	  */
	 public interface ProviderStatusListener {
		 /**
		  * This method will be called when the primary provider is changed. 
		  * This is called when the primary provider is removed and another 
		  * provider is available.
		  * @param URI primary provider
		  */
		 void newPrimaryProvider(URI newProvider);
	 }
}
