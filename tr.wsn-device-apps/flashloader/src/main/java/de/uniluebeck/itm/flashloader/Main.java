package de.uniluebeck.itm.flashloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.uniluebeck.itm.devicedriver.MacAddress;

/**
 * The Class Main.
 */
public class Main {

	/** The version. */
	private static double version = 1.0;

	private static String ipRegex = "(((\\d{1,3}.){3})(\\d{1,3}))";
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

		// add options for FlashLoader
		options.addOption("port", true, "port");
		options.addOption("server", true, "server");
		options.addOption("file", true, "file to flash the device.");
		options.addOption("user", true, "username to connect to the server");
		options.addOption("passwd", true, "password to connect to the server");
		options.addOption("device", true,
				"type of device in local case: jennec, telosb oder pacemate");
		options.addOption("id", true, "ID of the device in remote case");
		options.addOption("timeout", true,
				"optional timeout while flashing the device");
		options.addOption("macAddress", true,
				"the mac-address, that should be written on the device.");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		if (args.length == 0) {
			printHelp(options);
		} else {
			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				System.out.println("One of these options is not registered.");
			}
			if (cmd != null) {
				// standard-options
				if (cmd.hasOption("help")) {
					printHelp(options);
				}
				if (cmd.hasOption("version")) {
					System.out.println(version);
				}

				FlashLoader flashLoader = readCmd(cmd);
				flashLoader.connect();
				if (args[0].equals("flash")) {

					String file = cmd.getOptionValue("file");
					if (file == null) {
						System.out
								.println("Wrong input: Please enter file to flash the device!");
						validInput = false;
					} else {
						File f = new File(file);
						if (!f.exists()) {
							System.out
									.println("Wrong input: File does not exists!");
							validInput = false;
						}
					}
					if (validInput) {
						flashLoader.flash(file);
					}

				} else if (args[0].equals("readmac")) {
					if (validInput) {
						flashLoader.readmac();
					}

				} else if (args[0].equals("writemac")) {
					String macAddress = cmd.getOptionValue("macAddress");
					if (macAddress == null) {
						System.out
								.println("Wrong input: Please enter macAddress!");
						validInput = false;
					}else{
						if (!macAddress.matches("\\A\\b[0-9a-fA-F]+\\b\\Z")) {
							System.out
									.println("Wrong input: Please enter macAddress as hex!");
							validInput = false;
						}
					}
					if (validInput) {
						int length = macAddress.length();
						if (length != 16) {
							for (int i = length; i < 16; i++) {
								macAddress = macAddress + "0";
								length++;
							}
						}
						MacAddress macAdress = new MacAddress(
								hexStringToByteArray(macAddress));
						flashLoader.writemac(macAdress);
					}

				} else if (args[0].equals("reset")) {
					if (validInput) {
						flashLoader.reset();
					}
				}
			}
		}
	}

	/**
	 * Parses the Parameters from the given CommandLine gives them to the
	 * flashloader-object.
	 * 
	 * @param cmd
	 * @param flashLoader
	 * @throws IOException
	 */
	public static FlashLoader readCmd(CommandLine cmd) throws IOException {
		String port = cmd.getOptionValue("port");
		String server = cmd.getOptionValue("server");
		String user = cmd.getOptionValue("user");
		String password = cmd.getOptionValue("passwd");
		String device = cmd.getOptionValue("device");
		String id = cmd.getOptionValue("id");
		String timeout = cmd.getOptionValue("timeout");

		// Begin: validate input-data
		if (device == null && server == null) {
			System.out.println("Wrong input: Please enter device or server!");
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
						.println("Wrong input: This is no valide server address.");
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

		if (server != null
				&& (user == null && password == null || user == null)) {
			System.out.println("Username and Password is missing.");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.print("Username: ");
			user = in.readLine();
			System.out.print("Password: ");
			password = in.readLine();
			in.close();
		}
		if (server != null && (password == null)) {
			System.out.println("Password is missing.");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.print("Password: ");
			password = in.readLine();
			in.close();
		}
		FlashLoader flashLoader = new FlashLoader(port, server, user, password,
				device, id, timeout);
		return flashLoader;
	}

	/**
	 * Converts a hex-String to a byte array
	 * 
	 * @param s
	 * @return data, the byte array
	 */
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static void printHelp(Options options) {
		System.out.println("Example:");
		System.out
				.println("Flash: Remote-Example: flash -port 8181 -server localhost -id 1 -file jennec.bin");
		System.out
				.println("Flash: Local-Example: flash -port COM1 -file jennec.bin -device jennec");
		System.out
				.println("Write Mac: Local-Example: writemac -port COM1 -device jennec -macAddress 080020aefd7e");
		System.out
				.println("Read Mac: Local-Example: readmac -port COM1 -device jennec");
		System.out
				.println("Reset: Local-Example: reset -port COM1 -device telosb");
		System.out.println("");
		// for help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("help", options);
	}
}
