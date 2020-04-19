package cn.hisdar.file.share.tool.view.toolbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import cn.hisdar.file.share.tool.Global;
import cn.hisdar.lib.log.HLog;
import cn.hisdar.lib.ui.HLinearPanel;

public class ToolBar extends JPanel {

	private static final long serialVersionUID = 1L;

	private Color defaultColor = new Color(0xFFFFFF);
	private Color selectedColor = new Color(0xEEEEEE);
	
	private Border labelDefaultBorder = BorderFactory.createEmptyBorder(5, 10, 5, 10);

	private HashMap<JLabel, JPanel> menusMap;
	private JLabel currentTitleLabel;
	private JPanel currentPanel;

	private HLinearPanel titlePanel;
	private MouseEventHandler mouseEventHandler;
	
	private JLabel fileLabel;
	
	public ToolBar() {
		currentPanel = null;
		currentTitleLabel = null;
		menusMap = new HashMap<>();
		setLayout(new BorderLayout());
		
		titlePanel = new HLinearPanel(HLinearPanel.HORIZONTAL);
		
		setPreferredSize(new Dimension(0, 150));
		titlePanel.setBackground(defaultColor);
		titlePanel.setOpaque(true);
		
		add(titlePanel, BorderLayout.NORTH);
		
		fileLabel = new JLabel("нд╪Ч", JLabel.CENTER);
		fileLabel.setFont(Global.getDefaultFont());
		fileLabel.setOpaque(true);
		fileLabel.setBackground(new Color(0x0066B4));
		fileLabel.setPreferredSize(new Dimension(80, 0));
		fileLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		titlePanel.add(fileLabel);
		
		mouseEventHandler = new MouseEventHandler();
	}
	
	public void addItem(String name, JPanel itemPanel) {
		JLabel titleLabel = new JLabel(name);
		titleLabel.setFont(Global.getDefaultFont());
		titleLabel.setOpaque(true);
		titleLabel.setBackground(defaultColor);;
		titleLabel.addMouseListener(mouseEventHandler);

		titleLabel.setBorder(labelDefaultBorder);
		titlePanel.add(titleLabel);
		menusMap.put(titleLabel, itemPanel);
				
		if (menusMap.size() == 1) {
			itemPanel.setOpaque(true);
			itemPanel.setBackground(selectedColor);
			add(itemPanel, BorderLayout.CENTER);

			titleLabel.setBackground(selectedColor);
			
			currentPanel = itemPanel;
			currentTitleLabel = titleLabel;
		}
		repaint();
	}
	
	private class MouseEventHandler extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			JLabel newLabel = (JLabel)e.getSource();
			if (newLabel == currentTitleLabel) {
				return;
			}
			
			JPanel panel = menusMap.get(newLabel);
			if (panel == null) {
				return;
			}

			newLabel.setBackground(selectedColor);
			newLabel.setOpaque(true);
			
			remove(currentPanel);
			add(panel, BorderLayout.CENTER);

			currentPanel = panel;
			currentTitleLabel.setBackground(defaultColor);
			currentTitleLabel = newLabel;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			JLabel newLabel = (JLabel)e.getSource();
			if (newLabel == currentTitleLabel) {
				return;
			}
			
			newLabel.setBackground(new Color(0x00CCFF));
		}

		@Override
		public void mouseExited(MouseEvent e) {
			JLabel newLabel = (JLabel)e.getSource();
			if (newLabel == currentTitleLabel) {
				return;
			}
			
			newLabel.setBackground(defaultColor);
		}
		
	}
}
