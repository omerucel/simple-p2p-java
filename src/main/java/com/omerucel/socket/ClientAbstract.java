package com.omerucel.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

abstract public class ClientAbstract implements IClient{
    private IClientEventHandler eventHandler;
    private Socket socket = null;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private IClientIdentity clientIdentity;
    private String host = null;
    private int port = 0;

    public ClientAbstract(String host, int port)
    {
        this.host = host;
        this.port = port;
        setUp();
    }

    public ClientAbstract(Socket socket)
    {
        this.socket = socket;
        this.host = socket.getInetAddress().getHostAddress();
        this.port = socket.getPort();
        setUp();
    }

    public String getHost()
    {
        return this.host;
    }

    public int getPort()
    {
        return this.port;
    }

    public void setEventHandler(IClientEventHandler eventHandler)
    {
        this.eventHandler = eventHandler;
    }

    public IClientEventHandler getEventHandler()
    {
        return this.eventHandler;
    }

    public synchronized IClientIdentity getClientIdentity()
    {
        if (clientIdentity == null)
            clientIdentity = new DefaultClientIdentity(
                    this.socket.getInetAddress().getHostAddress());

        return clientIdentity;
    }

    public void writeObject(Object object)
    {
        try {
            out.writeObject(object);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void disconnect()
    {
        try
        {
            socket.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public Boolean isOpen()
    {
        return !socket.isClosed();
    }

    public void run()
    {
        if (socket == null)
        {
            try {
                socket = new Socket(host, port);
            } catch (Exception ex) {
                getEventHandler().handleConnectionFailed(ex);
                return;
            }
        }

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception ex) {
            getEventHandler().handleConnectionFailed(ex);
            return;
        }

        eventHandler.handleConnected();
        while(true)
        {
            Object objectMessage = null;
            try {
                objectMessage = in.readObject();
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
            if (objectMessage == null) break;

            try
            {
                getEventHandler().handle(objectMessage);
            }catch(Exception ex){
                getEventHandler().handle(ex);
            }
        }

        try {
            out.close();
            in.close();
        } catch (IOException ex) {
        }

        getEventHandler().handleDisconnected();
    }
}
