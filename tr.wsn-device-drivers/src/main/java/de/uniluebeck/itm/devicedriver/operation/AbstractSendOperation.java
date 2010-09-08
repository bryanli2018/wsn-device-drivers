package de.uniluebeck.itm.devicedriver.operation;

import de.uniluebeck.itm.devicedriver.MessagePacket;

public abstract class AbstractSendOperation extends AbstractOperation<Void> implements SendOperation {
	
	protected MessagePacket messagePacket;
	
	@Override
	public void setMessagePacket(MessagePacket messagePacket) {
		this.messagePacket = messagePacket;
	}
	
	public MessagePacket getMessagePacket() {
		return messagePacket;
	}
}