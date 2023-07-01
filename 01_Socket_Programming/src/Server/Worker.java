package src.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Set;

class FileInfo {

    boolean isPrivate;
    File file;

    public FileInfo(boolean isPrivate, File file) {
        this.isPrivate = isPrivate;
        this.file = file;
    }

}

class Worker extends Thread {
    /** The socket with client */
    Socket socket;

    ObjectInputStream in;
    ObjectOutputStream out;
    /** The File obj for main ftp directory */
    File mainFtpDir;
    /** the File obj for client directory */
    File clientDirectory;
    /** The File obj for logging all exception using addToErrors(Exception) */
    File exceptionLog;
    /** The clientName associated with this Worker */
    String clientName;
    /** the Set of all active client names */
    Set<String> activeClientList;
    /** the Set of all client names */
    Set<String> allClientList;
    /** HashTable of String fileID and FileInfo objects for clients file */
    // Hashtable<String, FileInfo> fileTable;
    /** HashTable mapping clientName to Hashtable of clients files */
    Hashtable<String, Hashtable<String, FileInfo>> clientToFileIDMap = new Hashtable<String, Hashtable<String, FileInfo>>();

    /**
     * @param e the Exception e to be traced in the exception log file
     */
    void addToErrors(Exception e) {
        try {
            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(exceptionLog, true));
            errorWriter.append("This client's worker had error: " + clientName + " \n" + e.getMessage());
            errorWriter.newLine();
            for (StackTraceElement s : e.getStackTrace()) {
                errorWriter.append(s.toString());
                errorWriter.newLine();
            }
            errorWriter.newLine();
            errorWriter.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    /**
     * makes a directory in mainFtpDir using clientName
     * 
     * @return the status of operation: -1 already present directory, 0 success, 1
     *         exception occurred
     */
    int makeClientDirectory() {
        int status = 0;
        try {
            // File[] allDirectories = mainFtpDir.listFiles();

            // for (File file : allDirectories) {
            // if (file.getName().equalsIgnoreCase(clientName)) {
            // return -1;
            // }
            // }
            clientDirectory = new File(mainFtpDir, clientName);
            if (clientDirectory.exists()) {
                return -1;
            }
            clientDirectory.mkdirs();
            status = 0;
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("error while making this client's directory: " + clientName);
            addToErrors(e);
            status = 1;
        }
        return status;
    }

    /**
     * sends a ServerFtpMessage containing the file name, file id and file privacy
     * of files in client Directory or all other clients public files
     * 
     * @param out the socket ObjectOutputStream to write to
     */
    void sendClientFileList(ObjectOutputStream out) {
        String clientFileList = "FILEID \t\t FILE \t\t PRIVACY\n";
        Hashtable<String, FileInfo> tempHashtable = clientToFileIDMap.get(clientName);
        if (tempHashtable.isEmpty()) {
            clientFileList = "(empty)\n";
        } else {
            for (String fileID : tempHashtable.keySet()) {
                clientFileList += fileID + " \t\t " + tempHashtable.get(fileID).file.getName() + " \t\t "
                        + ((tempHashtable.get(fileID).isPrivate) ? "Private\n" : "Public\n");
            }
        }

        ServerMessage serverMessage = new ServerMessage(SERVER_PACKET_TYPE.FILE_LIST);
        serverMessage.putMessage(clientFileList);
        try {
            out.writeObject(serverMessage);
        } catch (IOException e) {
            addToErrors(e);
        }
    }

    /**
     * sends list of all public files of all the other users
     * 
     * @param out the socket ObjectOutputStream to write to
     */
    void sendAllFileList(ObjectOutputStream out) {
        String message = "";

        for (String clientName : clientToFileIDMap.keySet()) {
            message += "USER: " + clientName + "\nFILEID \t\t FILE \t\t PRIVACY\n";
            if (clientToFileIDMap.get(clientName).isEmpty()) {
                message += "(empty)\n";
            } else {
                for (String fileId : clientToFileIDMap.get(clientName).keySet()) {
                    if (!(clientToFileIDMap.get(clientName).get(fileId).isPrivate)) {
                        message += fileId + " \t\t " + clientToFileIDMap.get(clientName).get(fileId).file.getName()
                                + " \t\t Public\n";
                    }
                }
            }
        }

        ServerMessage serverMessage = new ServerMessage(SERVER_PACKET_TYPE.ALL_FILE_LIST);
        serverMessage.putMessage(message);
        try {
            out.writeObject(serverMessage);
        } catch (IOException e) {
            addToErrors(e);
        }
    }

    /**
     * sends list of all public files of all the other users
     * 
     * @param out the socket ObjectOutputStream to write to
     */
    void sendAllUserList(ObjectOutputStream out) {
        String message = "All clients:\n";
        for (String client : allClientList) {
            message += client + ((activeClientList.contains(client)) ? "(active)\n" : "\n");
        }

        ServerMessage serverMessage = new ServerMessage(SERVER_PACKET_TYPE.CLIENT_LIST);
        serverMessage.putMessage(message);
        try {
            out.writeObject(serverMessage);
        } catch (Exception e) {
            addToErrors(e);
        }
    }

    /**
     * logs out client by closing it's socket
     * TODO: handle things in upload n download
     * 
     * @param out the client socket OutputObjectStream
     * @return true if successfully closed socket, false otherwise
     */
    boolean handleLogoutRequest(ObjectOutputStream out) {
        ServerMessage serverMessage = new ServerMessage(SERVER_PACKET_TYPE.LOGOUT_RESPONSE);
        serverMessage.putMessage("Success");
        try {
            out.writeObject(serverMessage);
            activeClientList.remove(clientName);
            // TODO: vejal ase
            out.close();
            in.close();
            socket.close();
        } catch (Exception e) {
            addToErrors(e);
            return false;
        }
        return true;
    }

    /**
     * broadcast file request to all clients and send response to this client
     * 
     * @param clientMessage ClientFtpMessage recieved from client with file request
     *                      description
     * @param out           client Socket ObjectOutputStream to write to
     * @return true if successfully broadcast message and sent response to client,
     *         false otherwise
     */
    boolean handleFileRequest(ClientMessage clientMessage, ObjectOutputStream out) {
        // TODO: handle broadcast message and message queues

        ServerMessage serverMessage = new ServerMessage(SERVER_PACKET_TYPE.IMMEDIATE_FILE_REQUEST_RESPONSE);
        // TODO: Conditioned on successful broadcast
        serverMessage.putMessage("Success");
        try {
            out.writeObject(serverMessage);
        } catch (Exception e) {
            addToErrors(e);
            return false;
        }
        return true;
    }

    /**
     * class extending Thread
     * 
     * @param serverData SharedServerData object holding common items
     * @param clientName String clientName for this thread
     * @param socket     Socket for client
     * @param in         ObjectInputStream for client Socket
     * @param out        ObjectOutputStream for client Socket
     */
    public Worker(SharedServerData serverData, String clientName, Socket socket, ObjectInputStream in,
            ObjectOutputStream out) {
        this.socket = socket;
        this.exceptionLog = serverData.exceptionLog;
        this.clientName = clientName;
        this.activeClientList = serverData.activeClients;
        this.allClientList = serverData.allClients;
        this.mainFtpDir = serverData.mainFtpDir;
        // this.fileTable = new Hashtable<String, FileInfo>();
        this.clientToFileIDMap = serverData.clientToFileIDMap;

        this.out = out;
        this.in = in;

    }

    public void run() {
        int makeClientDirectoryStatus = makeClientDirectory();
        if (makeClientDirectoryStatus == 1) {
            return;
        }

        try {

            while (true) {
                ClientMessage clientMessage = (ClientMessage) in.readObject();
                if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.ALL_FILE_LIST_REQUEST) {
                    sendAllFileList(out);

                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.CLIENT_LIST_REQUEST) {
                    sendAllUserList(out);
                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.FILE_LIST_REQUEST) {
                    sendClientFileList(out);
                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.FILE_REQUEST) {
                    handleFileRequest(clientMessage, out);
                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.LOGOUT_REQUEST) {
                    boolean logoutStatus = handleLogoutRequest(out);
                    if (logoutStatus) {
                        return;
                    }
                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.UPLOAD_INITATE_REQUEST) {

                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.DOWNLOAD_REQUEST) {

                }
            }
        } catch (Exception e) {
            addToErrors(e);
            activeClientList.remove(this.clientName);
        }
    }
}
