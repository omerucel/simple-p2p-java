package p2p.socket;

import com.omerucel.socket.message.IRequest;

public class RequestDownloadPart implements IRequest{
    private String hash;
    private int part;

    public RequestDownloadPart(String hash, int part)
    {
        this.hash = hash;
        this.part = part;
    }

    public String getHash()
    {
        return this.hash;
    }

    public int getPart()
    {
        return this.part;
    }
}
