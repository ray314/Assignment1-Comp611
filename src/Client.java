package src;

import java.io.Serializable;
import java.net.Socket;

public class Client implements Serializable{
    private String userName;
    private String ipAddress;
    private Socket socket;

    /**
     * Creates a client instance
     * @param userName - User name
     * @param ipAddress - IP address
     * @param socket - Socket
     */
    public Client (String userName, Socket socket) {
        this.userName = userName;
        this.socket = socket;
    }

    /**
     * Returns the socket for this client
     * @return Socket
     */
    public Socket getSocket() {
        return this.socket;
    }

    /**
     * Returns the IP address for this client
     * @return String
     */
    public String getIPAddress() {
        return this.ipAddress;
    }
    /**
     * Sets the IP Address for this client
     * @param ipAddress - The IP address
     */
    public void setIPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    /**
     * Returns the username
     */
    public String toString() {
        return this.userName;
    }
}


