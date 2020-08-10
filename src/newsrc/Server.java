import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    // Create a new instance of Server
    public final static Server server = new Server();
    public final static int PORT = 7777; // Port number

    // Create a HashMap to store clients
    // Map clients to username + ipaddress
    private HashMap<String, Client> map;
    private boolean stopRequested; // Stop the server

    private Server() {
        map = new HashMap<>();
        stopRequested = false;
    }
    /**
     * Add a client to the server
     * @param client
     */
    public void addClient(Client client) {
        map.put(client.getUserName() + ":" + client.getIPAddress(), client);
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
                pw.println(client.getUserName() + ": " + message);
                socket.close();
                pw.close();
            } catch (IOException e) {
                System.err.println("An error occured: "+e);
            }
        }
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
             
            // Send the IP address to client to store it
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
             //BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             // Retrieve the name
             //String name = br.readLine();
             // Add name to JList
             //model.addElement(name);
             // Add name and socket to map
             //map.put(name, socket);
             // Close stream
             //br.close();
          }
          serverSocket.close();
       }
       catch (IOException e)
       {  System.err.println("Can't accept client connection: " + e);
       }
       System.out.println("Server finishing");
    }
}