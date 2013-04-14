/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.Tika;
import p2p.socket.RequestAddFile;
import p2p.socket.RequestFileClients;
import p2p.socket.RequestRemoveFile;
import p2p.socket.RequestSearch;

/**
 *
 * @author omer
 */
public class WindowClient extends WindowAbstract {

    private static WindowClient instance;
    DefaultTableModel downloadTableModel;
    DefaultTableModel searchTableModel;
    DefaultTableModel shareTableModel;
    private HashMap<String, Map> sharedFiles;
    private HashMap<String, Map> downloadingFiles;
    private HashMap<String, Map> searchFiles;
    private HashMap<String, DownloadData> downloadData;

    public static WindowClient getInstance()
    {
        if (instance == null)
            instance = new WindowClient();

        return instance;
    }


    /**
     * Creates new form WindowClient
     */
    private WindowClient() {
        initComponents();

        downloadTableModel = new DefaultTableModel();
        downloadTableModel.addColumn("Hash");
        downloadTableModel.addColumn("Adı");
        downloadTableModel.addColumn("Türü");
        downloadTableModel.addColumn("Boyutu");
        downloadTableModel.addColumn("Bağlanılan Eş Sayısı");
        downloadTableModel.addColumn("İndirilen Parça");
        downloadTableModel.addColumn("Kalan Parça");
        downloadTableModel.addColumn("Toplam Parça");
        downloadTable.setModel(downloadTableModel);

        searchTableModel = new DefaultTableModel();
        searchTableModel.addColumn("Hash");
        searchTableModel.addColumn("Adı");
        searchTableModel.addColumn("Türü");
        searchTableModel.addColumn("Boyutu");
        searchTableModel.addColumn("Eş Sayısı");
        searchTable.setModel(searchTableModel);

        shareTableModel = new DefaultTableModel();
        shareTableModel.addColumn("Hash");
        shareTableModel.addColumn("Adı");
        shareTableModel.addColumn("Türü");
        shareTableModel.addColumn("Boyutu");
        shareTableModel.addColumn("Dosya Yolu");
        shareTable.setModel(shareTableModel);

        setLocationRelativeTo(null);

        sharedFiles = new HashMap<String, Map>();
        downloadingFiles = new HashMap<String, Map>();
        searchFiles = new HashMap<String, Map>();
        downloadData = new HashMap<String, DownloadData>();

        shareTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        downloadTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void focusDownloadingTab()
    {
        jTabbedPane1.setSelectedIndex(0);
    }

    public synchronized void addLog(String message)
    {
        downloadLog.setText(downloadLog.getText() + message + "\n");
    }

    public synchronized void incrementDownloadedFilePartNumberOnTable(String hash)
    {
        int number=0;
        for(int i = 0; i < downloadTableModel.getRowCount(); i++)
        {
            if (downloadTableModel.getValueAt(i, 0).equals(hash))
            {
                number = Integer.parseInt(downloadTableModel.getValueAt(i, 5).toString());
                downloadTableModel.setValueAt(number+1, i, 5);

                number = Integer.parseInt(downloadTableModel.getValueAt(i, 6).toString());
                downloadTableModel.setValueAt(number-1, i, 6);
            }
        }
    }

    public synchronized void setConnectedClientNumberOnTable(String hash, Boolean increment)
    {
        int number=0;
        for(int i = 0; i < downloadTableModel.getRowCount(); i++)
        {
            if (downloadTableModel.getValueAt(i, 0).equals(hash))
            {
                number = Integer.parseInt(downloadTableModel.getValueAt(i, 4).toString());
                if (increment)
                {
                    downloadTableModel.setValueAt(number+1, i, 4);
                }else{
                    downloadTableModel.setValueAt(number-1, i, 4);
                }
            }
        }
    }

    public synchronized void addToDownloadingFiles(String fileHash) throws FileNotFoundException
    {
        Map fileInfo = getSearchFileInfo(fileHash);

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

        downloadTableModel.addRow(new Object[]{fileHash, fileInfo.get("name"), 
            fileInfo.get("file_type"), size, 0,
            0, totalPart, totalPart});
    }

    public synchronized DownloadData getDownloadData(String hash) throws FileNotFoundException
    {
        if (!downloadData.containsKey(hash))
        {
            Map fileInfo = getDownloadFileInfo(hash);
            downloadData.put(hash, new DownloadData(this,
                    Integer.parseInt(fileInfo.get("size").toString()),
                    Integer.parseInt(fileInfo.get("total_part").toString()),
                    new File(fileInfo.get("file_path").toString())));
        }

        return downloadData.get(hash);
    }

    public synchronized void downloadedFile(String hash) throws FileNotFoundException
    {
        Map temp = getDownloadFileInfo(hash);
        File[] files = new File[]{new File(temp.get("file_path").toString())};
        addSharedFiles(files);
        downloadingFiles.remove(hash);
        downloadData.remove(hash);
    }

    public Map getSearchFileInfo(String hash) throws FileNotFoundException
    {
        if (!searchFiles.containsKey(hash))
            throw new FileNotFoundException("İstenilen dosya paylaşımda değil.");

        return searchFiles.get(hash);
    }

    public Map getFileInfo(String hash) throws FileNotFoundException
    {
        if (!sharedFiles.containsKey(hash))
            throw new FileNotFoundException("İstenilen dosya paylaşımda değil.");

        return sharedFiles.get(hash);
    }

    public Map getDownloadFileInfo(String hash) throws FileNotFoundException
    {
        if (!downloadingFiles.containsKey(hash))
            throw new FileNotFoundException("İstenilen dosya paylaşımda değil.");

        return downloadingFiles.get(hash);
    }

    public void addSharedFiles(File[] selectedFiles)
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
                    shareTableModel.addRow(new Object[]{
                        hash, name, fileType, size.intValue(), file.getAbsolutePath()});

                    Map temp = new LinkedHashMap();
                    temp.put("hash", hash);
                    temp.put("name", name);
                    temp.put("file_type", fileType);
                    temp.put("size", size.intValue());
                    temp.put("file_path", file.getAbsolutePath());

                    sharedFiles.put(hash, temp);
                }
            } catch (Exception ex) {
                showErrorMessage(ex.getMessage());
                continue;
            }
        }

        DialogLoading.getInstance().toggle(true);
        ObjectContainer.getMainServerConnection().writeObject(requestAddFile);
    }

    public void reset()
    {
        clearDownloadTable();
        clearSearchTable();
        clearShareTable();
    }

    public void clearDownloadTable()
    {
        downloadLog.setText("");
        downloadingFiles.clear();
        downloadData.clear();
        downloadTableModel.setRowCount(0);
    }

    public void clearSearchTable()
    {
        searchBox.setText("");
        searchFiles.clear();
        searchTableModel.setRowCount(0);
    }

    public void clearShareTable()
    {
        sharedFiles.clear();
        shareTableModel.setRowCount(0);
    }

    public synchronized void addFileToSearchTable(String hash, String name, String fileType, int size,
            int clientCount)
    {
        Map temp = new LinkedHashMap();
        temp.put("hash", hash);
        temp.put("name", name);
        temp.put("file_type", fileType);
        temp.put("size", size);
        temp.put("client_count", clientCount);
        searchFiles.put(hash, temp);

        searchTableModel.addRow(new Object[]{hash, name, 
            fileType, size, clientCount});
    }

    public void showClientSelectDialog(String fileHash, ArrayList<Map> fileClients)
    {
        DialogLoading.getInstance().toggle(false);

        DialogClientSelect dialogClientSelect = new DialogClientSelect(this, true);
        dialogClientSelect.loadClients(fileHash, fileClients);
        dialogClientSelect.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        exitButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        downloadTable = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        downloadLog = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        searchTable = new javax.swing.JTable();
        searchButton = new javax.swing.JButton();
        searchBox = new javax.swing.JTextField();
        downloadButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        dosyaEkle = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        shareTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        exitButton.setText("Bağlantıyı Kapat");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        downloadTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(downloadTable);

        downloadLog.setEditable(false);
        downloadLog.setColumns(20);
        downloadLog.setRows(5);
        jScrollPane4.setViewportView(downloadLog);

        jLabel1.setText("Log");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
            .add(jScrollPane4)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel1)
                .add(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .add(2, 2, 2)
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 128, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("İndirilenler", jPanel1);

        searchTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(searchTable);

        searchButton.setText("Ara");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        downloadButton.setText("Seçili Dosyayı İndir");
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(searchBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchButton))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(downloadButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(searchButton)
                    .add(searchBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(downloadButton))
        );

        jTabbedPane1.addTab("Dosya Ara", jPanel2);

        dosyaEkle.setText("Dosya Ekle");
        dosyaEkle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dosyaEkleActionPerformed(evt);
            }
        });

        shareTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(shareTable);

        jButton1.setText("Paylaşımı Kaldır");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, dosyaEkle)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton1))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(dosyaEkle)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton1)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Paylaşılan Dosyalar", jPanel3);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(exitButton))
                    .add(jTabbedPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(exitButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed

        DialogLoading.getInstance().toggle(true);
        ObjectContainer.getMainServerConnection().disconnect();
    }//GEN-LAST:event_exitButtonActionPerformed

    private void dosyaEkleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dosyaEkleActionPerformed

        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(true);

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File[] selectedFiles = fc.getSelectedFiles();

            addSharedFiles(selectedFiles);
        }
    }//GEN-LAST:event_dosyaEkleActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        int[] indexes =  shareTable.getSelectedRows();

        String hash;
        RequestRemoveFile requestRemoveFile = new RequestRemoveFile();
        for(int i : indexes)
        {
            hash = shareTableModel.getValueAt(i, 0).toString();
            sharedFiles.remove(hash);
            requestRemoveFile.addFile(hash);
            shareTableModel.removeRow(i);
        }

        ObjectContainer.getMainServerConnection().writeObject(requestRemoveFile);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed

        DialogLoading.getInstance().toggle(true);
        this.clearSearchTable();

        RequestSearch requestSearch = new RequestSearch(searchBox.getText().toString());

        ObjectContainer.getMainServerConnection().writeObject(requestSearch);
    }//GEN-LAST:event_searchButtonActionPerformed

    private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed

        DialogLoading.getInstance().toggle(true);
        int selectedIndex = searchTable.getSelectedRow();
        if (selectedIndex < 0)
        {
            DialogLoading.getInstance().toggle(false);
            showErrorMessage("Lütfen bir dosya seçiniz!");
            return;
        }

        String hash = searchTableModel.getValueAt(selectedIndex, 0).toString();

        if (!searchFiles.containsKey(hash))
        {
            DialogLoading.getInstance().toggle(false);
            showErrorMessage("Lütfen bir dosya seçiniz!");
            return;
        }

        if (sharedFiles.containsKey(hash))
        {
            DialogLoading.getInstance().toggle(false);
            showErrorMessage("Bu dosyayı zaten paylaşmaktasınız!");
            return;
        }

        if (downloadingFiles.containsKey(hash))
        {
            DialogLoading.getInstance().toggle(false);
            showErrorMessage("Bu dosyayı zaten indiriyorsunuz!");
            return;
        }

        RequestFileClients requestFileClients = new RequestFileClients(hash);

        ObjectContainer.getMainServerConnection().writeObject(requestFileClients);
    }//GEN-LAST:event_downloadButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton dosyaEkle;
    private javax.swing.JButton downloadButton;
    private javax.swing.JTextArea downloadLog;
    private javax.swing.JTable downloadTable;
    private javax.swing.JButton exitButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField searchBox;
    private javax.swing.JButton searchButton;
    private javax.swing.JTable searchTable;
    private javax.swing.JTable shareTable;
    // End of variables declaration//GEN-END:variables
}
