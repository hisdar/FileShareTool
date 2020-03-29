package cn.hisdar.file.share.tool.view.device;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

import cn.hisdar.file.share.tool.server.DeviceStateListener;
import cn.hisdar.file.share.tool.server.SocketServerSearcher;
import cn.hisdar.lib.ui.TitlePanel;

public class DeviceView extends JPanel implements DeviceStateListener {

	private TitlePanel titlePanel;
	private ArrayList<String> onlineDevices;
	
	public DeviceView() {
		titlePanel = new TitlePanel("在线设备");
		
		setLayout(new BorderLayout());
		add(titlePanel, BorderLayout.NORTH);
		SocketServerSearcher serverSearcher = SocketServerSearcher.getInstance();
		serverSearcher.addDeviceStateListener(this);
	}

	@Override
	public void deviceOnline(String ipAddress) {
		for (int i = 0; i < onlineDevices.size(); i++) {
			if (onlineDevices.get(i).equals(ipAddress)) {
				return;
			}
		}
		
		onlineDevices.add(ipAddress);
	}

	@Override
	public void deviceOffline(String ipAddress) {
		for (int i = 0; i < onlineDevices.size(); i++) {
			if (onlineDevices.get(i).equals(ipAddress)) {
				onlineDevices.remove(i);
				return;
			}
		}
	}
	
}
