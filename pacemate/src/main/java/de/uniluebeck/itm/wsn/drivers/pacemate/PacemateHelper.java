package de.uniluebeck.itm.wsn.drivers.pacemate;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.uniluebeck.itm.util.StringUtils;
import de.uniluebeck.itm.wsn.drivers.core.Connection;
import de.uniluebeck.itm.wsn.drivers.core.exception.InvalidChecksumException;
import de.uniluebeck.itm.wsn.drivers.core.exception.TimeoutException;
import de.uniluebeck.itm.wsn.drivers.core.exception.UnexpectedResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PacemateHelper {

	/**
	 * This is the Start Address in the RAM to write data
	 */
	public static final long START_ADDRESS_IN_RAM = 1073742336;

	private static final Logger LOG = LoggerFactory.getLogger(PacemateHelper.class);

	private static final int TIMEOUT_WAIT_DATA_AVAILABLE = 2000;

	private static final int ASCII_CR = 13;

	private static final int ASCII_LF = 10;

	private static final int ASCII_ZERO = 48;

	private boolean echo = true;

	private final Connection connection;

	@Inject
	public PacemateHelper(Connection connection) {
		this.connection = connection;
	}

	public boolean isEcho() {
		return echo;
	}

	@Inject(optional = true)
	public void setEcho(@Named("pacemate.echo") boolean echo) {
		this.echo = echo;
	}

	public void sendBootLoaderMessage(byte[] message) throws IOException {
		// Allocate buffer for message + CR and LF
		byte[] data = new byte[message.length + 2];

		// Copy message into the buffer
		System.arraycopy(message, 0, data, 0, message.length);

		// add CR and LF
		data[data.length - 2] = 0x0D; // <CR>
		data[data.length - 1] = 0x0A; // <LF>

		// Send message
		final OutputStream outputStream = connection.getOutputStream();
		outputStream.write(data);
		outputStream.flush();
	}

	public void clearStreamData() throws IOException {

		final InputStream inStream = connection.getInputStream();

		// Allocate message buffer max 255 bytes to read
		byte[] message = new byte[255];

		int index = 0;

		// Read the data
		boolean a = true;
		while ((inStream.available() > 0) && (a) && (index < 255)) {
			try {
				//System.out.println("************ Reading from stream");
				message[index] = (byte) inStream.read();
				//System.out.println("************ Done reading from stream");
			} catch (IOException e) {
				LOG.error("" + e, e);
			}
			if (message[index] == -1) {
				a = false;
			} else {
				index++;
			}
		}
	}

	public void configureFlash(int start, int end) throws Exception {
		LOG.debug("Configuring flash from " + start + " to " + end + "...");

		enableFlashErase();

		// Send flash configure request
		sendBootLoaderMessage(Messages.flashConfigureRequestMessage(start, end));

		// Read flash configure response
		receiveBootLoaderReplySuccess(Messages.CMD_SUCCESS);

		LOG.debug("Flash is configured");
	}

	public void copyRAMToFlash(long flashAddress, long ramAddress, int length) throws Exception {
		// Send flash program request
		LOG.debug("Sending program request for address " + ramAddress + " with " + length + " bytes");
		sendBootLoaderMessage(Messages.copyRAMToFlashRequestMessage(flashAddress, ramAddress, length));

		// Read flash program response
		receiveBootLoaderReplySuccess(Messages.CMD_SUCCESS);

		LOG.debug("Copy Ram to Flash ok");
	}

	public void eraseFlash(int start, int end) throws Exception {
		LOG.debug("Erasing sector from " + start + " to " + end + "...");
		sendBootLoaderMessage(Messages.flashEraseRequestMessage(start, end));
		receiveBootLoaderReplySuccess(Messages.CMD_SUCCESS);
		try {
			receiveBootLoaderReplySuccess(Messages.CMD_SUCCESS);
		} catch (TimeoutException e) {
			LOG.debug("one line erase response");
		}
		LOG.debug("Flash erased");
	}

	public void enableFlashErase() throws Exception {
		LOG.debug("Enabling Erase Flash...");
		sendBootLoaderMessage(Messages.Unlock_RequestMessage());
		receiveBootLoaderReplySuccess(Messages.CMD_SUCCESS);
	}

	/**
	 * Receive the bsl reply message to all request messages with a success answer
	 *
	 * @param type
	 * 		type of operation
	 *
	 * @return the response code as String
	 *
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.TimeoutException
	 * 		if a timeout occurs
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.UnexpectedResponseException
	 * 		if an error occurs
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.InvalidChecksumException
	 * 		if an error occurs
	 * @throws java.io.IOException
	 * 		if an error occurs
	 * @throws java.lang.NullPointerException
	 * 		if an error occurs
	 */
	protected String receiveBootLoaderReplySuccess(String type)
			throws TimeoutException, UnexpectedResponseException, InvalidChecksumException, IOException,
			NullPointerException {

		byte[] reply = echo ? readInputStream(3) : readInputStream(2);

		String replyStr = StringUtils.toASCIIString(reply);

		// split the lines from the response message
		String[] parts = replyStr.split("<CR><LF>");

		for (final String part : parts) {
			LOG.trace("BL parts " + part);
		}

		// does the node echo all messages or not
		if (echo) {
			if (parts.length >= 2) {
				if (parts[1].compareTo("0") == 0) // 0 = everything is OK
				{
					if (parts.length >= 3) {
						return parts[2];
					} else {
						return "";
					}
				}
			}
		} else {
			if (parts.length >= 1) {
				if (parts[0].compareTo("0") == 0) // 0 = everything is OK
				{
					if (parts.length >= 1) {
						return parts[1];
					} else {
						return "";
					}
				}
			}
		}

		throw new UnexpectedResponseException("Error in response *" + replyStr + "*", -1, -1);
	}


	/**
	 * Receive the BSL reply message for the autobaud / synchronize request
	 *
	 * @param type
	 * 		type of operation
	 *
	 * @return the BSL reply message
	 *
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.TimeoutException
	 * 		if a timeout occurs
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.UnexpectedResponseException
	 * 		if an error occurs
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.InvalidChecksumException
	 * 		if an error occurs
	 * @throws java.io.IOException
	 * 		if an error occurs
	 * @throws java.lang.NullPointerException
	 * 		if an error occurs
	 */
	protected byte[] receiveBootLoaderReplySynchronized(String type)
			throws TimeoutException, UnexpectedResponseException, InvalidChecksumException, IOException,
			NullPointerException {

		byte[] reply;
		if (type.compareTo(Messages.SYNCHRONIZED) == 0) {
			reply = readInputStream(1);
		} else {
			reply = readInputStream(3);
		}
		String replyStr = StringUtils.toASCIIString(reply);

		if ((replyStr.compareTo("Synchronized<CR><LF>") == 0)
				|| (replyStr.compareTo("Synchronized<CR><LF>OK<CR><LF>") == 0)) {
			return reply;
		} else if ((type.compareTo(Messages.SYNCHRONIZED_OK) == 0)) {
			return reply;
		}

		throw new UnexpectedResponseException("Wrong response " + StringUtils.toASCIIString(reply) + " and not " + type,
				-1, -1
		);
	}

	/**
	 * Read the echo for a line of data
	 *
	 * @return the echo
	 *
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.TimeoutException
	 * 		if a timeout occurs
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.UnexpectedResponseException
	 * 		if an error occurs
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.InvalidChecksumException
	 * 		if an error occurs
	 * @throws java.io.IOException
	 * 		if an error occurs
	 * @throws java.lang.NullPointerException
	 * 		if an error occurs
	 */
	protected String receiveBootLoaderReplySendDataEcho()
			throws TimeoutException, UnexpectedResponseException, InvalidChecksumException, IOException,
			NullPointerException {

		byte[] reply;

		reply = readInputStream(1);

		return StringUtils.toASCIIString(reply);
	}

	/**
	 * Read the requested line from the Flash
	 *
	 * @return requested line from the Flash
	 *
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.TimeoutException
	 * 		if a timeout occurs
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.UnexpectedResponseException
	 * 		if an error occurs
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.InvalidChecksumException
	 * 		if an error occurs
	 * @throws java.io.IOException
	 * 		if an error occurs
	 * @throws java.lang.NullPointerException
	 * 		if an error occurs
	 */
	protected byte[] receiveBootLoaderReplyReadData()
			throws TimeoutException, UnexpectedResponseException, InvalidChecksumException, IOException,
			NullPointerException {

		byte[] reply;
		if (this.echo) {
			reply = readInputStream(4);
		} else {
			reply = readInputStream(3);
		}

		int i = 0;
		if (this.echo) {
			for (i = 0; i < reply.length; i++) {
				if (reply[i] == 13) { // skip the echo and cr
					i = i + 2; // and lf as well
					break;
				}
			}
		}
		int len = (reply.length - (i + 5));

		byte[] lineFromFlash = new byte[len];

		if ((i + 3 < reply.length) && (reply[i] == 48)) { // copy the line and skip the answer and cr lf
			System.arraycopy(reply, i + 3, lineFromFlash, 0, len);
			//System.out.println(StringUtils.toASCIIString(lineFromFlash));
			return lineFromFlash;
		}

		throw new UnexpectedResponseException("Error in response *" + StringUtils.toASCIIString(reply) + "*", -1, -1);
	}

	/**
	 * Read the response to the CRC message
	 *
	 * @return the response
	 *
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.TimeoutException
	 * 		if a timeout occurs
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.UnexpectedResponseException
	 * 		if an error occurs
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.InvalidChecksumException
	 * 		if an error occurs
	 * @throws java.io.IOException
	 * 		if an error occurs
	 * @throws java.lang.NullPointerException
	 * 		if an error occurs
	 */
	protected byte[] receiveBootLoaderReplyReadCRCOK()
			throws TimeoutException, UnexpectedResponseException, InvalidChecksumException, IOException,
			NullPointerException {

		byte[] reply;
		if (this.echo) {
			reply = readInputStream(2);
		} else {
			reply = readInputStream(1);
		}

		String replyStr = StringUtils.toASCIIString(reply);

		// split the lines from the response message
		String[] parts = replyStr.split("<CR><LF>");

		if (this.echo) {
			if (parts[1].compareTo(Messages.OK) == 0) {
				return reply;
			} else {
				LOG.debug("Received boot loader msg: " + replyStr);
				throw new InvalidChecksumException("Invalid checksum - resend " + replyStr);
			}
		} else {
			if (parts[0].compareTo(Messages.OK) == 0) {
				return reply;
			} else {
				LOG.debug("Received boot loader msg: " + replyStr);
				throw new InvalidChecksumException("Invalid checksum - resend " + replyStr);
			}
		}
	}

	/**
	 * Read from the Input stream from the Pacemate. The length of the expected pacemate reply message is given with
	 * the expected number of &lt;cr&gt;&lt;lf&gt; chars.
	 *
	 * @param CRLFCount
	 * 		expected number of lines
	 *
	 * @return lines from the input stream
	 *
	 * @throws de.uniluebeck.itm.wsn.drivers.core.exception.TimeoutException
	 * 		if a timeout occurs
	 * @throws java.io.IOException
	 * 		if an error occurs
	 */
	private byte[] readInputStream(int CRLFCount) throws TimeoutException, IOException {
		final byte[] message = new byte[255];

		int index = 0;
		int counter = 0;
		int wait = 5;
		connection.waitDataAvailable(TIMEOUT_WAIT_DATA_AVAILABLE);

		// Read the message - read CRLFCount lines of response
		final InputStream inStream = connection.getInputStream();
		while ((index < 255) && (counter < CRLFCount)) {
			if (inStream.available() > 0) {
				message[index] = (byte) inStream.read();
				if (message[index] == 0x0a) {
					counter++;
				}
				if (message[index] != -1) {
					index++;
				}
			} else {
				// message is smaller then expected
				// check if the last line was cr lf 0 cr lf == Success message without more infos
				if (index >= 5 && checkResponseMessage(message, index)) {
					break;
				}

				try {
					connection.waitDataAvailable(1000);
				} catch (final TimeoutException e) {
					// Do nothing
				}

				wait--;
				if (wait == 0) {
					final byte[] fullMessage = new byte[index];
					System.arraycopy(message, 0, fullMessage, 0, index);
					throw new TimeoutException("Not a complete response message from the node *" + StringUtils
							.toASCIIString(fullMessage) + "*"
					);
				}
			}
		}

		// copy to real length
		byte[] fullMessage = new byte[index];
		System.arraycopy(message, 0, fullMessage, 0, index);
		LOG.trace("read lines " + StringUtils.toASCIIString(fullMessage));
		return fullMessage;
	}

	/**
	 * Check if the last received bytes were &lt;cr&gt;&lt;lf&gt;0&lt;cr&gt;&lt;lf&gt; == Success message without more
	 * infos.
	 *
	 * @param message
	 * 		the received message
	 * @param index
	 * 		the position in the message to check
	 *
	 * @return {@code true} if success, {@code false} otherwise
	 */
	private boolean checkResponseMessage(byte[] message, int index) {
		return (message[index - 5] == ASCII_CR)
				&& (message[index - 4] == ASCII_LF)
				&& (message[index - 3] == ASCII_ZERO)
				&& (message[index - 2] == ASCII_CR)
				&& (message[index - 1] == ASCII_LF);
	}

	protected void waitForBootLoader() throws IOException {
		try {
			// Send flash read request (in fact, this could be any valid message
			// to which the
			// device is supposed to respond)
			sendBootLoaderMessage(Messages.ReadPartIDRequestMessage());
			receiveBootLoaderReplySuccess(Messages.CMD_SUCCESS);
			LOG.debug("Device connection established");

		} catch (Exception error) {
			LOG.warn("Exception while waiting for connection", error);
			connection.clear();
			throw new IOException(error);
		}

	}

	public boolean autobaud() {
		try {
			sendBootLoaderMessage(Messages.AutoBaudRequestMessage());
			receiveBootLoaderReplySynchronized(Messages.SYNCHRONIZED);
			sendBootLoaderMessage(Messages.AutoBaudRequest2Message());
			receiveBootLoaderReplySynchronized(Messages.SYNCHRONIZED);
			sendBootLoaderMessage(Messages.AutoBaudRequest3Message());
			receiveBootLoaderReplySynchronized(Messages.SYNCHRONIZED_OK);
			LOG.debug("Autobaud");
		} catch (TimeoutException to) {
			LOG.debug("Still waiting for a connection.");
		} catch (Exception error) {
			LOG.warn("Exception while waiting for connection", error);
		}
		return true;
	}

	public void writeToRAM(long address, int len) throws Exception {
		// Send flash program request
		// LOG.debug("Sending program request for address " + address + " with " + data.length + " bytes");
		sendBootLoaderMessage(Messages.writeToRAMRequestMessage(address, len));
		//System.out.println("send ready");
		// Read flash program response
		receiveBootLoaderReplySuccess(Messages.CMD_SUCCESS);

		// LOG.debug("write to RAM ok");
	}

	/**
	 * Writes the byte array to the out stream
	 *
	 * @param dataMessage
	 * 		the message to write
	 *
	 * @throws NullPointerException
	 * 		if an error occurs
	 * @throws InvalidChecksumException
	 * 		if an error occurs
	 * @throws UnexpectedResponseException
	 * 		if an error occurs
	 * @throws TimeoutException
	 * 		if an error occurs
	 */
	public void sendDataMessage(byte[] dataMessage) throws IOException, TimeoutException, UnexpectedResponseException,
			InvalidChecksumException, NullPointerException {

		// Allocate buffer for message + CR and LF
		int array_length = dataMessage.length + 2;

		byte[] data = new byte[array_length];

		// Copy message into the buffer
		System.arraycopy(dataMessage, 0, data, 0, dataMessage.length);

		// add CR and LF
		data[dataMessage.length] = 0x0D; // <CR>
		data[dataMessage.length + 1] = 0x0A; // <LF>

		// Print message
		// LOG.debug("Sending data msg: " + Tools.toASCIIString(data));

		// Send message
		final OutputStream outputStream = connection.getOutputStream();
		outputStream.write(data);
		outputStream.flush();

		receiveBootLoaderReplySendDataEcho();
	}

	public void sendChecksum(long CRC)
			throws IOException, TimeoutException, UnexpectedResponseException, InvalidChecksumException,
			NullPointerException {

		// LOG.debug("Send CRC after 20 Lines or end of Block");
		sendBootLoaderMessage(Messages.writeCRCRequestMessage(CRC));

		receiveBootLoaderReplyReadCRCOK();
	}

	/**
	 * Writes the CRC to the last two bytes of the flash
	 *
	 * @param crc
	 * 		CRC value to write
	 *
	 * @return {@code true} if succeeded, {@code false} otherwise
	 *
	 * @throws java.lang.Exception
	 * 		if an error occurs
	 */
	public boolean writeCRCtoFlash(int crc) throws Exception {
		byte crc_bytes[] = new byte[256];
		for (int i = 0; i < 256; i++) {
			crc_bytes[i] = (byte) 0xff;
		}
		crc_bytes[254] = (byte) ((crc & 0xff00) >> 8);
		crc_bytes[255] = (byte) (crc & 0xff);

		LOG.trace("CRC = " + crc + " " + crc_bytes[254] + " " + crc_bytes[255]);

		try {
			configureFlash(14, 14);
		} catch (Exception e) {
			LOG.debug("Error while configure flash!");
			return false;
		}

		try {
			eraseFlash(14, 14);
		} catch (Exception e) {
			LOG.debug("Error while erasing flash!");
			return false;
		}

		try {
			writeToRAM(START_ADDRESS_IN_RAM, 256);
		} catch (Exception e) {
			LOG.debug("Error while write to RAM!");
			return false;
		}

		int counter = 0;

		int crc_checksum = 0;

		byte[] line;

		// each block is sent in parts of 20 lines a 45 bytes
		while (counter < crc_bytes.length) {
			int offset = 0;
			if (counter + 45 < crc_bytes.length) {
				line = new byte[PacemateBinaryImage.LINESIZE]; // a line with 45 bytes
				System.arraycopy(crc_bytes, counter, line, 0, PacemateBinaryImage.LINESIZE);
				counter = counter + PacemateBinaryImage.LINESIZE;
			} else {
				if (((crc_bytes.length - counter) % 3) == 1) {
					offset = 2;
				} else if (((crc_bytes.length - counter) % 3) == 2) {
					offset = 1;
				}
				line = new byte[crc_bytes.length - counter + offset];
				line[line.length - 1] = 0;
				line[line.length - 2] = 0;
				System.arraycopy(crc_bytes, counter, line, 0, crc_bytes.length - counter);
				counter = counter + (crc_bytes.length - counter);
			}

			for (int i = 0; i < line.length; i++) {
				crc_checksum = PacemateBinaryImage.calcCRCChecksum(crc_checksum, line[i]);
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Sending data msg: " + StringUtils.toHexString(line));
			}

			sendDataMessage(PacemateBinaryImage.encodeCRCData(line, line.length - offset));
		}

		try {
			sendChecksum(crc_checksum);
		} catch (Exception e) {
			LOG.debug("Error while sending checksum for crc!");
			return false;
		}

		// if block is completed copy data from RAM to Flash
		int crc_block_start = 0x3ff00;

		LOG.trace("Prepare Flash and Copy Ram to Flash 14 14 " + crc_block_start);

		try {
			configureFlash(14, 14);
			copyRAMToFlash(crc_block_start, START_ADDRESS_IN_RAM, 256);
		} catch (Exception e) {
			LOG.debug("Error while copy RAM to Flash!");
			return false;
		}

		return true;
	}
}
