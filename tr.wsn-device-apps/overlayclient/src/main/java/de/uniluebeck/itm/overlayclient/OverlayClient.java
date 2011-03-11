package de.uniluebeck.itm.overlayclient;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import de.uniluebeck.itm.metadaten.remote.client.*;
import de.uniluebeck.itm.metadaten.remote.entity.*;

/**
 * The Class OverlayClient.
 * Connects to the MetaDataServer and searches for nodes in the network.
 */
public class OverlayClient {
	
	private String username;
	private String password;
	private String server;
	private String serverPort;
	private String clientPort;
	
	/**
	 * Instantiates a new overlay client.
	 *
	 * @param username the username
	 * @param password the password
	 * @param server the server
	 * @param serverPort the server port
	 * @param clientPort the client port
	 */
	public OverlayClient(String username, String password, String server, String serverPort, String clientPort) {
		this.username = username;
		this.password = password;
		this.server = server;
		this.serverPort = serverPort;
		this.clientPort = clientPort;
	}
	
	/**
	 * Search device with the given id, microcontroller and/or capabilities.
	 *
	 * @param ID 
	 * @param microcontroller
	 * @param capabilities
	 * @throws Exception the exception
	 */
	public void searchDevice(String ID, String microcontroller, List<Capability> capabilities){
		//connecting to the server
		MetaDatenClient client = null;
		if(clientPort != null){
			client = new MetaDatenClient(username, password, server, Integer.valueOf(serverPort), Integer.valueOf(clientPort));

		}else{
			client = new MetaDatenClient(username, password, server, Integer.valueOf(serverPort));
		}
		
		//searching by example
		Node queryExample = new Node();
		if(ID != null){
			//searching device with given id
			queryExample.setId(ID);		
		}
		if(microcontroller != null){
			//searching device with given microcontroller
			queryExample.setMicrocontroller(microcontroller);	
		}	
		if(capabilities != null){
			//searching device with given capabilites
			queryExample.setCapabilityList(capabilities);
		}
		String query = ""; // String for searching by query. This is not used here.
		List<Node> results = new ArrayList<Node>();
		try {
			results = client.search(queryExample, query);  	//
		} catch (Exception e) {
			System.out.println("Error while searching the node.");
		}
		
		//printing the results
		System.out.println("Results: " + results.size());
		for (Node node : results) {
			printNode(node);
		}
	}
	
	/**
	 * Prints the node.
	 *
	 * @param node the node
	 */
	public void printNode(Node node){
		System.out.println("ID: "+node.getId());
		System.out.println("Description: "+node.getDescription());
		System.out.println("Microcontroller: "+node.getMicrocontroller());
		List<Capability> capabilities = node.getCapabilityList();
		System.out.println("Capabilites:");
		for(int i = 0; i < capabilities.size(); i++){
			System.out.println(capabilities.get(i).getName());
		}
		System.out.println("Timestamp: "+node.getTimestamp());
		System.out.println("IP-Address: "+node.getIpAddress());
		System.out.println("Port: "+node.getPort());
		System.out.println();
	}
}
