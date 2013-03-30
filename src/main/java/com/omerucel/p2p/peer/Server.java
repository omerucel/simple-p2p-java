/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author omer
 */
public class Server implements Runnable
{
    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Server(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public void run()
    {
        try {
            socket = new Socket(this.host, this.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String fromServer;
            while((fromServer = in.readLine()) != null)
            {
                
            }

            out.close();
            in.close();
            socket.close();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
