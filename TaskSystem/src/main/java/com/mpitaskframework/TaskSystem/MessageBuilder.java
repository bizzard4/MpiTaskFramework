package com.mpitaskframework.TaskSystem;

import com.mpitaskframework.TaskSystem.Messages.IntMessage;
import com.mpitaskframework.TaskSystem.Messages.NotifyMessage;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.ReadMarshallable;
import net.openhft.chronicle.wire.WireIn;

public class MessageBuilder implements ReadMarshallable {
	
	public Message result = null;
	
	@Override
	public void readMarshallable(WireIn arg0) throws IORuntimeException {
		int tag = arg0.read("tag").int32();
		int tid = arg0.read("tid").int32();
		
		switch (tid) {
		case IntMessage.INTMESSAGE_TID:
			int value = arg0.read("value").int32();
			result = new IntMessage(tag, value);
			break;
		case NotifyMessage.NOTIFYMESSAGE_TID:
			
			break;
		}
		
		
		
	}

}
