/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2p;

import javax.swing.SwingUtilities;

/**
 *
 * @author omer
 */
public class DialogLoading extends javax.swing.JDialog {

    private static DialogLoading instance;

    public synchronized static DialogLoading getInstance()
    {
        if (instance == null)
            instance = new DialogLoading(null, true);

        return instance;
    }

    /**
     * Creates new form DialogLoading
     */
    private DialogLoading(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
    }

    public synchronized void toggle(Boolean status)
    {
        if (status)
        {
            showDialogLoading();
        }else{
            hideDialogLoading();
        }
    }

    private void showDialogLoading()
    {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                DialogLoading.getInstance().setVisible(true);
            }
        });
    }

    private void hideDialogLoading()
    {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                DialogLoading.getInstance().setVisible(false);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setModal(true);
        setResizable(false);

        jLabel1.setText("Lütfen bekleyiniz... İşlem gerçekleştiriliyor.");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
