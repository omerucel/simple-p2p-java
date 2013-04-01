/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.simple;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author omer
 */
public class Server extends CommandAbstract implements Runnable
{
    public static final int MAIN_SERVER = 1;
    public static final int CLIENT = 2;

    private JFrame window;
    private ServerSocket serverSocket;
    private static Server instance;

    private int mode = MAIN_SERVER;

    static Server getInstance() {
        if (instance == null)
            instance = new Server();

        return instance;
    }

    public void setWindow(JFrame window)
    {
        this.window = window;
    }

    public void setMode(int mode)
    {
        this.mode = mode;
    }

    public void log(String message)
    {
        if (mode == MAIN_SERVER && this.window != null)
        {
            ((WindowMainServer) this.window).addLog(message);
        }else{
            System.out.println(message);
        }
    }

    public void run() {

        log("Sunucu başlatılıyor...");

        try {
            serverSocket = new ServerSocket(9090);
            emit("started");

            while(!serverSocket.isClosed())
            {
                Socket socket = serverSocket.accept();
                new Thread(new Client(this, socket)).start();
            }

            emit("stopped");
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            emit("starting-failed");
        }
    }

    public synchronized void connectedClient(Client client)
    {
        Database.getInstance().addClient(client.getId(), client.getIp());
        log(client + " connected");

        Map result = new LinkedHashMap();
        result.put("response", "welcome");
        result.put("id", client.getId());
        
        client.sendLine(result);
        notifyAll();
    }

    public synchronized void disconnectedClient(Client client)
    {
        Database.getInstance().removeClient(client.getId());
        log(client + " disconnected");
        notifyAll();
    }

    public void addNewFile(Client client, String hash, String name)
    {
        log(client + " added a file : " + hash + " " + name);
        Database.getInstance().addFile(client.getId(), hash, name);
    }

    public Map searchFile(String name)
    {
        return Database.getInstance().searchFile(name);
    }

    public ArrayList<String> getFileClients(String hash)
    {
        return Database.getInstance().getFileClients(hash);
    }
}
