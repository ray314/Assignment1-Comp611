package src;

import java.io.File;
import java.io.Serializable;

/**
 * CustomImage for representing an image and whether to post or send it as PM
 */
public class ImageWrapper implements Serializable{

    private boolean toPost;
    private File file;
    private String ipAddress;
    private String origUserName;
    
    public ImageWrapper(File file, String origUserName) {
        this.file = file;
        this.ipAddress = ipAddress;
        this.origUserName = origUserName;
    }
    /**
     * Set whether to Post or send the image to client
     * @param toPost
     */
    public void setPostBoolean(boolean toPost) {
        this.toPost = toPost;
    }
    /**
     * Returns if it set to post or send to client
     * @return a boolean
     */
    public boolean getPostBoolean() {
        return toPost;
    }
    /**
     * Returns the file that contains the image
     * @return the image file
     */
    public File getImage() {
        return file;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public void setIPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getOrigUserName() {
        return origUserName;
    }
}
