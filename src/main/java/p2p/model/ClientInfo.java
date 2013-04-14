package p2p.model;

public class ClientInfo {
    private String hash;
    private String ip;
    private int port;

    public ClientInfo(String hash, String ip)
    {
        this.hash = hash;
        this.ip = ip;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getHash()
    {
        return this.hash;
    }

    public String getIp()
    {
        return this.ip;
    }

    public int getPort()
    {
        return this.port;
    }
}
