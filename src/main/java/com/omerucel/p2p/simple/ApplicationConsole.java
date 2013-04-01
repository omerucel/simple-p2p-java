/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.simple;

/**
 *
 * @author omer
 */
public class ApplicationConsole
{
    public static void main(String[] args) throws InterruptedException
    {
        Thread thread = new Thread(Server.getInstance());
        thread.start();
        thread.join();
    }
}
