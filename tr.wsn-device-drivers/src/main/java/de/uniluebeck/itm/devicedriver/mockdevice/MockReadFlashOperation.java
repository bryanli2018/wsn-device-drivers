package de.uniluebeck.itm.devicedriver.mockdevice;

import de.uniluebeck.itm.devicedriver.Monitor;
import de.uniluebeck.itm.devicedriver.operation.AbstractReadFlashOperation;

public class MockReadFlashOperation extends AbstractReadFlashOperation {

	private final byte[] flashRom;
	
	public MockReadFlashOperation(byte[] flashRom) {
		this.flashRom = flashRom;
	}
	
	@Override
	public byte[] execute(Monitor monitor) throws Exception {
		for(int i = 1; i <= 10 && !isCanceled(); ++i) {
			Thread.sleep(100 * i);
			monitor.onProgressChange(0.1f * i);
		}
		byte[] result = new byte[length];
		System.arraycopy(flashRom, address, result, 0, length);
		return result;
	}

}
