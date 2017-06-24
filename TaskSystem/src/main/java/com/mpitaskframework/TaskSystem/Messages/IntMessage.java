package com.mpitaskframework.TaskSystem.Messages;

import com.mpitaskframework.TaskSystem.Message;

import io.mappedbus.MemoryMappedFile;

/**
 * A basic message containing an integer.
 * @author François Gingras <bizzard4>
 *
 */
public class IntMessage extends Message {
	
	public int value;
	
	public static final int INTMESSAGE_TID = 1;

	
	/**
	 * Default constructor.
	 */
	public IntMessage() {
		super(-1, -1);
		value = -1;
	}

	public IntMessage(int pTag, int pValue) {
		super(pTag, INTMESSAGE_TID);
		value = pValue;
	}

	@Override
	public void write(MemoryMappedFile mem, long pos) {
		mem.putInt(pos, this.getTag());
		mem.putInt(pos + 4, value);
	}

	@Override
	public void read(MemoryMappedFile mem, long pos) {
		setTid(INTMESSAGE_TID);
		setTag(mem.getInt(pos));
		value = mem.getInt(pos + 4);
	}
}
