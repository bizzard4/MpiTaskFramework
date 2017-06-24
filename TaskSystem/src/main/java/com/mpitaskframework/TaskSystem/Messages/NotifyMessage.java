package com.mpitaskframework.TaskSystem.Messages;

import com.mpitaskframework.TaskSystem.Message;

import io.mappedbus.MemoryMappedFile;
/**
 * A simple notify message with a success variable.
 * @author François Gingras <bizzard4>
 *
 */
public class NotifyMessage extends Message {
	
	public boolean success;
	
	public static final int NOTIFYMESSAGE_TID = 2;
	
	static {
		System.out.println("In static initializer");
	}

	protected NotifyMessage(int pTag, boolean pSuccess) {
		super(pTag, NOTIFYMESSAGE_TID);
		success = pSuccess;
	}

	@Override
	public void write(MemoryMappedFile mem, long pos) {
		mem.putInt(pos, this.getTag());
		mem.putInt(pos + 4, success ? 1 : 0);
	}

	@Override
	public void read(MemoryMappedFile mem, long pos) {
		setTid(NOTIFYMESSAGE_TID);
		setTag(mem.getInt(pos));
		success = mem.getInt(pos + 4) == 1 ? true : false;
	}
}
