package p2p.socket;

import com.omerucel.socket.ClientAbstract;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import p2p.DownloadData;
import p2p.WindowClient;
import p2p.socket.fileserver.ConnectionEventHandler;

public class FileServerConnection extends ClientAbstract{
    private WindowClient windowClient;
    private Map fileInfo;

    public FileServerConnection(String host, int port, Map fileInfo,
            WindowClient windowClient)
    {
        super(host, port);
        this.fileInfo = fileInfo;
        this.windowClient = windowClient;
    }

    public WindowClient getWindowClient()
    {
        return this.windowClient;
    }

    public String getFileHash()
    {
        return this.fileInfo.get("hash").toString();
    }

    public DownloadData getDownloadData() throws FileNotFoundException
    {
        return this.windowClient.getDownloadData(getFileHash());
    }

    public void setUp() {
        setEventHandler(new ConnectionEventHandler(this));
    }
    
}
