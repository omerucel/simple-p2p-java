/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.simple;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author omer
 */
public class Database {
    private static Database instance;
    Connection connection;

    public synchronized static Database getInstance()
    {
        if (instance == null)
        {
            instance = new Database();
            instance.init();
        }

        return instance;
    }

    private Database(){}

    public void init()
    {
        try {
            Driver driver = (Driver)Class.forName("org.sqlite.JDBC").newInstance();
            DriverManager.registerDriver(driver);

            connection = DriverManager.getConnection("jdbc:sqlite::memory:");
            Statement statement = newStatement();
            statement.executeUpdate("CREATE TABLE "
                    + "file(hash string, size int);");
            statement.executeUpdate("CREATE TABLE "
                    + "client(hash string, ip string);");
            statement.executeUpdate("CREATE TABLE "
                    + "client_file(file_hash string, client_hash string, name string);");
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected Statement newStatement()
    {
        try {
            return connection.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public synchronized void addClient(String hash, String ip)
    {
        try {
            Statement statement = newStatement();
            statement.executeUpdate("INSERT INTO client(hash, ip) "
                    + "VALUES('" + hash + "', '" + ip + "')");
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        notifyAll();
    }

    public synchronized void removeClient(String hash)
    {
        try {
            Statement statement = newStatement();
            statement.executeUpdate("DELETE FROM client "
                    + "WHERE hash = '" + hash + "'");
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        notifyAll();
    }

    public synchronized void addFile(String clientHash, String hash, String name)
    {
        try {
            Statement statement = newStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) AS count "
                    + "FROM file WHERE hash = '" + hash + "'");
            rs.next();
            if (rs.getInt("count") == 0)
                statement.executeUpdate("INSERT INTO file(hash) "
                        + "VALUES('" + hash + "')");
            rs.close();
            statement.executeUpdate("INSERT INTO "
                    + "client_file(file_hash, client_hash, name) "
                    + "VALUES('" + hash + "', '" + clientHash + "', '" + name + "')");
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        notifyAll();
    }

    public synchronized void removeFile(String hash)
    {
        try {
            Statement statement = newStatement();
            statement.executeUpdate("DELETE FROM file "
                    + "WHERE hash = '" + hash + "'");
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        notifyAll();
    }

    public ArrayList<Map> searchFile(String name)
    {
        ArrayList<Map> files = new ArrayList<Map>();

        try {
            Statement statement = newStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT name, "
                    + "(SELECT COUNT(*) FROM client_file "
                    + "WHERE file_hash = pf.file_hash) AS client_count "
                    + "FROM client_file pf WHERE name LIKE '" + name + "%' "
                    + "ORDER BY name ASC");
            while(resultSet.next())
            {
                Map temp = new LinkedHashMap();
                temp.put("name", resultSet.getString("name"));
                temp.put("client_count", resultSet.getInt("client_count"));
                temp.put("hash", resultSet.getString("hash"));
                files.add(temp);
            }
            resultSet.close();
            statement.close();

            return files;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return files;
        }
    }

    public ArrayList<String> getFileClients(String hash)
    {
        ArrayList<String> clients = new ArrayList<String>();

        try {
            Statement statement = newStatement();
            ResultSet resultSet = statement.executeQuery("SELECT ip FROM client "
                    + "WHERE hash IN "
                    + "(SELECT client_hash FROM client_file "
                    + "WHERE file_hash = '" + hash + "') "
                    + "GROUP BY ip");
            while(resultSet.next())
                clients.add(resultSet.getString("ip"));
            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }

        return clients;
    }
}
