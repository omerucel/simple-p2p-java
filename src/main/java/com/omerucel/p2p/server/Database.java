/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omerucel.p2p.server;

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

    public interface IQuery{
        public void run(Statement statement) throws SQLException;
    };

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
            statement.executeUpdate("CREATE TABLE file(hash string, size int);");
            statement.executeUpdate("CREATE TABLE peer(hash string, ip string);");
            statement.executeUpdate("CREATE TABLE peer_file(file_hash string, peer_hash string, name string);");
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

    public synchronized void addPeer(String hash, String ip)
    {
        try {
            Statement statement = newStatement();
            statement.executeUpdate("INSERT INTO peer(hash, ip) VALUES('" + hash + "', '" + ip + "')");
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        notifyAll();
    }

    public synchronized void removePeer(String hash)
    {
        try {
            Statement statement = newStatement();
            statement.executeUpdate("DELETE FROM peer WHERE hash = '" + hash + "'");
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        notifyAll();
    }

    public synchronized void addFile(String peerHash, String hash, String name)
    {
        try {
            Statement statement = newStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) AS count FROM file WHERE hash = '" + hash + "'");
            rs.next();
            if (rs.getInt("count") == 0)
                statement.executeUpdate("INSERT INTO file(hash) VALUES('" + hash + "')");
            rs.close();
            statement.executeUpdate("INSERT INTO peer_file(file_hash, peer_hash, name) VALUES('" + hash + "', '" + peerHash + "', '" + name + "')");
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
            statement.executeUpdate("DELETE FROM file WHERE hash = '" + hash + "'");
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        notifyAll();
    }

    public Map searchFile(String name)
    {
        Map files = new LinkedHashMap();
        try {
            Statement statement = newStatement();
            ResultSet resultSet = statement.executeQuery("SELECT name, (SELECT COUNT(*) FROM peer_file WHERE file_hash = pf.file_hash) AS peer_count FROM peer_file pf WHERE name LIKE '" + name + "%' ORDER BY name ASC");
            while(resultSet.next())
            {
                files.put(resultSet.getString("name").toString(), resultSet.getInt("peer_count"));
            }
            resultSet.close();
            statement.close();

            return files;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return files;
        }
    }

    public ArrayList<String> getFilePeers(String hash)
    {
        ArrayList<String> peers = new ArrayList<String>();

        try {
            Statement statement = newStatement();
            ResultSet resultSet = statement.executeQuery("SELECT ip FROM peer WHERE hash IN (SELECT peer_hash FROM peer_file WHERE file_hash = '" + hash + "') GROUP BY ip");
            while(resultSet.next())
                peers.add(resultSet.getString("ip"));
            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }

        return peers;
    }
}
