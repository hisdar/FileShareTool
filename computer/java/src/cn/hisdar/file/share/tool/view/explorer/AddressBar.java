package cn.hisdar.file.share.tool.view.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class AddressBar extends JPanel {

	private JButton upButton;
	private JButton leftButton;
	private JButton rightButton;
	
	public AddressBar() {
		setPreferredSize(new Dimension(0, 35));
		//setBorder(BorderFactory.createLineBorder(new Color(0x555555)));
		setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(null);

		upButton = new JButton(new ImageIcon("image/up.png"));
		leftButton = new JButton(new ImageIcon("image/left.png"));
		rightButton = new JButton(new ImageIcon("image/right.png"));
		
		upButton.setBorder(null);
		leftButton.setBorder(null);
		rightButton.setBorder(null);
		
		buttonPanel.add(upButton);
		buttonPanel.add(leftButton);
		buttonPanel.add(rightButton);
		
		add(buttonPanel, BorderLayout.WEST);
	}
}
