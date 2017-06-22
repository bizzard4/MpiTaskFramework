package com.mpitaskframework.TaskSystemExamples;

import com.mpitaskframework.TaskSystem.Message;
import com.mpitaskframework.TaskSystem.Task;
import com.mpitaskframework.TaskSystem.TaskSystem;
import com.mpitaskframework.TaskSystem.Messages.IntMessage;
import com.mpitaskframework.TaskSystemExamples.LocalRepReq.ReqResMessages;

/**
 * Echo server working in IPC mode.
 * @see LocaRepReq for sample inter-thread.
 * @author Francois Gingras <bizzard4>
 *
 */
public class IPCServer extends Task {
	
	/**
	 * Main.
	 * @param args
	 */
	public static void main(String[] args) {
		TaskSystem.activateSystem(true);
		Task.createTask(new IPCServer());
	}
	
	private int client_to_response;

	@Override
	protected void initialize() {
		client_to_response = 0;
	}

	@Override
	public void start() {
		System.out.println("IPCServer started with id=" + this.getTaskId());
		
		// Response to all request.
		long count = 0;
		
		long startTime = System.currentTimeMillis();
		while (true) {
			receive();
			
			IntMessage response = new IntMessage(ReqResMessages.RESPONSE_MSG.ordinal(), 200);
			send(response, client_to_response);
			
			count++;
			if (count%1000000 == 0) {
				long totalTime = System.currentTimeMillis() - startTime;
				
				double total_sec = totalTime/1000.0d;
				long req_s = (long)(1000000/(total_sec));
				System.out.println("Server : Request count=" + count + " at " + req_s + " req/s ");
				
				startTime = System.currentTimeMillis();
			}
		}
	}

	@Override
	public void receive() {
		// Loop until we get a new message.
		Message msg = this.getNextMessage();
		
		switch (ReqResMessages.values()[msg.getTag()]) {
		case REQUEST_MSG:
			IntMessage realMsg = (IntMessage)msg;
			//System.out.println("Server received request with client_id=" + realMsg.value);
			client_to_response = realMsg.value;
			break;
		case RESPONSE_MSG:
			System.out.println("Server should not receive response message");
			break;
		default:
			System.out.println("Received unsuported message");
			break;
		}
	}
}
