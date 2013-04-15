package p2p.socket;

import com.omerucel.socket.message.IResponse;

public class ResponseDownloadPart implements IResponse{
    private String hash;
    private int part;
    private Integer data[];

    public ResponseDownloadPart(String hash, int part, Integer data[])
    {
        this.hash = hash;
        this.part = part;
        this.data = data;
    }

    public String getHash()
    {
        return this.hash;
    }

    public int getPart()
    {
        return this.part;
    }

    public Integer[] getData()
    {
        return this.data;
    }
}
