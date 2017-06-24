package com.mpitaskframework.TaskSystem;

import io.mappedbus.MappedBusMessage;

/**
 * Abstract message class.
 * @author François Gingras <bizzard4>
 *
 */
public abstract class Message implements MappedBusMessage {
	
	/**
	 * This is a custom tag id representing the message type or order. The tag can be used as a form of state machine.
	 */
	private int m_tag;
	
	/**
	 * Unique ID identifying the concrete message.
	 */
	private int m_tid;
	
	/**
	 * Get the tag.
	 * @return Tag.
	 */
	public int getTag() {
		return m_tag;
	}
	
	/**
	 * Set the tag.
	 * @param pTag Tag.
	 */
	protected void setTag(int pTag) {
		m_tag = pTag;
	}
	
	/**
	 * Get the unique tid.
	 * @return Tid.
	 */
	public int getTid() {
		return m_tid;
	}
	
	/**
	 * Set Tid.
	 * @param pTid Tid.
	 */
	protected void setTid(int pTid) {
		m_tid = pTid;
	}
	
	/**
	 * Constructor. Need a tag.
	 * @param pTag Tag id.
	 */
	protected Message(int pTag, int pTid) {
		m_tag = pTag;
		m_tid = pTid;
	}
	
	@Override
	public int type() {
		return getTid();
	}
}
