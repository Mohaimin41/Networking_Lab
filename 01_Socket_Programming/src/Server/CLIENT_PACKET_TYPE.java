package src.Server;

public enum CLIENT_PACKET_TYPE {
    /** client stops file transmission */
    TIMEOUT,
    /** client requesting login with name */
    LOGIN_REQUEST,
    /** client requesting logut */
    LOGOUT_REQUEST,
    /** request for file with description */
    FILE_REQUEST,
    /** request to initiate upload */
    UPLOAD_INITATE_REQUEST,
    /** notifying server all chunk sent */
    UPLOAD_END,
    /** byte array chunk */
    FILE_CHUNK,
    /** request for own file list */
    FILE_LIST_REQUEST,
    /** for all public file list */
    ALL_FILE_LIST_REQUEST,
    /** for all clients */
    CLIENT_LIST_REQUEST,
    /** download request with fileID */
    DOWNLOAD_REQUEST,
}
