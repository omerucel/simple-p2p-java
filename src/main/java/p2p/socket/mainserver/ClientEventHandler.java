package p2p.socket.mainserver;

import com.omerucel.socket.IClientEventHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import p2p.model.FileInfo;
import p2p.socket.MainServerClient;
import p2p.socket.RequestAddFile;
import p2p.socket.RequestFileClients;
import p2p.socket.RequestPortUpdate;
import p2p.socket.RequestRemoveFile;
import p2p.socket.RequestSearch;
import p2p.socket.ResponseAddFile;
import p2p.socket.ResponseConnection;
import p2p.socket.ResponseFileClients;
import p2p.socket.ResponsePortUpdate;
import p2p.socket.ResponseSearch;

public class ClientEventHandler implements IClientEventHandler{
    private MainServerClient mainServerClient;

    public ClientEventHandler(MainServerClient mainServerClient)
    {
        this.mainServerClient = mainServerClient;
    }

    public MainServerClient getMainServerClient()
    {
        return this.mainServerClient;
    }

    public void handleConnected() {
        getMainServerClient().getMainServer().addClient(
                getMainServerClient().getClientIdentity().getId(),
                getMainServerClient().getClientIdentity().getIp());

        getMainServerClient().writeObject(new ResponseConnection());
    }

    public void handleConnectionFailed(Exception ex) {
        getMainServerClient().getMainServer().getWindowMainServer()
                .addLog("İstemci bağlanmaya çalışırken bir sorun oluştu.");
    }

    public void handleDisconnected() {
        getMainServerClient()
                .getMainServer()
                .removeClient(getMainServerClient().getClientIdentity().getId());
    }

    public void handle(Object message) {
        if (message instanceof RequestPortUpdate)
        {
            RequestPortUpdate request = (RequestPortUpdate)message;
            getMainServerClient().getMainServer().updateClientPort(
                    getMainServerClient().getClientIdentity().getId(),
                    request.getPort());
            getMainServerClient().writeObject(new ResponsePortUpdate());
        }else if(message instanceof RequestFileClients){
            RequestFileClients request = (RequestFileClients)message;

            ArrayList<Map> fileClients = getMainServerClient()
                    .getMainServer()
                    .getFileClients(request.getFileHash());

            ResponseFileClients responseFileClients = new ResponseFileClients(
                    request.getFileHash(), fileClients);
            getMainServerClient().writeObject(responseFileClients);
        }else if(message instanceof RequestSearch){
            RequestSearch request = (RequestSearch)message;

            ArrayList<Map> searchResult = getMainServerClient()
                    .getMainServer()
                    .searchFile(request.getName());

            ResponseSearch responseSearch = new ResponseSearch(searchResult);
            getMainServerClient().writeObject(responseSearch);
        }else if(message instanceof RequestAddFile){
            RequestAddFile request = (RequestAddFile)message;

            for(HashMap<String, Object> temp : request.getFiles())
            {
                FileInfo fileInfo = getMainServerClient()
                        .getMainServer()
                        .addFile(
                            getMainServerClient().getClientIdentity().getId(),
                            temp.get("hash").toString(),
                            temp.get("name").toString(),
                            Integer.parseInt(temp.get("size").toString()),
                            temp.get("file_type").toString());
            }

            ResponseAddFile responseAddFile = new ResponseAddFile();
            getMainServerClient().writeObject(responseAddFile);
        }else if(message instanceof RequestRemoveFile){
            RequestRemoveFile request = (RequestRemoveFile)message;

            for(String hash: request.getFiles())
            {
                getMainServerClient()
                        .getMainServer()
                        .removeFile(
                            getMainServerClient().getClientIdentity().getId(),
                            hash);
            }
        }else if(message instanceof Exception){
            Exception exception = (Exception)message;
            exception.printStackTrace();
        }
    }
}
