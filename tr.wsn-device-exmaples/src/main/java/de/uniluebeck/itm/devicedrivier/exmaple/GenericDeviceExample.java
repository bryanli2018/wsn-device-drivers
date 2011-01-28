package de.uniluebeck.itm.devicedrivier.exmaple;

import java.io.File;
import java.io.IOException;

import de.uniluebeck.itm.devicedriver.ChipType;
import de.uniluebeck.itm.devicedriver.Connection;
import de.uniluebeck.itm.devicedriver.ConnectionEvent;
import de.uniluebeck.itm.devicedriver.ConnectionListener;
import de.uniluebeck.itm.devicedriver.Device;
import de.uniluebeck.itm.devicedriver.MacAddress;
import de.uniluebeck.itm.devicedriver.MessagePacket;
import de.uniluebeck.itm.devicedriver.MessagePacketListener;
import de.uniluebeck.itm.devicedriver.async.AsyncAdapter;
import de.uniluebeck.itm.devicedriver.async.AsyncCallback;
import de.uniluebeck.itm.devicedriver.async.DeviceAsync;
import de.uniluebeck.itm.devicedriver.async.OperationQueue;
import de.uniluebeck.itm.devicedriver.async.QueuedDeviceAsync;
import de.uniluebeck.itm.devicedriver.async.thread.PausableExecutorOperationQueue;
import de.uniluebeck.itm.devicedriver.event.MessageEvent;
import de.uniluebeck.itm.devicedriver.util.FileUtil;

public class GenericDeviceExample implements MessagePacketListener, ConnectionListener {

	private final OperationQueue queue = new PausableExecutorOperationQueue();
	
	private Device device;
	
	private Connection connection;
	
	private DeviceAsync deviceAsync;
	
	private File image;
	
	private String uri;

	public void setDevice(Device device) {
		this.device = device;
	}

	public void setImage(File image) {
		this.image = image;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	private void init() {
		connection = device.getConnection();
		connection.addListener(this);
		deviceAsync = new QueuedDeviceAsync(queue, device);
	}
	
	public void connect() {
		System.out.println("Connecting to: " + uri);
		connection.connect(uri);
	}
	
	public void programImage() throws IOException {
		final AsyncCallback<Void> callback = new AsyncAdapter<Void>() {
			@Override
			public void onExecute() {
				System.out.println("Flashing image...");
			}
			
			@Override
			public void onProgressChange(float fraction) {
				final int percent = (int) (fraction * 100.0);
				System.out.println("Programming progress: " + percent + "%");
			}
			
			@Override
			public void onSuccess(Void result) {
				System.out.println("Image successfully flashed");
			}
		};
		
		final byte[] bytes = FileUtil.fileToBytes(image);
		System.out.println("Image length: " + bytes.length);
	    deviceAsync.program(bytes, 300000, callback);
	}
	
	public void resetOperation() {
		final AsyncCallback<Void> callback = new AsyncAdapter<Void>() {
			public void onExecute() {
				System.out.println("Resetting device...");
			}
			
			@Override
			public void onSuccess(Void result) {
				System.out.println("Device successful reseted");
			}
		};
		deviceAsync.reset(10000, callback);
	}
	
	public void macAddressOperations() {		
		final AsyncCallback<MacAddress> callback = new AsyncAdapter<MacAddress>() {
			
			@Override
			public void onExecute() {
				System.out.println("Reading mac address...");
			}
			
			@Override
			public void onProgressChange(float fraction) {
				final int percent = (int) (fraction * 100.0);
				System.out.println("Reading mac address progress: " + percent + "%");
			}
			
			@Override
			public void onSuccess(MacAddress result) {
				System.out.println("Mac Address: " + result.getMacString());
			}
		};
		
		deviceAsync.readMac(10000, callback);
		
		
		deviceAsync.writeMac(new MacAddress(1024), 10000, new AsyncAdapter<Void>() {

			@Override
			public void onExecute() {
				System.out.println("Setting Mac Address");
			}
			
			@Override
			public void onProgressChange(float fraction) {
				final int percent = (int) (fraction * 100.0);
				System.out.println("Writing mac address progress: " + percent + "%");
			}

			@Override
			public void onSuccess(Void result) {
				System.out.println("Mac Address written");
			}
		});
		deviceAsync.readMac(10000, callback);
	}
	
	public void readFlashOperation() {
		final AsyncCallback<byte[]> callback = new AsyncAdapter<byte[]>() {
			
			@Override
			public void onExecute() {
				System.out.println("Read flash from 0 to 32...");
			}
			
			@Override
			public void onProgressChange(float fraction) {
				System.out.println("Reading flash progress: " + fraction + "%");
			}
			
			@Override
			public void onSuccess(byte[] result) {
				System.out.println("Reading result: " + result);
			}
		};
		deviceAsync.readFlash(0, 32, 10000, callback);
	}
	
	public void chipTypeOperation() {
		deviceAsync.getChipType(10000, new AsyncAdapter<ChipType>() {

			@Override
			public void onExecute() {
				System.out.println("Reading ChipType from device...");
			}
			
			@Override
			public void onProgressChange(float fraction) {
				final int percent = (int) (fraction * 100.0);
				System.out.println("Reading chip type progress: " + percent + "%");
			}
			
			@Override
			public void onSuccess(ChipType result) {
				System.out.println("Chip Type: " + result);
			}
		});
	}
	
	public void finish() {
		// Wait until the queue is empty.
		while (!queue.getOperations().isEmpty()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Shutting down queue...");
		queue.shutdown(false);
		System.out.println("Queue terminated");
		System.out.println("Closing connection...");
		connection.shutdown(true);
		System.out.println("Connection closed");
	}
	
	public void run() {
		init();
		try {
			connect();
			
			programImage();
			//resetOperation();
			//macAddressOperations();
			//readFlashOperation();
			//chipTypeOperation();
			finish();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onMessagePacketReceived(MessageEvent<MessagePacket> event) {
		System.out.println(new String(event.getMessage().getContent()));
	}
	
	@Override
	public void onConnectionChange(ConnectionEvent event) {
		System.out.println("Connected with port: " + event.getUri());
	}
}
