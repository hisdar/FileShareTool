package cn.hisdar.file.share.tool.server;

public class IPAddressQueue {

	private int queueHead;
	private int queueTail;
	private int queueLength;
	private String[] ipAddressQueue;

	public IPAddressQueue(int length) {
		queueHead = 0;
		queueTail = 0;
		queueLength = length + 1; // need a empty item
		ipAddressQueue = new String[queueLength];
	}
	
	public int size() {
		int size = queueTail - queueHead;
		if (size < 0) {
			size += queueLength;
		}
		
		return size;
	}
	
	public String pop() {
		String result = null;
		synchronized(ipAddressQueue) {
			if (size() <= 0) {
				return null;
			}
			
			result = ipAddressQueue[queueHead];
			queueHead++;
			if (queueHead == queueLength) {
				queueHead = 0;
			}
		}
		return result;
	}
	
	public boolean push(String ipAddress) {
		synchronized(ipAddressQueue) {
			int queueSize = queueTail - queueTail;
			if (queueSize < 0) {
				queueSize += queueLength;
			}
			
			// 队列已经满了
			if (queueSize == queueLength - 1) {
				return false;
			}
			
			ipAddressQueue[queueTail] = ipAddress;
			queueTail += 1;
			if (queueTail == queueLength) {
				queueTail = 0;
			}
			
			return true;
		}
	}
	
	public void forcePush(String ipAddress) {
		synchronized(ipAddressQueue) {
			int queueSize = queueTail - queueTail;
			if (queueSize < 0) {
				queueSize += queueLength;
			}
			
			// 队列已经满了
			if (queueSize == queueLength - 1) {
				queueHead += 1;
				queueHead = queueHead % queueLength;
			}
			
			ipAddressQueue[queueTail] = ipAddress;
			queueTail += 1;
			if (queueTail == queueLength) {
				queueTail = 0;
			}
		}
	}
}
