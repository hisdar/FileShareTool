package cn.hisdar.file.share.tool.view.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import cn.hisdar.file.share.tool.command.RemoteFile;

public class ExplorerItemView extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public static final int REMOTE_FILE_EVENT_OPEN    = 0X4001;
	public static final int REMOTE_FILE_EVENT_POST    = 0X4002;
	public static final int REMOTE_FILE_EVENT_SAVE_AS = 0X4003;
	public static final int REMOTE_FILE_EVENT_DELETE  = 0X4004;
	public static final int REMOTE_FILE_EVENT_RENAME  = 0X4005;
	public static final int REMOTE_FILE_EVENT_MOVE    = 0X4006;
	public static final int REMOTE_FILE_EVENT_CREATE_FILE = 0X4007;
	public static final int REMOTE_FILE_EVENT_CREATE_DIRECTORY = 0X4008;
	
	private static final int EXPLORER_ITEM_HEIGHT = 30;
	
	private JPanel mainPanel;
	private JScrollPane mainScrollPanel;
	private PopMenuItemEventHandler popMenuItemEventHandler;
	private ArrayList<ExplorerItemPanel> explorerItemPanels;
	private ExplorerItemEventHandler explorerItemEventHandler;
	private ArrayList<RemoteFileEventListener> remoteFileEventListeners;
	
	public ExplorerItemView() {
		explorerItemPanels = new ArrayList<>();
		remoteFileEventListeners = new ArrayList<>();
		popMenuItemEventHandler = new PopMenuItemEventHandler();
		explorerItemEventHandler = new ExplorerItemEventHandler();
		
		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		
		mainScrollPanel = new JScrollPane(mainPanel);
		JScrollBar verticalBar = mainScrollPanel.getVerticalScrollBar();
		verticalBar.setUnitIncrement(10);
		
		setLayout(new BorderLayout());
		add(mainScrollPanel, BorderLayout.CENTER);
	}
	
	public void addExplorerItem(ExplorerItemPanel explorerItemPanel) {
		mainPanel.add(explorerItemPanel);
		explorerItemPanels.add(explorerItemPanel);
		explorerItemPanel.addExplorerItemListener(explorerItemEventHandler);
	}
	
	public void removeAllExplorerItem() {
		for (; explorerItemPanels.size() > 0; ) {
			int index = explorerItemPanels.size() - 1;
			ExplorerItemPanel explorerItemPanel = explorerItemPanels.get(index);
			explorerItemPanels.remove(index);
			mainPanel.remove(explorerItemPanel);
			explorerItemPanel.removeExplorerItemListener(explorerItemEventHandler);
		}
		
		repaint();
	}
	
	public void setColumnWidth(int[] columnWidthArray) {
		for (int i = 0; i < explorerItemPanels.size(); i++) {
			explorerItemPanels.get(i).setColumnWidth(columnWidthArray);
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		int totalHeight = explorerItemPanels.size() * EXPLORER_ITEM_HEIGHT;
		mainPanel.setPreferredSize(new Dimension(getWidth(), totalHeight));
		for (int i = 0; i < explorerItemPanels.size(); i++) {
			int currentY = i * EXPLORER_ITEM_HEIGHT;
			explorerItemPanels.get(i).setLocation(0, currentY);
			explorerItemPanels.get(i).setSize(getWidth(), EXPLORER_ITEM_HEIGHT);
		}

		repaint();
	}
	
	private void setSelecte(ExplorerItemPanel explorerItem) {
		for (int i = 0; i < explorerItemPanels.size(); i++) {
			explorerItemPanels.get(i).setSelected(false);
		}
		explorerItem.setSelected(true);
	}
	
	public void notifyRemoteFileEvent(RemoteFile file, int event) {
		for (int i = 0; i < remoteFileEventListeners.size(); i++) {
			remoteFileEventListeners.get(i).remoteFileEvent(file, event);
		}
	}
	
	public ExplorerItemPanel getSelectedItem() {
		for (int i = 0; i < explorerItemPanels.size(); i++) {
			if (explorerItemPanels.get(i).isSelected()) {
				return explorerItemPanels.get(i);
			}
		}
		
		return null;
	}
	
	private class ExplorerItemEventHandler extends ExplorerItemEventAdapter {
		private long lastClickTime;
		
		public ExplorerItemEventHandler() {
			lastClickTime = 0;
		}
		
		@Override
		public void mouseClicked(ExplorerItemPanel explorerItem, MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				setSelecte(explorerItem);
				long currentTime = System.currentTimeMillis();
				if (currentTime - lastClickTime < 500) {
					notifyRemoteFileEvent(explorerItem.getRemoteFile(), REMOTE_FILE_EVENT_OPEN);
				}
				lastClickTime = currentTime;
			} else if (e.getButton() == MouseEvent.BUTTON3) {
				setSelecte(explorerItem);
				popMenuItemEventHandler.show(e);
			} else if (e.getButton() == 4) {
				
			} else if (e.getButton() == 5) {
				
			}
		}

		@Override
		public void mouseEntered(ExplorerItemPanel explorerItem, MouseEvent e) {
			
		}

		@Override
		public void mouseExited(ExplorerItemPanel explorerItem, MouseEvent e) {
			
		}
	}
	
	private class PopMenuItemEventHandler implements ActionListener {
		private JPopupMenu popupMenu;
		private JMenuItem openMenuItem;
		private JMenuItem postMenuItem;
		private JMenuItem saveAsMenuItem;
		private JMenuItem deleteMenuItem;
		private JMenuItem renameMenuItem;
		private JMenuItem moveMenuItem;
		private JMenuItem createFileMenuItem;
		private JMenuItem createDirectoryMenuItem;
		
		public PopMenuItemEventHandler() {
			popupMenu = new JPopupMenu();
			openMenuItem = new JMenuItem("打开");
			postMenuItem = new JMenuItem("粘贴");
			saveAsMenuItem = new JMenuItem("另存为...");
			deleteMenuItem = new JMenuItem("删除");
			renameMenuItem = new JMenuItem("重命名");
			moveMenuItem = new JMenuItem("移动到...");
			createFileMenuItem = new JMenuItem("创建文件");
			createDirectoryMenuItem = new JMenuItem("创建文件夹");
			
			openMenuItem.addActionListener(this);
			postMenuItem.addActionListener(this);
			saveAsMenuItem.addActionListener(this);
			deleteMenuItem.addActionListener(this);
			moveMenuItem.addActionListener(this);
			createFileMenuItem.addActionListener(this);
			createDirectoryMenuItem.addActionListener(this);
			
			popupMenu.add(openMenuItem);
			popupMenu.add(postMenuItem);
			popupMenu.add(saveAsMenuItem);
			popupMenu.add(deleteMenuItem);
			popupMenu.add(renameMenuItem);
			popupMenu.add(moveMenuItem);
			popupMenu.add(createFileMenuItem);
			popupMenu.add(createDirectoryMenuItem);
		}
		
		public void show(MouseEvent e) {
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == openMenuItem) {
				notifyRemoteFileEvent(getSelectedItem().getRemoteFile(), REMOTE_FILE_EVENT_OPEN);
			} else if (e.getSource() == postMenuItem) {
				notifyRemoteFileEvent(getSelectedItem().getRemoteFile(), REMOTE_FILE_EVENT_POST);
			} else if (e.getSource() == saveAsMenuItem) {
				notifyRemoteFileEvent(getSelectedItem().getRemoteFile(), REMOTE_FILE_EVENT_SAVE_AS);
			} else if (e.getSource() == deleteMenuItem) {
				notifyRemoteFileEvent(getSelectedItem().getRemoteFile(), REMOTE_FILE_EVENT_DELETE);
			} else if (e.getSource() == renameMenuItem) {
				notifyRemoteFileEvent(getSelectedItem().getRemoteFile(), REMOTE_FILE_EVENT_RENAME);
			} else if (e.getSource() == moveMenuItem) {
				notifyRemoteFileEvent(getSelectedItem().getRemoteFile(), REMOTE_FILE_EVENT_MOVE);
			} else if (e.getSource() == createFileMenuItem) {
				notifyRemoteFileEvent(getSelectedItem().getRemoteFile(), REMOTE_FILE_EVENT_CREATE_FILE);
			} else if (e.getSource() == createDirectoryMenuItem) {
				notifyRemoteFileEvent(getSelectedItem().getRemoteFile(), REMOTE_FILE_EVENT_CREATE_DIRECTORY);
			}
		}
	}
	

	public void addRemoteFileEventListener(RemoteFileEventListener l) {
		for (int i = 0; i < remoteFileEventListeners.size(); i++) {
			if (remoteFileEventListeners.get(i) == l) {
				return;
			}
		}
		
		remoteFileEventListeners.add(l);
	}
	
	public void removeRemoteFileEventListener(RemoteFileEventListener l) {
		for (int i = 0; i < remoteFileEventListeners.size(); i++) {
			if (remoteFileEventListeners.get(i) == l) {
				remoteFileEventListeners.remove(i);
			}
		}
	}
}
