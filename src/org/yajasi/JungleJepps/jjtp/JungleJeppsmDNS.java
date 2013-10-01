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

	private static JmDNS jmDns;
	
	// Client requested server
	private static ServiceInfo provider;
	
	// Demonstration function
	public static void main(String[] args) throws InterruptedException {
		 JungleJeppsmDNS.startBroadcasting();
		 
		 Thread.sleep(2000);
		 
		 ServiceInfo server = JungleJeppsmDNS.getServiceProvider();
		 for(String a: server.getHostAddresses())
			 System.out.println("FOUND: " + a + ":" + server.getPort());
		 
		 Thread.sleep(15000);
		 
		 JungleJeppsmDNS.stopBroadcasting();
	 }

	// Getters and initializers
	/**
	 * Get a JmDNS instance and keep a handle locally.
	 * @return JmDNS an instance of JmDNS.create()
	 */
	private static JmDNS getJmDNS(){
		if( jmDns == null)
		{
			try {
				jmDns = JmDNS.create();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return jmDns;
	}
	 
	 public static ServiceInfo getServiceProvider(){
		JmDNS mdns = getJmDNS();
		 
		/* 
		  * TODO: Implement driver callback functionality to alert user/program 
		  * if service 
		  * broadcast is stopped. 
		  */
		 ServiceListener listener = new ServiceListener() {
			public void serviceAdded(ServiceEvent serviceEvent) {}
			public void serviceRemoved(ServiceEvent serviceEvent) {}
			public void serviceResolved(ServiceEvent serviceEvent) {}
		 };
			
			
		 try {
		 	// Create and register service listener			
		 	mdns.addServiceListener(MDNS_SERVICE_TYPE, listener);
		
			// Retrieve service info from either ServiceInfo[] returned here or listener callback method above.
			ServiceInfo[] serviceInfos = mdns.list(MDNS_SERVICE_TYPE);
			
		
			if(serviceInfos.length > 0)
				provider = serviceInfos[0];
						
			// Clean up
			mdns.removeServiceListener(MDNS_SERVICE_TYPE, listener);
			mdns.close();
				
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
		 JmDNS mdns = getJmDNS();
		 ServiceInfo jjtpService;

		try 
		{
			// Assign service name and info
			jjtpService = ServiceInfo.create(MDNS_SERVICE_TYPE, MDNS_SERVICE_NAME_PRETTY, SERVER_PORT, MDNS_SERVICE_NAME);
			
			// Register service and broadcast over LAN
			System.out.println("Registering service and starting broadcast...");
			mdns.registerService( jjtpService );
		}
		catch(IOException e){
			e.printStackTrace();
		}
	 }
	 
	 /**
	  * This method unregisters broadcaster
	  */
	 public static void stopBroadcasting(){
		 JmDNS mdns = getJmDNS();
		System.out.println("Unregistering service and killing broadcast...");
		 
		try
		{
			mdns.unregisterAllServices();
			mdns.close();
			System.out.println("Broadcast finished.");
		} 
		catch (IOException e) {
			System.out.println("Broadcast had error in stop command");
			e.printStackTrace();	 
		}
	 }
}
