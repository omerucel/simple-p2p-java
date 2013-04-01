/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.simple;

/**
 *
 * @author omer
 */
public class ClientFactory {
    private static Client mainServerClient;

    public static Client getMainServerClient()
    {
        return mainServerClient;
    }

    public static void setMainServerClient(Client client)
    {
        mainServerClient = client;
    }
}
