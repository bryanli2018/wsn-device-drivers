package de.uniluebeck.itm.messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.*;

/**
 * The Class Main.
 */
public class Main {

	/** The version. */
	private static double version = 1.0;

	/** The ip regex, to validate the server-address */
	private static String ipRegex = "(((\\d{1,3}.){3})(\\d{1,3}))";
	
	/** The hex regex., to validate the message as hex */
	private static String hexRegex = "\\A\\b[0-9a-fA-F]+\\b\\Z";
	
	/** The valid input gets false, when one of the input-parameters is wrong. */
	private static boolean validInput = true;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		// create Options object
		Option helpOption = new Option("help", "print this message");
		Option versionOption = new Option("version",
				"print the version information");

		Options options = new Options();

		options.addOption(helpOption);
		options.addOption(versionOption);

		// add options for Messenger
		options.addOption("port", true, "port");
		options.addOption("server", true, "server");
		options.addOption("message", true, "message to send as hex-code");
		options.addOption("user", true, "username to connect to the server");
		options.addOption("passwd", true, "password to connect to the server");
		options.addOption("device", true,
				"type of the device in local case: jennec, telosb or pacemate");
		options.addOption("id", true, "ID of the device in remote case");
		options.addOption("messageType", true, "Type of the Message to be send");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		if(args.length == 0){
			//if there is no input, print help message
			printHelp(options);
		}
		else{
			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				System.out.println("One of these options is not registered.");
				printHelp(options);
			}
			if (cmd != null) {
				// standard-options
				if (cmd.hasOption("help")) {
					printHelp(options);
				}
				if (cmd.hasOption("version")) {
					System.out.println(version);
				}

				//parameters for connection
				String server = cmd.getOptionValue("server");
				String port = cmd.getOptionValue("port");
				String id = cmd.getOptionValue("id");
				String user = cmd.getOptionValue("user");
				String password = cmd.getOptionValue("passwd");
				String device = cmd.getOptionValue("device");
				
				//parameters for the message
				String message = cmd.getOptionValue("message");
				String messageType = cmd.getOptionValue("messageType");
				
				//Begin: validate input-data
				if (device == null && server == null) {
					System.out.println("Wrong input: Please enter device(local) or server(remote)!");
					validInput = false;
				}
				if (device != null) {
					if (!device.equals("mock") && !device.equals("jennec")
							&& !device.equals("pacemate") && !device.equals("telosb")) {
						System.out
								.println("Wrong input: The device parameter can only be 'jennec', 'pacemate', 'telosb' or 'mock'.");
						validInput = false;
					}
				}
				if (server != null) {
					if (!server.matches(ipRegex) && !server.equals("localhost")) {
						System.out
								.println("Wrong input: This is no valid server address.");
						validInput = false;
					}
				}
				if (port == null) {
					System.out.println("Wrong input: Please enter port!");
					validInput = false;
				}
				if (server != null && id == null) {
					System.out.println("Wrong input: Please enter id of the device!");
					validInput = false;
				}
				if(messageType == null){
					System.out.println("Wrong input: Please enter a messageType!");
					validInput = false;
				}
				if (message == null) {
					System.out
							.println("Wrong input: Please enter message!");
					validInput = false;
				}else{
					if (!message.matches(hexRegex)) {
						System.out
								.println("Wrong input: Please enter message as hex!");
						validInput = false;
					}
				}
			    //End: validate input-data
				
				if(validInput){
					//username and password is required to connect to the server
					if (server != null && (user == null && password == null || user == null)) {
						System.out.println("Username and Password is missing.");
						BufferedReader in = new BufferedReader(
								new InputStreamReader(System.in));
						System.out.print("Username: ");
						user = in.readLine();
						System.out.print("Password: ");
						password = in.readLine();
						in.close();
					}
					if (server != null && (password == null)) {
						System.out.println("Password is missing.");
						BufferedReader in = new BufferedReader(
								new InputStreamReader(System.in));
						System.out.print("Password: ");
						password = in.readLine();
						in.close();
					}
					
					Messenger messenger = new Messenger(port, server, user, password, device, id, Integer.valueOf(messageType));
					messenger.connect();
					messenger.send(message);
				}
			}
		}	
	}
	
	/**
	 * Prints the help.
	 *
	 * @param options the options
	 */
	public static void printHelp(Options options){
		System.out.println("Examples:");
		System.out
				.println("Messenger: Remote example: -message 68616c6c6f -port 8181 -server localhost -id 1 -messageType 1");
		System.out
		.println("Messenger: Local example: -message 68616c6c6f -port COM1 -device jennec -messageType 1");
		System.out.println("");
		// for help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("help", options);
	}
}
