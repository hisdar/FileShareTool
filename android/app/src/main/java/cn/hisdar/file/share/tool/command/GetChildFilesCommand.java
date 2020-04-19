package cn.hisdar.file.share.tool.command;

import android.util.Log;

import java.io.File;

public class GetChildFilesCommand extends Command {

    private String TAG = "FileShare";
    private String FILE_TYPE_FILE      = "File";
    private String FILE_TYPE_DIRECTORY = "Directory";
    private String FILE_TYPE_ERROR     = "Error";

    public GetChildFilesCommand() {

    }

    public String generateCommand(String path) {
        File file = new File(path);

        Log.i(TAG, "path:" + path + ", can read:" + file.canRead());

        StringBuffer childFileNames = new StringBuffer();
        File[] childFiles = file.listFiles();
        if (childFiles == null) {
            Log.i(TAG, "get child files fail");
            return null;
        }

        StringBuffer childFilesCmd = new StringBuffer();
        childFilesCmd.append("<ChildFiles>\n");

        for (int i = 0; i < childFiles.length; i++) {
            String childFileName = childFiles[i].getName();
            String fileInfo = generateFileInfo(childFiles[i]);
            childFilesCmd.append("<"+ childFileName + ">\n");
            childFilesCmd.append(fileInfo);
            childFilesCmd.append("</"+ childFileName + ">\n");
        }
        childFilesCmd.append("</ChildFiles>\n");

        return childFilesCmd.toString();
    }

    private String generateFileInfo(File file) {
        String fileType = FILE_TYPE_ERROR;
        if (file.isFile()) {
            fileType = FILE_TYPE_FILE;
        } else if (file.isDirectory()) {
            fileType = FILE_TYPE_DIRECTORY;
        }

        StringBuffer result = new StringBuffer();
        result.append("<FileType>" + fileType + "</FileType>\n");
        result.append("<IsHidden>" + file.isHidden() + "</IsHidden>\n");
        result.append("<LastModified>" + file.lastModified() + "</LastModified>\n");
        result.append("<Length>" + file.length() + "</Length>\n");
        result.append("<CanRead>" + file.canRead() + "</CanRead>\n");
        result.append("<CanWrite>" + file.canWrite() + "</CanWrite>\n");
        result.append("<CanExecute>" + file.canExecute() + "</CanExecute>\n");
        return result.toString();
    }
}
