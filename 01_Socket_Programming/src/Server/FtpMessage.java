package src.Server;

import java.io.Serializable;
import java.util.Hashtable;

public class FtpMessage implements Serializable {
    /**
     * Hashtable holding all message data, keys are:
     * chunk, chunkSize, fileID, message, fileName, fileSize
     */
    protected Hashtable<String, String> messageContent;

    protected PACKET_TYPE packet_TYPE;

    /***
     * @param chunk the file chunk to be uploaded or downloaded by client
     */
    public void putFileChunk(byte[] chunk) {
        messageContent.put("fileChunk", new String(chunk));
    }

    /**
     * @return the file chunk byte array in this ftp message
     */

    public byte[] getFileChunk() {
        return messageContent.get("fileChunk").getBytes();
    }

    /**
     * @param fileID the fileID this FtpMessage will be associated to
     */
    public void putFileID(String fileID) {
        messageContent.put("fileID", fileID);
    }

    /**
     * @return the fileID of this FtpMessage
     */
    public String getFileID() {
        return messageContent.get("fileID");
    }

    /**
     * @param message appropriate message containing displayable information
     */
    public void putMessage(String message) {
        messageContent.put("message", message);
    }

    /**
     * @return message if contained in the FtpMessage or null
     */
    public String getMessage() {
        return messageContent.get("message");
    }

    /**
     * @param fileName the file to be uploaded/downloaded
     */
    public void putFileName(String fileName) {
        messageContent.put("FileName", fileName);
    }

    /**
     * @return the file client wants to upload/server wants to send
     */
    public String getfileName() {
        return messageContent.get("fileName");
    }

    /**
     * @param size total size of file in bytes
     */
    public void putfileSize(int size) {
        messageContent.put("fileSize", Integer.toString(size));
    }

    /**
     * @return total size of file in bytes
     */
    public int getfileSize() {
        return Integer.parseInt(messageContent.get("fileSize"));
    }

}