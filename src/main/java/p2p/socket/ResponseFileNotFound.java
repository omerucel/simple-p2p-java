package p2p.socket;

import com.omerucel.socket.message.IResponse;

public class ResponseFileNotFound implements IResponse{
    private String hash;

    public ResponseFileNotFound(String hash)
    {
        this.hash = hash;
    }

    public String getHash()
    {
        return this.hash;
    }
}
