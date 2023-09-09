package src.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

public class Server {
    /** Maximum file chunk size in bytes */
    static private int MAX_CHUNK_SIZE = 1000;
    /** Minimum file chunk size in bytes */
    static private int MIN_CHUNK_SIZE = 100;
    /** Maximum file buffer size used in Server */
    static private int MAX_BUFFER_SIZE = 50000000;
    /** Set of all clients names */
    static Set<String> allClients = new HashSet<String>();
    /** Set of all active clients names */
    static Set<String> activeClients = new HashSet<String>();
    /** Main directory of ftp client directories */
    static File mainFtpDir;

    /** Log file listing all exceptions in this session */
    static File exceptionLog;
    /** BufferedWriter to exception log file */
    static BufferedWriter errorWriter;
    /** SharedServerData object holding common server objects */
    static SharedServerData sharedServerData;
    /** the text file holding all clients */
    static final private String clientListFile = "all_clients.txt";
    /** the text file holding all client name, fileID, privacy and filename */
    static final private String clientToFileMapFile = "client_to_file_map.txt";

    /**
     * loads client name from clientListFile
     */
    static void loadClientNames() {

        try (Reader reader = new InputStreamReader(new FileInputStream(clientListFile), "UTF-8")) {
            BufferedReader br = new BufferedReader(reader);

            String clientName;
            while (true) {
                clientName = br.readLine();
                if (clientName == null)
                    break;
                allClients.add(clientName);
            }
        } catch (IOException e) {
            addToErrors(e);
        }
    }

    /**
     * @param clientName the new client name to be added to the set of all client
     *                   and the file of all client
     * @param outputFile the flie listing all client names
     */
    static void addToClientList(String clientName) {
        try {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(clientListFile, true));
            fileWriter.append(clientName);
            fileWriter.newLine();
            allClients.add(clientName);
            fileWriter.close();
        } catch (IOException e) {
            addToErrors(e);
            ;
        }
    }

    /**
     * @param e exception to be traced in the exception log file
     */
    static void addToErrors(Exception e) {
        try {
            errorWriter = new BufferedWriter(new FileWriter(exceptionLog, true));
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
     * makes the initial parent directory for all client directory(if not present
     * already) and creates the log file for all exceptions, all client list file
     * and client to file map file(if not present already)
     * 
     * @return true if successful, false otherwise
     */
    static boolean makeInitDirectory() {
        exceptionLog = new File("error_log.txt");
        mainFtpDir = new File("ftpDir");
        File clientListText = new File(clientListFile);
        File clientToFileMap = new File(clientToFileMapFile);

        try {
            if (exceptionLog.exists()) {
                exceptionLog.delete();
            }
            exceptionLog.createNewFile();
            mainFtpDir.mkdirs();
            clientListText.createNewFile();
            clientToFileMap.createNewFile();
            return true;
        } catch (Exception e) {
            System.out.println("error in making necessary files and directories, aborting");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * writes the given client to file mapping in clientToFileMapFile
     * 
     * @param clientToFileIDMap Hashtable mapping String client names to Hashtable
     *                          of String fileIDs and FileInfo objects
     * @return true if successful, false in any exceptions
     */
    static boolean writeFileTreeToStore(Hashtable<String, Hashtable<String, FileInfo>> clientToFileIDMap) {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(clientToFileMapFile, false));) {

            for (String clientName : clientToFileIDMap.keySet()) {
                for (String fileID : clientToFileIDMap.get(clientName).keySet()) {
                    FileInfo tempFileInfo = clientToFileIDMap.get(clientName).get(fileID);
                    fileWriter.write(clientName + ":" + fileID + ":" + (tempFileInfo.isPrivate ? "true" : "false") + ":"
                            + tempFileInfo.file.getName());
                    fileWriter.newLine();
                }
            }
            return true;
        } catch (Exception e) {
            addToErrors(e);
            return false;
        }
    }

    /**
     * returns Hashtable mapping String client name to a Hashtable of String fileID
     * and FileInfo, by reading the txt directory list file and checking if listed
     * files exist
     * 
     * @return Hashtable mapping String client name to a Hashtable of String fileID
     *         and FileInfo objects for file if successful, null otherwise
     */
    static Hashtable<String, Hashtable<String, FileInfo>> makeFileTreeFromStore() {
        File clientToFileMap = new File("client_to_file_map.txt");
        if (clientToFileMap.exists()) {
            Hashtable<String, Hashtable<String, FileInfo>> clientToFileListMap = new Hashtable<String, Hashtable<String, FileInfo>>();
            Set<String> processedUser = new HashSet<String>();

            try (Reader reader = new InputStreamReader(new FileInputStream(clientToFileMap), "UTF-8")) {
                BufferedReader br = new BufferedReader(reader);

                String line;
                while (true) {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }

                    String[] fileData = line.split(":");
                    String client = fileData[0], fileID = fileData[1], fileName = fileData[3];

                    boolean privacy = (fileData[2].equalsIgnoreCase("true") ? true : false);

                    File tempFile = new File(mainFtpDir.getName() + "\\" + client + "\\" + fileName);

                    if (tempFile.exists()) {
                        if (!processedUser.contains(client)) {
                            processedUser.add(client);
                            clientToFileListMap.put(client, new Hashtable<String, FileInfo>());
                        }

                        clientToFileListMap.get(client).put(fileID, new FileInfo(privacy, tempFile));
                    }
                }
                return clientToFileListMap;
            } catch (IOException e) {
                addToErrors(e);
                ;
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * recieves and responses to client Login request
     * 
     * @param clientMessage first ClientMessage from client
     * @param out           ObjectOutputStream to client Socket
     * @param in            ObjectInputStream to client Socket
     * @param socket        Socket to client
     * @return true if successful login conditions met, false otherwise or in any
     *         exception
     */
    static boolean handleLoginRequest(ClientMessage clientMessage, ObjectOutputStream out, ObjectInputStream in,
            Socket socket) {

        if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.LOGIN_REQUEST) {
            ServerMessage serverMessage = new ServerMessage(SERVER_PACKET_TYPE.LOGIN_RESPONSE);

            try {
                if (!activeClients.contains(clientMessage.getClientName())) {
                    serverMessage.putMessage("Success");
                    out.writeObject(serverMessage);

                    activeClients.add(clientMessage.getClientName());
                    if (!allClients.contains(clientMessage.getClientName())) {
                        addToClientList(clientMessage.getClientName());
                    }

                    sharedServerData.clientToFileIDMap.put(clientMessage.getClientName(),
                            new Hashtable<String, FileInfo>());
                    return true;
                } else {
                    serverMessage.putMessage("A client already logged in with the name: "
                            + clientMessage.getClientName() + ". Closing connection");
                    out.writeObject(serverMessage);
                    return false;
                }
            } catch (IOException e) {
                addToErrors(e);
                return false;
            }
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (!makeInitDirectory()) {
            return;
        }
        loadClientNames();

        // make updated fileTree
        Hashtable<String, Hashtable<String, FileInfo>> tempClientToFileIDMap = makeFileTreeFromStore();
        writeFileTreeToStore(tempClientToFileIDMap);

        sharedServerData = new SharedServerData(allClients, activeClients, mainFtpDir, exceptionLog, errorWriter);
        sharedServerData.setClientToFileIDMap(tempClientToFileIDMap);

        sharedServerData.setClientToFileIDMapFile(new File(clientToFileMapFile));

        try (
                ServerSocket welcomeSocket = new ServerSocket(6666);
                Scanner scanner = new Scanner(System.in);) {

            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = welcomeSocket.accept();

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                ClientMessage clientMessage = (ClientMessage) in.readObject();

                boolean loginState = handleLoginRequest(clientMessage, out, in, socket);
                if (loginState) {

                    Thread worker = new Worker(sharedServerData, clientMessage.getClientName(), socket, in, out);
                    worker.start();
                } else {
                    socket.close();
                }

            }
        } catch (Exception e) {
            addToErrors(e);
        }

    }
}
