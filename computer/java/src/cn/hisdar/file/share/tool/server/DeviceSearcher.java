package cn.hisdar.file.share.tool.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import cn.hisdar.lib.configuration.ConfigItem;
import cn.hisdar.lib.configuration.HConfig;
import cn.hisdar.lib.log.HLog;

public class DeviceSearcher {

	private static DeviceSearcher serverSeacher; 

	private int threadCount;
	private ArrayList<String> priorIPs;
	private ArrayList<Device> onlineDevices;
	private SocketServerSearchMasterWorker masterWorker;
	private ArrayList<DeviceStateListener> deviceStateListeners;
	private ArrayList<SocketServerSearchMasterWorker> masterWorkers;
	
	private HConfig priorIPConfig;
	
	private DeviceSearcher() {
		
		priorIPs = new ArrayList<>();
		onlineDevices = new ArrayList<>();
		deviceStateListeners = new ArrayList<>();
		
		priorIPConfig = HConfig.getInstance("config/priorIPs.xml");
		if (priorIPConfig != null) {
			ArrayList<ConfigItem> ipAddressItems = priorIPConfig.getConfigItemList();
			for (int i = 0; i < ipAddressItems.size(); i++) {
				priorIPs.add(ipAddressItems.get(i).getValue());
			}
		}
		

		threadCount = 16;
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
		notifyDeviceStete(device, true);
	}
	
	/**
	 * This function return the online devices's IP address.
	 * @return curently searched devices
	 */
	public ArrayList<Device> getOnlineDevices() {
		return onlineDevices;
	}
	
	private class SocketServerSearchMasterWorker extends Thread implements IPAddressProvider {
		
		private boolean isExit;
		private String ipAddress;
		private ArrayList<String> searchList;
		private ArrayList<SocketServerSearchSlaveWorler> searchSlaveWorlers;
		
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
		
		synchronized public String requestIPAddressSync() {
			if (searchList.size() == 0) {
				return null;
			}
			
			String ipAddress = searchList.get(0);
			searchList.remove(0);
			return ipAddress;
		}
		
		public void stopWorker() {
			isExit = true;
		}

		public void run() {
			while (!isExit) {
				searchList = getSearchList(ipAddress);

				for (int i = 0; i < threadCount; i++) {
					SocketServerSearchSlaveWorler slaveWorler = new SocketServerSearchSlaveWorler(this, i);
					searchSlaveWorlers.add(slaveWorler);
					slaveWorler.start();
				}
				
				try {
					sleep(1000 * 10);
				} catch (InterruptedException e) {}
			}
		}

		@Override
		public String requestIPAddress() {
			return requestIPAddressSync();
		}
		
	}
	
	private class SocketServerSearchSlaveWorler extends Thread {

		private int threadIndex;
		private IPAddressProvider ipAddressProvider;
		public SocketServerSearchSlaveWorler(IPAddressProvider provider, int index) {
			threadIndex = index;
			ipAddressProvider = provider;
		}
		
		public void run() {
			while (true) {
				String ipAddress = ipAddressProvider.requestIPAddress();
				if (ipAddress == null) {
					break;
				}
				
				Device device = new Device();
				if (!device.connect(ipAddress)) {
					continue;
				}
				
				
				updateOnlineDevices(device, true);
				addToPriorIPArray(ipAddress);
			}
		}
	}
	
	
	public ArrayList<String> getLocalIps() {
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
        	e.printStackTrace();
        }
        
        return localIps;
	}
}
