package de.uniluebeck.itm.datenlogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Datalogger Main Program
 * 
 * @author Fabian Kausche
 * 
 */
public class Main {

	/**
	 * version
	 */
	private static double version = 1.0;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(final String[] args) throws IOException {
		// create Options object
		Option helpOption = new Option("help", "print this message");
		Option versionOption = new Option("version",
				"print the version information");

		Options options = new Options();

		options.addOption(helpOption);
		options.addOption(versionOption);

		// add options for Datenlogger
		options.addOption("port", true, "port");
		options.addOption("server", true, "server");
		options.addOption("location", true, "path to the output file");
		options.addOption("bracketsFilter", true,
				"(datatype,begin,value)-filter");
		options.addOption("regexFilter", true, "regular expression-filter");
		options.addOption("user", true, "username to connect to the server");
		options.addOption("passwd", true, "password to connect to the server");
		options.addOption("device", true,
				"type of sensornode in local case: jennec, telosb oder pacemate");
		options.addOption("output", true,
				"Coding alternative of the output data hex or byte");
		options.addOption("id", true, "ID of the device in remote case");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		if (args.length == 0) {
			printHelp(options);
		} else {
			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				System.out.println("One of the parameters is not registered.");
			}
			if (cmd != null) {
				// standard-options
				if (cmd.hasOption("help")) {
					printHelp(options);
				}
				if (cmd.hasOption("version")) {
					System.out.println(version);
				}

				// der Datenlogger
				if (args[0].equals("startlog")) {
					System.out.println("start Datalogger...");

					String port = cmd.getOptionValue("port");
					String server = cmd.getOptionValue("server");
					String bracketsFilter = cmd
							.getOptionValue("bracketsFilter");
					String regexFilter = cmd.getOptionValue("regexFilter");
					String location = cmd.getOptionValue("location");
					String user = cmd.getOptionValue("user");
					String password = cmd.getOptionValue("passwd");
					String device = cmd.getOptionValue("device");
					String output = cmd.getOptionValue("output");
					String id = cmd.getOptionValue("id");

					if (device == null && server == null) {
						System.out.println("Please enter device or server!");
						System.exit(1);
					}
					if (port == null) {
						System.out.println("Please enter port!");
						System.exit(1);
					}
					if (server != null && id == null) {
						System.out.println("Please enter id of the node!");
						System.exit(1);
					}

					if (server != null
							&& (user == null && password == null || user == null)) {
						System.out.println("Username and Password is missing.");
						BufferedReader in = new BufferedReader(
								new InputStreamReader(System.in));
						System.out.print("Username: ");
						user = in.readLine();
						System.out.print("Password: ");
						password = in.readLine();
					}
					if (server != null && (password == null)) {
						System.out.println("Password is missing.");
						BufferedReader in = new BufferedReader(
								new InputStreamReader(System.in));
						System.out.print("Password: ");
						password = in.readLine();
					}
					// Init Writer
					PausableWriter writer = initWriter(bracketsFilter,
							regexFilter, location, output);

					Datalogger datenlogger = new Datalogger(writer, user,
							password, port, server, device, id);
					datenlogger.connect();
					datenlogger.startlog();

					while (true) {
						while (true) {
							final char in = (char) System.in.read();
							if (in == 10) {
								writer.pause();
								System.out
										.print("Write-mode entered, please enter your command: ");
								String input = new BufferedReader(
										new InputStreamReader(System.in))
										.readLine();
								if (input.startsWith("-bracketsFilter")) {
									String delims = " ";
									String[] tokens = input.split(delims);
									writer.addBracketFilter(tokens[1]);
								} else if (input.startsWith("-regexFilter")) {
									String delims = " ";
									String[] tokens = input.split(delims);
									writer.addRegexFilter(tokens[1]);
								} else if (input.equals("stoplog")) {
									datenlogger.stoplog();
									System.exit(0);
								} else if (input.startsWith("-location")) {
									String delims = " ";
									String[] tokens = input.split(delims);
									if (tokens.length < 2) {
										writer = initWriter(
												writer.getBracketFilter(),
												writer.getRegexFilter(), null,
												output);
										datenlogger.setWriter(writer);
									} else {
										writer = initWriter(
												writer.getBracketFilter(),
												writer.getRegexFilter(),
												tokens[1], output);
										datenlogger.setWriter(writer);
									}
								}
								System.out.println("Write-mode leaved!");
								writer.resume();
							}
						}
					}
				}
			}
		}
	}

	public static PausableWriter initWriter(String bracketsFilter,
			String regexFilter, String location, String output) {

		PausableWriter writer = null;

		if (location != null) {
			if (output != null && output.equals("hex")) {
				writer = new HexFileWriter();
				writer.setLocation(location);
			} else if (output != null && output.equals("byte")) {
				writer = new ByteFileWriter();
				writer.setLocation(location);
			} else {
				writer = new StringFileWriter();
				writer.setLocation(location);
			}
		} else {
			if (output != null && output.equals("hex")) {
				writer = new HexConsoleWriter();
			} else {
				writer = new StringConsoleWriter();
			}
		}
		if (regexFilter != null) {
			writer.setRegexFilter(regexFilter);
		}
		if (bracketsFilter != null) {
			writer.setBracketFilter(bracketsFilter);
		}

		return writer;
	}

	public static void printHelp(Options options) {
		System.out.println("Example:");
		System.out
				.println("Datalogger: Remote example: startlog -filter (104,23,4)&(104,24,5) -location filename.txt -server localhost -id 1 -port 8181");
		System.out
				.println("Datalogger: Local example: startlog -filter .*(4|3)*. -device telosb -port 1464");
		System.out.println("");

		// for help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("help", options);
	}
}
