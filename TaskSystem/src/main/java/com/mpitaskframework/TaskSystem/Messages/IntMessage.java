package com.mpitaskframework.TaskSystem.Messages;

import com.mpitaskframework.TaskSystem.Message;

/**
 * A basic message containing an integer.
 * @author François Gingras <bizzard4>
 *
 */
public class IntMessage extends Message {
	
	public int value;

	public IntMessage(int pTag, int pValue) {
		super(pTag);
		value = pValue;
	}

	@Override
	public Message clone() {
		return new IntMessage(this.getTag(), this.value); // Deep copy.
	}

}
