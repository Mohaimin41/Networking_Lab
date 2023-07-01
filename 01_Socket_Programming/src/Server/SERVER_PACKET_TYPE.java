package src.Server;

public enum SERVER_PACKET_TYPE {
    /** lets client know chunk recieved successfully */
    CHUNK_ACKNOWLEDGEMENT,
    /** lets client know the download stream is starting */
    DOWNLOAD_START,
    /** lets client know server ended the download stream */
    DOWNLOAD_END,
    /** response to client login request */
    LOGIN_RESPONSE,
    /** response to client logout request */
    LOGOUT_RESPONSE,
    /** broadcast message to clients about this file request */
    FILE_REQUEST_BROADCAST,
    /** lets client know if request servicable right */
    IMMEDIATE_FILE_REQUEST_RESPONSE,
    /** lets client know file is downloadable now */
    DELAYED_FILE_REQUEST_RESPONSE,
    /**
     * affirmative response to client upload initiation request, with informations
     */
    UPLOAD_BEGIN_CONFIRMATION,
    /** response to client upload completion message with status */
    UPLOAD_END_CONFIRMATION,
    /** byte array file chunk present */
    FILE_CHUNK,
    /** negative response to client upload initiation request */
    UPLOAD_BUFFER_FULL,
    /** contains client file list */
    FILE_LIST,
    /** contains all other clients file list */
    ALL_FILE_LIST,
    /** contains client list */
    CLIENT_LIST,
    /** contains queued message for client */
    CLIENT_MESSAGES,
}
