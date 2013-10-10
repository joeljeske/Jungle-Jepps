package org.yajasi.JungleJepps.jjtp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceListener;

import org.apache.http.client.utils.URIBuilder;

/**
 * This class manages the client-side and server-side of multicast DNS. 
 * It is capable of broadcasting its services as 
 * @author Joel Jeske
 */
public class JungleJeppsmDNS {
	
	// mDNS service values 
	public static final String PROPRIETARY_MDNS_SERVICE_TYPE = "_junglejepps._tcp.local.";
	public static final String HTTP_MDNS_SERVICE_TYPE = "_http._tcp.local.";

	public static final int SERVER_PORT = 8080;
	private static final String MDNS_SERVICE_NAME = "JJTP service";
	private static final String MDNS_SERVICE_NAME_PRETTY = "Jungle Jepps Desktop Service";

	private static JmDNS broadcasting, discovery;
	private static Listener listener;
	
	// Client requested server
	private static Set<URI> providers = new HashSet<URI>();

	// Demonstration function
	public static void main(String[] args) throws InterruptedException {
		 JungleJeppsmDNS.startBroadcasting();
		 
		 JungleJeppsmDNS.startDiscovery();
		 Thread.sleep(1000);
		 
		 for(URI provider : getProviders())
			 System.out.println(provider);
		 
		 JungleJeppsmDNS.stopDiscovery();
		 
		 
		 JungleJeppsmDNS.stopBroadcasting();
	 }
	
	public static URI[] getProviders(){
		int count = providers.size();
		URI[] providerArray = new URI[count];
		return providers.toArray( providerArray );
	}
	
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
		 ServiceInfo jjtpService, httpService;

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
			broadcasting.unregisterAllServices();
			broadcasting.close();
			System.out.println("Broadcast finished.");
		} 
		catch (IOException e) {
			System.out.println("Broadcast had error in stop command");
			e.printStackTrace();	 
		}
	 }
	 
	 static class Listener implements ServiceListener {
			
			/**
			 * Callback function to handle adding of services.
			 */
			public void serviceAdded(ServiceEvent serviceEvent) {
			}
			
			/**
			 * Callback function to handle removal of services from list
			 */
			public void serviceRemoved(ServiceEvent serviceEvent) {
				ServiceInfo info = serviceEvent.getInfo();
				
				for(String addr : info.getHostAddresses())
					providers.remove( buildAddress( addr, info.getPort() ) );
			}
			
			/**
			 * Callback function to handle addition (after resolution)
			 * of services to list
			 */
			public void serviceResolved(ServiceEvent serviceEvent) {
				ServiceInfo info = serviceEvent.getInfo();
				
				for(String addr : info.getHostAddresses())
					providers.add( buildAddress( addr, info.getPort() ) );
			}
			
			private URI buildAddress(String address, int port){
				URI uri= null;
				
				try {
					uri = new URIBuilder().setHost(address).setPort(port).build();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				
				return uri; 
			}
		 }
}
