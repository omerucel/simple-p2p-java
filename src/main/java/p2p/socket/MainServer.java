package p2p.socket;

import com.omerucel.socket.ServerAbstract;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import p2p.WindowMainServer;
import p2p.WindowMainServerStart;
import p2p.model.ClientFileInfo;
import p2p.model.ClientInfo;
import p2p.model.FileInfo;
import p2p.socket.mainserver.ServerEventHandler;

public class MainServer extends ServerAbstract{

    private WindowMainServer windowMainServer;
    private WindowMainServerStart windowMainServerStart;
    private HashMap<String, FileInfo> files;
    private HashMap<String, ClientInfo> clients;

    public MainServer(int port, WindowMainServerStart windowMainServerStart,
            WindowMainServer windowMainServer)
    {
        super(port);
        this.windowMainServerStart = windowMainServerStart;
        this.windowMainServer = windowMainServer;
        this.files = new HashMap<String, FileInfo>();
        this.clients = new HashMap<String, ClientInfo>();
    }

    public WindowMainServerStart getWindowMainServerStart()
    {
        return this.windowMainServerStart;
    }

    public WindowMainServer getWindowMainServer()
    {
        return this.windowMainServer;
    }

    public void setUp()
    {
        setEventHandler(new ServerEventHandler(this));
    }

    public synchronized void addClient(String hash, String ip)
    {
        getWindowMainServer().addLog("İstemci(" + hash + ") ekleniyor...");

        ClientInfo clientInfo = new ClientInfo(hash, ip);
        clients.put(clientInfo.getHash(), clientInfo);

        getWindowMainServer().addLog("İstemci(" + hash + ") eklendi.");
        getWindowMainServer().addClient(hash, ip);
    }

    public synchronized void updateClientPort(String hash, int port)
    {
        getWindowMainServer()
                .addLog("İstemci(" + hash + ") portu(" + port + ") güncelleniyor...");

        if (clients.containsKey(hash))
        {
            clients.get(hash).setPort(port);

            getWindowMainServer()
                    .addLog("İstemci(" + hash + ") port numarası(" + port + ") güncellendi.");

            getWindowMainServer()
                    .updateClientServerPort(hash, port);
        }
    }

    public synchronized void removeClient(String hash)
    {
        getWindowMainServer().addLog("İstemci(" + hash + ") siliniyor...");

        if (clients.containsKey(hash))
        {
            ClientInfo clientInfo = clients.get(hash);
            ArrayList<String> keys = new ArrayList<String>();
            for(FileInfo fileInfo : files.values())
            {
                fileInfo.removeClient(clientInfo);
                if (!fileInfo.hasClientFile())
                    keys.add(fileInfo.getHash());

                getWindowMainServer().removeFile(fileInfo.getHash(),
                        fileInfo.getClientCount());
            }

            for(String key : keys)
            {
                files.remove(key);
            }

            clients.remove(hash);

            getWindowMainServer().addLog("İstemci(" + hash + ") silindi.");
            getWindowMainServer().addLog("İstemciye(" + hash + ") bağlı " + keys.size() + " dosya silindi.");
            getWindowMainServer().removeClient(hash);
        }
    }

    public synchronized FileInfo addFile(String clientHash, String hash, String name
            , int size, String fileType)
    {
        getWindowMainServer()
                .addLog("Dosya (" + hash + ") ekleniyor...");

        if (!clients.containsKey(clientHash)) throw new NullPointerException();

        ClientInfo clientInfo = clients.get(clientHash);

        FileInfo fileInfo;
        if (files.containsKey(hash))
        {
            fileInfo = files.get(hash);
            if (fileInfo.hasClientFile(clientInfo)) throw new NullPointerException();
        }else{
            fileInfo = new FileInfo(hash, fileType, size);
        }

        ClientFileInfo clientFileInfo = new ClientFileInfo(clientInfo, name);
        fileInfo.addClientFile(clientFileInfo);
        files.put(hash, fileInfo);

        getWindowMainServer()
                .addLog("Dosya(" + fileInfo.getHash() + " eklendi.");

        getWindowMainServer().addFile(
            fileInfo.getHash(), 
            fileInfo.getFileType(), 
            fileInfo.getSize(), 
            fileInfo.getClientCount());

        return fileInfo;
    }

    public synchronized void removeFile(String clientHash, String hash)
    {
        getWindowMainServer()
                .addLog("Dosya(" + hash + ") siliniyor...");

        if (!clients.containsKey(clientHash)) return;
        if (!files.containsKey(hash)) return;

        ClientInfo clientInfo = clients.get(clientHash);
        FileInfo fileInfo = files.get(hash);

        fileInfo.removeClient(clientInfo);

        if (!fileInfo.hasClientFile())
            files.remove(fileInfo.getHash());

        getWindowMainServer()
                .addLog("Dosya(" + hash + " silindi.");

        getWindowMainServer().removeFile(
                hash, getFileClientCount(hash));
    }

    public int getFileClientCount(String hash)
    {
        if (!files.containsKey(hash)) return 0;

        return files.get(hash).getClientCount();
    }

    public ArrayList<Map> searchFile(String name)
    {
        getWindowMainServer()
                .addLog("Arama istedği (" + name + ")...");

        ArrayList<Map> searchResult = new ArrayList<Map>();

        for(FileInfo fileInfo : files.values())
        {
            ArrayList<Map> tempResult = fileInfo.search(name);
            if (tempResult.size() > 0)
                searchResult.addAll(tempResult);
        }

        getWindowMainServer()
                .addLog("Arama istedği (" + name + ")... " + searchResult.size() + " dosya bulundu.");

        return searchResult;
    }

    public ArrayList<Map> getFileClients(String hash)
    {
        getWindowMainServer()
                .addLog("Dosya(" + hash + ") istemcileri alınıyor...");

        ArrayList<Map> result = new ArrayList<Map>();
        if (files.containsKey(hash))
            result = files.get(hash).getClients();

        getWindowMainServer()
                .addLog("Dosya(" + hash + ") istemcileri alınıyor... " + result.size() + " istemci bulundu.");

        return result;
    }
}
