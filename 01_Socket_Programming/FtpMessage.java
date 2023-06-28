import java.io.Serializable;
import java.util.Hashtable;

enum PACKET_TYPE {
    SERVER,
    CLIENT
}

enum SERVER_PACKET_TYPE {
    CHUNK_ACKNOWLEDGEMENT, // lets client know chunk recieved successfully
    DOWNLOAD_START, // lets client know the download stream is starting
    DOWNLOAD_END, // lets client know server ended the download stream
    LOGIN_RESPONSE, // response to client login request
    LOGOUT_RESPONSE, // response to client logout request
    FILE_REQUEST_BROADCAST, // broadcast message to clients about this file request
    IMMEDIATE_FILE_REQUEST_RESPONSE, // lets client know if request servicable right
    DELAYED_FILE_REQUEST_RESPONSE, // lets client know file is downloadable now
    UPLOAD_BEGIN_CONFIRMATION, // affirmative response to client upload initiation request, with informations
    UPLOAD_END_CONFIRMATION, // response to client upload completion message with status
    FILE_CHUNK, // byte array file chunk present
    UPLOAD_BUFFER_FULL, // negative response to client upload initiation request
    FILE_LIST, // contains client file list
    ALL_FILE_LIST, // contains all other clients file list
    CLIENT_LIST, // contains client list
    CLIENT_MESSAGES, // contains queued message for client
}

enum CLIENT_PACKET_TYPE {
    TIMEOUT, // client stops file transmission
    LOGIN_REQUEST, // client requesting login with name
    LOGOUT_REQUEST, // client requesting logut
    FILE_REQUEST, // request for file with description
    UPLOAD_INITATE_REQUEST, // request to initiate upload
    UPLOAD_END, // notifying server all chunk sent
    FILE_CHUNK, // byte array chunk
    FILE_LIST_REQUEST, // request for own file list
    ALL_FILE_LIST_REQUEST, // for all public file list
    CLIENT_LIST_REQUEST, // for all clients
}

class FtpMessage implements Serializable {
    /**
     * Hashtable holding all message data, keys are:
     * chunk, chunkSize, fileID, message, fileName, fileSize
     */
    protected Hashtable<String, String> messageContent;

    protected PACKET_TYPE packet_TYPE;

    /***
     * @param chunk the file chunk to be uploaded or downloaded by client
     */
    void putFileChunk(byte[] chunk) {
        messageContent.put("fileChunk", new String(chunk));
    }

    /**
     * @return the file chunk byte array in this ftp message
     */

    byte[] getFileChunk() {
        return messageContent.get("fileChunk").getBytes();
    }

    /**
     * @param fileID the fileID this FtpMessage will be associated to
     */
    void putFileID(String fileID) {
        messageContent.put("fileID", fileID);
    }

    /**
     * @return the fileID of this FtpMessage
     */
    String getFileID() {
        return messageContent.get("fileID");
    }

    /**
     * @param message appropriate message containing displayable information
     */
    void putMessage(String message) {
        messageContent.put("message", message);
    }

    /**
     * @return message if contained in the FtpMessage or null
     */
    String getMessage() {
        return messageContent.get("message");
    }

    /**
     * @param fileName the file to be uploaded/downloaded
     */
    void putFileName(String fileName) {
        messageContent.put("FileName", fileName);
    }

    /**
     * @return the file client wants to upload/server wants to send
     */
    String getfileName() {
        return messageContent.get("fileName");
    }

    /**
     * @param size total size of file in bytes
     */
    void putfileSize(int size) {
        messageContent.put("fileSize", Integer.toString(size));
    }

    /**
     * @return total size of file in bytes
     */
    int getfileSize() {
        return Integer.parseInt(messageContent.get("fileSize"));
    }

}

class ClientMessage extends FtpMessage {
    protected CLIENT_PACKET_TYPE client_PACKET_TYPE;

    ClientMessage(CLIENT_PACKET_TYPE client_PACKET_TYPE, String clientName) {
        messageContent = new Hashtable<String, String>();
        this.packet_TYPE = PACKET_TYPE.CLIENT;
        this.client_PACKET_TYPE = client_PACKET_TYPE;
        messageContent.put("clientName", clientName);
    }

    /**
     * @param clientName the client sending this FtpMessage
     */
    void putClientName(String clientName) {
        messageContent.put("clientName", clientName);
    }

    /**
     * @return the client who sent this FtpMessage
     */
    String getClientName() {
        return messageContent.get("clientName");
    }
}

class ServerMessage extends FtpMessage {
    protected SERVER_PACKET_TYPE server_PACKET_TYPE;

    ServerMessage(SERVER_PACKET_TYPE server_PACKET_TYPE) {
        messageContent = new Hashtable<String, String>();
        this.packet_TYPE = PACKET_TYPE.SERVER;
        this.server_PACKET_TYPE = server_PACKET_TYPE;
    }

    /**
     * @param chunkSize the size of the chunk in bytes or the length of byte array
     */
    void putChunkSize(int chunkSize) {
        messageContent.put("chunkSize", Integer.toString(chunkSize));
    }

    /**
     * @return chunkSize to be used processing this FtpMessage
     */
    int getChunkSize() {
        return Integer.parseInt(messageContent.get("chunkSize"));
    }

}