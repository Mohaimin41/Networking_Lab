import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

class Client {
    /**
     * The array of available command Strings: download, files, logout, ls,
     * messages, request, upload, users
     */
    static private String[] commands = { "download", "files", "ls", "logout", "messages", "request", "upload",
            "users" };

    /**
     * sends request, recieves and outputs ServerFtpMessage data according to
     * CLIENT_PACKET_TYPE
     * 
     * @param client_PACKET_TYPE the CLIENT_PACKET_TYPE, of listing varieties: all
     *                           files, user files, users
     * @param clientName         the String clientName
     * @param out                ObjectOutputStream to send FtpMessage
     * @param in                 ObjectInputStream to recieve FtpMessage
     */
    static void listItemsFromServer(CLIENT_PACKET_TYPE client_PACKET_TYPE, String clientName, ObjectOutputStream out,
            ObjectInputStream in) {
        try {
            ClientMessage clientMessage = new ClientMessage(client_PACKET_TYPE, clientName);
            out.writeObject(clientMessage);

            ServerMessage serverMessage = (ServerMessage) in.readObject();

            if (client_PACKET_TYPE == CLIENT_PACKET_TYPE.CLIENT_LIST_REQUEST
                    && serverMessage.server_PACKET_TYPE == SERVER_PACKET_TYPE.CLIENT_LIST) {
                System.out.println("All clients:\n" + serverMessage.getMessage());
            } else if (client_PACKET_TYPE == CLIENT_PACKET_TYPE.FILE_LIST_REQUEST
                    && serverMessage.server_PACKET_TYPE == SERVER_PACKET_TYPE.FILE_LIST) {
                System.out.println("Your files stored in server:\n" + serverMessage.getMessage());
            } else if (client_PACKET_TYPE == CLIENT_PACKET_TYPE.ALL_FILE_LIST_REQUEST
                    && serverMessage.server_PACKET_TYPE == SERVER_PACKET_TYPE.ALL_FILE_LIST) {
                System.out.println("All public files in the server:\n" + serverMessage.getMessage());
            } else {
                System.out.println("Internal Server/Client error, please retry.");
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.getMessage());
        }
    }

    static boolean sendLogoutRequest(ObjectOutputStream out, ObjectInputStream in, String clientName) {
        ServerMessage serverMessage;
        try {
            ClientMessage clientMessage = new ClientMessage(CLIENT_PACKET_TYPE.LOGOUT_REQUEST, clientName);
            out.writeObject(clientMessage);
            serverMessage = (ServerMessage) in.readObject();
            if (serverMessage.server_PACKET_TYPE == SERVER_PACKET_TYPE.LOGOUT_RESPONSE) {
                if (serverMessage.getMessage().equalsIgnoreCase("Success")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    static boolean sendFileRequest(ObjectOutputStream out, ObjectInputStream in, String clientName,
            String description) {
        ClientMessage clientMessage = new ClientMessage(CLIENT_PACKET_TYPE.FILE_REQUEST, clientName);
        clientMessage.putMessage(description);

        try {
            out.writeObject(clientMessage);

            ServerMessage serverMessage = (ServerMessage) in.readObject();

            if (serverMessage.server_PACKET_TYPE == SERVER_PACKET_TYPE.IMMEDIATE_FILE_REQUEST_RESPONSE) {
                if (serverMessage.getMessage().equalsIgnoreCase("Success")) {

                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Arrays.sort(commands);
        try (Socket socket = new Socket("localhost", 6666); Scanner scanner = new Scanner(System.in);) {

            // buffers
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            boolean loginComplete = false;

            String clientName = "";

            System.out.println("Enter username:");
            while (true) {
                if (scanner.hasNextLine()) {
                    clientName = scanner.nextLine();
                    ClientMessage clientMessage = new ClientMessage(CLIENT_PACKET_TYPE.LOGIN_REQUEST, clientName);
                    clientMessage.putClientName(clientName);
                    out.writeObject(clientMessage);
                    ServerMessage serverMessage = (ServerMessage) in.readObject();
                    if ((serverMessage.server_PACKET_TYPE == SERVER_PACKET_TYPE.LOGIN_RESPONSE)) {
                        if (serverMessage.getMessage().equalsIgnoreCase("Success")) {
                            System.out.println("Logged in as " + clientName);
                            loginComplete = true;

                        } else {
                            System.out.println("Failure: " + serverMessage.getMessage());
                        }
                        break;
                    }
                }
            }

            if (loginComplete) {
                for (int i = 0; i < 30; i++) {
                    System.out.print("=");
                }
                System.out.println("");
                System.out.println("Available Commands:\n" +
                        "ls \tlist all of your files in Server\n" +
                        "users \tlist all the users of Server\n" +
                        "files \tlist all publice files in Server\n" +
                        "request \trequest for a file with its description\n" +
                        "download \tstart downloading a file using its fileID\n" +
                        "upload \tinitiate upload request for a file\n" +
                        "messages \tview all unread messages\n" +
                        "logout \tlogout from Server");
            }
            while (loginComplete) {

                System.out.println("Enter Command:");
                String userCommand = "";
                if (scanner.hasNextLine()) {
                    userCommand = scanner.nextLine();
                }

                int commandIndex = Arrays.binarySearch(commands, 0, 8, userCommand);

                if (commandIndex < 0) {
                    System.out.println("Enter a valid command:");
                    continue;
                } else if (commandIndex == 0) {// download

                } else if (commandIndex == 1) {// files
                    listItemsFromServer(CLIENT_PACKET_TYPE.ALL_FILE_LIST_REQUEST, clientName, out, in);
                } else if (commandIndex == 2) {// logout
                    // if (logou)
                    if (sendLogoutRequest(out, in, clientName)) {
                        System.out.println("Logout successful");
                        loginComplete = false;
                        break;
                    } else {
                        System.out.println("Logout failed, please try again.");
                    }
                } else if (commandIndex == 3) {// ls
                    listItemsFromServer(CLIENT_PACKET_TYPE.FILE_LIST_REQUEST, clientName, out, in);
                } else if (commandIndex == 4) {// messages
                    // TODO: figure out
                } else if (commandIndex == 5) {// request
                    System.out.println("Enter short file description:");
                    String description = "";
                    if (scanner.hasNextLine()) {
                        description = scanner.nextLine();
                    }

                    if (sendFileRequest(out, in, clientName, description)) {
                        System.out.println("Your request was broadcast to all other users successfully");
                    } else {
                        System.out.println("Your request could not be processed, please try again");

                    }

                } else if (commandIndex == 6) {// upload

                } else if (commandIndex == 7) {// users
                    listItemsFromServer(CLIENT_PACKET_TYPE.CLIENT_LIST_REQUEST, clientName, out, in);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
