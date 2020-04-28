package cn.hisdar.file.share.tool.view.explorer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.Remote;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.sun.corba.se.spi.orbutil.fsm.Action;
import com.sun.prism.Image;

import cn.hisdar.file.share.tool.Global;
import cn.hisdar.file.share.tool.command.RemoteFile;
import cn.hisdar.lib.log.HLog;
import jdk.internal.dynalink.beans.StaticClass;

public class ExplorerItemPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int REMOTE_FILE_EVENT_OPEN    = 0X4001;
	public static final int REMOTE_FILE_EVENT_POST    = 0X4002;
	public static final int REMOTE_FILE_EVENT_SAVE_AS = 0X4003;
	public static final int REMOTE_FILE_EVENT_DELETE  = 0X4004;
	public static final int REMOTE_FILE_EVENT_RENAME  = 0X4005;
	public static final int REMOTE_FILE_EVENT_MOVE    = 0X4006;
	public static final int REMOTE_FILE_EVENT_CREATE_FILE = 0X4007;
	public static final int REMOTE_FILE_EVENT_CREATE_DIRECTORY = 0X4008;
	
	private int dividerSize = 4;
	private int[] titleLableSizeArray;

	private String filePath;
	private RemoteFile file;
	
	private JPopupMenu popupMenu;
	private JMenuItem openMenuItem;
	private JMenuItem postMenuItem;
	private JMenuItem saveAsMenuItem;
	private JMenuItem deleteMenuItem;
	private JMenuItem renameMenuItem;
	private JMenuItem moveMenuItem;
	private JMenuItem createFileMenuItem;
	private JMenuItem createDirectoryMenuItem;
	
	private ArrayList<JLabel> titleLabels;
	private MouseEventHandler mouseEventHandler;
	private PopMenuItemEventHandler popMenuItemEventHandler;
	private ArrayList<RemoteFileEventListener> remoteFileEventListeners;
	
	public ExplorerItemPanel(int itemCount) {
		init(itemCount);
	}
	
	private void initPopupMenu() {
		popMenuItemEventHandler = new PopMenuItemEventHandler();
		
		popupMenu = new JPopupMenu();
		openMenuItem = new JMenuItem("��");
		postMenuItem = new JMenuItem("ճ��");
		saveAsMenuItem = new JMenuItem("���Ϊ...");
		deleteMenuItem = new JMenuItem("ɾ��");
		renameMenuItem = new JMenuItem("������");
		moveMenuItem = new JMenuItem("�ƶ���...");
		createFileMenuItem = new JMenuItem("�����ļ�");
		createDirectoryMenuItem = new JMenuItem("�����ļ���");
		
		openMenuItem.addActionListener(popMenuItemEventHandler);
		postMenuItem.addActionListener(popMenuItemEventHandler);
		saveAsMenuItem.addActionListener(popMenuItemEventHandler);
		deleteMenuItem.addActionListener(popMenuItemEventHandler);
		moveMenuItem.addActionListener(popMenuItemEventHandler);
		createFileMenuItem.addActionListener(popMenuItemEventHandler);
		createDirectoryMenuItem.addActionListener(popMenuItemEventHandler);
		
		popupMenu.add(openMenuItem);
		popupMenu.add(postMenuItem);
		popupMenu.add(saveAsMenuItem);
		popupMenu.add(deleteMenuItem);
		popupMenu.add(renameMenuItem);
		popupMenu.add(moveMenuItem);
		popupMenu.add(createFileMenuItem);
		popupMenu.add(createDirectoryMenuItem);
		
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
	
	private void init(int itemCount) {

		initPopupMenu();
		filePath = null;
		setLayout(null);
		titleLableSizeArray = null;
		titleLabels = new ArrayList<>();
		remoteFileEventListeners = new ArrayList<>();
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
	
	public void notifyRemoteFileEvent(RemoteFile file, int event) {
		for (int i = 0; i < remoteFileEventListeners.size(); i++) {
			remoteFileEventListeners.get(i).remoteFileEvent(file, event);
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
	
	private class MouseEventHandler extends MouseAdapter {
		private Color defaultBgColor;
		private long lastClickTime;
		
		public MouseEventHandler(Component component) {
			defaultBgColor = null;
			lastClickTime = 0;
		}

		private void handleRightButtonEvent(MouseEvent e) {
			
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				handleRightButtonEvent(e);
				return;
			}
			
			if (e.getButton() == MouseEvent.BUTTON1) {
				long currentTime = System.currentTimeMillis();
				if (currentTime - lastClickTime < 500) {
					notifyRemoteFileEvent(file, REMOTE_FILE_EVENT_OPEN);
				}
				lastClickTime = currentTime;
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			defaultBgColor = getBackground();
			setBackground(new Color(0xAAAAFF));
		}

		@Override
		public void mouseExited(MouseEvent e) {
			setBackground(defaultBgColor);
		}
	}
	
	private class PopMenuItemEventHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == openMenuItem) {
				notifyRemoteFileEvent(file, REMOTE_FILE_EVENT_OPEN);
			} else if (e.getSource() == postMenuItem) {
				notifyRemoteFileEvent(file, REMOTE_FILE_EVENT_POST);
			} else if (e.getSource() == saveAsMenuItem) {
				notifyRemoteFileEvent(file, REMOTE_FILE_EVENT_SAVE_AS);
			} else if (e.getSource() == deleteMenuItem) {
				notifyRemoteFileEvent(file, REMOTE_FILE_EVENT_DELETE);
			} else if (e.getSource() == renameMenuItem) {
				notifyRemoteFileEvent(file, REMOTE_FILE_EVENT_RENAME);
			} else if (e.getSource() == moveMenuItem) {
				notifyRemoteFileEvent(file, REMOTE_FILE_EVENT_MOVE);
			} else if (e.getSource() == createFileMenuItem) {
				notifyRemoteFileEvent(file, REMOTE_FILE_EVENT_CREATE_FILE);
			} else if (e.getSource() == createDirectoryMenuItem) {
				notifyRemoteFileEvent(file, REMOTE_FILE_EVENT_CREATE_DIRECTORY);
			}
		}
	}
}
