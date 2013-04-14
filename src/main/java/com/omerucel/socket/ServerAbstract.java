package com.omerucel.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

abstract public class ServerAbstract implements IServer{
    private IServerEventHandler eventHandler;
    private ServerSocket serverSocket;
    private int port;

    public ServerAbstract(int port)
    {
        this.port = port;
        setUp();
    }

    public void setEventHandler(IServerEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public IServerEventHandler getEventHandler() {
        return this.eventHandler;
    }

    public synchronized void stop()
    {
        if (!serverSocket.isClosed())
        {
            try {
                serverSocket.close();
            } catch (IOException ex) {
            }
        }
        notifyAll();
    }

    public Boolean isOpen()
    {
        return !serverSocket.isClosed();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            getEventHandler().handleStartingFailed(ex);
            return;
        }

        getEventHandler().handleStarted();

        while(!serverSocket.isClosed())
        {
            try {
                Socket socket = serverSocket.accept();
                getEventHandler().handleClientConnected(socket);
            } catch (IOException ex) {
                getEventHandler().handleClientConnectionFailed(ex);
            }
        }

        getEventHandler().handleClosed();
    }
}
