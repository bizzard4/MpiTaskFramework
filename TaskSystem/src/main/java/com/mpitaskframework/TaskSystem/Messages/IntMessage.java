package com.mpitaskframework.TaskSystem.Messages;

import com.mpitaskframework.TaskSystem.Message;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;

/**
 * A basic message containing an integer.
 * @author François Gingras <bizzard4>
 *
 */
public class IntMessage extends Message {
	
	public int value;
	
	public static final int INTMESSAGE_TID = 1;

	public IntMessage(int pTag, int pValue) {
		super(pTag, INTMESSAGE_TID);
		value = pValue;
	}

	@Override
	public void append(ExcerptAppender appender) {
		appender.writeDocument(w -> w.write("message").marshallable(
				m -> m.write("tag").int32(this.getTag())
						.write("tid").int32(this.getTid())
						.write("value").int32(value)));
	}
}
