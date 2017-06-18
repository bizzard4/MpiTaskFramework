package com.mpitaskframework.TaskSystem.Messages;

import com.mpitaskframework.TaskSystem.Message;

/**
 * A simple notify message with a success variable.
 * @author François Gingras <bizzard4>
 *
 */
public class NotifyMessage extends Message {
	
	public boolean success;

	protected NotifyMessage(int pTag, boolean pSuccess) {
		super(pTag);
		success = pSuccess;
	}

	@Override
	public Message clone() {
		return new NotifyMessage(this.getTag(), this.success);
	}

}
