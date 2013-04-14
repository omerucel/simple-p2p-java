package com.omerucel.socket;

public interface IServer extends Runnable{
    public void stop();
    public void setEventHandler(IServerEventHandler eventHandler);
    public IServerEventHandler getEventHandler();
    public void setUp();
    public Boolean isOpen();
}
