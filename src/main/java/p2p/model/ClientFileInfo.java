package p2p.model;

public class ClientFileInfo {
    private ClientInfo clientInfo;
    private String fileName;

    public ClientFileInfo(ClientInfo clientInfo, String fileName)
    {
        this.clientInfo = clientInfo;
        this.fileName = fileName;
    }

    public ClientInfo getClientInfo()
    {
        return clientInfo;
    }

    public String getFileName()
    {
        return fileName;
    }
}
