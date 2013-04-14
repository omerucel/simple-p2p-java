package p2p.socket;

import com.omerucel.socket.message.IRequest;

public class RequestFileClients implements IRequest{
    private String fileHash;

    public RequestFileClients(String fileHash)
    {
        this.fileHash = fileHash;
    }

    public String getFileHash()
    {
        return this.fileHash;
    }
}
