package p2p.socket;

import com.omerucel.socket.ClientAbstract;
import p2p.WindowClient;
import p2p.WindowClientStart;
import p2p.socket.mainserver.ConnectionEventHandler;

public class MainServerConnection extends ClientAbstract{
    private WindowClientStart windowClientStart;
    private WindowClient windowClient;

    public MainServerConnection(String host, int port,
            WindowClientStart windowClientStart,
            WindowClient windowClient)
    {
        super(host, port);
        this.windowClientStart = windowClientStart;
        this.windowClient = windowClient;
    }

    public WindowClientStart getWindowClientStart()
    {
        return this.windowClientStart;
    }

    public WindowClient getWindowClient()
    {
        return this.windowClient;
    }

    public void setUp()
    {
        setEventHandler(new ConnectionEventHandler(this));
    }
}
