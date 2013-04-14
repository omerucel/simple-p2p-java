package com.omerucel.socket;

import java.net.Socket;

public interface IServerEventHandler extends IEventHandler{
    public void handleStartingFailed(Exception ex);
    public void handleStarted();
    public void handleClientConnected(Socket socket);
    public void handleClientConnectionFailed(Exception ex);
    public void handleClosed();
}
