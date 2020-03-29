package cn.hisdar.file.share.tool.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ToolBar extends JPanel {

	public ToolBar() {
		setPreferredSize(new Dimension(0, 45));
		setBackground(new Color(0xEEEEFF));
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		setLayout(flowLayout);
	}
	
	public JButton addButton() {
		JButton button = new JButton();
		button.setPreferredSize(new Dimension(40, 40));

		add(button);
		return button;
	}
}
