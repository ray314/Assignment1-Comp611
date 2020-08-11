package src;

public class PrivateMessage extends Message {
    private String ipAddress;
    private String destUserName;

    public PrivateMessage(String origUserName, String ipAddress, String destUserName, String message) {
        super(origUserName, message);
        this.origUserName = origUserName;
        this.ipAddress = ipAddress;
        this.destUserName = destUserName;
        this.message = message;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public String getDestUserName() {
        return destUserName;
    }
}