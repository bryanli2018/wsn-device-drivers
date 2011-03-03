package de.uniluebeck.itm.overlayclient;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.uniluebeck.itm.metadaten.remote.client.*;
import de.uniluebeck.itm.metadaten.remote.entity.*;

/**
 * The Class OverlayClient.
 * Connects to the MetaDataServer and searches for nodes in the network.
 */
public class OverlayClient {
	
	private static Log log = LogFactory.getLog(OverlayClient.class);
	private String username;
	private String password;
	private String server;
	private String serverPort;
	private String clientPort;
	
	/**
	 * Instantiates a new overlay client.
	 */
	public OverlayClient(String username, String password, String server, String serverPort, String clientPort) {
		this.username = username;
		this.password = password;
		this.server = server;
		this.serverPort = serverPort;
		this.clientPort = clientPort;
	}
	
	/**
	 * Search device with id.
	 * 
	 * @param ID
	 *            the iD
	 */
	public void searchDeviceWithId(String ID) throws java.lang.Exception {
		MetaDatenClient client;
		if(clientPort != null){
			client = new MetaDatenClient(username, password, server, Integer.valueOf(serverPort), Integer.valueOf(clientPort));
		}else{
			client = new MetaDatenClient(username, password, server, Integer.valueOf(serverPort));
		}
		Node queryExample = new Node();
		queryExample.setId(ID);
		String query = ""; // not used
		List<Node> results = new ArrayList<Node>();
		try {
			results = client.search(queryExample, query);
		} catch (Exception e) {
			log.error("Error while searching the node.");
		}
		System.out.println("Results: " + results.size());
		for (Node node : results) {
			System.out.println(node.getId());
			System.out.println(node.getPort());
			System.out.println(node.getIpAddress());
			System.out.println();
		}
	}

	/**
	 * Search device with microcontroller.
	 * 
	 * @param microcontroller
	 *            the microcontroller
	 */
	public void searchDeviceWithMicrocontroller(String microcontroller) throws java.lang.Exception {
		MetaDatenClient client;
		if(clientPort != null){
			client = new MetaDatenClient(username, password, server, Integer.valueOf(serverPort), Integer.valueOf(clientPort));
		}else{
			client = new MetaDatenClient(username, password, server, Integer.valueOf(serverPort));
		}
		Node queryExample = new Node();
		queryExample.setMicrocontroller(microcontroller);
		String query = ""; // not used
		List<Node> results = new ArrayList<Node>();
		try {
			results = client.search(queryExample, query);
		} catch (Exception e) {
			log.error("Error while searching the node.");
		}
		System.out.println("Results: " + results.size());
		for (Node node : results) {
			System.out.println(node.getId());
			System.out.println(node.getPort());
			System.out.println(node.getIpAddress());
			System.out.println();
		}
	}

	/**
	 * Search device with the capability
	 * 
	 * @param sensor
	 *            the sensor
	 */
	public void searchDeviceWithCapability(String sensor) throws java.lang.Exception {
		List<Capability> sensoren = new ArrayList<Capability>();
		Capability capability = new Capability(sensor, null, null, 0);
		sensoren.add(capability);
		MetaDatenClient client;
		if(clientPort != null){
			client = new MetaDatenClient(username, password, server, Integer.valueOf(serverPort), Integer.valueOf(clientPort));
		}else{
			client = new MetaDatenClient(username, password, server, Integer.valueOf(serverPort));
		}
		Node queryExample = new Node();
		queryExample.setCapabilityList(sensoren);
		String query = ""; // not used
		List<Node> results = new ArrayList<Node>();
		try {
			results = client.search(queryExample, query);
		} catch (Exception e) {
			log.error("Error while searching the node.");
		}
		System.out.println("Results: " + results.size());
		for (Node node : results) {
			System.out.println(node.getId());
			System.out.println(node.getPort());
			System.out.println(node.getIpAddress());
			System.out.println();
		}
	}
}
