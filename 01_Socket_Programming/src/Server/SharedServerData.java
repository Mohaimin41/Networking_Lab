package src.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Set;

public class SharedServerData implements Serializable{
    /** Set of all clients names */
    Set<String> allClients;
    /** Set of all active clients names */
    Set<String> activeClients;
    /** Main directory of ftp client directories */
    File mainFtpDir;

    /** the main text file listing client names, fileIDs, privacy and File names */
    File clientToFileIDMapFile;
    /** Log file listing all exceptions in this session */
    File exceptionLog;
    /** BufferedWriter to exception log file */
    BufferedWriter errorWriter;
    /** HashTable mapping clientName to Hashtable of clients file */
    Hashtable<String, Hashtable<String, FileInfo>> clientToFileIDMap;

    /**
     * Object holding common server data items
     * 
     * @param allClients    Set of String names of all clients
     * @param activeClients Set of String names of all active client names
     * @param mainFtpDir    File object of parent directory for all client director
     * @param exceptionLog  File to log all exception stack traces
     * @param errorWriter   Writer to exceptionLog file
     */
    public SharedServerData(Set<String> allClients, Set<String> activeClients, File mainFtpDir, File exceptionLog,
            BufferedWriter errorWriter) {
        this.allClients = allClients;
        this.activeClients = activeClients;
        this.mainFtpDir = mainFtpDir;
        this.exceptionLog = exceptionLog;
        this.errorWriter = errorWriter;
    }

    public Set<String> getAllClients() {
        return allClients;
    }

    public void setAllClients(Set<String> allClients) {
        this.allClients = allClients;
    }

    public Set<String> getActiveClients() {
        return activeClients;
    }

    public void setActiveClients(Set<String> activeClients) {
        this.activeClients = activeClients;
    }

    public File getMainFtpDir() {
        return mainFtpDir;
    }

    public void setMainFtpDir(File mainFtpDir) {
        this.mainFtpDir = mainFtpDir;
    }

    public File getExceptionLog() {
        return exceptionLog;
    }

    public void setExceptionLog(File exceptionLog) {
        this.exceptionLog = exceptionLog;
    }

    public BufferedWriter getErrorWriter() {
        return errorWriter;
    }

    public void setErrorWriter(BufferedWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    public Hashtable<String, Hashtable<String, FileInfo>> getClientToFileIDMap() {
        return clientToFileIDMap;
    }

    public void setClientToFileIDMap(Hashtable<String, Hashtable<String, FileInfo>> clientToFileIDMap) {
        this.clientToFileIDMap = clientToFileIDMap;
    }

    public File getClientToFileIDMapFile() {
        return clientToFileIDMapFile;
    }

    public void setClientToFileIDMapFile(File clientToFileIDMapFile) {
        this.clientToFileIDMapFile = clientToFileIDMapFile;
    }

}
