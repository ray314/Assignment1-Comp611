package src;

import java.io.Serializable;
/**
 * An abstract class for encapsulating messages
 */
public abstract class Message implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected String origUserName;
    protected String message;

    public Message(String origUserName, String message) {
        this.origUserName = origUserName;
        this.message = message;
    }

    public String getOrigUserName() {
        return origUserName;
    }

    public String getMessage() {
        return message;
    }
}