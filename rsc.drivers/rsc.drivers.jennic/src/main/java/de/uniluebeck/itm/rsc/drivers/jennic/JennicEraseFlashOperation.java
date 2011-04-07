package de.uniluebeck.itm.rsc.drivers.jennic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.rsc.drivers.core.Monitor;
import de.uniluebeck.itm.rsc.drivers.core.exception.FlashEraseFailedException;
import de.uniluebeck.itm.rsc.drivers.core.operation.AbstractOperation;
import de.uniluebeck.itm.rsc.drivers.core.operation.EraseFlashOperation;

public class JennicEraseFlashOperation extends AbstractOperation<Void> implements EraseFlashOperation {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(JennicEraseFlashOperation.class);
	
	private final JennicDevice device;
	
	public JennicEraseFlashOperation(JennicDevice device) {
		this.device = device;
	}
	
	private void eraseFlash(final Monitor monitor) throws Exception {
		device.sendBootLoaderMessage(Messages.statusRegisterWriteMessage((byte) 0x00));
		monitor.onProgressChange(0.25f);
		byte[] response = device.receiveBootLoaderReply(Messages.WRITE_SR_RESPONSE);

		if (response[1] != 0x0) {
			log.error(String.format("Failed to write status register."));
			throw new FlashEraseFailedException();
		}
		
		if (isCanceled()) {
			return;
		}
		
		monitor.onProgressChange(0.5f);
		log.trace("Erasing flash");
		device.sendBootLoaderMessage(Messages.flashEraseRequestMessage());
		response = device.receiveBootLoaderReply(Messages.FLASH_ERASE_RESPONSE);

		if (response[1] != 0x0) {
			throw new FlashEraseFailedException("Failed to erase flash.");
		}
		monitor.onProgressChange(1.0f);
	}
	
	@Override
	public Void execute(Monitor monitor) throws Exception {
		executeSubOperation(device.createEnterProgramModeOperation(), monitor);
		try {
			eraseFlash(monitor);
		} finally {
			executeSubOperation(device.createLeaveProgramModeOperation(), monitor);
		}
		return null;
	}

}
