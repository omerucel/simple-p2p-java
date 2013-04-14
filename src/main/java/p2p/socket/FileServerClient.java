package p2p.socket;

import com.omerucel.socket.ClientAbstract;
import java.net.Socket;
import p2p.socket.fileserver.ClientEventHandler;

public class FileServerClient extends ClientAbstract{

    private FileServer fileServer;

    public FileServerClient(FileServer fileServer, Socket socket)
    {
        super(socket);
        this.fileServer = fileServer;
    }

    public FileServer getFileServer()
    {
        return this.fileServer;
    }

    public void setUp() {
        setEventHandler(new ClientEventHandler(this));
    }

}
