package de.uniluebeck.itm.rsc.drivers.jennic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.rsc.drivers.core.ChipType;
import de.uniluebeck.itm.rsc.drivers.core.MacAddress;
import de.uniluebeck.itm.rsc.drivers.core.Monitor;
import de.uniluebeck.itm.rsc.drivers.core.operation.AbstractOperation;
import de.uniluebeck.itm.rsc.drivers.core.operation.GetChipTypeOperation;
import de.uniluebeck.itm.rsc.drivers.core.operation.ReadFlashOperation;
import de.uniluebeck.itm.rsc.drivers.core.operation.ReadMacAddressOperation;

public class JennicReadMacAddressOperation extends AbstractOperation<MacAddress>implements ReadMacAddressOperation {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(JennicReadMacAddressOperation.class);
	
	private final JennicDevice device;
	
	public JennicReadMacAddressOperation(JennicDevice device) {
		this.device = device;
	}
	
	@Override
	public MacAddress execute(final Monitor monitor) throws Exception {
		log.trace("Reading MAC Adress");
		// Connection established, determine chip type
		final GetChipTypeOperation getChipTypeOperation = device.createGetChipTypeOperation();
		final ChipType chipType = executeSubOperation(getChipTypeOperation, monitor);
		log.trace("Chip type is " + chipType);

		// Connection established, read flash header
		final int address = chipType.getMacInFlashStart();
		final ReadFlashOperation readFlashOperation = device.createReadFlashOperation();
		readFlashOperation.setAddress(address, 8);
		final byte[] header = executeSubOperation(readFlashOperation, monitor);

		final MacAddress macAddress = new MacAddress(header);
		log.trace("Done, result is: " + macAddress);
		return macAddress;
	}

}
