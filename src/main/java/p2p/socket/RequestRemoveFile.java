package p2p.socket;

import com.omerucel.socket.message.IRequest;
import java.util.ArrayList;

public class RequestRemoveFile implements IRequest{
    private ArrayList<String> files;

    public RequestRemoveFile()
    {
        this.files = new ArrayList<String>();
    }

    public void addFile(String hash)
    {
        this.files.add(hash);
    }

    public ArrayList<String> getFiles()
    {
        return this.files;
    }
}
