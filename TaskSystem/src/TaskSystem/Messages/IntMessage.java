package TaskSystem.Messages;

import TaskSystem.Message;

/**
 * A basic message containing an integer.
 * @author bizzard4
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
