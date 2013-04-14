package com.omerucel.socket;

import java.util.UUID;

public class DefaultClientIdentity implements IClientIdentity{
    private String id;
    private String ip;

    public DefaultClientIdentity(String ip)
    {
        this.id = UUID.randomUUID().toString();
        this.ip = ip;
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }
}
