package p2p;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import p2p.socket.RequestSearch;

public class SearchManager {
    private WindowClient windowClient;
    private HashMap<String, Map> searchFiles;

    public SearchManager(WindowClient windowClient)
    {
        this.windowClient = windowClient;
        this.searchFiles = new HashMap<String, Map>();
    }

    public WindowClient getWindowClient()
    {
        return windowClient;
    }

    public Map getFileInfo(String hash) throws FileNotFoundException
    {
        if (!searchFiles.containsKey(hash))
            throw new FileNotFoundException("İstenilen dosya paylaşımda değil.");

        return searchFiles.get(hash);
    }

    public Boolean hasFile(String hash)
    {
        return searchFiles.containsKey(hash);
    }

    public void clear()
    {
        searchFiles.clear();
        getWindowClient()
                .getSearchTableModel()
                .setRowCount(0);
        getWindowClient()
                .getSearchBox()
                .setText("");
    }

    public void search()
    {
        clear();
        String name = getWindowClient()
                .getSearchBox()
                .getText()
                .toString();

        ObjectContainer
                .getMainServerConnection()
                .writeObject(new RequestSearch(name));
    }

    public void addFile(String hash, String name, String fileType, int size,
            int clientCount)
    {
        Map temp = new LinkedHashMap();
        temp.put("hash", hash);
        temp.put("name", name);
        temp.put("file_type", fileType);
        temp.put("size", size);
        temp.put("client_count", clientCount);
        searchFiles.put(hash, temp);

        getWindowClient()
                .getSearchTableModel()
                .addRow(new Object[]{hash, name, fileType, size, clientCount});
    }
}
