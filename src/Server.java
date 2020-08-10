package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class Server {
    // Create a new instance of Server
    public final static Server server = new Server();
    public final static int PORT = 7777; // Port number

    // Create a HashMap to store clients
    // Map clients to username + ipaddress
    private HashMap<String, Client> map;
    private DefaultListModel<Client> model;
    // Client list
    private JList<Client> list;
    private boolean stopRequested; // Stop the server

    private Server() {
        map = new HashMap<>();
        stopRequested = false;
        model = new DefaultListModel<>();
        list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    public JList<Client> getClientList() {
        return list;
    }
    /**
     * Sends a message to a client. This method is synchronized.
     * @param userName - Username
     * @param ipAddress - IP address
     * @param message - Message
     */
    public void sendToOneClient(String userName, String ipAddress, String message) {
        synchronized(this) {
            Client client = map.get(userName + ":" + ipAddress);

            Socket socket = client.getSocket();
            
            // Sending the response back to the client
            try {
                PrintWriter pw = new PrintWriter(socket.getOutputStream());
                pw.println(client.toString()+ ": " + message);
                socket.close();
                pw.close();
            } catch (IOException e) {
                System.err.println("An error occured: "+e);
            }
        }
    }

    // start the server if not already started and repeatedly listen
    // for client connections until stop requested
    public void startServer() {
        stopRequested = false;
        ServerSocket serverSocket = null;
        try { 
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started at " + InetAddress.getLocalHost() + " on port " + PORT);
        } catch (IOException e) { 
            System.err.println("Server can't listen on port: " + e);
            System.exit(-1);
        }
        try {  
            while (!stopRequested) {   // block until the next client requests a connection
            // note that the server socket could set an accept timeout
            Socket socket = serverSocket.accept();
            System.out.println("Connection made with " + socket.getInetAddress());
            //Client client = new Client(socket.getInetAddress().getHostName(), socket.getInetAddress().getHostAddress(), socket);
            //map.put(client.toString() + ":" + client.getIPAddress(), client);
            
            Room room = new Room(socket);
            Thread thread = new Thread(room);
            thread.start();
            }
            serverSocket.close();
        }
       catch (IOException e)
       {  System.err.println("Can't accept client connection: " + e);
       }
       System.out.println("Server finishing");
    }

    private class Room implements Runnable{

        private Socket socket;

        public Room(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            // Create object output and input streams
            ObjectOutputStream oos;
            ObjectInputStream ois;
            try {
                // Create input stream
                ois = new ObjectInputStream(socket.getInputStream());
                // Receive input and send while client is up
                do {
                    Object serverResponse = ois.readObject();
                    // Check what object did the server receive
                    if (serverResponse instanceof SendMessage) {
                        forwardMessage(serverResponse);
                    } else if (serverResponse instanceof Client) { // Retrieve client details
                        addClient(serverResponse);
                    } else if (serverResponse instanceof JList) { // Send in the client list
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(list); // Write list into stream
                    }

                } while(!socket.isClosed());
                // Close streams when finished
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("An error occured: " + e);
            } 
            
        }
        // Add client to map and Jlist
        private void addClient(Object serverResponse) {
            // Typecast into Client object
            Client client = (Client) serverResponse;
            client.setIPAddress(socket.getInetAddress().getHostAddress());
            // Put client into the hash map
            map.put(client.toString() + ":" + client.getIPAddress(), client);
            // Add client to JList
            model.addElement(client);
        }
        // Forward the message to the destination client
        private void forwardMessage(Object serverResponse) throws IOException {
            ObjectOutputStream oos;
            // Typecast into SendMessage object
            SendMessage sendMsg = (SendMessage) serverResponse;
            // Obtain message details
            String destUserName = sendMsg.getDestUserName();
            String destIPAddress = sendMsg.getIPAddress();
            // Create new message object
            Socket destSocket = map.get(destUserName + ":" + destIPAddress).getSocket();
            // Create output stream for destination socket
            oos = new ObjectOutputStream(destSocket.getOutputStream());
            // Write object to stream
            oos.writeObject(sendMsg);
        }
    }
}