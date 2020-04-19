package cn.hisdar.file.share.tool.common;

import java.io.IOException;
import java.io.OutputStream;

public class CommandUtil {
	
	public final static byte COMMAND_HEAD                = 0X1E;
	
    public final static int COMMAND_TYPE_REQUEST         = 0X1001;
    public final static int COMMAND_TYPE_RESPONSE        = 0X1002;
    
    public final static int COMMAND_NONE                 = 0X2000;
    public final static int COMMAND_GET_DEVICE_NAME      = 0X2001;
    public final static int COMMAND_GET_CHILD_FILES      = 0X2002;
    public final static int COMMAND_GET_FILE_ATTR        = 0X2003;
    public final static int COMMAND_GET_SDCARD_INFO      = 0X2004;

    public final static byte DATA_TYPE_NONE   = 0;
    public final static byte DATA_TYPE_INT    = 1;
    public final static byte DATA_TYPE_CHAR   = 2;
    public final static byte DATA_TYPE_FLOAT  = 3;
    public final static byte DATA_TYPE_DOUBLE = 4;
    public final static byte DATA_TYPE_BYTE   = 5;
        
    public final static int COMMAND_HEAD_SIZE = 1;
    public final static int COMMAND_TYPE_SIZE = 4;
    public final static int COMMAND_SIZE      = 4;
    public final static int DATA_TYPE_SIZE    = 1;
    public final static int DATA_LENGTH_SIZE  = 4;

    public static void writeCommand(
    		OutputStream out,
    		int commandType,
    		int command,
    		char dataType,
    		int dataLength,
    		byte[] data) {

    	byte[] cmdHead = new byte[1];
		cmdHead[0] = COMMAND_HEAD;
    
    	try {
			out.write(cmdHead);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static byte[] generateCommandHead(int commandType, int command, byte dataType, int dataLength) {
    	
    	int headLen = COMMAND_HEAD_SIZE + COMMAND_TYPE_SIZE + COMMAND_SIZE + DATA_TYPE_SIZE + DATA_LENGTH_SIZE;
    	byte[] buffer = new byte[headLen];
    	buffer[0] = COMMAND_HEAD;
    	
    	byte[] cmdTypeBytes = encodeInt(commandType);
    	buffer[1] = cmdTypeBytes[0];
    	buffer[2] = cmdTypeBytes[1];
    	buffer[3] = cmdTypeBytes[2];
    	buffer[4] = cmdTypeBytes[3];
    	
    	byte[] cmdBytes = encodeInt(command);
    	buffer[5] = cmdBytes[0];
    	buffer[6] = cmdBytes[1];
    	buffer[7] = cmdBytes[2];
    	buffer[8] = cmdBytes[3];
    	
    	buffer[9] = dataType;
    	
    	byte[] dataLengthBytes = encodeInt(dataLength);
    	buffer[10] = dataLengthBytes[0];
    	buffer[11] = dataLengthBytes[1];
    	buffer[12] = dataLengthBytes[2];
    	buffer[13] = dataLengthBytes[3];
    	
    	return buffer;
    }

    public static int decodeInt(byte[] bytesData) {
        int data = bytesData[0];
        data |= bytesData[1] << 8;
        data |= bytesData[2] << 16;
        data |= bytesData[3] << 24;

        return data;
    }

    public static byte[] encodeInt(int data) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte)(data & 0xFF);
        bytes[1] = (byte)(data >> 8 & 0xFF);
        bytes[2] = (byte)(data >> 16 & 0xFF);
        bytes[3] = (byte)(data >> 24 & 0xFF);

        return bytes;
    }
}
