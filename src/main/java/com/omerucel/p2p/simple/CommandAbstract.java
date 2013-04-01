/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.simple;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author omer
 */
public class CommandAbstract {

    private static HashMap<String, ArrayList<Command>> callbacks = new HashMap<String, ArrayList<Command>>();

    public interface Command
    {
        public void execute(Object data);
    }

    public synchronized void on(String event, Command command){
        if (callbacks.containsKey(event))
        {
            callbacks.get(event).add(command);
        }else{
            ArrayList<Command> list = new ArrayList<Command>();
            list.add(command);
            callbacks.put(event, list);
        }
        System.out.println("added " + event + " command");
        notifyAll();
    }

    public void emit(String event)
    {
        if (!callbacks.containsKey(event)) return;

        System.out.println("emitted " + event + " command.");

        for(Command command : callbacks.get(event))
        {
            command.execute(null);
        }
    }
}
