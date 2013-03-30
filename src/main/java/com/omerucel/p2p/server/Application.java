/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author omer
 */
public class Application {
    private ServerSocket serverSocket;
    private HashMap<String, PeerClient> peers;
    private HashMap<String, FileInfo> files;

    public Application()
    {
        peers = new HashMap<String, PeerClient>();
        files = new HashMap<String, FileInfo>();
    }

    public void run()
    {
        try {
            serverSocket = new ServerSocket(9090);

            while(true)
            {
                Socket socket = serverSocket.accept();
                new Thread(new PeerClient(this, socket)).start();
            }

        } catch (IOException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }

    /**
     * 
     * @param peer 
     */
    public synchronized void connectedPeer(PeerClient peer)
    {
        Database.getInstance().addPeer(peer.getId(), peer.getIp());
        System.out.println(peer + " connected");
        notifyAll();
    }

    /**
     * 
     * @param peer 
     */
    public synchronized void disconnectedPeer(PeerClient peer)
    {
        Database.getInstance().removePeer(peer.getId());
        System.out.println(peer + " disconnected");
        notifyAll();
    }

    /**
     * 
     * @param peerClient
     * @param hash
     * @param name 
     */
    public void addNewFile(PeerClient peerClient, String hash, String name)
    {
        System.out.println(peerClient + " added a file : " + hash + " " + name);
        Database.getInstance().addFile(peerClient.getId(), hash, name);
    }

    public Map searchFile(String name)
    {
        return Database.getInstance().searchFile(name);
    }

    public ArrayList<String> getFilePeers(String hash)
    {
        return Database.getInstance().getFilePeers(hash);
    }

    public static void main(String[] args)
    {
        Application application = new Application();
        application.run();
    }
}
