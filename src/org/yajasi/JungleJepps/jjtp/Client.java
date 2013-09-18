package org.yajasi.JungleJepps.jjtp;

import java.io.IOException;

import javax.jmdns.*;

public class Client {
	
	public static JmDNS mdnsService;

	public static void main(String[] args) throws IOException{
		
		ServiceListener mdnsServiceListener = new ServiceListener() {
			  public void serviceAdded(ServiceEvent serviceEvent) {
				// Test service is discovered. requestServiceInfo() will trigger serviceResolved() callback.
				  mdnsService.requestServiceInfo(JJTP.MDNS_SERVICE_TYPE, serviceEvent.getName());
			  }

			  public void serviceRemoved(ServiceEvent serviceEvent) {
			    // Test service is disappeared.
				  System.out.println("Removed");
			  }

			  public void serviceResolved(ServiceEvent serviceEvent) {
			    // Test service info is resolved.
			    String[] serviceUrls = serviceEvent.getInfo().getURLs();
			    for(String url : serviceUrls)
			    	System.out.println("urls: " + url);
			    // serviceURL is usually something like http://192.168.11.2:6666/my-service-name
			  }
		};

		mdnsService = JmDNS.create();
		System.out.println("Created");

		mdnsService.addServiceListener(JJTP.MDNS_SERVICE_TYPE, mdnsServiceListener);
		System.out.println("Listening");

		// Retrieve service info from either ServiceInfo[] returned here or listener callback method above.
		ServiceInfo[] serviceInfos = mdnsService.list(JJTP.MDNS_SERVICE_TYPE);

		
		for (ServiceInfo info : serviceInfos) {
			  System.out.println("## resolve service " + info.getName()  + " : " + info.getURL());
		}		
		
		
		mdnsService.removeServiceListener(JJTP.MDNS_SERVICE_TYPE, mdnsServiceListener);
		mdnsService.close();
	}
	
}
