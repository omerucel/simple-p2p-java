package p2p.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileInfo {
    private String hash;
    private String fileType;
    private int size;
    private HashMap<String, ClientFileInfo> clientFiles;

    public FileInfo(String hash, String fileType, int size)
    {
        this.hash = hash;
        this.fileType = fileType;
        this.size = size;
        this.clientFiles = new HashMap<String, ClientFileInfo>();
    }

    public String getHash()
    {
        return this.hash;
    }

    public String getFileType()
    {
        return this.fileType;
    }

    public int getSize()
    {
        return this.size;
    }

    public void addClientFile(ClientFileInfo clientFileInfo)
    {
        if (!clientFiles.containsKey(clientFileInfo.getClientInfo().getHash()))
            clientFiles.put(clientFileInfo.getClientInfo().getHash(), clientFileInfo);
    }

    public void removeClientFile(ClientFileInfo clientFileInfo)
    {
        if (clientFiles.containsKey(clientFileInfo.getClientInfo().getHash()))
            clientFiles.remove(clientFileInfo.getClientInfo().getHash());
    }

    public void removeClient(ClientInfo clientInfo)
    {
        clientFiles.remove(clientInfo.getHash());
    }

    public Boolean hasClientFile()
    {
        return clientFiles.size() > 0;
    }

    public Boolean hasClientFile(ClientInfo clientInfo)
    {
        return clientFiles.containsKey(clientInfo.getHash());
    }

    public int getClientCount()
    {
        return clientFiles.size();
    }

    public ArrayList<Map> search(String name)
    {
        ArrayList<String> tempCheckList = new ArrayList<String>();
        ArrayList<Map> result = new ArrayList<Map>();
        for(ClientFileInfo clientFileInfo : clientFiles.values())
        {
            if (clientFileInfo.getFileName().indexOf(name) > -1
                    && !tempCheckList.contains(clientFileInfo.getFileName()))
            {
                tempCheckList.add(clientFileInfo.getFileName());

                Map temp = new LinkedHashMap();
                temp.put("name", clientFileInfo.getFileName());
                temp.put("client_count", clientFiles.size());
                temp.put("hash", getHash());
                temp.put("size", getSize());
                temp.put("file_type", getFileType());
                result.add(temp);
            }
        }

        return result;
    }

    public ArrayList<Map> getClients()
    {
        ArrayList<Map> result = new ArrayList<Map>();
        for(ClientFileInfo clientFileInfo : clientFiles.values())
        {
            Map temp = new LinkedHashMap();
            temp.put("ip", clientFileInfo.getClientInfo().getIp());
            temp.put("port", clientFileInfo.getClientInfo().getPort());
            result.add(temp);
        }

        return result;
    }
}
