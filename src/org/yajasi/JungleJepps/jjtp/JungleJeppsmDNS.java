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

	public static final int SERVER_PORT = 8080;
	private static final String MDNS_SERVICE_NAME = "JJTP service";
	private static final String MDNS_SERVICE_NAME_PRETTY = "Jungle Jepps Desktop Service";

	// Must have 2 different objects for use when discovering and broadcasting
	private static JmDNS broadcasting, discovery;
	private static Listener listener;
	
	// Client requested server
	private static Queue<URI> providers = new ArrayDeque<URI>();
	
	// Set of listeners to notify of changes
	private static Set<ProviderStatusListener> listeners = new HashSet<ProviderStatusListener>();

	// Demonstration function
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
		 Thread startBroadcast = new Thread(){
			 public void run(){
				 _startBroadcasting();
			 }
		 };
		 
		 startBroadcast.start();
	 }
	 
	 private static void _startBroadcasting(){
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
	 
	 /**
	  * This class implements the JmDNS Service listener and modifies
	  * the providers queue and notifies any listeners of changes
	  * @author joeljeske14
	  *
	  */
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
				ServiceInfo info = serviceEvent.getInfo();
				boolean notifyListeners = providers.isEmpty(); 
				
				for(String addr : info.getHostAddresses())
					providers.add( buildAddress( addr, info.getPort() ) );
				
				// Need to notify listeners
				if( notifyListeners  && !providers.isEmpty()) 
					notifyListeners();				
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
			
			private void notifyListeners(){
				URI newProvider = providers.peek();
				for(ProviderStatusListener listener : listeners)
					listener.newPrimaryProvider(newProvider);
			}
	}
	 
	
	 /**
	  * Register a listener to be notified of changes in providers
	  * @param listener
	  */
	 public static void registerChangeListener(ProviderStatusListener listener){
		 if(listener == null) return;
		 
		 listeners.add(listener);
		 
		 if( !providers.isEmpty() )
			 listener.newPrimaryProvider( providers.peek() );
	 }
	 
	 /**
	  * Unregister a listener to stop receiving notifications of changes
	  * @param listener
	  */
	 public static void unregisterChangeListener(ProviderStatusListener listener){
		 listeners.remove(listener);
	 }
	 
	 /**
	  * This is a listener interface used to allow sub-clients to listen on
	  * the statuses of providers.
	  * @author joeljeske14
	  *
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
