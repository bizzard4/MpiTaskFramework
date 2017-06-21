package com.mpitaskframework.TaskSystem;

import java.io.IOException;
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
	 * Shared system location.
	 */
	public static final String SYSTEM_SHARED_PATH = "/tmp/TS_SYSTEM";
	
	/**
	 * Reference to wait and signal thread. Will be null if this process is not
	 * the system creator.
	 */
	private Thread m_threadRef;
	
	/**
	 * Link to data in shared space.
	 */
	private SharedSystemData m_sharedData;
	
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
	
	
	/**
	 * The system is unique process wide. But in the case of IPC, it will need to be
	 * acquired instead of created.
	 */
	private static TaskSystem instance = null;
	
	/**
	 * Prepare and activate the system. Can be created or acquire depending on the context.
	 * @param create True to create the system.
	 */
	public static void activateSystem(boolean create) {
		if (instance != null) {
			System.err.println("Errir, system already activated");
			System.exit(-1);
		}
		
		// Create the object.
		instance = new TaskSystem();
		
		if (create) {
			try {
				instance.createSystem();
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("Error, " + e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}
		} else {
			instance.acquireSystem();
		}
	}
	
	/**
	 * This is not the best solution, but the one I will use for now. The system need to be prepared
	 * before used. It is an unique instance across all process.
	 * @return The system is initialized.
	 */
	public static TaskSystem getInstance() {
		if (instance==null) {
			System.err.print("Error, system is not initialized");
			System.exit(-1);
		}
		
		return instance;
	}
	
	/**
	 * Constructor. Put default values.
	 */
	private TaskSystem() {
		m_sharedData = null;
		
		for (int i = 0; i < MAX_TASK_COUNT; i++) {
			sleepers[i] = sleeper_lock.newCondition();
		}
	}
	
	/**
	 * Prepare a new instance of the system within a shared space.
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private void createSystem() throws IOException, ClassNotFoundException {
		// Create and initialize shared data
		m_sharedData = new SharedSystemData(SYSTEM_SHARED_PATH, true);
		
		// Start he wait and signal loop
		m_threadRef = new Thread(this);
		m_threadRef.start();
	}
	
	/**
	 * Acquire an existing instance of the system from the shared space.
	 */
	private void acquireSystem() {
		
	}
	
	/**
	 * Signal system for a clean exit.
	 */
	public void destroy() {
		m_sharedData.setShutdownSignal(true);
		
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
		
		if (m_queues[pTaskId] == null) {
			// Need to acquire
		}
		
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
		return m_sharedData.incrementNextTaskId();
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
		
		while(!m_sharedData.getShutdownSignal()) {
			
			// Signal each task with mesage waiting
			int current_max_id = m_sharedData.getNextTaskId();
			
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
