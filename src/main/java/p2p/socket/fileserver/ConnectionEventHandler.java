package p2p.socket.fileserver;

import com.omerucel.socket.IClientEventHandler;
import java.io.FileNotFoundException;
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
        new Thread(new Runnable() {

            public void run() {
                getFileServerConnection()
                        .getWindowClient()
                        .setConnectedClientNumberOnTable(
                            getFileServerConnection().getFileHash(), true);
            }
        }).start();
    }

    public void addLog(final String message)
    {
        new Thread(new Runnable() {

            public void run() {
                getFileServerConnection()
                        .getWindowClient()
                        .addLog("Dosya("
                            + getFileServerConnection().getFileHash() 
                            + ") Sunucu("
                            + getFileServerConnection().getHost()
                            + ":" + getFileServerConnection().getPort()
                            + ") : " + message);
            }
        }).start();
    }

    public void handleConnectionFailed(Exception ex) {
        addLog("Bağlanırken bir sorun oluştu : " + ex.getMessage());
    }

    public void handleDisconnected() {
        addLog("Bağlantı sonlandı.");
        new Thread(new Runnable() {

            public void run() {
                getFileServerConnection()
                        .getWindowClient()
                        .setConnectedClientNumberOnTable(
                            getFileServerConnection().getFileHash(), false);
            }
        }).start();
    }

    public void handle(Object message) {
        if (message instanceof ResponseConnection){
            addLog("Bağlantı sağlandı.");

            int part;
            try {
                part = getFileServerConnection().getDownloadData().getRandomPendingPart();
            } catch (FileNotFoundException ex) {
                addLog("Yeni parça alınırken bir sorun oluştu.");
                return;
            }

            if (part == 0)
            {
                addLog("İndirilecek parça bulunamadı.");
                return;
            }

            RequestDownloadPart request = new RequestDownloadPart(
                    getFileServerConnection().getFileHash(), part);
            getFileServerConnection()
                    .writeObject(request);

            addLog("Parçanın(" + part + ") indirilmesi için istekte bulunuldu.");
        }else if (message instanceof ResponseDownloadPart){
            final ResponseDownloadPart response = (ResponseDownloadPart)message;
            try {
                Boolean isCompleted = getFileServerConnection()
                        .getDownloadData()
                        .saveDownloadedPartAndReturnIsCompleted(
                            response.getPart(),
                            response.getData());

                addLog("Parça(" + response.getPart() + ") indirildi.");

                new Thread(new Runnable() {

                    public void run() {
                        getFileServerConnection()
                                .getWindowClient()
                                .incrementDownloadedFilePartNumberOnTable(response.getHash());
                    }
                }).start();

                if (!isCompleted)
                {
                    int part;
                    try {
                        part = getFileServerConnection().getDownloadData().getRandomPendingPart();
                    } catch (FileNotFoundException ex) {
                        addLog("Yeni parça alınırken bir sorun oluştu.");
                        return;
                    }

                    RequestDownloadPart request = new RequestDownloadPart(
                            getFileServerConnection().getFileHash(),
                            part);
                    getFileServerConnection().writeObject(request);

                    addLog("Parçanın(" + part + ") indirilmesi için istekte bulunuldu.");
                }else{
                    addLog("Tüm parçalar indirildi.");

                    new Thread(new Runnable() {

                        public void run() {
                            getFileServerConnection().disconnect();
                            try {
                                getFileServerConnection()
                                        .getWindowClient()
                                        .downloadedFile(response.getHash());
                            } catch (FileNotFoundException ex) {
                                addLog("İndirilen dosya paylaşılan dosyalar arasına eklenirken bir sorun oluştu : " + ex.getMessage());
                            }
                        }
                    }).start();
                }
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
