package TaskSystem;

/**
 * Abstract message class.
 * @author bizzard4
 *
 */
public abstract class Message {
	
	/**
	 * This is a custom tag id representing the message type or order. The tag can be used as a form of state machine.
	 */
	private int m_tag;
	
	/**
	 * Message are clone at each send.
	 */
	public abstract Message clone();
	
	/**
	 * Get the tag.
	 * @return Tag.
	 */
	public int getTag() {
		return m_tag;
	}
	
	/**
	 * Constructor. Need a tag.
	 * @param pTag Tag id.
	 */
	protected Message(int pTag) {
		m_tag = pTag;
	}
}
