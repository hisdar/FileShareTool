package cn.hisdar.file.share.tool.view.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public class ExplorerItemView extends JPanel {

	private static final int EXPLORER_ITEM_HEIGHT = 30;
	
	private JPanel mainPanel;
	private JScrollPane mainScrollPanel;
	private ArrayList<ExplorerItemPanel> explorerItemPanels;
	
	public ExplorerItemView() {
		explorerItemPanels = new ArrayList<>();
		
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
	}
	
	public void removeAllExplorerItem() {
		for (int i = explorerItemPanels.size(); i > 0 ; i--) {
			mainPanel.remove(explorerItemPanels.get(i - 1));
			explorerItemPanels.remove(i - 1);
		}
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
}
