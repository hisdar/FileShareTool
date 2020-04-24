package cn.hisdar.file.share.tool.view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import cn.hisdar.file.share.tool.view.device.DeviceView;
import cn.hisdar.file.share.tool.view.explorer.AddressBar;
import cn.hisdar.file.share.tool.view.explorer.ExplorerView;
import cn.hisdar.file.share.tool.view.toolbar.MainPagePanel;
import cn.hisdar.file.share.tool.view.toolbar.ToolBar;
import cn.hisdar.lib.ui.HSplitPane;
import cn.hisdar.lib.ui.UIAdapter;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private ToolBar toolBar;
	private AddressBar addressBar;
	private ExplorerView explorerView;
	
	public MainFrame() {
		setTitle("啄木鸟文件共享工具");
		setSize(1400, 700);
		setLocation(UIAdapter.getCenterLocation(null, this));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		initToolBar();
		addressBar = new AddressBar();
		explorerView = new ExplorerView();
		explorerView.getExplorerAddressListenerManager().addListener(addressBar);
		addressBar.addAddressBarListener(explorerView.getAddressBarListener());

		HSplitPane deviceSplitPane = new HSplitPane(HSplitPane.VERTICAL_SPLIT);
		deviceSplitPane.setDividerLocation(0.15f);
		deviceSplitPane.setLeftComponent(new DeviceView());
		deviceSplitPane.setRightComponent(explorerView);
		
		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		
		JPanel addressBarAndExplorerView = new JPanel();
		addressBarAndExplorerView.setLayout(new BorderLayout());
		addressBarAndExplorerView.add(addressBar, BorderLayout.NORTH);
		addressBarAndExplorerView.add(deviceSplitPane, BorderLayout.CENTER);
		
		add(addressBarAndExplorerView, BorderLayout.CENTER);
	}
	
	public void initToolBar() {
		toolBar = new ToolBar();
		toolBar.addItem("主页", new MainPagePanel());
		toolBar.addItem("帮助", new JPanel());
	}
}
