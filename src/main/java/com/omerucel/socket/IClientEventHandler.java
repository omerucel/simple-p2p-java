package com.omerucel.socket;

public interface IClientEventHandler extends IEventHandler{
    public void handleConnected();
    public void handleConnectionFailed(Exception ex);
    public void handleDisconnected();
    public void handle(Object message);
}
