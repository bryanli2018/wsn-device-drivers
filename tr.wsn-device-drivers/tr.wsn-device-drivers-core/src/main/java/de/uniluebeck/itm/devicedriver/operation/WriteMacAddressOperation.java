package de.uniluebeck.itm.devicedriver.operation;

import de.uniluebeck.itm.devicedriver.MacAddress;

/**
 * Interface that defines an <code>Operation</code> that write a <code>MacAddress</code> to the device.
 * 
 * @author Malte Legenhausen
 */
public interface WriteMacAddressOperation extends Operation<Void> {

	/**
	 * Sets the <code>MacAddress</code> that has to be written to the device.
	 * 
	 * @param macAddress The <code>MacAddress</code> that has to be written.
	 */
	void setMacAddress(MacAddress macAddress);
}