package cn.hisdar.file.share.tool.view.explorer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import jdk.internal.org.objectweb.asm.tree.IntInsnNode;

public class ExplorerTitlePanel extends JPanel implements DividerLabelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int dividerSize = 4;

	private ArrayList<JLabel> titleLabels;
	private ArrayList<DividerLabel> dividerLabels;
	private int[] titleLableSizeArray;
	
	public ExplorerTitlePanel(String[] titles) {
		
		setLayout(null);
		titleLabels = new ArrayList<>();
		dividerLabels = new ArrayList<>();
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

	@Override
	public void dividerLabelResize(DividerLabel label, int offsetX, int offsetY) {
		int index = -1;
		for (int i = 0; i < dividerLabels.size(); i++) {
			if (dividerLabels.get(i) == label) {
				index = i;
				break;
			}
		}
		
		if (index == -1) {
			return;
		}
		
		titleLableSizeArray[index] += offsetX;
		repaint();
	}

	@Override
	public void dividerLabelAutoResize(DividerLabel label) {
		//System.err.println("auto resize");
	}
}
