package cn.hisdar.file.share.tool.view;

import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.border.Border;
import javax.tools.Tool;

import cn.hisdar.file.share.tool.view.device.DeviceView;
import cn.hisdar.lib.ui.HSplitPane;
import cn.hisdar.lib.ui.UIAdapter;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DeviceView deviceView;
	private OutputView outputView;
	private ToolBar toolBar;
	private StartServerActionListener startServerActionListener;
	
	public MainFrame() {
		setTitle("啄木鸟文件共享工具");
		setSize(1400, 700);
		setLocation(UIAdapter.getCenterLocation(null, this));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		initToolBar();
		outputView = new OutputView();
		deviceView = new DeviceView();

		HSplitPane deviceSplitPane = new HSplitPane(HSplitPane.VERTICAL_SPLIT);
		deviceSplitPane.setLeftComponent(deviceView);
		
		HSplitPane outputSplitPane = new HSplitPane(HSplitPane.HORIZONTAL_SPLIT);
		outputSplitPane.setBottomComponent(outputView);
		outputSplitPane.setTopComponent(deviceSplitPane);
		outputSplitPane.setDividerLocation(0.7f);
		
		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		add(outputSplitPane, BorderLayout.CENTER);
	}
	
	public void initToolBar() {
		String[][] toolBarParam = 
		{
				{"image/start.png", "image/start-mouse-on.png", "image/start-clicked.png"},
				{},
				{}
		};
		
		toolBar = new ToolBar();
		for (int i = 0; i < toolBarParam.length; i++) {
			if (toolBarParam[i].length <= 0) {
				continue;
			}
			
			JButton button = toolBar.addButton();
			button.setFocusPainted(false);
			button.setBorder(BorderFactory.createEmptyBorder());
			
			ImageIcon defaultIcon = new ImageIcon("image/start.png");
			ImageIcon clickedIcon = new ImageIcon("image/start-clicked.png");
			ImageIcon mouseOnIcon = new ImageIcon("image/start-mouse-on.png");
			
			button.setIcon(defaultIcon);
			button.setPressedIcon(clickedIcon);
			button.setRolloverIcon(mouseOnIcon);
			
			startServerActionListener = new StartServerActionListener();
			button.addActionListener(startServerActionListener);
		}
	}
}
