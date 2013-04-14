package p2p.socket;

import com.omerucel.socket.message.IRequest;

public class RequestSearch implements IRequest{
    private String name;

    public RequestSearch(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }
}
