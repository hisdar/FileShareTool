package cn.hisdar.file.share.tool.view.device;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cn.hisdar.file.share.tool.Global;
import cn.hisdar.file.share.tool.server.Device;
import cn.hisdar.file.share.tool.server.DeviceInformation;
import cn.hisdar.file.share.tool.server.DeviceStateAdapter;
import cn.hisdar.file.share.tool.server.DeviceSearcher;
import cn.hisdar.lib.log.HLog;
import cn.hisdar.lib.ui.HLinearPanel;
import cn.hisdar.lib.ui.TitlePanel;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;

public class DeviceView extends JPanel implements ItemListener {

	private static final long serialVersionUID = 1L;

	private TitlePanel titlePanel;
	private JPanel deviceInforPanel;
	private HLinearPanel quickAccessPanel;
	private JComboBox<String> deviceList;
	private HashMap<Integer, Device> deviceMap;
	private DeviceStateEventHandler deviceStateEventHandler;
	
	public DeviceView() {
		deviceMap = new HashMap<>();
		
		titlePanel = new TitlePanel("设备/文件");
		deviceInforPanel = new JPanel();
		deviceInforPanel.setLayout(new BorderLayout());
		
		deviceList = new JComboBox<>();
		deviceList.setFont(Global.getDefaultFont());
		deviceList.addItemListener(this);
		deviceInforPanel.add(deviceList, BorderLayout.NORTH);
		
		setLayout(new BorderLayout());
		add(titlePanel, BorderLayout.NORTH);
		//add(deviceInforPanel, BorderLayout.NORTH);
				
		quickAccessPanel = new HLinearPanel();
		quickAccessPanel.add(deviceInforPanel);
		add(quickAccessPanel, BorderLayout.CENTER);
		
		DeviceSearcher serverSearcher = DeviceSearcher.getInstance();
		deviceStateEventHandler = new DeviceStateEventHandler();
		serverSearcher.addDeviceStateListener(deviceStateEventHandler);
	}
	
	private void updateDevicesList(Device dev, boolean online) {
		if (online) {
			HLog.il("device online\n");
			deviceMap.put(deviceList.getItemCount(), dev);
			deviceList.addItem(dev.getDeviceInformation().getDeviceName());
			
		} else {
			HLog.il("device offline\n");
			Iterator<Entry<Integer, Device>> iterator = deviceMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Integer, Device> entry = (Map.Entry<Integer, Device>)iterator.next();
				if (entry.getValue() == dev) {
					Integer index = (Integer)entry.getKey();
					deviceList.remove(index);
					deviceMap.remove(index);
					break;
				}
			}
		}
	}
	
	private class DeviceStateEventHandler extends DeviceStateAdapter {
		@Override
		public void deviceOnline(Device dev) {
			updateDevicesList(dev, true);
		}

		@Override
		public void deviceOffline(Device dev) {
			HLog.il("call updateDevicesList");
			updateDevicesList(dev, false);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == deviceList) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				Integer index = deviceList.getSelectedIndex();
				HLog.il("select device:" + e.getItem().toString() + ", index:" + index);
				Device device = deviceMap.get(index);
				if (device == null) {
					HLog.il("device is null");
					return;
				}
				device.connect();
				DeviceInformation deviceInformation = device.getDeviceInformation();
				String innerSdcardPath = deviceInformation.getInnerSDCardPath();
				JButton innerSdcardButton = new JButton("内置SD卡");
				innerSdcardButton.setFont(Global.getDefaultFont());
				quickAccessPanel.add(innerSdcardButton);
				for (int i = 0; i < deviceInformation.getExternalSdcardCount(); i++) {
					JButton externalSdcardButton = new JButton("外置SD卡-" + i);
					externalSdcardButton.setFont(Global.getDefaultFont());
					quickAccessPanel.add(externalSdcardButton);
				}
			}
		}
	}
}
