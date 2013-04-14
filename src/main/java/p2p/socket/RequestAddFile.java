package p2p.socket;

import com.omerucel.socket.message.IRequest;
import java.util.ArrayList;
import java.util.HashMap;

public class RequestAddFile implements IRequest{
    private ArrayList<HashMap<String, Object>> files;

    public RequestAddFile()
    {
        this.files = new ArrayList<HashMap<String, Object>>();
    }

    public void addFile(String hash, String name, int size, String fileType)
    {
        HashMap<String, Object> temp = new HashMap<String, Object>();
        temp.put("hash", hash);
        temp.put("name", name);
        temp.put("size", size);
        temp.put("file_type", fileType);

        this.files.add(temp);
    }

    public ArrayList<HashMap<String, Object>> getFiles()
    {
        return this.files;
    }
}
