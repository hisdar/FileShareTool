package cn.hisdar.file.share.tool.view.explorer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ExplorerTitlePanel extends JPanel implements DividerLabelListener {

	private static final long serialVersionUID = 1L;
	
	private int dividerSize = 4;

	private int[] titleLableSizeArray; 
	private ArrayList<JLabel> titleLabels;
	private ArrayList<DividerLabel> dividerLabels;
	private ArrayList<ExplorerTitleEventListener> explorerTitleEventListeners;
	
	public ExplorerTitlePanel(String[] titles) {
		
		setLayout(null);
		titleLabels = new ArrayList<>();
		dividerLabels = new ArrayList<>();
		explorerTitleEventListeners = new ArrayList<>();
		titleLableSizeArray = null;
		
		for (int i = 0; i < titles.length; i++) {
			JLabel label = new JLabel(titles[i]);
			titleLabels.add(label);
			add(label);
		}
		
		for (int i = 0; i < titles.length - 1; i ++) {
			DividerLabel dividerLabel = new DividerLabel();
			dividerLabel.addDividerLabelListener(this);
			dividerLabels.add(dividerLabel);
			add(dividerLabel);
		}

		setPreferredSize(new Dimension(0, 30));
		setBackground(new Color(0xCCCCCC));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (titleLableSizeArray == null) {
			int dividerSizeTotal = dividerSize * dividerLabels.size();
			int titleSizeTotal = getWidth() - dividerSizeTotal;
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
		
		startIndexX = 0;
		for (int i = 0; i < dividerLabels.size(); i++) {
			JLabel dividerLabel = dividerLabels.get(i);
			startIndexX += titleLableSizeArray[i];
			
			dividerLabel.setBounds(startIndexX, 0, dividerSize, getHeight());
			dividerLabel.setPreferredSize(new Dimension(dividerSize, getHeight()));
			startIndexX = startIndexX + dividerSize;
		}
		
		validate();
	}

	private int getDividerIndex(DividerLabel label) {
		int index = -1;
		for (int i = 0; i < dividerLabels.size(); i++) {
			if (dividerLabels.get(i) == label) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	@Override
	public void dividerLabelResize(DividerLabel label, int offsetX, int offsetY) {
		
		int index = getDividerIndex(label);
		if (index == -1) {
			return;
		}
		
		titleLableSizeArray[index] += offsetX;
		int[] columnWidthArray = new int[titleLableSizeArray.length];
		for (int i = 0; i < titleLableSizeArray.length; i++) {
			columnWidthArray[i] = titleLableSizeArray[i];// + dividerSize;
		}
		
		for (int i = 0; i < explorerTitleEventListeners.size(); i++) {
			explorerTitleEventListeners.get(i).setColumnSize(columnWidthArray);
		}
		repaint();
	}

	@Override
	public void dividerLabelAutoResize(DividerLabel label) {
		int index = getDividerIndex(label);
		if (index == -1) {
			return;
		}
		
		for (int i = 0; i < explorerTitleEventListeners.size(); i++) {
			explorerTitleEventListeners.get(i).autoSetColumnSize(index);
		}
	}
	
	public void addExplorTitleListener(ExplorerTitleEventListener listener) {
		for (int i = 0; i < explorerTitleEventListeners.size(); i++) {
			if (explorerTitleEventListeners.get(i) == listener) {
				return;
			}
		}
		
		explorerTitleEventListeners.add(listener);
	}
	
	public void removeExplorTitleListener(ExplorerTitleEventListener listener) {
		for (int i = 0; i < explorerTitleEventListeners.size(); i++) {
			if (explorerTitleEventListeners.get(i) == listener) {
				explorerTitleEventListeners.remove(i);
				return;
			}
		}
	}

	
}
