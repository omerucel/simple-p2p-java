package p2p.socket.fileserver;

import com.omerucel.socket.IClientEventHandler;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import p2p.Config;
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
                        .getDownloadManager()
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
                        .getShareManager()
                        .getFileInfo(request.getHash());
                int fileSize = Integer.parseInt(fileInfo.get("size").toString());
                int totalPart = Math.round(fileSize/Config.PART_LIMIT)+1;
                int skipIndex = (request.getPart()-1)*Config.PART_LIMIT;

                RandomAccessFile raf = new RandomAccessFile(
                        fileInfo.get("file_path").toString(), "r");
                try {
                    raf.seek(skipIndex);
                    byte[] data = new byte[Config.PART_LIMIT];
                    int i = 0;
                    for(i=0;i<Config.PART_LIMIT;i++)
                    {
                        try
                        {
                            data[i] = raf.readByte();
                        }catch(EOFException ex){
                            byte temp[] = new byte[i];
                            System.arraycopy(data, 0, temp, 0, i);
                            data = temp;
                            break;
                        }
                    }
                    raf.close();

                    System.out.println("--- START ---\n"
                            + "Breaks on : " + i + "\n"
                            + "Part : " + request.getPart() + "\n"
                            + "Seek : " + skipIndex + "\n"
                            + "Size : " + fileSize + "\n"
                            + "Data : " + data.length + "\n"
                            + "--- END ---" + "\n");

                    Integer integers[] = new Integer[data.length];
                    for(i=0;i<data.length;i++)
                        integers[i] = (int)data[i];

                    getFileServerClient()
                            .writeObject(new ResponseDownloadPart(
                                request.getHash(),
                                request.getPart(),
                                integers));

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
