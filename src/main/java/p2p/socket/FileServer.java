package p2p.socket;

import com.omerucel.socket.ServerAbstract;
import p2p.WindowClient;
import p2p.WindowClientStart;
import p2p.socket.fileserver.ServerEventHandler;

public class FileServer extends ServerAbstract{

    private WindowClientStart windowClientStart;
    private WindowClient windowClient;

    public FileServer(int port, WindowClientStart windowClientStart,
            WindowClient windowClient)
    {
        super(port);
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

    public void setUp() {
        setEventHandler(new ServerEventHandler(this));
    }

}
