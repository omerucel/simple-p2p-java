package p2p;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class WindowAbstract extends javax.swing.JFrame {
    public void showErrorMessage(String message)
    {
        JOptionPane.showMessageDialog(this, message);
    }
}
