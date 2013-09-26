package org.yajasi.JungleJepps.jjtp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceListener;

/**
 * This class manages the client-side and server-side of multicast DNS. 
 * It is capable of broadcasting its services as 
 * @author Joel Jeske
 */
public class JungleJeppsmDNS {
	
	// mDNS service values 
	public static final String MDNS_SERVICE_TYPE = "_junglejepps._tcp.local.";
	public static final int SERVER_PORT = 8080;
	
	private static final String MDNS_SERVICE_NAME = "JJTP service";
	private static final String MDNS_SERVICE_NAME_PRETTY = "Jungle Jepps Desktop Service";
	
	// Thread manager properties
	private static boolean isBroadcasting = false;
	private static Thread broadcastThread;
	
	// Client requested server
	private static ServiceInfo provider;
	protected static JmDNS mdnsService;



	 public static void main(String[] args) throws InterruptedException {
		 JungleJeppsmDNS.startBroadcasting();
		 
		 Thread.sleep(2000);
		 
		 ServiceInfo server = JungleJeppsmDNS.findServiceProvider();
		 for(String a: server.getHostAddresses())
			 System.out.println("FOUND: " + a + ":" + server.getPort());
		 
		 Thread.sleep(5000);
		 
		 JungleJeppsmDNS.stopBroadcasting();
	 }
	 
	 public static ServiceInfo findServiceProvider(){
			
		 /* 
		  * TODO: Implement driver callback functionality to alert user/program 
		  * if service 
		  * broadcast is stopped. 
		  */
		 ServiceListener mdnsServiceListener = new ServiceListener() {
			public void serviceAdded(ServiceEvent serviceEvent) {}
			public void serviceRemoved(ServiceEvent serviceEvent) {}
			public void serviceResolved(ServiceEvent serviceEvent) {}
		 };
			
			
		 try {
			 	// Create and register service listener
				mdnsService = JmDNS.create();
				mdnsService.addServiceListener(MDNS_SERVICE_TYPE, mdnsServiceListener);
			
				// Retrieve service info from either ServiceInfo[] returned here or listener callback method above.
				ServiceInfo[] serviceInfos = mdnsService.list(MDNS_SERVICE_TYPE);
				
			
				if(serviceInfos.length > 0)
					provider = serviceInfos[0];
							
				// Clean up
				mdnsService.removeServiceListener(MDNS_SERVICE_TYPE, mdnsServiceListener);
				mdnsService.close();
				
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		 
		 return provider;
	 }
	 
	 /**
	  * This method starts a new thread to broadcast JungleJepps service 
	  * name over multicast DNS (mDNS). 
	  */
	 public static void startBroadcasting(){
		 if( !isBroadcasting )
		 {
			 // Set flag to broadcast
			 isBroadcasting = true;

			 // Create and start new thread for broadcasting
			 broadcastThread = new Thread( new Broadcaster() );
			 broadcastThread.start();
		 }
	 }
	 
	 /**
	  * This method sets a flag to stop the broadcast thread.
	  */
	 public static void stopBroadcasting(){
		 // Clear flag to stop broadcasting in separate thread
		 isBroadcasting = false;
	 }
	 
	 /**
	  * This class is used to broadcast the services in a separate thread.
	  * @author Joel Jeske
	  */
	 private static class Broadcaster implements Runnable{

		@Override
		public void run() {
			JmDNS mdnsServer;
			ServiceInfo jjtpService;
			
			try 
			{
				// Create a server instance to broadcast from the local machine on the final port number
				
				mdnsServer = JmDNS.create( );//new InetSocketAddress("localhost", SERVER_PORT).getAddress() );
				
				// Assign service name and info
				jjtpService = ServiceInfo.create(MDNS_SERVICE_TYPE, MDNS_SERVICE_NAME_PRETTY, SERVER_PORT, MDNS_SERVICE_NAME);
				
				// Register service and broadcast over LAN
				System.out.println("Registering service and starting broadcast...");
				mdnsServer.registerService(jjtpService);
				
				// Wait until flag is set to stop broadcasting
				while( isBroadcasting )
				{
					Thread.sleep(1000);
				}
				
				// Clean up
				System.out.println("Unregistering service and killing broadcast...");
				mdnsServer.unregisterAllServices();
				mdnsServer.close();
				System.out.println("Broadcast finished.");
				
			} 
			catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		} // End run()
		
	 } // End Broadcaster Class
	 
} // End JungleJeppsmDNS class
