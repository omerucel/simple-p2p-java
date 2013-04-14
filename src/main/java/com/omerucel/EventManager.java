package com.omerucel;

import java.util.ArrayList;
import java.util.HashMap;

public class EventManager {
    private HashMap<String, ArrayList<EventProxy>> events;

    private class EventProxy implements IEvent
    {
        private IEvent event;
        private Boolean once = false;

        public EventProxy(IEvent event, Boolean once)
        {
            this.event = event;
            this.once = once;
        }

        public Boolean isOnce() {
            return once;
        }

        public void setOnce(Boolean once) {
            this.once = once;
        }

        public IEvent getEvent()
        {
            return this.event;
        }

        @Override
        public void execute(Object... objs) {
            this.event.execute(objs);
        }

        @Override
        public void handleException(Exception e, Object... objs) {
            this.event.handleException(e, objs);
        }
    }

    public EventManager()
    {
        events = new HashMap<String, ArrayList<EventProxy>>();
    }

    public void on(String name, IEvent event)
    {
        on(name, event, false);
    }

    public void once(String name, IEvent event)
    {
        on(name, event, true);
    }

    private synchronized void on(String name, IEvent event, Boolean once)
    {
        if (!events.containsKey(name))
            events.put(name, new ArrayList<EventProxy>());

        events.get(name).add(new EventProxy(event, once));
        notifyAll();
    }

    public synchronized void removeListener(String name, IEvent event)
    {
        if (!events.containsKey(name)) return;

        for(EventProxy eventProxy : events.get(name))
        {
            if (eventProxy.getEvent().equals(event))
                events.get(name).remove(eventProxy);
        }

        notifyAll();
    }

    public synchronized void removeAllListener(String name)
    {
        if (!events.containsKey(name)) return;
        events.remove(name);
        notifyAll();
    }

    public synchronized void emit(String name, Object...objs)
    {
        if (!events.containsKey(name)) return;

        for(EventProxy eventProxy : events.get(name))
        {
            try
            {
                eventProxy.execute(objs);
            }catch(Exception e){
                eventProxy.handleException(e, objs);
            }

            if (eventProxy.isOnce())
                events.get(name).remove(eventProxy);
        }
        notifyAll();
    }
}
