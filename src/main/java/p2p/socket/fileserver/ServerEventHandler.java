package p2p.socket.fileserver;

import com.omerucel.socket.IServerEventHandler;
import java.net.Socket;
import p2p.DialogLoading;
import p2p.ObjectContainer;
import p2p.socket.FileServer;
import p2p.socket.FileServerClient;
import p2p.socket.MainServerConnection;

public class ServerEventHandler implements IServerEventHandler{

    private FileServer fileServer;

    public ServerEventHandler(FileServer fileServer)
    {
        this.fileServer = fileServer;
    }

    public FileServer getFileServer()
    {
        return this.fileServer;
    }

    public void handleStartingFailed(Exception ex) {
        DialogLoading.getInstance().toggle(false);
        getFileServer()
                .getWindowClientStart()
                .showErrorMessage("Dosya sunucusu başlatılırken bir sorun oluştu.");
    }

    public void handleStarted() {
        MainServerConnection mainServerConnection = new MainServerConnection(
                getFileServer()
                    .getWindowClientStart().getMainServerIp(),
                getFileServer()
                    .getWindowClientStart().getMainServerPort(),
                getFileServer()
                    .getWindowClientStart(),
                getFileServer()
                    .getWindowClient());
        ObjectContainer.setMainServerConnection(mainServerConnection);

        new Thread(mainServerConnection).start();
    }

    public void handleClientConnected(Socket socket) {
        new Thread(new FileServerClient(getFileServer(), socket)).start();
    }

    public void handleClientConnectionFailed(Exception ex) {
        getFileServer()
                .getWindowClient()
                .addLog("İstemci bağlanırken bir sorun yaşandı : " + ex.getMessage());
    }

    public void handleClosed() {
        DialogLoading.getInstance().toggle(false);

        getFileServer()
                .getWindowClient()
                .showErrorMessage("Uygulama kapandı.");

        getFileServer()
                .getWindowClient()
                .reset();
        getFileServer()
                .getWindowClient()
                .setVisible(false);
        getFileServer()
                .getWindowClientStart()
                .setVisible(true);
    }

}
