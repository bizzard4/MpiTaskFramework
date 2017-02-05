package TaskSystem;

/**
 * An abstract task. A task is a small process (a thread in this case) that can send and receive messages.
 * @author bizzard4
 *
 */
public abstract class Task implements Runnable {
	
	private int m_taskId;
	
	/**
	 * Protected constructor, to create a task we need to use the createTask. The actual instance of this class
	 * should never be available to the user.
	 * @param pTaskId
	 */
	public Task() {
		m_taskId = TaskSystem.getInstance().getNextTaskId();
	}
	
	/**
	 * Return task identifier.
	 * @return Task id.
	 */
	public int getTaskId() {
		return m_taskId;
	}
	
	/**
	 * Create the task and return the unique task id generated with it. A user could use directly the refenrece of the task
	 * but that would break the idea behind.
	 * @return
	 */
	public static <T extends Task> int createTask(T task) {
		TaskSystem.getInstance().createMessageQueue(task.getTaskId());
		task.initialize();
		(new Thread(task)).start();
		return task.getTaskId();
	}
	
	/**
	 * Task specific initializer.
	 * @return
	 */
	protected abstract void initialize();
	
	/**
	 * This is the entry method for the task.
	 */
	public abstract void start();
	
	/**
	 * Receive method. See sample for receive strategies.
	 */
	public abstract void receive();
	
	/**
	 * Thread "main" entry.
	 */
	@Override
	public void run() {
		start();
	}
	
	/**
	 * Wrapper so send a message to another task.
	 * @param pMsg Message.
	 * @param pTaskId Destination task id.
	 */
	protected void send(Message pMsg, int pTaskId) {
		TaskSystem.getInstance().send(pMsg, pTaskId);
	}
	
	
}
