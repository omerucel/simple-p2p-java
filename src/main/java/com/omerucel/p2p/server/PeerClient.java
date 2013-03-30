/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author omer
 */
public class PeerClient implements Runnable {
    String uuid = null;
    Application application;
    Socket socket;
    Boolean run = true;
    PrintWriter out;
    BufferedReader in;
    

    public PeerClient(Application application, Socket socket)
    {
        this.application = application;
        this.socket = socket;
    }

    public String getId()
    {
        if (uuid == null)
            uuid = UUID.randomUUID().toString();

        return uuid;
    }

    public String getIp()
    {
        return this.socket.getInetAddress().getHostAddress().toString();
    }

    @Override
    public String toString()
    {
        return this.getId();
    }

    @Override
    public void run()
    {
        this.application.connectedPeer(this);

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            JSONParser jsonParser = new JSONParser();
            while(run != false && (inputLine = in.readLine().trim()) != null)
            {
                Map result = new LinkedHashMap();
                System.out.println(getId() + " send : " + inputLine);

                try {
                    JSONObject jsonObject = (JSONObject)jsonParser.parse(inputLine);

                    if (jsonObject.containsKey("command"))
                    {
                        String command = jsonObject.get("command").toString();

                        try{
                            Method method = this.getClass().getMethod("handle" + command.toUpperCase(Locale.US), Map.class, JSONObject.class);
                            method.invoke(this, result, jsonObject);
                        }catch(NoSuchMethodException e){
                            Logger.getLogger(PeerClient.class.getName()).log(Level.SEVERE, null, e);
                            result.put("error", "Error.003");
                            out.println(JSONValue.toJSONString(result));
                        }
                    }else{
                        result.put("error", "Error.002");
                        out.println(JSONValue.toJSONString(result));
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(PeerClient.class.getName()).log(Level.SEVERE, null, ex);

                    result.put("error", "Error.001");
                    out.println(JSONValue.toJSONString(result));
                }
            }

            in.close();
            out.close();
            socket.close();
        } catch (Exception ex) {
            Logger.getLogger(PeerClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.application.disconnectedPeer(this);
    }

    public void handleEXIT(Map result, JSONObject jsonObject)
    {
        run = false;
    }

    public void handleUPDATE(Map result, JSONObject jsonObject)
    {
        if (jsonObject.containsKey("files") && jsonObject.get("files") instanceof JSONArray)
        {
            for(Object file : (JSONArray)jsonObject.get("files"))
            {
                JSONObject temp = (JSONObject)file;
                if (temp.containsKey("name") && temp.containsKey("hash"))
                {
                    this.application.addNewFile(this,
                            temp.get("hash").toString(),
                            temp.get("name").toString());
                }
            }
            result.put("response-type", "update");
            out.println(JSONValue.toJSONString(result));
        }
    }

    public void handleSEARCH(Map result, JSONObject jsonObject)
    {
        if (jsonObject.containsKey("name"))
        {
            result.put("response-type", "search");
            result.put("files", this.application.searchFile(jsonObject.get("name").toString()));
            out.println(JSONValue.toJSONString(result));
        }
    }

    public void handleDownload(Map result, JSONObject jsonObject)
    {
        if (jsonObject.containsKey("hash"))
        {
            result.put("response-type", "download");
            result.put("peers", this.application.getFilePeers(jsonObject.get("hash").toString()));
            out.println(JSONValue.toJSONString(result));
        }
    }
}
