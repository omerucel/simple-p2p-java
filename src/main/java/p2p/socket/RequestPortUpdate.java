package p2p.socket;

import com.omerucel.socket.message.IRequest;

public class RequestPortUpdate implements IRequest{
    private int port;

    public RequestPortUpdate(int port)
    {
        this.port = port;
    }

    public int getPort()
    {
        return this.port;
    }
}
