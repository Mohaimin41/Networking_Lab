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

class ServerData {
    /** the text file holding all clients */
    private String clientListFile = "all_clients.txt";
    /** Maximum file chunk size in bytes */
    private int MAX_CHUNK_SIZE = 1000;
    /** Minimum file chunk size in bytes */
    private int MIN_CHUNK_SIZE = 100;
    /** Maximum file buffer size used in Server */
    private int MAX_BUFFER_SIZE = 50000000;
    /** Set of all clients names */
    Set<String> allClients = new HashSet<String>();
    /** Set of all active clients names */
    Set<String> activeClients = new HashSet<String>();
    /** Main directory of ftp client directories */
    File mainFtpDir;

    /** Log file listing all exceptions in this session */
    File exceptionLog;
    /** BufferedWriter to exception log file */
    BufferedWriter errorWriter;
    /** HashTable mapping clientName to Set of client's fileIDs */
    Hashtable<String, Set<String>> clientToFileIDMap = new Hashtable<String, Set<String>>();

}

class Server {
    /** the text file holding all clients */
    static private String clientListFile = "all_clients.txt";
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

    /**
     * @param fileName the file listing all clients
     */
    static void loadClientNames(String fileName) {
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
            System.out.println(e);
        }
    }

    /**
     * @param outputFile the flie listing all client names
     * @param clientName the new client name to be added to the set of all client
     *                   and the file of all client
     */
    static void addToClientList(String outputFile, String clientName) {
        try {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile, true));
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

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        loadClientNames("all_clients.txt");

        exceptionLog = new File("error_log.txt");
        mainFtpDir = new File("ftpDir");
        try {
            exceptionLog.createNewFile();
            mainFtpDir.mkdirs();
        } catch (Exception e) {
            System.out.println("error in making necessary files and directories");
            e.printStackTrace();
            return;
        }

        try (
                ServerSocket welcomeSocket = new ServerSocket(6666);
                Scanner scanner = new Scanner(System.in);) {

            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = welcomeSocket.accept();

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                ClientMessage clientMessage = (ClientMessage) in.readObject();

                if (clientMessage.client_PACKET_TYPE == CLIENT_PACKET_TYPE.LOGIN_REQUEST) {
                    ServerMessage serverMessage = new ServerMessage(SERVER_PACKET_TYPE.LOGIN_RESPONSE);

                    if (!activeClients.contains(clientMessage.getClientName())) {
                        serverMessage.putMessage("Success");
                        out.writeObject(serverMessage);

                        activeClients.add(clientMessage.getClientName());
                        if (!allClients.contains(clientMessage.getClientName())) {
                            addToClientList(clientListFile, clientMessage.getClientName());
                        }
                        // open thread
                        Thread worker = new Worker(socket, activeClients, allClients, exceptionLog,
                                clientMessage.getClientName(), mainFtpDir);
                        worker.start();

                    } else {
                        serverMessage.putMessage("A client already logged in with the name: "
                                + clientMessage.getClientName() + ". Closing connection");
                        out.writeObject(serverMessage);
                        socket.close();
                        ;
                    }
                }

            }
        } catch (Exception e) {
            addToErrors(e);
        }

    }
}
