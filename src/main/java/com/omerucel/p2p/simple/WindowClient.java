/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author omer
 */
public class WindowClient extends javax.swing.JFrame {

    private static WindowClient instance;

    static WindowClient getInstance() {
        if (instance == null)
            instance = new WindowClient();

        return instance;
    }

    /**
     * Creates new form WindowClient
     */
    public WindowClient() {
        initComponents();
    }

    public void searchLoading(Boolean status)
    {
        searchButton.setEnabled(!status);
        searchQuery.setEnabled(!status);
    }

    public void clearSearchList()
    {
        searchFileTable.removeAll();
    }

    public void addToSearchList(String name, int clientCount, String size)
    {
        ((DefaultTableModel)searchFileTable.getModel())
                .addColumn(new Object[]{name, size, clientCount});
    }

    public static class OnResponseSearch implements CommandAbstract.Command
    {
        public void execute(Object data)
        {
            WindowClient.getInstance().clearSearchList();

            JSONObject jsonObject = (JSONObject)data;
            if (jsonObject.containsKey("files") 
                    && jsonObject.get("files") instanceof JSONArray)
            {
                for(Object file : (JSONArray) jsonObject.get("files"))
                {
                    JSONObject temp = (JSONObject)file;

                    if (temp.containsKey("name") 
                            && temp.containsKey("hash")
                            && temp.containsKey("client_count"))
                    {
                        WindowClient.getInstance()
                                .addToSearchList(
                                    temp.get("name").toString(),
                                    Integer.getInteger(temp.get("client_count").toString()),
                                    "10 MB");
                    }
                }
            }

            WindowClient.getInstance().searchLoading(false);
        }
    }

    public static class OnResponseWelcome implements CommandAbstract.Command
    {
        public void execute(Object data)
        {
            WindowClient.getInstance().setTitle("Client ID : " 
                    + ((JSONObject)data).get("id").toString());

            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setMultiSelectionEnabled(false);

            int returnVal = fc.showOpenDialog(WindowClient.getInstance());
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File folder = fc.getSelectedFile();

                ArrayList<Map> files = new ArrayList<Map>();
                for(File file : folder.listFiles())
                {
                    if (file.isDirectory()) continue;

                    try {
                        Map temp = new LinkedHashMap();
                        temp.put("name", file.getAbsoluteFile().getName());
                        temp.put("hash", DigestUtils.md5Hex(new FileInputStream(file)));
                        files.add(temp);
                    } catch (IOException ex) {
                        Logger.getLogger(WindowClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                Map request = new LinkedHashMap();
                request.put("request", "update");
                request.put("files", files);

                ClientFactory.getMainServerClient().sendLine(request);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        fileTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        searchButton = new javax.swing.JButton();
        searchQuery = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        searchFileTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        fileTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Adı", "Eş Sayısı", "Boyutu", "İndirilme Durumu %"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(fileTable);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("İndirilen Dosyalar", jPanel1);

        searchButton.setText("Ara");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        searchFileTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Adı", "Boyutu", "Eş Sayısı"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(searchFileTable);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(searchQuery)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(searchButton)
                    .add(searchQuery, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Dosya Ara", jPanel2);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed

        searchLoading(true);

        Map request = new LinkedHashMap();
        request.put("request", "search");
        request.put("name", searchQuery.getText());

        ClientFactory.getMainServerClient().sendLine(request);
    }//GEN-LAST:event_searchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable fileTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton searchButton;
    private javax.swing.JTable searchFileTable;
    private javax.swing.JTextField searchQuery;
    // End of variables declaration//GEN-END:variables
}
