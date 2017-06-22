package com.mpitaskframework.TaskSystem;

import com.mpitaskframework.TaskSystem.Messages.IntMessage;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;

/**
 * Abstract message class.
 * @author François Gingras <bizzard4>
 *
 */
public abstract class Message {
	
	/**
	 * This is a custom tag id representing the message type or order. The tag can be used as a form of state machine.
	 */
	private int m_tag;
	
	/**
	 * Unique ID identifying the concrete message.
	 */
	private int m_tid;
	
	/**
	 * Message self-write itself into the queue.
	 * @param appender Appender to write on.
	 */
	public abstract void append(ExcerptAppender appender);
	
	/**
	 * Get the tag.
	 * @return Tag.
	 */
	public int getTag() {
		return m_tag;
	}
	
	/**
	 * Get the unique tid.
	 * @return Tid.
	 */
	public int getTid() {
		return m_tid;
	}
	
	/**
	 * Constructor. Need a tag.
	 * @param pTag Tag id.
	 */
	protected Message(int pTag, int pTid) {
		m_tag = pTag;
		m_tid = pTid;
	}
}
