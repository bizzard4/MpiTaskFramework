package com.mpitaskframework.TaskSystemExamples;

import com.mpitaskframework.TaskSystem.Message;
import com.mpitaskframework.TaskSystem.Task;
import com.mpitaskframework.TaskSystem.TaskSystem;
import com.mpitaskframework.TaskSystem.Messages.IntMessage;

/**
 * In this sample, I will show how to create a task, a new message type and how to send messages between task.
 * @author François Gingras <bizzard4>
 *
 */
public class GettingStarted {

	/**
	 * Simple task A. 
	 * @author bizzard4
	 *
	 */
	public class A extends Task {

		@Override
		protected void initialize() {	
			// Custom task initialization here
		}

		/**
		 * Start method is the "process" run method.
		 */
		@Override
		public void start() {
			System.out.format("Task A create with Id=%d\n", this.getTaskId());
			
			// Create a task B, never use reference to the task. This is part of the good practices
			// for task system.
			int b_id = Task.createTask(new B());
			
			// Send an this task id as message
			IntMessage msg = new IntMessage(0, this.getTaskId());
			TaskSystem.getInstance().send(msg, b_id);
			
			// Get response
			receive();
		}

		/**
		 * Call this method to retrieve and process messages. There are various way to handle this, see 
		 * other samples.
		 */
		@Override
		public void receive() {
			// Loop until we get a new message. This is equivalent to a blocking call. Later this task
			// could be put to sleep until a new message arrive.
			int tag = TaskSystem.getInstance().getMessageTag(this.getTaskId());
			while (tag == -1) {
				tag = TaskSystem.getInstance().getMessageTag(this.getTaskId());
			}
			
			Message msg = TaskSystem.getInstance().receive(this.getTaskId());
			
			switch (msg.getTag()) {
			case 0: // Tag can be a enumeration
				IntMessage realMsg = (IntMessage)msg;
				System.out.println("Task A got the int back from B (" + realMsg.value + ")");
				default:
					
			}
		}
	}
	
	/**
	 * Simple task B.
	 * @author bizzard4
	 *
	 */
	public class B extends Task {
		
		private int m_aId;

		@Override
		protected void initialize() {
			m_aId = -1;
		}

		@Override
		public void start() {
			System.out.format("Task B create with Id=%d\n", this.getTaskId());
			
			// Wait on a message
			receive();
			
			// Send back the id to A
			IntMessage msg = new IntMessage(0, m_aId);
			TaskSystem.getInstance().send(msg, m_aId);
		}

		@Override
		public void receive() {
			// Loop until we get a new message. This is equivalent to a blocking call. Later this task
			// could be put to sleep until a new message arrive.
			int tag = TaskSystem.getInstance().getMessageTag(this.getTaskId());
			while (tag == -1) {
				tag = TaskSystem.getInstance().getMessageTag(this.getTaskId());
			}
			
			Message msg = TaskSystem.getInstance().receive(this.getTaskId());
			
			switch (msg.getTag()) {
			case 0:
				IntMessage realMsg = (IntMessage)msg;
				System.out.println("Task B got the int (" + realMsg.value + ") message");
				m_aId = realMsg.value; // Set the property
				default:
					
			}
		}	
	}
	
	/**
	 * Sample showing creating task and sending messages.
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		GettingStarted sample = new GettingStarted();
		
		TaskSystem.activateSystem(true);
		
		// Start the main task
		Task.createTask(sample.new A());
		
		// 1s sleep until everything is done.
		Thread.sleep(1000);
		
		TaskSystem.getInstance().destroy();
	}

}
