package p2p.socket;

import com.omerucel.socket.ClientAbstract;
import java.io.FileNotFoundException;
import java.util.Map;
import p2p.DownloadData;
import p2p.DownloadManager;
import p2p.WindowClient;
import p2p.socket.fileserver.ConnectionEventHandler;

public class FileServerConnection extends ClientAbstract{
    private DownloadManager downloadManager;
    private Map fileInfo;

    public FileServerConnection(String host, int port, Map fileInfo,
            DownloadManager downloadManager)
    {
        super(host, port);
        this.fileInfo = fileInfo;
        this.downloadManager = downloadManager;
    }

    public DownloadManager getDownloadManager()
    {
        return this.downloadManager;
    }

    public String getFileHash()
    {
        return this.fileInfo.get("hash").toString();
    }

    public DownloadData getDownloadData() throws FileNotFoundException
    {
        return getDownloadManager().getDownloadData(getFileHash());
    }

    public void setUp() {
        setEventHandler(new ConnectionEventHandler(this));
    }
    
}
