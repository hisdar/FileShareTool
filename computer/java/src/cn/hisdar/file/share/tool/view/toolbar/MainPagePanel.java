package cn.hisdar.file.share.tool.view.toolbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import cn.hisdar.file.share.tool.view.explorer.DividerLabel;
import cn.hisdar.lib.ui.HLinearPanel;

public class MainPagePanel extends JPanel {
	
	private HLinearPanel mainPanel;
	
	public MainPagePanel() {
		mainPanel = new HLinearPanel(HLinearPanel.HORIZONTAL);
		
		DividerLabel dividerLabel = new DividerLabel(false);
		dividerLabel.setPreferredSize(new Dimension(2, 0));
		dividerLabel.setDividerColor(new Color(0xAAAAAA));
		mainPanel.add(dividerLabel);
		
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		
	}
	
}
