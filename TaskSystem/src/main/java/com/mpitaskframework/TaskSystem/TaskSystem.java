package com.mpitaskframework.TaskSystem;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * System class is a central point where message queues are kept. Every method can be call from any task, this
 * class need to be lock-free and thread-safe. One mutex is used for the incrementing task id.
 * @author François Gingras <bizzard4>
 *
 */
public class TaskSystem implements Runnable {
	
	/**
	 * Maximum number of task.
	 */
	public static final int MAX_TASK_COUNT = 1000;
	
	/**
	 * Mutex for next task id. Only mutex in all the system. This would need a lock-free solution.
	 */
	private final Object mutex = new Object();
	
	/**
	 * For task id increase.
	 */
	private int m_nextTaskId;
	
	/**
	 * Reference to wait and signal thread.
	 */
	private Thread m_threadRef;
	
	/**
	 * Shutdown signal. Set to true to shutdown the wait and signal loop.
	 */
	private boolean m_shutdownSignal;
	
	/**
	 * We use a concurrent queue. The best would be to have a lock-free queue.
	 * Current maximum is 1000, but this system could be extended to a more generic one.
	 */
	@SuppressWarnings("unchecked")
	private ConcurrentLinkedQueue<Message>[] m_queues = new ConcurrentLinkedQueue[MAX_TASK_COUNT];
	
	/**
	 * Wait and signal condition and lock.
	 */
	final Lock sleeper_lock = new ReentrantLock();
	final Condition[] sleepers = new Condition[MAX_TASK_COUNT];
	
	
	// System is a singleton, but that may not be a proper way of doing it.
	private static TaskSystem instance = null;
	public static TaskSystem getInstance() {
		if (instance==null) {
			instance = new TaskSystem();
			instance.initialize();
		}
		
		return instance;
	}
	
	/**
	 * Constructor.
	 */
	private TaskSystem() {
		m_nextTaskId = 0;
		m_shutdownSignal = false;
		
		for (int i = 0; i < MAX_TASK_COUNT; i++) {
			sleepers[i] = sleeper_lock.newCondition();
		}
	}
	
	/**
	 * Prepare a new instance of the task system.
	 * @return
	 */
	private void initialize() {
		// Start he wait and signal loop
		m_threadRef = new Thread(this);
		m_threadRef.start();
	}
	
	/**
	 * Signal system for a clean exit.
	 */
	public void destroy() {
		m_shutdownSignal = true;
		
		try {
			m_threadRef.join();
		} catch (InterruptedException e) {
			System.err.println("System failed to join wait and signal thread");
			e.printStackTrace();
		}
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
	
	/**
	 * Return next free task id.
	 * @return
	 */
	public int getNextTaskId() {
		synchronized(mutex) {
			m_nextTaskId++;
		}
		return m_nextTaskId;
	}
	
	/**
	 * Return true if there are no message in the Q.
	 * @param pTaskId
	 * @return
	 */
	public boolean message_immediate(int pTaskId) {
		return m_queues[pTaskId].isEmpty();
	}
	
	/**
	 * Look for a message in the Q, if no message put to sleep and the
	 * wait and signal loop will wake him up when a new message is present.
	 * @param pTaskId
	 */
	public void message_notify(int pTaskId) {
		if (message_immediate(pTaskId)) {
			try {
				sleepers[pTaskId].await();
			} catch (InterruptedException e) {
				System.err.println("Condition variable failed to await");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Yield the current thread.
	 * @param pTaskId
	 */
	public void message_wait(int pTaskId) {
		Thread.yield();
	}

	/**
	 * Wait and signal loop.
	 */
	@Override
	public void run() {
		System.out.println("Wait and signal loop started");
		
		while(!this.m_shutdownSignal) {
			
			// Signal each task with mesage waiting
			int current_max_id;
			synchronized(mutex) {
				current_max_id = this.m_nextTaskId;
			}
			
			for (int i = 0; i < current_max_id; i++) {
				if ((m_queues[i] != null) && (!m_queues[i].isEmpty())) {
					sleeper_lock.lock();
					try {
						this.sleepers[i].signal();
					} finally {
						sleeper_lock.unlock();
					}
				}
			}
			
			try {
				Thread.sleep(10); // 10ms sleep
			} catch (InterruptedException e) {
				System.err.println("Wait and signal loop failed to sleep");
				e.printStackTrace();
			} 
		}
		
		System.out.println("Wait and signal loop shutdown");
	}
}
