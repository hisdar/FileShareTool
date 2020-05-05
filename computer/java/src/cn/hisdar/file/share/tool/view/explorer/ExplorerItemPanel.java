package cn.hisdar.file.share.tool.view.explorer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import cn.hisdar.file.share.tool.Global;
import cn.hisdar.file.share.tool.command.RemoteFile;

public class ExplorerItemPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Color SELECTED_COLOR = new Color(0xCCCCFF);
	private static final Color MOUSE_ON_COLOR = new Color(0xDDDDFF);
	
	private int dividerSize = 4;
	private int[] titleLableSizeArray;

	private String filePath;
	private RemoteFile file;
	private boolean isSelected;
	private boolean isMouseIn;
	private Color defaultBgColor;
	
	private ArrayList<JLabel> titleLabels;
	private MouseEventHandler mouseEventHandler;
	private ArrayList<ExplorerItemListener> explorerItemListeners;
	
	public ExplorerItemPanel(int itemCount) {
		init(itemCount);
	}
	
	private void initPopupMenu() {
		explorerItemListeners = new ArrayList<>();
		setBorder(null);
	}
	
	public ExplorerItemPanel(String[] titles) {
		init(titles.length);
		for (int i = 0; i < titles.length; i++) {
			titleLabels.get(i).setText(titles[i]);
		}
	}
	
	public void setFilePath(String path) {
		filePath = path;
	}
	
	public void setRemoteFile(RemoteFile remoteFile) {
		file = remoteFile;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public RemoteFile getRemoteFile() {
		return file;
	}
	
	private void init(int itemCount) {
		isSelected = false;
		isMouseIn = false;
		initPopupMenu();
		filePath = null;
		setLayout(null);
		titleLableSizeArray = null;
		titleLabels = new ArrayList<>();
		mouseEventHandler = new MouseEventHandler(this);
		
		for (int i = 0; i < itemCount; i++) {
			JLabel label = new JLabel();
			label.setFont(Global.getDefaultFont());
			label.addMouseListener(mouseEventHandler);
			label.addMouseMotionListener(mouseEventHandler);
			titleLabels.add(label);
			add(label);
		}
	}
	
	public void setItemText(String text, int index) {
		if (index < 0 || index >= titleLabels.size()) {
			return;
		}
		
		titleLabels.get(index).setText(text);
	}
	
	public void setIcon(Icon icon) {
		titleLabels.get(0).setIcon(icon);
	}
	
	public void setColumnWidth(int[] columnWidthArray) {
		titleLableSizeArray = columnWidthArray;
		repaint();
	}
	
	public void addExplorerItemListener(ExplorerItemListener listener) {
		for (int i = 0; i < explorerItemListeners.size(); i++) {
			if (explorerItemListeners.get(i) == listener) {
				return;
			}
		}
		
		explorerItemListeners.add(listener);
	}
	
	public void removeExplorerItemListener(ExplorerItemListener listener) {
		for (int i = 0; i < explorerItemListeners.size(); i++) {
			if (explorerItemListeners.get(i) == listener) {
				explorerItemListeners.remove(i);
				return;
			}
		}
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (titleLableSizeArray == null) {
			int titleSizeTotal = getWidth();
			int componentWidth = titleSizeTotal / titleLabels.size();

			titleLableSizeArray = new int[titleLabels.size()];
			for (int i = 0; i < titleLableSizeArray.length; i++) {
				titleLableSizeArray[i] = componentWidth;
			}
		}
		
		int startIndexX = 0;
		for (int i = 0; i < titleLabels.size(); i++) {
			JLabel label = titleLabels.get(i);
			int componentWidth = titleLableSizeArray[i];
			label.setBounds(startIndexX, 0, componentWidth, getHeight());
			label.setPreferredSize(new Dimension(componentWidth, getHeight()));
			startIndexX = startIndexX + componentWidth + dividerSize;
		}
		
		validate();
	}
	
	public void setSelected(boolean selected) {
		isSelected = selected;
		if (selected) {
			if (!isMouseIn) {
				defaultBgColor = getBackground();
			}
			setBackground(SELECTED_COLOR);
		} else {
			if (isMouseIn) {
				setBackground(MOUSE_ON_COLOR);
			} else {
				setBackground(defaultBgColor);
			}
		}
	}
	
	public boolean isSelected() {
		return isSelected;
	}
	
	private class MouseEventHandler extends MouseAdapter {
		
		private ExplorerItemPanel explorerItemPanel;
		
		public MouseEventHandler(ExplorerItemPanel explorerItem) {
			defaultBgColor = null;
			explorerItemPanel = explorerItem;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			for (int i = 0; i < explorerItemListeners.size(); i++) {
				explorerItemListeners.get(i).mouseClicked(explorerItemPanel, e);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			isMouseIn = true;
			if (!isSelected) {
				defaultBgColor = getBackground();
				setBackground(MOUSE_ON_COLOR);
			}
			
			for (int i = 0; i < explorerItemListeners.size(); i++) {
				explorerItemListeners.get(i).mouseEntered(explorerItemPanel, e);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			isMouseIn = false;
			if (!isSelected) {
				setBackground(defaultBgColor);
			}
			
			for (int i = 0; i < explorerItemListeners.size(); i++) {
				explorerItemListeners.get(i).mouseExited(explorerItemPanel, e);
			}
		}
	}
}
