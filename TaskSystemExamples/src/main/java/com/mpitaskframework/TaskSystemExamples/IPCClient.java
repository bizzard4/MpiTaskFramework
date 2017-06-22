package com.mpitaskframework.TaskSystemExamples;

import com.mpitaskframework.TaskSystem.Message;
import com.mpitaskframework.TaskSystem.Task;
import com.mpitaskframework.TaskSystem.TaskSystem;
import com.mpitaskframework.TaskSystem.Messages.IntMessage;
import com.mpitaskframework.TaskSystemExamples.LocalRepReq.ReqResMessages;

/**
 * Client for the echo sample in IPC mode.
 * @see LocaRepReq for sample inter-thread.
 * @author Francois Gingras <bizzard4>
 *
 */
public class IPCClient extends Task {

	/**
	 * Main.
	 * @param args
	 */
	public static void main(String[] args) {
		TaskSystem.activateSystem(false); // The client is not responsible to create the system. It will acquire it.
		Task.createTask(new IPCClient(1)); // Here server id will be 1, but we would need a way to know that.
		
		// In future work, there will be a repository feature.
	}
	
	private int server_id;
	public IPCClient(int pServerId) {
		server_id = pServerId;
	}

	@Override
	protected void initialize() { }

	@Override
	public void start() {
		System.out.println("IPCClient started with id=" + this.getTaskId());
		
		// Send request
		long count = 0;
		
		long startTime = System.currentTimeMillis();
		while (true) {
			IntMessage request = new IntMessage(ReqResMessages.REQUEST_MSG.ordinal(), this.getTaskId());
			send(request, server_id);
			
			// Use the wait strategy to yield cpu because there arre very low
			// chance that the response to already ready.
			message_wait();
			receive();
			
			count++;
			if (count%1000000 == 0) {
				long totalTime = System.currentTimeMillis() - startTime;
				
				double total_sec = totalTime/1000.0d;
				long req_s = (long)(1000000/(total_sec));
				System.out.println("Client("+this.getTaskId()+") : Request count=" + count + " at " + req_s + " req/s ");
				
				startTime = System.currentTimeMillis();
			}
		}
	}

	@Override
	public void receive() {
		// Loop until we get a new message.
		Message msg = this.getNextMessage();
		
		switch (ReqResMessages.values()[msg.getTag()]) {
		case RESPONSE_MSG:
			IntMessage realMsg = (IntMessage)msg;
			//System.out.println("Client received response=" + realMsg.value);
			break;
		case REQUEST_MSG:
			System.out.println("Client should not receive response message");
			break;
		default:
			System.out.println("Received unsuported message");
			break;
		}
	}
	
	

}
