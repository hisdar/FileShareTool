package cn.hisdar.file.share.tool.command;

public class CommandUtil {
    public final static int COMMAND_TYPE_REQUEST  = 0X1001;
    public final static int COMMAND_TYPE_RESPONSE = 0X1002;

    //public final static int COMMAND_ = 0X1001;
    //public final static int COMMAND_ = 0X1002;

    public final static int COMMAND_HEAD_SIZE = 1;
    public final static int COMMAND_TYPE_SIZE = 4;
    public final static int COMMAND_SIZE      = 4;
    public final static int DATA_TYPE_SIZE    = 1;
    public final static int DATA_LENGTH_SIZE  = 4;

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
