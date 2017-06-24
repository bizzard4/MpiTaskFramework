package com.mpitaskframework.TaskSystem;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

/**
 * This class represent the part of the system that is put inside the shared space.
 * @author Francois Gingras <bizzard4>
 *
 */
public class SharedSystemData implements Serializable {
	
	// Shared data structure : INT nextTaskId, INT shutdownSignal
	
	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 7998018154382518790L;

	/**
	 * Channel to the file. Not part of data.
	 */
	private transient FileChannel channel;
	
	/**
	 * Mapped byte buffer. Not part of data.
	 */
	private transient MappedByteBuffer buffer;
	
	/**
	 * Constructor.
	 * @throws IOException 
	 */
	public SharedSystemData(String path, boolean create) throws IOException {
		File f = new File(path);
		
		if (create) {
			if (f.exists()) {
				System.out.println("Existing system detected, deleting");
				f.delete(); // Delete if present.
			}
		} else {
			if (!f.exists()) {
				System.err.println("ERROR, system dont exist");
				System.exit(-1);
			}
		}
		channel = FileChannel.open(f.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		buffer = channel.map(MapMode.READ_WRITE, 0, 4000);
		
		if (create) {
			setNextTaskId(0);
			setShutdownSignal(false);
		}
	}
	
	@Override
	public String toString() {
		return new String("NextTaskId=" + getNextTaskId() + " ShutdownSignal=" + getShutdownSignal());
	}
	
	/**
	 * Acquire file lock and set next task id.
	 */
	public synchronized void setNextTaskId(int pNextTaskId) {
		try {
			FileLock l = channel.lock();
			buffer.position(0);
			buffer.putInt(pNextTaskId);
			l.release();
		} catch (IOException e) {
			System.err.println("SharedSystemData, IOException : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public synchronized int incrementNextTaskId() {
		int toRet = -1;
		try {
			FileLock l = channel.lock();
			buffer.position(0);
			int current = buffer.getInt();
			buffer.position(0);
			current++;
			toRet = current;
			buffer.putInt(current);
			l.release();
		} catch (IOException e) {
			System.err.println("SharedSystemData, IOException : " + e.getMessage());
			e.printStackTrace();
		}
		
		return toRet;
	}
	
	/**
	 * Acquire file lock and get next task id.
	 * @return
	 */
	public synchronized int getNextTaskId() {
		int toRet = -1;
		try {
			FileLock l = channel.lock();
			buffer.position(0);
			toRet = buffer.getInt();
			l.release();
		} catch (IOException e) {
			System.err.println("SharedSystemData, IOException : " + e.getMessage());
			e.printStackTrace();
		}
		
		return toRet;
	}
	
	public synchronized void setShutdownSignal(boolean pShutdownSignal) {
		try {
			FileLock l = channel.lock();
			buffer.position(Integer.BYTES);
			buffer.putInt(pShutdownSignal ? 1 : 0);
			l.release();
		} catch (IOException e) {
			System.err.println("SharedSystemData, IOException : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public boolean getShutdownSignal() {
		boolean toRet = false;
		try {
			FileLock l = channel.lock(Integer.BYTES, Integer.BYTES, false);
			buffer.position(Integer.BYTES);
			toRet = buffer.getInt() == 1 ? true : false;
			l.release();
		} catch (IOException e) {
			System.err.println("SharedSystemData, IOException : " + e.getMessage());
			e.printStackTrace();
		}
		
		return toRet;
	}
	
}
