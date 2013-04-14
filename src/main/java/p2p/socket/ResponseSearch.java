package p2p.socket;

import com.omerucel.socket.message.IResponse;
import java.util.ArrayList;
import java.util.Map;

public class ResponseSearch implements IResponse{
    private ArrayList<Map> files;

    public ResponseSearch(ArrayList<Map> files)
    {
        this.files = files;
    }

    public ArrayList<Map> getFiles()
    {
        return files;
    }
}
