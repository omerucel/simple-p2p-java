package p2p.socket;

import java.util.ArrayList;

public class ResponseDownloadPart extends RequestDownloadPart{
    private ArrayList<Integer> data;

    public ResponseDownloadPart(String hash, int part, ArrayList<Integer> data)
    {
        super(hash, part);
        this.data = data;
    }

    public ArrayList<Integer> getData()
    {
        return this.data;
    }
}
