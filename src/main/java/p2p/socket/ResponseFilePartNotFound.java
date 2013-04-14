package p2p.socket;

import com.omerucel.socket.message.IResponse;

public class ResponseFilePartNotFound implements IResponse{
    private String hash;
    private int part;

    public ResponseFilePartNotFound(String hash, int part)
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
