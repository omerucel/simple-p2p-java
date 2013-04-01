/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.simple;

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
public class Client extends CommandAbstract implements Runnable{

    private Socket socket;
    PrintWriter out;
    BufferedReader in;
    Server server;

    private int MAIN_SERVER_CLIENT = 1;
    private int PEER_CLIENT = 2;

    private Boolean run = true;
    private String host;
    private String uuid;
    private int mode = MAIN_SERVER_CLIENT;

    public Client(String host)
    {
        this.host = host;
        this.mode = PEER_CLIENT;
    }

    public Client(Server server, Socket socket)
    {
        this.socket = socket;
        this.mode = MAIN_SERVER_CLIENT;
        this.server = server;
    }

    public String getId()
    {
        if (uuid == null)
            uuid = UUID.randomUUID().toString();

        return uuid;
    }

    public String getIp()
    {
        if (socket.isClosed()) return "undefined";

        return this.socket.getInetAddress().getHostAddress().toString();
    }

    @Override
    public String toString()
    {
        return this.getId();
    }

    public void sendError(String message)
    {
        Map result = new LinkedHashMap();
        result.put("error", message);

        sendLine(result);
    }

    public void sendLine(Map result)
    {
        out.println(JSONValue.toJSONString(result));
    }

    public void run() {
        if (mode == PEER_CLIENT)
        {
            try {
                socket = new Socket(host, 9090);
                emit("connected");
            } catch (Exception ex) {
                Logger.getLogger(Client.class.getName())
                        .log(Level.SEVERE, null, ex);
                emit("connection-failed");
                return;
            }
        }

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            if (mode == MAIN_SERVER_CLIENT)
                this.server.connectedClient(this);

            String line;
            JSONParser jsonParser = new JSONParser();
            while(run && (line = in.readLine().trim()) != null)
            {
                try {
                    JSONObject jsonObject = (JSONObject)jsonParser.parse(line);

                    if (jsonObject.containsKey("request"))
                    {
                        String request = jsonObject.get("request").toString();

                        try {
                            Method method = this.getClass().getMethod(
                                    "request" + request.toUpperCase(Locale.US),
                                    JSONObject.class);
                            method.invoke(this, jsonObject);
                        } catch (Exception ex) {
                            Logger.getLogger(this.getClass().getName())
                                    .log(Level.SEVERE, null, ex);
                            sendError("Error.003");
                        }

                    }else{
                        sendError("Error.002");
                    }

                } catch (ParseException ex) {
                    Logger.getLogger(this.getClass().getName())
                            .log(Level.SEVERE, null, ex);
                    sendError("Error.001");
                }
            }

            if (mode == MAIN_SERVER_CLIENT)
                this.server.disconnectedClient(this);

            in.close();
            out.close();
            socket.close();

            emit("disconnected");
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName())
                    .log(Level.SEVERE, null, ex);
            emit("connection-failed");
        }
    }

    public void requestEXIT(JSONObject jsonObject)
    {
        run = false;
    }

    public void requestUPDATE(JSONObject jsonObject)
    {
        if (mode != MAIN_SERVER_CLIENT) return;

        if (jsonObject.containsKey("files") 
                && jsonObject.get("files") instanceof JSONArray)
        {
            for(Object file : (JSONArray)jsonObject.get("files"))
            {
                JSONObject temp = (JSONObject)file;
                if (temp.containsKey("name") && temp.containsKey("hash"))
                {
                }
            }

            Map result = new LinkedHashMap();
            result.put("response", "update");

            sendLine(result);
        }
    }

    public void requestSEARCH(JSONObject jsonObject)
    {
        if (mode != MAIN_SERVER_CLIENT) return;

        if (jsonObject.containsKey("name"))
        {
            Map result = new LinkedHashMap();
            result.put("response", "search");
            result.put("files", server.searchFile(jsonObject.get("name").toString()));
            sendLine(result);
        }
    }

    public void requestDOWNLOAD(JSONObject jsonObject)
    {
        if (mode != MAIN_SERVER_CLIENT) return;

        if (jsonObject.containsKey("hash"))
        {
            Map result = new LinkedHashMap();
            result.put("response", "download");
            result.put("peers", server.getFileClients(jsonObject.get("hash").toString()));
            sendLine(result);
        }
    }
}
