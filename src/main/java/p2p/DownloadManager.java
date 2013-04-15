package p2p;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import p2p.socket.FileServerConnection;

public class DownloadManager {
    private WindowClient windowClient;
    private HashMap<String, DownloadData> downloadData;
    private HashMap<String, Map> downloadingFiles;

    public DownloadManager(WindowClient windowClient)
    {
        this.windowClient = windowClient;
        this.downloadingFiles = new HashMap<String, Map>();
        this.downloadData = new HashMap<String, DownloadData>();
    }

    public WindowClient getWindowClient()
    {
        return this.windowClient;
    }

    public Map getFileInfo(String hash) throws FileNotFoundException
    {
        if (!downloadingFiles.containsKey(hash))
            throw new FileNotFoundException("İstenilen dosya paylaşımda değil.");

        return downloadingFiles.get(hash);
    }

    public DownloadData getDownloadData(String hash) throws FileNotFoundException
    {
        if (!downloadData.containsKey(hash))
        {
            Map fileInfo = getFileInfo(hash);
            downloadData.put(hash, new DownloadData(this,
                    Integer.parseInt(fileInfo.get("size").toString()),
                    Integer.parseInt(fileInfo.get("total_part").toString()),
                    new File(fileInfo.get("file_path").toString())));
        }

        return downloadData.get(hash);
    }

    public Boolean hasFile(String hash)
    {
        return downloadingFiles.containsKey(hash);
    }

    public void clear()
    {
        getWindowClient()
                .getDownloadLog()
                .setText("");
        downloadingFiles.clear();
        downloadData.clear();
        getWindowClient()
                .getDownloadTableModel()
                .setRowCount(0);
    }

    public void addLog(String message)
    {
        getWindowClient()
                .getDownloadLog()
                .setText(getWindowClient()
                            .getDownloadLog()
                            .getText() + message + "\n");
    }

    public synchronized void incrementDownloadedFilePartNumberOnTable(String hash)
    {
        int number=0;
        for(int i = 0; i < getWindowClient().getDownloadTableModel().getRowCount(); i++)
        {
            if (getWindowClient().getDownloadTableModel().getValueAt(i, 0).equals(hash))
            {
                number = Integer.parseInt(getWindowClient().getDownloadTableModel().getValueAt(i, 5).toString());
                getWindowClient().getDownloadTableModel().setValueAt(number+1, i, 5);

                number = Integer.parseInt(getWindowClient().getDownloadTableModel().getValueAt(i, 6).toString());
                getWindowClient().getDownloadTableModel().setValueAt(number-1, i, 6);
            }
        }
    }

    public synchronized void setConnectedClientNumberOnTable(String hash, Boolean increment)
    {
        int number=0;
        for(int i = 0; i < getWindowClient().getDownloadTableModel().getRowCount(); i++)
        {
            if (getWindowClient().getDownloadTableModel().getValueAt(i, 0).equals(hash))
            {
                number = Integer.parseInt(getWindowClient().getDownloadTableModel().getValueAt(i, 4).toString());
                if (increment)
                {
                    getWindowClient().getDownloadTableModel().setValueAt(number+1, i, 4);
                }else{
                    getWindowClient().getDownloadTableModel().setValueAt(number-1, i, 4);
                }
            }
        }
    }

    public void downloadFile(String fileHash, ArrayList<Map> clients) throws FileNotFoundException
    {
        getWindowClient().focusTab(0);

        Map fileInfo = getWindowClient().getSearchManager().getFileInfo(fileHash);

        File file = new File(WindowClientStart.getInstance().getDownloadFolder() + "/" + fileInfo.get("name").toString());
        int size = Integer.parseInt(fileInfo.get("size").toString());
        int totalPart = (size/524288) + 1;

        Map temp = new LinkedHashMap();
        temp.put("hash", fileHash);
        temp.put("name", fileInfo.get("name"));
        temp.put("file_type", fileInfo.get("file_type"));
        temp.put("size", size);
        temp.put("file_path", file.getAbsolutePath());
        temp.put("total_part", totalPart);
        downloadingFiles.put(fileHash, temp);

        getWindowClient().getDownloadTableModel().addRow(new Object[]{
            fileHash, fileInfo.get("name"), 
            fileInfo.get("file_type"), size, 0,
            0, totalPart, totalPart});

        for(Map map : clients)
        {
            String host = map.get("host").toString();
            int port = Integer.parseInt(map.get("port").toString());

            try {
                fileInfo = windowClient.getSearchManager().getFileInfo(fileHash);
                new Thread(
                        new FileServerConnection(host, port, fileInfo, this)
                        ).start();
            } catch (FileNotFoundException ex) {
            }
        }
    }

    public void downloadedFile(String fileHash) throws FileNotFoundException
    {
        Map temp = getFileInfo(fileHash);
        File[] files = new File[]{new File(temp.get("file_path").toString())};
        getWindowClient()
                .getShareManager()
                .addFiles(files);
        downloadingFiles.remove(fileHash);
        downloadData.remove(fileHash);
    }
}
