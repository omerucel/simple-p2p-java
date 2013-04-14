package com.omerucel;

public interface IEvent {
    public void execute(Object...objs);
    public void handleException(Exception e, Object...objs);
}
