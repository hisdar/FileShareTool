package cn.hisdar.file.share.tool.command;

import java.io.IOException;
import java.io.OutputStream;

public class CommandOutputStream {

	private byte[] writeBuffer;
	private int writeBufferLen;
	private int writeBufferTail;
	private OutputStream outputStream;
	public CommandOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
		
		writeBufferLen = 10240;
		writeBuffer = new byte[writeBufferLen];
		writeBufferTail = 0;
	}
	
	public int write(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			if (writeBufferTail == writeBufferLen) {
				try {
					outputStream.write(writeBuffer);
					outputStream.flush();
					writeBufferTail = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			writeBuffer[writeBufferTail] = data[i];
			writeBufferTail++;
		}

		return data.length;
	}
	
	public void flush() {
		try {
			outputStream.write(writeBuffer, 0, writeBufferTail);
			outputStream.flush();
			writeBufferTail = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
