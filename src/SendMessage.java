package src;

import java.io.Serializable;

public class SendMessage implements Serializable{
    private String origUserName;
    private String ipAddress;
    private String destUserName;
    private String message;

    public SendMessage(String origUserName, String ipAddress, String destUserName, String message) {
        this.origUserName = origUserName;
        this.ipAddress = ipAddress;
        this.destUserName = destUserName;
        this.message = message;
    }

    public String getOrigUserName() {
        return origUserName;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public String getDestUserName() {
        return destUserName;
    }

    public String getMessage() {
        return message;
    }
}