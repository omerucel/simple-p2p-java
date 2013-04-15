package p2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.Tika;
import p2p.socket.RequestAddFile;
import p2p.socket.RequestRemoveFile;

public class ShareManager {
    private WindowClient windowClient;
    private HashMap<String, Map> sharedFiles;

    public ShareManager(WindowClient windowClient)
    {
        this.windowClient = windowClient;
        this.sharedFiles = new HashMap<String, Map>();
    }


    public WindowClient getWindowClient()
    {
        return windowClient;
    }

    public Map getFileInfo(String hash) throws FileNotFoundException
    {
        if (!sharedFiles.containsKey(hash))
            throw new FileNotFoundException("İstenilen dosya paylaşımda değil.");

        return sharedFiles.get(hash);
    }

    public Boolean hasFile(String hash)
    {
        return sharedFiles.containsKey(hash);
    }

    public void clear()
    {
        sharedFiles.clear();
        getWindowClient()
                .getShareTableModel()
                .setRowCount(0);
    }

    public void addFiles(File[] selectedFiles)
    {
        Tika tika = new Tika();

        FileInputStream inputStream;
        RequestAddFile requestAddFile = new RequestAddFile();
        for(File file : selectedFiles)
        {
            try {
                inputStream = new FileInputStream(file);
                String name = file.getAbsoluteFile().getName();
                Long size = file.length();
                String fileType = tika.detect(file);
                String hash = DigestUtils.md5Hex(inputStream);

                if (!sharedFiles.containsKey(hash))
                {

                    requestAddFile.addFile(hash, name, size.intValue(), fileType);

                    getWindowClient()
                            .getShareTableModel()
                            .addRow(new Object[]{
                                hash, name, fileType, 
                                size.intValue(), 
                                file.getAbsolutePath()});

                    Map temp = new LinkedHashMap();
                    temp.put("hash", hash);
                    temp.put("name", name);
                    temp.put("file_type", fileType);
                    temp.put("size", size.intValue());
                    temp.put("file_path", file.getAbsolutePath());

                    sharedFiles.put(hash, temp);
                }
            } catch (Exception ex) {
                getWindowClient()
                        .showErrorMessage(ex.getMessage());
                continue;
            }
        }

        DialogLoading.getInstance().toggle(true);
        ObjectContainer.getMainServerConnection().writeObject(requestAddFile);
    }

    public void removeFiles()
    {
        int[] indexes =  getWindowClient()
                .getShareTable()
                .getSelectedRows();

        if (indexes.length == 0)
        {
            getWindowClient()
                    .showErrorMessage("Tablodan bir dosya seçmelisiniz.");
        }else{
            int status = JOptionPane.showConfirmDialog(getWindowClient(), "Emin misiniz?");
            if (status != JOptionPane.OK_OPTION)
                return;
        }

        String hash;
        RequestRemoveFile requestRemoveFile = new RequestRemoveFile();
        for(int i : indexes)
        {
            hash = getWindowClient()
                    .getShareTableModel()
                    .getValueAt(i, 0).toString();
            sharedFiles.remove(hash);
            requestRemoveFile.addFile(hash);
            getWindowClient()
                    .getShareTableModel()
                    .removeRow(i);
        }

        ObjectContainer.getMainServerConnection().writeObject(requestRemoveFile);
    }
}
