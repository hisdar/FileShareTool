package cn.hisdar.file.share.tool.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JDialog;

import cn.hisdar.lib.adapter.FileAdapter;
import cn.hisdar.lib.log.HLog;

public class DataSocket {

	private static final int READ_BUFFER_LENGTH = 4096;

	private enum IOWorkerType {
		IOWorkerWrite,
		IOWorkerRead,
	}
	
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private BufferedInputStream bufferedInputStream;
	
	private byte[] readBuffer;
	private ProcessDialog processDialog;

	public DataSocket(Socket dataSocket) {
		
		processDialog = new ProcessDialog();
		
		socket = dataSocket;
		if (socket != null) {
			try {
				inputStream = socket.getInputStream();
				bufferedInputStream = new BufferedInputStream(inputStream);
				
				outputStream = socket.getOutputStream();
			} catch (IOException e) {
				HLog.el(e);
			}
			
			readBuffer = new byte[READ_BUFFER_LENGTH];
		} else {
			inputStream = null;
			bufferedInputStream = null;
		}
	}
	
	public boolean send(String srcPath) {
		try {
			FileInputStream fileInputStream = new FileInputStream(new File(srcPath));
			byte[] readBuffer = new byte[1024 * 8];
			int readLen = fileInputStream.read(readBuffer);
			while (readLen > 0) {
				outputStream.write(readBuffer, 0, readLen);
				HLog.il("write data size:" + readLen);
				readLen = fileInputStream.read(readBuffer);
			}
			
			outputStream.flush();
			fileInputStream.close();
			
		} catch (Exception e) {
			HLog.el(e);
			return false;
		}
		
		return true;
	}
	
	public boolean receive(int length, String savePath) {
		IOWorker worker = new IOWorker(savePath, length, IOWorkerType.IOWorkerRead);
		worker.start();
		
		processDialog.setModal(true);
		processDialog.setVisible(true);
		
		return true;
	}
		
	private boolean receive(int length, String savePath, boolean showProcess) {
		
		if (bufferedInputStream == null) {
			return false;
		}
		
		int readLength = 0;
		try {
			FileAdapter.initFile(savePath);
			FileOutputStream fileOutputStream = new FileOutputStream(new File(savePath));
			
			int totalReadLength = 0;
			while (totalReadLength < length) {
				readLength = bufferedInputStream.read(readBuffer);
				fileOutputStream.write(readBuffer, 0, readLength);

				totalReadLength += readLength;
				if (showProcess) {
					processDialog.setProcess(1.0f * totalReadLength / length);
				}
			}
			
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (IOException e) {
			HLog.el(e);
		}

		return true;
	}


	
	private class IOWorker extends Thread {
		
		private String path;
		private int dataLength;
		private IOWorkerType workerType;
		
		public IOWorker(String path, int dataLength, IOWorkerType type) {
			this.path = path;
			this.dataLength = dataLength;
			workerType = type;
		}
		
		public void run() {
			if (workerType == IOWorkerType.IOWorkerRead) {
				receive(dataLength, path, true);
				processDialog.setVisible(false);
			}
		}
	}

	private class ProcessDialog extends JDialog {
		public ProcessDialog() {
			setSize(600, 300);
		}
		
		public void setProcess(float process) {
			int processInt = (int)(process * 100);
			String title = "" + processInt;
			setTitle(title);
		}
	}
}
