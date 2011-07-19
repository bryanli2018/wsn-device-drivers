package de.uniluebeck.itm.wsn.drivers.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.uniluebeck.itm.wsn.drivers.core.concurrent.OperationExecutor;
import de.uniluebeck.itm.wsn.drivers.core.concurrent.OperationFuture;
import de.uniluebeck.itm.wsn.drivers.core.operation.EraseFlashOperation;
import de.uniluebeck.itm.wsn.drivers.core.operation.GetChipTypeOperation;
import de.uniluebeck.itm.wsn.drivers.core.operation.Operation;
import de.uniluebeck.itm.wsn.drivers.core.operation.ProgramOperation;
import de.uniluebeck.itm.wsn.drivers.core.operation.ReadFlashOperation;
import de.uniluebeck.itm.wsn.drivers.core.operation.ReadMacAddressOperation;
import de.uniluebeck.itm.wsn.drivers.core.operation.ResetOperation;
import de.uniluebeck.itm.wsn.drivers.core.operation.SendOperation;
import de.uniluebeck.itm.wsn.drivers.core.operation.WriteFlashOperation;
import de.uniluebeck.itm.wsn.drivers.core.operation.WriteMacAddressOperation;


/**
 * Facade for calling operation async on the device.
 * 
 * @author Malte Legenhausen
 */
@Singleton
public class SimpleDevice implements Device {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SimpleDevice.class);
	
	/**
	 * Pattern used for operations that are not available.
	 */
	private static final String NOT_AVAILABLE_PATTERN = "%s is not available.";
	
	/**
	 * Message for the exception that is thrown when a negative address was given.
	 */
	private static final String NEGATIVE_ADDRESS_MESSAGE = "Negative address is not allowed.";
	
	/**
	 * Message for the exception that is thrown when a negative length was given.
	 */
	private static final String NEGATIVE_LENGTH_MESSAGE = "Negative length is not allowed.";
	
	/**
	 * Queue that schedules all <code>Operation</code> instances.
	 */
	private final OperationExecutor queue;
	
	private final Connection connection;
	
	private final InputStream inputStream;
	
	private final Provider<OutputStream> outputStreamProvider;
	
	private final Provider<SendOperation> sendProvider;
	
	private Provider<GetChipTypeOperation> getChipTypeProvider;
	
	private Provider<EraseFlashOperation> eraseFlashProvider;
	
	private Provider<ProgramOperation> programProvider;
	
	private Provider<ReadFlashOperation> readFlashProvider;
	
	private Provider<ReadMacAddressOperation> readMacAddressProvider;
	
	private Provider<ResetOperation> resetProvider;
	
	private Provider<WriteFlashOperation> writeFlashProvider;
	
	private Provider<WriteMacAddressOperation> writeMacAddressProvider;

	/**
	 * Constructor.
	 *
	 * @param queue  The <code>OperationExecutor</code> that schedules all operations.
	 * @param device The <code>Device</code> that provides all operations that can be executed.
	 */
	@Inject
	public SimpleDevice(OperationExecutor queue, Connection connection, InputStream inputStream,
			Provider<OutputStream> outputStreamProvider,
			Provider<SendOperation> sendProvider) {
		this.connection = connection;
		this.queue = queue;
		this.inputStream = inputStream;
		this.outputStreamProvider = outputStreamProvider;
		this.sendProvider = sendProvider;
	}

	@Override
	public OperationFuture<ChipType> getChipType(long timeout, @Nullable OperationCallback<ChipType> callback) {
		LOG.trace("Reading Chip Type (Timeout: " + timeout + "ms)");
		GetChipTypeOperation operation = createOperation(getChipTypeProvider, timeout, callback, "getChipType");
		return queue.submitOperation(operation);
	}

	@Override
	public OperationFuture<Void> eraseFlash(long timeout, @Nullable OperationCallback<Void> callback) {
		LOG.trace("Erase flash (Timeout: " + timeout + "ms)");
		EraseFlashOperation operation = createOperation(eraseFlashProvider, timeout, callback, "eraseFlash");
		return queue.submitOperation(operation);
	}

	@Override
	public OperationFuture<Void> program(byte[] data, long timeout, @Nullable OperationCallback<Void> callback) {
		LOG.trace("Program device (timeout: " + timeout + "ms)");
		ProgramOperation operation = createOperation(programProvider, timeout, callback, "program is not available");
		operation.setBinaryImage(data);
		return queue.submitOperation(operation);
	}

	@Override
	public OperationFuture<byte[]> readFlash(int address, int length, long timeout, 
			@Nullable OperationCallback<byte[]> callback) {
		LOG.trace("Read flash (address: " + address + ", length: " + length + ", timeout: " + timeout + "ms)");
		ReadFlashOperation operation = createOperation(readFlashProvider, timeout, callback, "readFlash");
		checkArgument(address >= 0, NEGATIVE_LENGTH_MESSAGE);
		checkArgument(length >= 0, NEGATIVE_ADDRESS_MESSAGE);
		operation.setAddress(address, length);
		return queue.submitOperation(operation);
	}

	@Override
	public OperationFuture<MacAddress> readMac(long timeout, @Nullable OperationCallback<MacAddress> callback) {
		LOG.trace("Read mac (timeout: " + timeout + "ms)");
		ReadMacAddressOperation operation = createOperation(readMacAddressProvider, timeout, callback, "readMac");
		return queue.submitOperation(operation);
	}

	@Override
	public OperationFuture<Void> reset(long timeout, @Nullable OperationCallback<Void> callback) {
		LOG.trace("Reset device (timeout: " + timeout + "ms)");
		ResetOperation operation = createOperation(resetProvider, timeout, callback, "reset is not available");
		return queue.submitOperation(operation);
	}

	@Override
	public OperationFuture<Void> send(byte[] message, long timeout, @Nullable OperationCallback<Void> callback) {
		LOG.trace("Send packet to device (timeout: " + timeout + "ms)");
		SendOperation operation = createOperation(sendProvider, timeout, callback, "send is not available");
		operation.setMessage(message);
		return queue.submitOperation(operation);
	}

	@Override
	public OperationFuture<Void> writeFlash(int address, byte[] data, int length, long timeout, 
			@Nullable OperationCallback<Void> callback) {
		LOG.trace("Write flash (address: " + address + ", length: " + length + ", timeout: " + timeout + "ms)");
		WriteFlashOperation operation = createOperation(writeFlashProvider, timeout, callback, "writeFlash");
		checkArgument(address >= 0, NEGATIVE_LENGTH_MESSAGE);
		checkNotNull(data, "Null data is not allowed.");
		checkArgument(length >= 0, NEGATIVE_ADDRESS_MESSAGE);
		operation.setData(address, data, length);
		return queue.submitOperation(operation);
	}

	@Override
	public OperationFuture<Void> writeMac(MacAddress macAddress, long timeout, 
			@Nullable OperationCallback<Void> callback) {
		LOG.trace("Write mac (mac address: " + macAddress + ", timeout: " + timeout + "ms)");
		WriteMacAddressOperation operation = createOperation(writeMacAddressProvider, timeout, callback, "writeMac");
		checkNotNull(macAddress, "Null macAdress is not allowed.");
		operation.setMacAddress(macAddress);
		return queue.submitOperation(operation);
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public OutputStream getOutputStream() {
		return outputStreamProvider.get();
	}

	@Override
	public void close() throws IOException {
		connection.close();
	}
	
	@Override
	public void connect(String uri) throws IOException {
		connection.connect(uri);
	}

	@Override
	public boolean isConnected() {
		return connection.isConnected();
	}
	
	@Override
	public boolean isClosed() {
		return connection.isClosed();
	}

	@Override
	public void addListener(ConnectionListener listener) {
		connection.addListener(listener);
	}

	@Override
	public void removeListener(ConnectionListener listener) {
		connection.removeListener(listener);
	}

	@Override
	public int[] getChannels() {
		return connection.getChannels();
	}

	private static <T, O extends Operation<T>> O createOperation(Provider<O> provider, long timeout, 
			OperationCallback<T> callback, String operationName) {
		if (provider == null) {
			throwUnsupportedOperationException(operationName);
		}
		O operation = provider.get();
		if (operation == null) {
			throwUnsupportedOperationException(operationName);
		}
		checkArgument(timeout >= 0, "Negative timeout is not allowed.");
		operation.setTimeout(timeout);
		operation.setAsyncCallback(callback);
		return operation;
	}
	
	private static void throwUnsupportedOperationException(String operationName) {
		throw new UnsupportedOperationException(String.format(NOT_AVAILABLE_PATTERN, operationName));
	}

	@Inject(optional = true)
	public void setGetChipTypeProvider(Provider<GetChipTypeOperation> getChipTypeProvider) {
		this.getChipTypeProvider = getChipTypeProvider;
	}

	@Inject(optional = true)
	public void setEraseFlashProvider(
			Provider<EraseFlashOperation> eraseFlashProvider) {
		this.eraseFlashProvider = eraseFlashProvider;
	}

	@Inject(optional = true)
	public void setProgramProvider(Provider<ProgramOperation> programProvider) {
		this.programProvider = programProvider;
	}

	@Inject(optional = true)
	public void setReadFlashProvider(Provider<ReadFlashOperation> readFlashProvider) {
		this.readFlashProvider = readFlashProvider;
	}

	@Inject(optional = true)
	public void setReadMacAddressProvider(Provider<ReadMacAddressOperation> readMacAddressProvider) {
		this.readMacAddressProvider = readMacAddressProvider;
	}

	@Inject(optional = true)
	public void setResetProvider(Provider<ResetOperation> resetProvider) {
		this.resetProvider = resetProvider;
	}

	@Inject(optional = true)
	public void setWriteFlashProvider(Provider<WriteFlashOperation> writeFlashProvider) {
		this.writeFlashProvider = writeFlashProvider;
	}

	@Inject(optional = true)
	public void setWriteMacAddressProvider(Provider<WriteMacAddressOperation> writeMacAddressProvider) {
		this.writeMacAddressProvider = writeMacAddressProvider;
	}
}
