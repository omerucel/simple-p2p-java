/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.simple;

import javax.swing.SwingUtilities;

/**
 *
 * @author omer
 */
public class Application {
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                WindowStart.getInstance().setVisible(true);
            }
        });
    }
}
