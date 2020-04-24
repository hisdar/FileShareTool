package cn.hisdar.file.share.tool.view.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cn.hisdar.lib.log.HLog;

public class AddressBar extends JPanel implements ExplorerAddressListener {

	private static final long serialVersionUID = -567583029973090275L;
	
	private static final int MAX_HISTORY_PATHS = 64;
	
	private JButton upButton;
	private JButton leftButton;
	private JButton rightButton;
	
	private JButton execButton;
	private JTextField addressField;
	
	private int historyIndex;
	private boolean isCheckHistoryPath;
	private ArrayList<String> historyPaths;
	
	private KeyEventHandler keyEventHandler;
	private MouseEventHandler mouseEventHandler;
	private ActionEventHandler actionEventHandler;
	
	private ArrayList<AddressBarListener> listeners;
	
	public AddressBar() {
		listeners = new ArrayList<>();
		
		historyIndex = 0;
		isCheckHistoryPath = false;
		historyPaths = new ArrayList<>();
		actionEventHandler = new ActionEventHandler();
		
		keyEventHandler = new KeyEventHandler();
		mouseEventHandler = new MouseEventHandler();
		
		setPreferredSize(new Dimension(0, 30));
		setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel();
		initButtons(buttonPanel);
		add(buttonPanel, BorderLayout.WEST);
		
		// init address text
		JPanel addressPanel = new JPanel();
		initAddressBar(addressPanel);
		add(addressPanel, BorderLayout.CENTER);
	}
	
	private void initAddressBar(JPanel addressPanel) {
		addressPanel.setLayout(new BorderLayout());
		
		execButton = new JButton(new ImageIcon("image/refresh.png"));
		execButton.setPressedIcon(new ImageIcon("image/refresh-pressed.png"));
		execButton.setRolloverIcon(new ImageIcon("image/refresh-selected.png"));
		setButtonUI(execButton);
		
		addressField = new JTextField();
		addressField.addKeyListener(keyEventHandler);
		addressField.addMouseListener(mouseEventHandler);
		
		addressPanel.add(addressField, BorderLayout.CENTER);
		addressPanel.add(execButton, BorderLayout.EAST);
	}
	
	private void initButtons(JPanel buttonPanel) {

		buttonPanel.setBorder(null);

		upButton = new JButton(new ImageIcon("image/up.png"));
		upButton.setPressedIcon(new ImageIcon("image/up-pressed.png"));
		upButton.setRolloverIcon(new ImageIcon("image/up-selected.png"));
		
		leftButton = new JButton(new ImageIcon("image/left.png"));
		leftButton.setPressedIcon(new ImageIcon("image/left-pressed.png"));
		leftButton.setRolloverIcon(new ImageIcon("image/left-selected.png"));
		
		rightButton = new JButton(new ImageIcon("image/right.png"));
		rightButton.setPressedIcon(new ImageIcon("image/right-pressed.png"));
		rightButton.setRolloverIcon(new ImageIcon("image/right-selected.png"));
		
		setButtonUI(upButton);
		setButtonUI(leftButton);
		setButtonUI(rightButton);
		
		buttonPanel.add(upButton);
		buttonPanel.add(leftButton);
		buttonPanel.add(rightButton);
		
		upButton.addActionListener(actionEventHandler);
		leftButton.addActionListener(actionEventHandler);
		rightButton.addActionListener(actionEventHandler);
	}
	
	private void setButtonUI(JButton btn) {
		btn.setBorder(null);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setContentAreaFilled(false);
	}

	public void addAddressBarListener(AddressBarListener listener) {
		for (int i = 0; i < listeners.size(); i++) {
			if (listeners.get(i) == listener) {
				return;
			}
		}
		
		listeners.add(listener);
	}
	
	public void removeAddressBarListener(AddressBarListener listener) {
		for (int i = 0; i < listeners.size(); i++) {
			if (listeners.get(i) == listener) {
				listeners.remove(i);
				return;
			}
		}
	}
	
	private void notifyAddressBarEvent(String path) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).updateAddress(path);
		}
	}
	
	@Override
	public void updateExplorerAddress(String address) {
		addressField.setText(address);
		if (historyPaths.size() == MAX_HISTORY_PATHS) {
			historyPaths.remove(0);
			if (historyIndex > 0) {
				historyIndex -= 1;
			} else {
				HLog.il("HistoryIndex is 0, index:" + historyIndex);
			}
		}
		
		historyPaths.add(address);
		if (isCheckHistoryPath) {
			isCheckHistoryPath = false;
			
		} else {
			historyIndex = historyPaths.size() - 1;
		}

	}
	
	private void nextHistoryPath() {
		if (historyIndex == historyPaths.size() - 1) {
			return;
		}
		
		isCheckHistoryPath = true;
		historyIndex += 1;
		String newPath = historyPaths.get(historyIndex);
		notifyAddressBarEvent(newPath);
	}
	
	private void previousHistoryPath() {
		//HLog.il("history :" + historyIndex + ", total history paths:" + historyPaths.size());
		if (historyIndex == 0) {
			HLog.il("history index is 0, total history paths:" + historyPaths.size());
			return;
		}
		
		isCheckHistoryPath = true;
		historyIndex -= 1;
		String newPath = historyPaths.get(historyIndex);
		notifyAddressBarEvent(newPath);
	}
	
	private class MouseEventHandler extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == 4) {
				previousHistoryPath();
			} else if (e.getButton() == 5) {
				nextHistoryPath();
			}
		}
	}
	
	private class KeyEventHandler extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER) {
				String currentPath = addressField.getText().trim();
				notifyAddressBarEvent(currentPath);
			}
		}
	}
	
	private class ActionEventHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == upButton) {
				String currentPath = addressField.getText();
				if (currentPath == null || currentPath.length() == 0) {
					return;
				}
				
				currentPath.replace('\\', '/');
				int index = currentPath.lastIndexOf("/");
				if (index < 0) {
					return;
				}
				
				String parentPath = currentPath.substring(0, index);
				notifyAddressBarEvent(parentPath);
			} else if (e.getSource() == leftButton) {
				previousHistoryPath();
			} else if (e.getSource() == rightButton) {
				nextHistoryPath();
			} else if (e.getSource() == execButton) {
				String currentPath = addressField.getText().trim();
				notifyAddressBarEvent(currentPath);
			}
		}
	}
}
