package p2p.socket.mainserver;

import com.omerucel.socket.IClientEventHandler;
import java.util.Map;
import p2p.DialogLoading;
import p2p.ObjectContainer;
import p2p.socket.MainServerConnection;
import p2p.socket.RequestPortUpdate;
import p2p.socket.ResponseAddFile;
import p2p.socket.ResponseConnection;
import p2p.socket.ResponseFileClients;
import p2p.socket.ResponsePortUpdate;
import p2p.socket.ResponseSearch;

public class ConnectionEventHandler implements IClientEventHandler{

    private MainServerConnection mainServerConnection;

    public ConnectionEventHandler(MainServerConnection mainServerConnection)
    {
        this.mainServerConnection = mainServerConnection;
    }

    public MainServerConnection getMainServerConnection()
    {
        return this.mainServerConnection;
    }

    public void handleConnected() {
    }

    public void handleConnectionFailed(Exception ex) {
        DialogLoading.getInstance().toggle(false);

        getMainServerConnection()
                .getWindowClientStart()
                .showErrorMessage("Sunucuya bağlanılırken bir sorun oluştu!");
    }

    public void handleDisconnected() {
        if (ObjectContainer.getFileServer() != null
                && ObjectContainer.getFileServer().isOpen())
        {
            ObjectContainer.getFileServer().stop();
            return;
        }else{
            DialogLoading.getInstance().toggle(false);

            getMainServerConnection()
                    .getWindowClient()
                    .showErrorMessage("Ana sunucu ile olan bağlantı koptu.");

            getMainServerConnection()
                    .getWindowClient()
                    .reset();
            getMainServerConnection()
                    .getWindowClient()
                    .setVisible(false);
            getMainServerConnection()
                    .getWindowClientStart()
                    .setVisible(true);
        }
    }

    public void handle(Object message) {
        if (message instanceof ResponseConnection)
        {
            ResponseConnection response = (ResponseConnection)message;

            int port = getMainServerConnection()
                    .getWindowClientStart()
                    .getClientPort();

            RequestPortUpdate requestPortUpdate = new RequestPortUpdate(port);
            getMainServerConnection().writeObject(requestPortUpdate);
        }else if(message instanceof ResponsePortUpdate){
            ResponsePortUpdate response = (ResponsePortUpdate)message;

            DialogLoading.getInstance().toggle(false);

            getMainServerConnection()
                    .getWindowClientStart()
                    .setVisible(false);
            getMainServerConnection()
                    .getWindowClient()
                    .setVisible(true);
        }else if(message instanceof ResponseAddFile){
            DialogLoading.getInstance().toggle(false);
        }else if(message instanceof ResponseSearch){
            ResponseSearch response = (ResponseSearch)message;

            for(Map temp : response.getFiles())
            {
                getMainServerConnection()
                        .getWindowClient()
                        .getSearchManager()
                        .addFile(
                            temp.get("hash").toString(),
                            temp.get("name").toString(),
                            temp.get("file_type").toString(),
                            Integer.parseInt(temp.get("size").toString()),
                            Integer.parseInt(temp.get("client_count").toString()));
            }

            DialogLoading.getInstance().toggle(false);
        }else if(message instanceof ResponseFileClients){
            ResponseFileClients response = (ResponseFileClients)message;

            getMainServerConnection()
                    .getWindowClient()
                    .showClientSelectDialog(
                        response.getFileHash(),
                        response.getFileClients());
        }else if(message instanceof Exception){
            Exception exception = (Exception)message;
            exception.printStackTrace();
        }
    }
}
