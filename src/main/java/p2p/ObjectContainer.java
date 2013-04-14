package p2p;

import p2p.socket.FileServer;
import p2p.socket.MainServer;
import p2p.socket.MainServerConnection;

public class ObjectContainer {
    private static MainServer mainServer = null;
    private static MainServerConnection mainServerConnection = null;
    private static FileServer fileServer = null;

    public static MainServer getMainServer() {
        return mainServer;
    }

    public static void setMainServer(MainServer mainServer) {
        ObjectContainer.mainServer = mainServer;
    }

    public static FileServer getFileServer() {
        return fileServer;
    }

    public static void setFileServer(FileServer fileServer) {
        ObjectContainer.fileServer = fileServer;
    }

    public static MainServerConnection getMainServerConnection() {
        return mainServerConnection;
    }

    public static void setMainServerConnection(MainServerConnection mainServerConnection) {
        ObjectContainer.mainServerConnection = mainServerConnection;
    }

}
