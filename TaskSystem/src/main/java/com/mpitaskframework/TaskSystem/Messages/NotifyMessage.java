package com.mpitaskframework.TaskSystem.Messages;

import com.mpitaskframework.TaskSystem.Message;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;

/**
 * A simple notify message with a success variable.
 * @author François Gingras <bizzard4>
 *
 */
public class NotifyMessage extends Message {
	
	public boolean success;
	
	public static final int NOTIFYMESSAGE_TID = 2;

	protected NotifyMessage(int pTag, boolean pSuccess) {
		super(pTag, NOTIFYMESSAGE_TID);
		success = pSuccess;
	}

	@Override
	public void append(ExcerptAppender appender) {
		// TODO Auto-generated method stub
		
	}
}
