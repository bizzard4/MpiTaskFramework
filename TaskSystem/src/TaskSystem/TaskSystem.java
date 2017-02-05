package TaskSystem;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * System class is a central point where message queues are kept. Every method can be call from any task, this
 * class need to be lock-free and thread-safe. One mutex is used for the incrementing task id.
 * @author bizzard4
 *
 */
public class TaskSystem {
	
	/**
	 * Mutex for next task id. Only mutex in all the system. This would need a lock-free solution.
	 */
	private final Object mutex = new Object();
	
	/**
	 * For task id increase.
	 */
	private int m_nextTagId;
	
	/**
	 * We use a concurrent queue. The best would be to have a lock-free queue.
	 * Current maximum is 1000, but this system could be extended to a more generic one.
	 */
	private ConcurrentLinkedQueue<Message>[] m_queues = new ConcurrentLinkedQueue[1000];
	
	// System is a singleton, but that may not be a proper way of doing it.
	private static TaskSystem instance = null;
	public static TaskSystem getInstance() {
		if (instance==null) {
			instance = new TaskSystem();
		}
		
		return instance;
	}
	
	/**
	 * Constructor.
	 */
	private TaskSystem() {
		m_nextTagId = 0;
	}
	
	/**
	 * Add a message to a task queue. Communication is only between threads in the same process, but
	 * this system should be extensible to inter-process and network communication. This is part of the scaling.
	 * @param pMsg
	 * @param pTaskId
	 */
	public void send(Message pMsg, int pTaskId) {
		Message clone = pMsg.clone();
		m_queues[pTaskId].offer(clone);
	}
	
	/**
	 * Get the message from a task queue.
	 * @param pTaskId
	 * @return
	 */
	public Message receive(int pTaskId) {
		return m_queues[pTaskId].poll();
	}
	
	/**
	 * Retreive message tag if available.
	 * @param pTaskId
	 * @return
	 */
	public int getMessageTag(int pTaskId) {
		Message head = m_queues[pTaskId].peek();
		if (head==null) {
			return -1;
		}
		
		return head.getTag();
	}
	
	/**
	 * Drop the message.
	 * @param pTaskId
	 */
	public void dropMessage(int pTaskId) {
		m_queues[pTaskId].poll();
	}
	
	/**
	 * Create a new queue for a task id.
	 * @param pTaskId
	 */
	public void createMessageQueue(int pTaskId) {
		m_queues[pTaskId] = new ConcurrentLinkedQueue<>();
	}
	
	public int getNextTaskId() {
		synchronized(mutex) {
			m_nextTagId++;
		}
		return m_nextTagId;
	}
}
