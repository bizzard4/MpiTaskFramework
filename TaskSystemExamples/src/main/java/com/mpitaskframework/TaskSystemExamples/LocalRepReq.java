package com.mpitaskframework.TaskSystemExamples;

import com.mpitaskframework.TaskSystem.Message;
import com.mpitaskframework.TaskSystem.Task;
import com.mpitaskframework.TaskSystem.TaskSystem;
import com.mpitaskframework.TaskSystem.Messages.IntMessage;

/**
 * Example of a request and response application style intra-thread.
 * @author Francois Gingras <bizzard4>
 *
 */
public class LocalRepReq {

	/**
	 * This sample will start a server and a client. The client send request to the server and response
	 * back to the client.
	 */
	public static void main(String[] args) {
		LocalRepReq sample = new LocalRepReq();
		
		int server_id = Task.createTask(sample.new ServerTask());

		if (args.length != 1) {
			System.out.println("Missing program argument : number of client");
			System.exit(-1);
		}
		
		int nb_client = Integer.parseInt(args[0]);
		for (int i = 0; i < nb_client; i++) {
			Task.createTask(sample.new ClientTask(server_id));
		}
	}
	
	public enum ReqResMessages { REQUEST_MSG, RESPONSE_MSG };
	
	public class ServerTask extends Task {
		
		/*
		 * This task will response to any request from n clients.
		 */
		
		private int client_to_response;
		
		@Override
		protected void initialize() {
			client_to_response = 0;
		}
		
		@Override
		public void start() {
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
			int tag = TaskSystem.getInstance().getMessageTag(this.getTaskId());
			while (tag == -1) {
				tag = TaskSystem.getInstance().getMessageTag(this.getTaskId());
			}
			
			Message msg = TaskSystem.getInstance().receive(this.getTaskId());
			
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
	
	public class ClientTask extends Task {
		
		/*
		 * This task will response to send request to the server.
		 */
		
		private int server_id;
		
		public ClientTask(int pServerId) {
			server_id = pServerId;
		}
		
		@Override
		protected void initialize() { }
		
		@Override
		public void start() {
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
			int tag = TaskSystem.getInstance().getMessageTag(this.getTaskId());
			while (tag == -1) {
				tag = TaskSystem.getInstance().getMessageTag(this.getTaskId());
			}
			
			Message msg = TaskSystem.getInstance().receive(this.getTaskId());
			
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
}
