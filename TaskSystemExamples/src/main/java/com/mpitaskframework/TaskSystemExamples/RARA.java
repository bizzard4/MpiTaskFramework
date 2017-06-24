package com.mpitaskframework.TaskSystemExamples;

import com.mpitaskframework.TaskSystem.Message;
import com.mpitaskframework.TaskSystem.Task;
import com.mpitaskframework.TaskSystem.TaskSystem;

import io.mappedbus.MemoryMappedFile;

/**
 * Request Ack Response Ack is a common message format.
 * @author François Gingras <bizzard4>
 *
 */
public class RARA {
	
	/**
	 * This sample will show how to implement a very useful request acknowledge response acknowledge (RARA) 
	 * communication pattern. This pattern is very useful for asynchronous execution.
	 * @param args
	 */
	public static void main(String[] args) {
		RARA sample = new RARA();
		
		Task.createTask(sample.new RequestTask()); // Create the main task
	}
	
	public enum RARAMessages { REQ_TAG, REQ_ACK_TAG, RES_TAG, RES_ACK_TAG}; 
	
	public class RequestTask extends Task {
		
		/*
		 * This task, need a request (a sum) to be done by another task. But, while this
		 * request is executed it can still progress into his task until a certain point
		 * where it will need the response. This is a very common scenario.
		 * 
		 * Here the scenario we will simulate.
		 * 1) Ask another task to do a sum for the main task
		 * 2) Continue progressing while the other task is working on the sum request
		 * 3) At some point, wait on response before progressing
		 * 4) When response arrive, send the acknowledge
		 * 
		 * The acknowledges are optional. In a network situation that last acknowledge is used to be sure
		 * that the message wasn't lost. The first ack could also be used to detect error in the request.
		 * 
		 * See wiki for more communication pattern.
		 * 
		 */
		
		private int m_sumResult;
		
		@Override
		protected void initialize() {
			m_sumResult = -1;
		}

		@Override
		public void start() {
			// <<Do some work>>
			
			// This task need a sum to be done. (Its a large sum and can be done in //)
			// In this case, we create the task. In reality, this task could have been created
			// somewhere else.
			int response_task_id = Task.createTask(new ResponseTask());
			
			// Send the request
			SumRequest req = new SumRequest(RARAMessages.REQ_TAG.ordinal(), 0, 10000, this.getTaskId());
			send(req, response_task_id);
			
			// Wait on ACK
			receive(); // Error can be treated here if needed
			
			// <<Do more work while sum is computed>>
			for (int i = 0; i < 100000; i++) { }
			
			// Get the response.
			receive();
			
			// Send ACK
			SumAck ack = new SumAck(RARAMessages.RES_ACK_TAG.ordinal(), true);
			send(ack, response_task_id);
			
			// <<Use the response to do some work>>
		}

		@Override
		public void receive() {
			// Loop until we get a new message.
			Message msg = this.getNextMessage();
			
			switch (RARAMessages.values()[msg.getTag()]) {
			case REQ_ACK_TAG:
				SumAck realMsg = (SumAck)msg;
				System.out.println("Sum request acknowledge received with success=" + realMsg.isOk);
				break;
			case RES_TAG:
				SumResponse responseMsg = (SumResponse)msg;
				System.out.println("Sum response received with result=" + responseMsg.result);
				this.m_sumResult = responseMsg.result;
				break;
			default:
				System.out.println("Received unsuported message");
				break;
			}
		}
	}
	
	public class ResponseTask extends Task {
		private int m_from;
		private int m_to;
		private int m_answer_to;
		@Override
		protected void initialize() {
			m_from = -1;
			m_to = -1;
			m_answer_to = -1;
		}

		@Override
		public void start() {
			// This task is responsible to execute sum request
			// We start by waiting for a request
			receive();
			
			// Send request ACK
			SumAck ack = new SumAck(RARAMessages.REQ_ACK_TAG.ordinal(), true);
			send(ack, m_answer_to);
			
			// Compute the sum
			int sum = 0;
			for (int i = m_from; i < m_to; i++) {
				sum += i;
			}
			
			// Send response
			SumResponse res = new SumResponse(RARAMessages.RES_TAG.ordinal(), sum);
			send(res, m_answer_to);
			
			// Wait for response ACK
			receive();
		}

		@Override
		public void receive() {
			// Loop until we get a new message.
			Message msg = this.getNextMessage();
			
			switch (RARAMessages.values()[msg.getTag()]) {
			case REQ_TAG:
				SumRequest reqMsg = (SumRequest)msg;
				System.out.println("Sum request received");
				// We could also keep a reference to the message, but this may be tempting to share data among
				// threads using this message. This is the kind of stuff that break the scalability of a program
				// but are so easy to do in Java.
				m_from = reqMsg.from;
				m_to = reqMsg.to;
				m_answer_to = reqMsg.responseToId;
				break;
			case RES_ACK_TAG:
				SumAck responseAck = (SumAck)msg;
				System.out.println("Sum response acknowledge received with sucess=" + responseAck.isOk);
				break;
			default:
				System.out.println("Received unsuported message");
				break;
			}
		}
		
	}
	
	/**
	 * Sum request message.
	 */
	public class SumRequest extends Message {	
		public int from;
		public int to;
		public int responseToId;

		protected SumRequest(int pTag, int pFrom, int pTo, int pResponseToId) {
			super(pTag, 2);
			
			from = pFrom;
			to = pTo;
			responseToId = pResponseToId;
		}

		@Override
		public Message clone() {
			return new SumRequest(this.getTag(), this.from, this.to, this.responseToId);
		}

		@Override
		public void write(MemoryMappedFile mem, long pos) {
			mem.putInt(pos, this.getTag());
			mem.putInt(pos + 4, from);
			mem.putInt(pos + 8, to);
			mem.putInt(pos + 12, responseToId);
		}

		@Override
		public void read(MemoryMappedFile mem, long pos) {
			setTid(2);
			setTag(mem.getInt(pos));
			from = mem.getInt(pos + 4);
			to = mem.getInt(pos + 8);
			responseToId = mem.getInt(pos + 12);
		}
	}
	
	/**
	 * Sum response message.
	 */
	public class SumResponse extends Message {
		public int result;
		
		protected SumResponse(int pTag, int pResult) {
			super(pTag, 3);
			result = pResult;
		}

		@Override
		public Message clone() {
			return new SumResponse(this.getTag(), this.result);
		}

		@Override
		public void write(MemoryMappedFile mem, long pos) {
			mem.putInt(pos, this.getTag());
			mem.putInt(pos + 4, result);
		}

		@Override
		public void read(MemoryMappedFile mem, long pos) {
			setTid(3);
			setTag(mem.getInt(pos));
			result = mem.getInt(pos + 4);
		}
	}
	
	/**
	 * Sum acknowledge. Using different tag we will difer from request to response acknowledge. 
	 * But we will reuse this message.
	 */
	public class SumAck extends Message {
		
		public boolean isOk;

		protected SumAck(int pTag, boolean pIsOk) {
			super(pTag, 4);
			
			isOk = pIsOk;
		}

		@Override
		public Message clone() {
			return new SumAck(this.getTag(), this.isOk);
		}

		@Override
		public void write(MemoryMappedFile mem, long pos) {
			mem.putInt(pos, this.getTag());
			mem.putInt(pos + 4, isOk ? 1 : 0);
		}

		@Override
		public void read(MemoryMappedFile mem, long pos) {
			setTid(4);
			setTag(mem.getInt(pos));
			isOk = mem.getInt(pos + 4) == 1 ? true : false;
		}
	}

}
