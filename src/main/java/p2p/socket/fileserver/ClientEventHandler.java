package p2p.socket.fileserver;

import com.omerucel.socket.IClientEventHandler;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Map;
import p2p.socket.FileServerClient;
import p2p.socket.RequestDownloadPart;
import p2p.socket.ResponseConnection;
import p2p.socket.ResponseDownloadPart;
import p2p.socket.ResponseFileNotFound;
import p2p.socket.ResponseFilePartNotFound;

public class ClientEventHandler implements IClientEventHandler{

    private FileServerClient fileServerClient;

    public ClientEventHandler(FileServerClient fileServerClient)
    {
        this.fileServerClient = fileServerClient;
    }

    public FileServerClient getFileServerClient()
    {
        return this.fileServerClient;
    }

    public void addLog(final String message)
    {
        new Thread(new Runnable() {

            public void run() {
                getFileServerClient()
                        .getFileServer()
                        .getWindowClient()
                        .addLog("İstemci("
                            + getFileServerClient().getClientIdentity().getId()
                            + ") : " + message);
            }
        }).start();
    }

    public void handleConnected() {
        getFileServerClient()
                .writeObject(new ResponseConnection());

        addLog("Bağlandı.");
    }

    public void handleConnectionFailed(Exception ex) {
        addLog("Bağlantı sağlanırken bir sorun çıktı : " + ex.getMessage());
    }

    public void handleDisconnected() {
        addLog("Bağlantı sonlandı.");
    }

    public void handle(Object message) {
        if (message instanceof RequestDownloadPart)
        {
            RequestDownloadPart request = (RequestDownloadPart)message;

            Map fileInfo;
            try
            {
                fileInfo = getFileServerClient()
                        .getFileServer()
                        .getWindowClient()
                        .getFileInfo(request.getHash());
                int fileSize = Integer.parseInt(fileInfo.get("size").toString());
                int totalPart = (fileSize/512) + 1;
                int part = request.getPart()-1;
                int skipIndex = part*512;

                RandomAccessFile raf = new RandomAccessFile(
                        fileInfo.get("file_path").toString(), "r");
                try {
                    raf.seek(skipIndex);

                    ArrayList<Integer> buffer = new ArrayList<Integer>();
                    for(int i=0;i<512;i++)
                    {
                        try
                        {
                            buffer.add(raf.readInt());
                        }catch(EOFException ex){
                            break;
                        }
                    }
                    raf.close();

                    getFileServerClient()
                            .writeObject(new ResponseDownloadPart(
                                request.getHash(),
                                part,
                                buffer));

                    addLog("Dosyanın("
                            + request.getHash() + ") "
                            + request.getPart() + ". parçası gönderildi.");
                } catch (IOException ex) {
                    addLog("Dosyanın("
                            + request.getHash() + ") "
                            + request.getPart() + ". parçası istendi ancak ilgili bölüm mevcut değil.");

                    getFileServerClient()
                            .writeObject(new ResponseFilePartNotFound(
                                request.getHash(), request.getPart()));
                }
            }catch(FileNotFoundException ex){
                addLog("Dosyanın("
                        + request.getHash() + ") "
                        + request.getPart() + ". parçası istendi ancak dosya mevcut değil.");

                getFileServerClient()
                        .writeObject(new ResponseFileNotFound(request.getHash()));
            }
        }
    }

}
