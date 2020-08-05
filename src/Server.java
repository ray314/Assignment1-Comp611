package src;

/**
   A class that represents a server in a number guessing game where
   GuessClient objects connect to this GuessServer and try to guess
   a random integer value between min (incl) and max (excl)
   The game initiates with a response from the server and ends when
   the server responds with "Correct guess!"
   @author Andrew Ensor
*/
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
   private boolean stopRequested;
   private static Server instance;
   public static final int PORT = 7777; // some unused port number
   
   private Server() {  
      stopRequested = false;
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
               
            // start a chatbox with this connection, note that a server
            // might typically keep a reference to each chatbox
            ChatBox chatbox = new ChatBox(socket);
            Thread thread = new Thread(chatbox);
            thread.start();
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