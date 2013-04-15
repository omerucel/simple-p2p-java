package p2p.socket.fileserver;

import com.omerucel.socket.IClientEventHandler;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import p2p.socket.FileServerConnection;
import p2p.socket.RequestDownloadPart;
import p2p.socket.ResponseConnection;
import p2p.socket.ResponseDownloadPart;
import p2p.socket.ResponseFileNotFound;
import p2p.socket.ResponseFilePartNotFound;

public class ConnectionEventHandler implements IClientEventHandler{
    private FileServerConnection fileServerConnection;

    public ConnectionEventHandler(FileServerConnection fileServerConnection)
    {
        this.fileServerConnection = fileServerConnection;
    }

    public FileServerConnection getFileServerConnection()
    {
        return this.fileServerConnection;
    }

    public void handleConnected() {
        getFileServerConnection()
                .getDownloadManager()
                .setConnectedClientNumberOnTable(
                    getFileServerConnection().getFileHash(), true);
    }

    public void addLog(final String message)
    {
        getFileServerConnection()
                .getDownloadManager()
                .addLog("Dosya("
                    + getFileServerConnection().getFileHash() 
                    + ") Sunucu("
                    + getFileServerConnection().getHost()
                    + ":" + getFileServerConnection().getPort()
                    + ") : " + message);
    }

    public void handleConnectionFailed(Exception ex) {
        addLog("Bağlanırken bir sorun oluştu : " + ex.getMessage());

        getFileServerConnection()
                .getDownloadManager()
                .setConnectedClientNumberOnTable(
                    getFileServerConnection().getFileHash(), false);
    }

    public void handleDisconnected() {
        addLog("Bağlantı sonlandı.");

        getFileServerConnection()
                .getDownloadManager()
                .setConnectedClientNumberOnTable(
                    getFileServerConnection().getFileHash(), false);
    }

    private void newDownloadRequest()
    {
        ArrayList<Integer> parts;
        try {
            parts = getFileServerConnection().getDownloadData().getRandomPendingPart(1);
        } catch (FileNotFoundException ex) {
            addLog("Yeni parça alınırken bir sorun oluştu.");
            return;
        }

        if (parts.isEmpty())
        {
            addLog("İndirilecek parça bulunamadı.");
            return;
        }

        for(Integer part : parts)
        {
            RequestDownloadPart request = new RequestDownloadPart(
                    getFileServerConnection().getFileHash(), part);

            getFileServerConnection()
                    .writeObject(request);

            addLog("Parçanın(" + part + ") indirilmesi için istekte bulunuldu.");
        }
    }

    public void handle(Object message) {
        if (message instanceof ResponseConnection){
            addLog("Bağlantı sağlandı.");

            newDownloadRequest();
        }else if (message instanceof ResponseDownloadPart){
            final ResponseDownloadPart response = (ResponseDownloadPart)message;
            try {
                getFileServerConnection()
                        .getDownloadData()
                        .saveDownloadedPartAndReturnIsCompleted(
                            response.getPart(),
                            response.getData());

                addLog("Parça(" + response.getPart() + ") indirildi.");

                newDownloadRequest();
            } catch (FileNotFoundException ex) {
                addLog("Parça(" + response.getPart() + ") kaydedilirken sorun oluştu.");
            }
        }else if(message instanceof ResponseFileNotFound){
            addLog("İstenen dosya sunucuda bulunamadı.");
        }else if(message instanceof ResponseFilePartNotFound){
            ResponseFilePartNotFound response = (ResponseFilePartNotFound)message;
            addLog("İstenen parça(" + response.getPart() + ") sunucuda bulunamadı.");
        }
    }

}
