package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

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
    // Client listthis.ois = new ObjectInputStream(socket.getInputStream());
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
        private Client client;
        // Streams
        private ObjectOutputStream oos;
        private ObjectInputStream ois;

        public Room(Socket socket) {
            this.socket = socket;
            try {
                this.oos = new ObjectOutputStream(socket.getOutputStream());
                this.ois = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("An error occured when creating streams");
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (!stopRequested) {
                    
                    System.out.println("Test");
                    // Receive input and send while client is up
                    Object serverResponse = ois.readObject();
                    // Check what object did the server receive
                    if (serverResponse instanceof PrivateMessage) {
                        forwardMessage(serverResponse);
                    } else if (serverResponse instanceof Client) { // Retrieve client details
                        addClient(serverResponse);
                    } else if (serverResponse instanceof JList) { // Send in the client list
                        updateJList();
                    } else if (serverResponse instanceof Post) { // Send to all
                        sendToAll(serverResponse);
                    }
                }
                oos.close();
                //ois.close();
            } catch (SocketException e) {
                System.err.println("A client has disconnected");
                // Remove client and socket
                model.removeElement(client);
                map.remove(client.getIPAddress());
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + e);
            } catch (IOException e) {
                System.err.println("I/O error"+e);
            } 
        }

        private void sendToAll(Object serverResponse) throws IOException {
            for (Map.Entry<String,Socket> entry : map.entrySet()) {
                ObjectOutputStream oos = new ObjectOutputStream(entry.getValue().getOutputStream());
                Post post = (Post) serverResponse;
                oos.writeObject(post);
                oos.flush();
                oos.reset();
            }
        }

        private void updateJList() throws IOException {
            oos.writeObject(list); // Write list into stream
            oos.flush();
            oos.reset();
        }
        // Add client to map and Jlist
        private void addClient(Object serverResponse) {
            // Typecast into Client object
            Client client = (Client) serverResponse;
            client.setIPAddress(socket.getInetAddress().getHostAddress());
            // Put client into the hash map
            map.put(client.getIPAddress(), socket);
            this.client = client;
            // Add client to JList
            model.addElement(client);
        }
        // Forward the message to the destination client
        private void forwardMessage(Object serverResponse) throws IOException {
            ObjectOutputStream oos;
            // Typecast into PrivateMessage object
            PrivateMessage sendMsg = (PrivateMessage) serverResponse;
            // Obtain message details
            String destIPAddress = sendMsg.getIPAddress();
            // Create new message object
            Socket destSocket = map.get(destIPAddress);
            // Create output stream for destination socket
            oos = new ObjectOutputStream(destSocket.getOutputStream());
            // Write object to stream
            oos.writeObject(sendMsg);
            //Close stream
            oos.flush();
            oos.reset();
        }
    }
}