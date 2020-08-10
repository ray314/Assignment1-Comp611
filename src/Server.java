package src;

import java.io.BufferedReader;
/**
   A class that represents a server in a number guessing game where
   GuessClient objects connect to this GuessServer and try to guess
   a random integer value between min (incl) and max (excl)
   The game initiates with a response from the server and ends when
   the server responds with "Correct guess!"
   @author Andrew Ensor
*/
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class Server {

    private boolean stopRequested;
    private static Server instance;
    public static final int PORT = 7777; // some unused port number

    // Create a JList with a Default list model
    private final static DefaultListModel<String> model = new DefaultListModel<>();
    private final static JList<String> clientList = new JList<>(model);
    // Create a HashMap to store the sockets and map them to names+IP address
    private final static HashMap<String, Socket> map = new HashMap<>();
   
    private Server() {  
       stopRequested = false;
       // Set list to single selection only
       clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       clientList.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    public static Server getInstance() {
        if (instance == null) {
            synchronized(Server.class) { // Synchronize so concurrent 
                                         //threads don't access at the same time
                 if (instance == null) {
                     instance = new Server();
                 }
            }   
        }
        return instance;
    }
 
    /**
     * Returns the JList
     * @return JList
     */
    public static JList<String> getClientList() {
        return Server.clientList;
    }
 
    /**
     * Returns the client socket based off the name
     * @param name - String
     * @return Socket
     */
    public static Socket getClientSocket(String name) {
        return Server.map.get(name);
    }
    
    // start the server if not already started and repeatedly listen
    // for client connections until stop requested
    public void startServer()
    {  stopRequested = false;
       ServerSocket serverSocket = null;
       try
       {  serverSocket = new ServerSocket(PORT);
          System.out.println("Server started at "
             + InetAddress.getLocalHost() + " on port " + PORT);
       }
       catch (IOException e)
       {  System.err.println("Server can't listen on port: " + e);
          System.exit(-1);
       }
       try
       {  while (!stopRequested)
          {  // block until the next client requests a connection
             // note that the server socket could set an accept timeout
             Socket socket = serverSocket.accept();
             System.out.println("Connection made with "
                + socket.getInetAddress());
             
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             // Retrieve the name
             String name = br.readLine();
             // Add name to JList
             model.addElement(name);
             // Add name and socket to map
             map.put(name, socket);
             // Close stream
             br.close();
          }
          serverSocket.close();
       }
       catch (IOException e)
       {  System.err.println("Can't accept client connection: " + e);
       }
       System.out.println("Server finishing");
    }
    
    // stops server AFTER the next client connection has been made
    // (since this server socket doesn't timeout on client connections)
    public void requestStop()
    {  stopRequested = true;
    }
    
}