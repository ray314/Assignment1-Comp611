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

    // Create a HashMap to store sockets
    // Map sockets to ip addresses
    private HashMap<String, Socket> map;
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
            ObjectOutputStream oos = null;
            // Create input stream
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                while (!stopRequested) {
                    
                    // Receive input and send while client is up
                    Object serverResponse = ois.readObject();
                    // Check what object did the server receive
                    if (serverResponse instanceof SendMessage) {
                        forwardMessage(serverResponse);
                    } else if (serverResponse instanceof Client) { // Retrieve client details
                        addClient(serverResponse);
                    } else if (serverResponse instanceof JList) { // Send in the client list
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(list); // Write list into stream
                        oos.flush();
                    }
                    // Close streams when finished
                    //ois.close();
                }
                oos.close();
                ois.close();
            } catch (IOException e) {
                System.err.println("An error occured: " + e);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + e);
            } 
            
        }
        // Add client to map and Jlist
        private void addClient(Object serverResponse) {
            // Typecast into Client object
            Client client = (Client) serverResponse;
            client.setIPAddress(socket.getInetAddress().getHostAddress());
            // Put client into the hash map
            map.put(client.getIPAddress(), socket);
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
            Socket destSocket = map.get(destIPAddress);
            // Create output stream for destination socket
            oos = new ObjectOutputStream(destSocket.getOutputStream());
            // Write object to stream
            oos.writeObject(sendMsg);
            //Close stream
            oos.close();
        }
    }
}