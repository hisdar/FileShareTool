package cn.hisdar.file.share.tool.view.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public class ExplorerItemView extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final int EXPLORER_ITEM_HEIGHT = 30;
	
	private JPanel mainPanel;
	private JScrollPane mainScrollPanel;
	private ArrayList<ExplorerItemPanel> explorerItemPanels;
	private ExplorerItemEventHandler explorerItemEventHandler;
	
	public ExplorerItemView() {
		explorerItemPanels = new ArrayList<>();
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
	
	private class ExplorerItemEventHandler extends ExplorerItemEventAdapter {

		@Override
		public void mouseClicked(ExplorerItemPanel explorerItem, MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {
				for (int i = 0; i < explorerItemPanels.size(); i++) {
					explorerItemPanels.get(i).setSelected(false);
				}
				explorerItem.setSelected(true);
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
}
