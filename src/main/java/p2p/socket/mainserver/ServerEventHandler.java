package p2p.socket.mainserver;

import com.omerucel.socket.IServerEventHandler;
import java.net.Socket;
import p2p.DialogLoading;
import p2p.socket.MainServer;
import p2p.socket.MainServerClient;

public class ServerEventHandler implements IServerEventHandler{
    private MainServer mainServer;

    public ServerEventHandler(MainServer mainServer)
    {
        this.mainServer = mainServer;
    }

    public MainServer getMainServer()
    {
        return this.mainServer;
    }

    public void handleStartingFailed(Exception ex) {
        DialogLoading.getInstance().toggle(false);
        getMainServer()
                .getWindowMainServerStart()
                .showErrorMessage("Sunucu başlatılırken bir sorun oluştu!");
    }

    public void handleStarted() {
        DialogLoading.getInstance().toggle(false);
        getMainServer()
                .getWindowMainServerStart().setVisible(false);
        getMainServer()
                .getWindowMainServer().setVisible(true);
        getMainServer()
                .getWindowMainServer()
                .addLog("Sunucu bağlantıları kabul etmeye başladı.");
    }

    public void handleClientConnected(Socket socket) {
        new Thread(new MainServerClient(getMainServer(), socket)).start();
    }

    public void handleClientConnectionFailed(Exception ex) {
        getMainServer()
                .getWindowMainServer()
                .addLog("Bir istemci sunucuya bağlanamadı.");
    }

    public void handleClosed() {
        DialogLoading.getInstance().toggle(false);
        getMainServer()
                .getWindowMainServer()
                .showErrorMessage("Sunucu kapandı!");
        getMainServer()
                .getWindowMainServer()
                .setVisible(false);
        getMainServer()
                .getWindowMainServerStart()
                .setVisible(true);
    }

}
