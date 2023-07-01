package src.Server;

import java.util.Hashtable;

public class ClientMessage extends FtpMessage {
    public CLIENT_PACKET_TYPE client_PACKET_TYPE;

    public ClientMessage(CLIENT_PACKET_TYPE client_PACKET_TYPE, String clientName) {
        messageContent = new Hashtable<String, String>();
        this.packet_TYPE = PACKET_TYPE.CLIENT;
        this.client_PACKET_TYPE = client_PACKET_TYPE;
        messageContent.put("clientName", clientName);
    }

    /**
     * @param clientName the client sending this FtpMessage
     */
    public void putClientName(String clientName) {
        messageContent.put("clientName", clientName);
    }

    /**
     * @return the client who sent this FtpMessage
     */
    String getClientName() {
        return messageContent.get("clientName");
    }
}
