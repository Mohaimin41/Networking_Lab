package src.Server;

import java.util.Hashtable;

public class ServerMessage extends FtpMessage {
    public SERVER_PACKET_TYPE server_PACKET_TYPE;

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
    public int getChunkSize() {
        return Integer.parseInt(messageContent.get("chunkSize"));
    }

}
