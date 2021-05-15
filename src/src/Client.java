package src;

import java.io.Serializable;

/**
 * The client
 * @author fbb3628
 */
public class Client implements Serializable{
    private String userName;
    private String ipAddress;
    private boolean addRemove;
    /**
     * Creates a client instance
     * @param userName - User name
     */
    public Client (String userName) {
        this.userName = userName;
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
     * Set the boolean flag on whether to add or remove client from the list
     * @param addRemove - The boolean flag
     */ 
    public void setAddRemoveClient(boolean addRemove) {
        this.addRemove = addRemove;
    }

    public boolean getAddRemoveClient() {
        return addRemove;
    }
    /**
     * Returns the username
     */
    public String toString() {
        return this.userName;
    }
}


