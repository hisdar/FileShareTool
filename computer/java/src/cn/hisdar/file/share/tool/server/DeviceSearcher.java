package cn.hisdar.file.share.tool.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;

import cn.hisdar.file.share.tool.command.Command;
import cn.hisdar.lib.configuration.ConfigItem;
import cn.hisdar.lib.configuration.HConfig;
import cn.hisdar.lib.log.HLog;

public class DeviceSearcher {

	private static DeviceSearcher serverSeacher; 
	
	private final static int BroadcastMessagePort = 5298;

	private int threadCount;
	private ArrayList<String> priorIPs;
	private ArrayList<Device> onlineDevices;
	private SocketServerSearchMasterWorker masterWorker;
	private ArrayList<DeviceStateListener> deviceStateListeners;
	private ArrayList<SocketServerSearchMasterWorker> masterWorkers;
	
	private HConfig priorIPConfig;
	private RemoteDeviceListener remoteDeviceListener;
	
	private DeviceSearcher() {
		
		threadCount = 16;

		// 存储高优先级的IP,
		// 如果某个设备在线, 就会把这个设备的IP
		// 存下来, 以后再搜索的时候优先搜索
		priorIPs = new ArrayList<>();
		
		// 存储在线的设备, 在后续搜索中不会搜索此列表中的IP
		onlineDevices = new ArrayList<>();
		
		// 存储监听器
		deviceStateListeners = new ArrayList<>();
		
		// 从配置文件中导入高优先的IP
		priorIPConfig = HConfig.getInstance("config/priorIPs.xml");
		if (priorIPConfig != null) {
			ArrayList<ConfigItem> ipAddressItems = priorIPConfig.getConfigItemList();
			for (int i = 0; i < ipAddressItems.size(); i++) {
				priorIPs.add(ipAddressItems.get(i).getValue());
			}
		}
		
		initSearchThreads();
		remoteDeviceListener = new RemoteDeviceListener();
		remoteDeviceListener.start();
	}

	// 初始化搜索线程, 此方法首先获取本机的所有IP
	// 当然会过滤掉网关以及 local host
	// 然后为每一个使用的 IP
	// 所在的网段创建一个搜索线程
	private void initSearchThreads() {
		ArrayList<String> localIps = getLocalIps();
		for (int i = localIps.size() - 1; i >= 0; i--) {
			String ipString = localIps.get(i);
			String[] ipArray = ipString.split("[.]");
			if (ipArray.length != 4) {
				localIps.remove(i);
				continue;
			}
			
			if (ipArray[3].equals("1")) {
				localIps.remove(i);
				continue;
			}
		}

		masterWorkers = new ArrayList<>();
		for (int i = 0; i < localIps.size(); i++) {
			masterWorker = new SocketServerSearchMasterWorker(localIps.get(i));
			masterWorkers.add(masterWorker);
			masterWorker.start();
		}
	}

	private ArrayList<String> getLocalIps() {
		ArrayList<String> localIps = new ArrayList<>();

        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface nif = netInterfaces.nextElement();
                Enumeration<InetAddress> InetAddress = nif.getInetAddresses();
                while (InetAddress.hasMoreElements()) {
                    String ip = InetAddress.nextElement().getHostAddress();

                    localIps.add(ip);
                }
            }
        } catch (SocketException e) {
        	HLog.el(e);
        }
        
        return localIps;
	}
	
	public static DeviceSearcher getInstance() {
		if (serverSeacher != null) {
			return serverSeacher;
		}
		
		synchronized (DeviceSearcher.class) {
			if (serverSeacher == null) {
				serverSeacher = new DeviceSearcher();
			}
		}
		return serverSeacher;
	}
	
	public void addDeviceStateListener(DeviceStateListener listener) {
		for (int i = 0; i < deviceStateListeners.size(); i++) {
			if (deviceStateListeners.get(i) == listener) {
				return;
			}
		}
		
		deviceStateListeners.add(listener);
	}
	
	public void removeDeviceStateListener(DeviceStateListener listener) {
		for (int i = 0; i < deviceStateListeners.size(); i++) {
			if (deviceStateListeners.get(i) == listener) {
				deviceStateListeners.remove(i);
				return;
			}
		}
	}

	synchronized public void notifyDeviceStete(Device dev, boolean isOnline) {
		for (int i = 0; i < deviceStateListeners.size(); i++) {
			if (isOnline) {
				deviceStateListeners.get(i).deviceOnline(dev);
			} else {
				deviceStateListeners.get(i).deviceOffline(dev);
			}
		}
	}
	
	synchronized public void notifyDeviceConnectState(Device dev, boolean connected) {
		for (int i = 0; i < deviceStateListeners.size(); i++) {
			if (connected) {
				deviceStateListeners.get(i).deviceConnected(dev);
			} else {
				HLog.il("call deviceDisconnected");
				deviceStateListeners.get(i).deviceDisconnected(dev);
			}
		}
	}
	
	private boolean isDeviceOnline(String ipAddress) {
		for (int i = 0; i < onlineDevices.size(); i++) {
			if (onlineDevices.get(i).getIPAddress().equals(ipAddress)) {
				return true;
			}
		}
		
		return false;
	}
	
	private void addToPriorIPArray(String ipAddress) {
		for (int i = 0; i < priorIPs.size(); i++) {
			if (priorIPs.get(i).equals(ipAddress)) {
				return;
			}
		}
		
		priorIPs.add(ipAddress);
		priorIPConfig.addConfigItem(new ConfigItem("IP", ipAddress));
	}
	
	synchronized private void updateOnlineDevices(Device device, boolean isOnline) {
		onlineDevices.add(device);
		addToPriorIPArray(device.getIPAddress());
		notifyDeviceStete(device, true);
	}
	
	/**
	 * This function return the online devices's IP address.
	 * @return curently searched devices
	 */
	public ArrayList<Device> getOnlineDevices() {
		return onlineDevices;
	}
	
	private class SocketServerSearchMasterWorker extends Thread {
		
		private boolean isExit;
		private String ipAddress;
		private ArrayList<String> searchList;
		private ArrayList<SocketServerSearchSlaveWorker> searchSlaveWorlers;
		
		public SocketServerSearchMasterWorker(String ip) {
			isExit = false;
			ipAddress = ip;
			
			HLog.il("search network:" + ip);
			searchSlaveWorlers = new ArrayList<>();
		}
		
		private boolean isPriorIPAddress(String ipAddress) {
			for (int i = 0; i < priorIPs.size(); i++) {
				if (ipAddress.equals(priorIPs.get(i))) {
					return true;
				}
			}
			
			return false;
		}
		
		public ArrayList<String> getSearchList(String ipAddress) {
			ArrayList<String> searchList = new ArrayList<>();
			String[] ipArray = ipAddress.split("[.]");
			if (ipArray.length != 4) {
				return searchList;
			}

			int lastNumer = 0;
			try {
				lastNumer = Integer.parseInt(ipArray[3]);
			} catch (NumberFormatException e) {
				return searchList;
			}
			
			// add first search IPs
			for (int i = 0; i < priorIPs.size(); i++) {
				if (isDeviceOnline(priorIPs.get(i))) {
					continue;
				}
				if (priorIPs.get(i) == null) {
					HLog.il("priorIP is null, index:" + i);
				}
				searchList.add(priorIPs.get(i));
			}

			for (int i = 1; i < 255; i++) {
				if (i == lastNumer) {
					continue;
				}

				String searchIp = ipArray[0] + "." + ipArray[1] + "." + ipArray[2] + "." + i;
				if (isDeviceOnline(searchIp) || isPriorIPAddress(searchIp)) {
					continue;
				}

				searchList.add(searchIp);
			}
			
			return searchList;
		}
		
		public void run() {
			
			for (int i = 0; i < threadCount; i++) {
				SocketServerSearchSlaveWorker slaveWorler = new SocketServerSearchSlaveWorker(i);
				searchSlaveWorlers.add(slaveWorler);
				slaveWorler.start();
			}
			
			while (!isExit) {
				searchList = getSearchList(ipAddress);
				
				// 将地址分配个每个线程
				for (int i = 0; i < searchList.size(); i++) {
					int threadIndex = i % threadCount;
					String ipAddress = searchList.get(i);
					if (ipAddress == null) {
						HLog.il("ipAddress is null");
					}
					searchSlaveWorlers.get(threadIndex).addIPAddressToQueue(ipAddress);
				}
				
				// 等待所有线程搜索完成
				while (true) {
					int workingThread = 0;
					for (int i = 0; i < searchSlaveWorlers.size(); i++) {
						if (searchSlaveWorlers.get(i).isThreadWorking()) {
							workingThread++;
						}
					}
					
					if (workingThread == 0) {
						break;
					} else {
						try {
							sleep(1000 * 10);
						} catch (InterruptedException e) {}
					}
				}

				// 等 10
				// 秒后再开始新的一轮搜索
				try {
					sleep(1000 * 10);
				} catch (InterruptedException e) {}
				//break;
			}
		}
	}
	
	private class SocketServerSearchSlaveWorker extends Thread {

		private static final int IP_ADDRESS_QUEUE_DEFAULT_SIZE = 256;
		
		private int threadIndex;

		private boolean exit;
		private boolean isWorking;
		
		private IPAddressQueue ipAddressQueue;
		
		public SocketServerSearchSlaveWorker(int index) {
			threadIndex = index;

			exit = false;
			isWorking = false;
			ipAddressQueue = new IPAddressQueue(IP_ADDRESS_QUEUE_DEFAULT_SIZE);
		}
		
		public boolean isThreadWorking() {
			return isWorking;
		}
		
		public boolean addIPAddressToQueue(String ipAddress) {
			
			//HLog.il("push ipAddress : " + ipAddress + ", threadID:" + threadIndex);
			boolean bRet = ipAddressQueue.push(ipAddress);
			if (!bRet) {
				return false;
			}
			
			this.interrupt();
			return bRet;
		}
		
		public int getThreadIndex() {
			return threadCount;
		}
		
		public void run() {
			while (!exit) {
				if (ipAddressQueue.size() <= 0) {
					isWorking = false;
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {}
					
					continue;
				}
				
				isWorking = true;
				String ipAddress = ipAddressQueue.pop();
				Device device = new Device();
				if (!device.connect(ipAddress)) {
					continue;
				}
				
				updateOnlineDevices(device, true);
			}
			
			isWorking = false;
		}
	}

	
	private String getNetworkDomain(String ipAddress) {
		String[] ipItems = ipAddress.split("[.]");
		if (ipItems.length != 4) {
			return null;
		}
		
		int[] ipItemsInt = new int[4];
		try {
			for (int i = 0; i < ipItems.length; i++) {
				ipItemsInt[i] = Integer.parseInt(ipItems[i]);
			}
		} catch (NumberFormatException e) {
			return null;
		}
		
		if (ipItemsInt[0] >= 1 && ipItemsInt[0] <= 126) {
			return ipItems[0];
		} else if (ipItemsInt[0] >= 127 && ipItemsInt[0] <= 191) {
			return ipItems[0] + "." + ipItems[1];
		} else if (ipItemsInt[0] >= 192 && ipItemsInt[0] <= 223) {
			return ipItems[0] + "." + ipItems[1] + "." + ipItems[2];
		}
		
		return null;
	}
	
	private class RemoteDeviceListener extends Thread {
		private boolean exit;
		
		public RemoteDeviceListener() {
			
		}
		
		public void stopThread() {
			exit = true;
		}
		
		private void responseLetMeHearYou(Command cmd) {
			String ipAddress = cmd.getCommandItem("IPAddress");
			String netWorkDomain = getNetworkDomain(ipAddress);
			ArrayList<String> localIps = getLocalIps();
			
			String localIp = null;
			for (int i = 0; i < localIps.size(); i++) {
				if (localIps.get(i).startsWith(netWorkDomain)) {
					localIp = localIps.get(i);
				}
			}
			
			// not ip address found
			if (localIp == null) {
				return;
			}
			
			String cmdStr = Command.getFormatedCommandType(Command.COMMAND_TYPE_RESOPNSE);
			cmdStr += Command.getFormatedCommand(cmd.getCommand());
			cmdStr += "<IPAddress>" + localIp + "</IPAddress>\n";
			cmdStr = Command.addCommandHeadAndTail(cmdStr);
			
			HLog.il(cmdStr);
			
			byte[] msgBytes = cmdStr.getBytes();
			try {
				InetAddress inetAddress = InetAddress.getByName(ipAddress);
	            DatagramPacket packet = new DatagramPacket(msgBytes, msgBytes.length, inetAddress, 5298);
	            DatagramSocket socket = new DatagramSocket();
	            socket.send(packet);
	            socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void parseBroadcastMessage(byte[] msgArray) {
			String msgStr = new String(msgArray, StandardCharsets.UTF_8);

			Command command = new Command();
			command.parseCommand(new StringBuffer(msgStr));
			msgStr = command.getCommandItem(Command.COMMAND_SHELL);
			if (msgStr == null) {
				// not my message ignore
				return;
			}
			
			command.clear();
			command.parseCommand(new StringBuffer(msgStr));
			String cmdType = command.getCommandItem(Command.COMMAND_TYPE_KEY);
			if (cmdType.equals(Command.COMMAND_TYPE_REQUEST)) {
				if (command.getCommand().equals(Command.COMMAND_LET_ME_HEAR_YOU)) {
					responseLetMeHearYou(command);
				}
			} else if (cmdType.equals(Command.COMMAND_TYPE_RESOPNSE)) {
				
			}
		}
		
		public void run() {
			try {
				exit = false;
				byte[] arr = new byte[1024 * 8];
				DatagramSocket serverSocket = new DatagramSocket(BroadcastMessagePort);
		        DatagramPacket packet = new DatagramPacket(arr, arr.length);
		        
		        while (!exit) {
			        serverSocket.receive(packet);
			        parseBroadcastMessage(packet.getData());
		        }
		        serverSocket.close();
			} catch (SocketException e) {
				HLog.el(e);
			} catch (IOException e) {
				HLog.el(e);
			}
		}
	}
}
