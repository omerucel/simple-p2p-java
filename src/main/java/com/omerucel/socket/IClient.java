package com.omerucel.socket;

public interface IClient extends Runnable{
    public IClientIdentity getClientIdentity();
    public void setEventHandler(IClientEventHandler eventHandler);
    public IClientEventHandler getEventHandler();
    public void writeObject(Object object);
    public void disconnect();
    public void setUp();
    public Boolean isOpen();
    public String getHost();
    public int getPort();
}
