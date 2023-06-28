
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
    /** HashTable of fileID and FileInfo */
    Hashtable<String, FileInfo> fileTable;

    /**
     * @param e the Exception e to be traced in the exception log file
     */
    void addToErrors(Exception e) {
        try {
            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(exceptionLog, true));
            errorWriter.append(e.getMessage());
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
            File[] allDirectories = mainFtpDir.listFiles();

            for (File file : allDirectories) {
                if (file.getName().equalsIgnoreCase(clientName)) {
                    return -1;
                }
            }
            clientDirectory = new File(mainFtpDir, clientName);
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
     * of files in client Directory
     * 
     * @param out the socket ObjectOutputStream to write to
     */
    void listClientFiles(ObjectOutputStream out) {
        String clientFileList = "FILEID \t\t FILE \t\t PRIVACY\n";
        for (String fileID : fileTable.keySet()) {
            clientFileList += fileID + " \t\t " + fileTable.get(fileID).file.getName() + " \t\t "
                    + ((fileTable.get(fileID).isPrivate) ? "Private" : "Public\n");
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
     * @param socket           the Socket to client
     * @param activeClientList the active client name Set
     * @param allClientList    the Set of all client names
     * @param exceptionLog     the File to log Exceptions
     * @param clientName       String clientName to be associated with this Worker
     * @param mainFtpDir       main ftp directory
     */
    public Worker(Socket socket, Set<String> activeClientList, Set<String> allClientList, File exceptionLog,
            String clientName, File mainFtpDir) {
        this.socket = socket;
        this.exceptionLog = exceptionLog;
        this.clientName = clientName;
        this.activeClientList = activeClientList;
        this.allClientList = allClientList;
        this.mainFtpDir = mainFtpDir;
        this.fileTable = new Hashtable<String, FileInfo>();
    }

    public void run() {
        int makeClientDirectoryStatus = makeClientDirectory();
        if (makeClientDirectoryStatus == 1) {
            return;
        }

        try {
            // buffers

            ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream());

            while (true) {
                ClientMessage clientMessage = (ClientMessage) in.readObject();
                if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.ALL_FILE_LIST_REQUEST) {

                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.CLIENT_LIST_REQUEST) {

                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.FILE_LIST_REQUEST) {

                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.FILE_REQUEST) {

                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.LOGOUT_REQUEST) {

                } else if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.UPLOAD_INITATE_REQUEST) {

                }
            }
        } catch (Exception e) {
            addToErrors(e);
        }
    }
}
