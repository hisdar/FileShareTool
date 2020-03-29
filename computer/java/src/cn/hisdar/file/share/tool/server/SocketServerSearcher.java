package cn.hisdar.file.share.tool.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import cn.hisdar.file.share.tool.view.device.DeviceDetailsPanel;
import cn.hisdar.lib.log.HLog;
import sun.rmi.runtime.Log;

public class SocketServerSearcher {

	private static SocketServerSearcher serverSeacher; 
	private SocketServerSearchMasterWorker masterWorker;
	private int threadCount;
	private ArrayList<SocketServerSearchMasterWorker> searchMasterWorkers;
	private ArrayList<DeviceStateListener> deviceStateListeners;
	private ArrayList<String> onlineDevices;
	
	private SocketServerSearcher() {
		
		threadCount = 8;
		
		ArrayList<String> localIps = getLocalIps();
		for (int i = localIps.size() - 1; i >= 0; i--) {
			String ipString = localIps.get(i);
			String[] ipArray = ipString.split("[.]");
			if (ipArray.length != 4) {
				HLog.il("remove:" + ipString);
				localIps.remove(i);
				continue;
			}
			
			if (ipArray[3].equals("1")) {
				HLog.il("remove:" + ipString);
				localIps.remove(i);
				continue;
			}
		}

		searchMasterWorkers = new ArrayList<>();
		for (int i = 0; i < localIps.size(); i++) {
			masterWorker = new SocketServerSearchMasterWorker(localIps.get(i));
			searchMasterWorkers.add(masterWorker);
			masterWorker.start();
		}
		
		deviceStateListeners = new ArrayList<>();
		onlineDevices = new ArrayList<>();
	}
	
	public static SocketServerSearcher getInstance() {
		if (serverSeacher != null) {
			return serverSeacher;
		}
		
		synchronized (SocketServerSearcher.class) {
			if (serverSeacher == null) {
				serverSeacher = new SocketServerSearcher();
			}
		}
		return serverSeacher;
	}

	public DeviceDetails getDeviceDetails(String ipAddress) {
		
		DeviceDetails details = new DeviceDetails();
		try {
			Socket socket = new Socket(ipAddress, 5299);
			OutputStream outputStream = socket.getOutputStream();
			//outputStream.write(b);
			//outputStream.flush();
			
			
			
			socket.close();
		} catch (UnknownHostException e) {
			HLog.il("Host " + ipAddress + " not found.");
			return null;
		} catch (IOException e) {
			
			return null;
		}
		
		
		
		
		return details;
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

	synchronized private void notifyDeviceStete(String ipAddress, boolean isOnline) {
		for (int i = 0; i < deviceStateListeners.size(); i++) {
			if (isOnline) {
				deviceStateListeners.get(i).deviceOnline(ipAddress);
			} else {
				deviceStateListeners.get(i).deviceOffline(ipAddress);
			}
		}
	}
	
	synchronized private void updateOnlineDevices(String ipAddress, boolean isOnline) {
		
		int index = -1;
		for (int i = 0; i < onlineDevices.size(); i++) {
			if (onlineDevices.get(i).equals(ipAddress)) {
				index = i;
				break;
			}
		}
		
		if (isOnline) {
			if (index == -1) {
				onlineDevices.add(ipAddress);
			}
		} else {
			if (index >= 0 && index <= onlineDevices.size()) {
				onlineDevices.remove(index);
			}
		}
	}
	
	/**
	 * This function return the online devices's IP address.
	 * @return curently searched devices
	 */
	public ArrayList<String> getOnlineDevices() {
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

			for (int i = 1; i < 255; i++) {
				if (i == lastNumer) {
					continue;
				}

				String searchIp = ipArray[0] + "." + ipArray[1] + "." + ipArray[2] + "." + i;
				searchList.add(searchIp);
			}
			
			return searchList;
		}
		
		synchronized public String requestIPAddressSync() {
			if (searchList.size() == 0) {
				return null;
			}
			
			String ipAddress = searchList.get(searchList.size() - 1);
			searchList.remove(searchList.size() - 1);
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
					sleep(1000 * 60);
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
				
				Socket slave = new Socket();
                SocketAddress address = new InetSocketAddress(ipAddress, 5299);
                try {
					slave.connect(address, 500);
					slave.close();
					updateOnlineDevices(ipAddress, true);
					notifyDeviceStete(ipAddress, true);
                } catch (IOException e) {
                	updateOnlineDevices(ipAddress, false);
                	notifyDeviceStete(ipAddress, false);
					continue;
				}
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
