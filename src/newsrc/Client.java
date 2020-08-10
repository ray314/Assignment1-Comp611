import java.net.Socket;

public class Client {
    private String userName;
    private String ipAddress;
    private Socket socket;

    /**
     * Creates a client instance
     * @param userName - User name
     * @param ipAddress - IP address
     * @param socket - Socket
     */
    public Client (String userName, String ipAddress, Socket socket) {
        this.userName = userName;
        this.ipAddress = ipAddress;
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
     * Returns the username for this client
     * @return String
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Returns the IP address for this client
     * @return String
     */
    public String getIPAddress() {
        return this.ipAddress;
    }
}


