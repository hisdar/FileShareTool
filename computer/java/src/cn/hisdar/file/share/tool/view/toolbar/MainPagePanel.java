package cn.hisdar.file.share.tool.view.toolbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cn.hisdar.file.share.tool.Global;
import cn.hisdar.file.share.tool.server.Device;
import cn.hisdar.file.share.tool.server.DeviceStateAdapter;
import cn.hisdar.file.share.tool.server.DeviceSearcher;
import cn.hisdar.file.share.tool.view.explorer.DividerLabel;
import cn.hisdar.lib.log.HLog;
import cn.hisdar.lib.ui.HLinearPanel;

public class MainPagePanel extends JPanel {
	
	private JLabel titleLabel;
	private JPanel devicePanel;
	private HLinearPanel mainPanel;
	private JCheckBox autoConnectBox;
	private JLabel stateLabel;
	private JComboBox<String> deviceList;
	private JButton connectButon;
	private DeviceStateEventHandler deviceStateEventHandler;
	
	public MainPagePanel() {
		initDevicePanel();
		mainPanel = new HLinearPanel(HLinearPanel.HORIZONTAL);
		mainPanel.add(devicePanel);
		
		DividerLabel dividerLabel = new DividerLabel(false);
		dividerLabel.setPreferredSize(new Dimension(2, 0));
		dividerLabel.setDividerColor(new Color(0xAAAAAA));
		mainPanel.add(dividerLabel);
		
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		
		deviceStateEventHandler = new DeviceStateEventHandler();
		DeviceSearcher.getInstance().addDeviceStateListener(deviceStateEventHandler);
	}
	
	private void initDevicePanel() {
		devicePanel = new JPanel();
		devicePanel.setPreferredSize(new Dimension(200, 0));
		devicePanel.setLayout(new BorderLayout());

		titleLabel = new JLabel("设备", JLabel.CENTER);
		titleLabel.setFont(Global.getDefaultFont());
		devicePanel.add(titleLabel, BorderLayout.SOUTH);
	
		JPanel allDevicePanel = new JPanel();
		allDevicePanel.setLayout(new GridLayout(3, 1));
		
		stateLabel = new JLabel();
		stateLabel.setFont(Global.getDefaultFont());
		allDevicePanel.add(stateLabel);
		
		deviceList = new JComboBox<String>();
		allDevicePanel.add(deviceList);
		
		autoConnectBox = new JCheckBox("设为默认设备");
		autoConnectBox.setFont(Global.getDefaultFont());
		allDevicePanel.add(autoConnectBox);

		connectButon = new JButton("连接");
		connectButon.setFont(Global.getDefaultFont());
		
		JPanel connectAndDeviceInfoPanel = new JPanel();
		connectAndDeviceInfoPanel.setLayout(new BorderLayout());
		connectAndDeviceInfoPanel.add(connectButon, BorderLayout.EAST);
		connectAndDeviceInfoPanel.add(allDevicePanel, BorderLayout.CENTER);
		
		devicePanel.add(connectAndDeviceInfoPanel, BorderLayout.CENTER);
		devicePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}
	
	private class DeviceStateEventHandler extends DeviceStateAdapter {

		@Override
		public void deviceOnline(Device dev) {
			HLog.il("device online ... ");
			
			JPanel deviceInfoPanel = new JPanel();
			deviceInfoPanel.setLayout(new BorderLayout());
			
			String itemValue = dev.getName() + "(" + dev.getIPAddress() + ")";
			deviceList.addItem(itemValue);
			
			HLog.il("add finished");
		}

		@Override
		public void deviceOffline(Device dev) {

		}
	}
}
