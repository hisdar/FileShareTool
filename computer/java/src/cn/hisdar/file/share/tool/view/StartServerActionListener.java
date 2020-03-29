package cn.hisdar.file.share.tool.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import cn.hisdar.file.share.tool.server.SocketServerSearcher;

public class StartServerActionListener implements ActionListener{

	public StartServerActionListener() {
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object object = e.getSource();
		JButton button = (JButton)object;
		//ServerManager.getInstance().startServer();
		SocketServerSearcher.getInstance();
		button.setEnabled(false);
	}
	
	
}
