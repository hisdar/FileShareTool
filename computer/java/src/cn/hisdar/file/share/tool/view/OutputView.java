package cn.hisdar.file.share.tool.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import cn.hisdar.lib.log.HLog;
import cn.hisdar.lib.log.HLogInterface;

public class OutputView extends JPanel implements HLogInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea logArea;
	public OutputView() {
		logArea = new JTextArea();
		setLayout(new BorderLayout());
		JScrollPane logScrollPane = new JScrollPane(logArea);
		add(logScrollPane, BorderLayout.CENTER);
		HLog.addHLogInterface(this);
	}
	@Override
	public void info(String log) {
		if (logArea.getLineCount() > 1000) {
			logArea.setText("");
		}
		logArea.append(log);
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}
	@Override
	public void error(String log) {
		if (logArea.getLineCount() > 1000) {
			logArea.setText("");
		}
		logArea.append(log);
		logArea.setCaretPosition(logArea.getDocument().getLength());
		
	}
	@Override
	public void debug(String log) {
		if (logArea.getLineCount() > 1000) {
			logArea.setText("");
		}
		logArea.append(log);
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}
	
	
	
}
