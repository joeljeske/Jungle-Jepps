package org.yajasi.JungleJepps.jjtp;


import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;


public class Server {

	 public static void main(String[] args) throws IOException {
		 
		JmDNS mdnsServer = JmDNS.create("localhost");
		
		// Register a test service
		ServiceInfo testService = ServiceInfo.create(JJTP.MDNS_SERVICE_TYPE, "Jungle Jepps Desktop Service", JJTP.SERVER_PORT, "JJTP service");
		mdnsServer.registerService(testService);
		
	 }
}
