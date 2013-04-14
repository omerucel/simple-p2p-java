package p2p.socket;

import com.omerucel.socket.message.IResponse;
import java.util.ArrayList;
import java.util.Map;

public class ResponseFileClients implements IResponse{
    private String fileHash;
    private ArrayList<Map> fileClients;

    public ResponseFileClients(String fileHash, ArrayList<Map> fileClients)
    {
        this.fileHash = fileHash;
        this.fileClients = fileClients;
    }

    public String getFileHash()
    {
        return this.fileHash;
    }

    public ArrayList<Map> getFileClients()
    {
        return this.fileClients;
    }
}
