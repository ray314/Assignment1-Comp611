package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Server {
    // Create a new instance of Server
    public final static Server server = new Server();
    public final static int PORT = 7777; // Port number

    // Create a HashMap to store socket's output stream
    // Map stream to ip address
    private HashMap<String, ObjectOutputStream> map;
    // List to store all rooms
    private List<Room> roomList;
    // List to store all clients
    private List<Client> list;
    private boolean stopRequested; // Stop the server

    public static void main(String[] args) {
        server.startServer();
    }

    private Server() {
        map = new HashMap<>();
        roomList = new ArrayList<Room>();
        stopRequested = false;
        list = new ArrayList<>();
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
            //socketMap.put(client.toString() + ":" + client.getIPAddress(), client);
            
            Room room = new Room(socket);
            Thread thread = new Thread(room);
            roomList.add(room);
            thread.start();
            }
            serverSocket.close();
        }
       catch (IOException e)
       {  System.err.println("Can't accept client connection: " + e);
       }
       System.out.println("Server finishing");
    }
    // Send to all clients currently connected
    private void sendToAll(Object message) throws IOException {
        Iterator<Room> it = roomList.iterator();
        while(it.hasNext()) {
            it.next().sendToClient(message);
        }
    }
    // Room that has the socket, client and i/o streams
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
                    // Receive input and send while client is up
                    Object serverResponse = ois.readObject();
                    // Check what object did the server receive
                    if (serverResponse instanceof PrivateMessage) {
                        forwardMessage(serverResponse);
                    } else if (serverResponse instanceof Client) { // Retrieve client details
                        addClient(serverResponse);
                    } else if (serverResponse instanceof Post) { // Send to all
                        sendToAll((Post) serverResponse);
                    } else if (serverResponse instanceof ImageWrapper) {
                        // Send image or post depending on ImageWrapper boolean
                        sendImage(serverResponse);
                    }
                }
                oos.close();
                //ois.close();
            } catch (SocketException e) {
                System.err.println("A client has disconnected");
                // Remove client and socket
                list.remove(client);
                map.remove(client.getIPAddress());
                // Remove room from ArrayList
                roomList.remove(this);
                // Update JList by updating model
                try {
                    sendToAll(list);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + e);
            } catch (IOException e) {
                System.err.println("I/O error"+e);
            } 
        }

        private void sendImage(Object serverResponse) throws IOException {
            // Typecast into ImageWrapper
            ImageWrapper imageWrapper = (ImageWrapper) serverResponse;
            if (imageWrapper.getPostBoolean()) {
                // Post
                sendToAll(imageWrapper);
            } else { // Forward to another client
                // Obtain the output stream from map
                ObjectOutputStream oos = map.get(imageWrapper.getIPAddress());
                oos.writeObject(imageWrapper);
                oos.reset();
            }
        }
        // Send a message to this client
        private void sendToClient(Object message) {
            try {
                oos.writeObject(message);
                oos.reset();
            } catch (IOException e) {
                System.err.println("An error occured when sending to client: " + e);
            }
        }
        // Add client to socketMap and Jlist
        private void addClient(Object serverResponse) throws IOException {
            // Typecast into Client object
            Client client = (Client) serverResponse;
            client.setIPAddress(socket.getInetAddress().getHostAddress());
            // Map output stream to ip address
            map.put(client.getIPAddress(), oos);
            this.client = client;
            // Add client to list
            list.add(client);
            // Update all client's JList
            sendToAll(list);
        }
        // Forward the message to the destination client
        private void forwardMessage(Object serverResponse) throws IOException {
            // Typecast into PrivateMessage object
            PrivateMessage sendMsg = (PrivateMessage) serverResponse;
            // Obtain message details
            String destIPAddress = sendMsg.getIPAddress();
            // Obtain the destination stream
            ObjectOutputStream destOOS = map.get(destIPAddress); 
            // Write message to destination stream
            destOOS.writeObject(sendMsg);
            // Also write message to origin stream
            oos.writeObject(sendMsg);
            oos.reset();
        }
    }
}