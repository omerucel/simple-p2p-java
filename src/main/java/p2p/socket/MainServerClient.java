package p2p.socket;

import com.omerucel.socket.ClientAbstract;
import java.net.Socket;
import p2p.socket.mainserver.ClientEventHandler;

public class MainServerClient extends ClientAbstract{
    private MainServer mainServer;

    public MainServerClient(MainServer mainServer, Socket socket)
    {
        super(socket);
        this.mainServer = mainServer;
    }

    public MainServer getMainServer()
    {
        return this.mainServer;
    }

    public void setUp()
    {
        setEventHandler(new ClientEventHandler(this));
    }
}
