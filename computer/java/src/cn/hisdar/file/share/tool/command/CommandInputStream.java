package cn.hisdar.file.share.tool.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import cn.hisdar.lib.log.HLog;

public class CommandInputStream {

	private static int INPUT_BUFFER_LEN = 4096;
	
	private InputStream inputStream;
	
	private int inputBufferHead;
	private int inputBufferTail;
	private byte inputBuffer[];
	private boolean isStreamStoped;
	
	public CommandInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
		isStreamStoped = false;
		inputBufferHead = 0;
		inputBufferTail = 0;
		inputBuffer = new byte[INPUT_BUFFER_LEN];
	}
	
	public boolean streamStoped() {
		return isStreamStoped;
	}
	
	public String readLine() {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		while (true) {
			boolean isFoundLineEnd = false;
			while (inputBufferHead < inputBufferTail) {
				
				if (inputBuffer[inputBufferHead] == '\n') {
					inputBufferHead++;
					isFoundLineEnd = true;
					break;
				}
				
				byteArray.write(inputBuffer, inputBufferHead, 1);
				inputBufferHead++;
			}
			
			if (isFoundLineEnd) {
				break;
			}
			
			fillBuffer();
			if (inputBufferTail == 0) {
				// if no data read
				break;
			}
		}
		
		if (byteArray.size() <= 0) {
			return null;
		}
		
		byte[] bytes = byteArray.toByteArray();
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	public int read(byte[] readBuffer) {
		
		int readBufferIndex = 0;
		int readBufferLength = readBuffer.length;
		while (readBufferLength > 0) {
			
			int currentReadLength = readBufferLength;
			int inputBufferDataLength = inputBufferTail - inputBufferHead;
			if (currentReadLength > inputBufferDataLength) {
				currentReadLength = inputBufferDataLength;
			}
			
			for (int i = 0; i < currentReadLength; i++) {
				readBuffer[readBufferIndex] = inputBuffer[inputBufferHead];
				readBufferIndex++;
				inputBufferHead++;
			}
			readBufferLength -= currentReadLength;
			
			if (inputBufferHead >= inputBufferTail) {
				fillBuffer();
			}	
		}
		
		return readBufferIndex;
	}
	
	private void fillBuffer() {
		
		if (inputStream == null) {
			return;
		}
		
		inputBufferHead = 0;
		inputBufferTail = 0;
		
		try {
			if (inputStream.available() <= 0) {
				// no data to read
				//HLog.il("no data to read");
				return;
			}
			
			inputBufferTail = inputStream.read(inputBuffer);
		} catch (IOException e) {
			isStreamStoped = true;
			return;
		}
	}
}
