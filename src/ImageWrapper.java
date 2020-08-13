package src;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import java.awt.image.BufferedImage;

/**
 * ImageWrapper for representing an image and whether to post or send it as PM
 */
public class ImageWrapper implements Serializable {

    private boolean toPost;
    private byte[] bytes;
    private String ipAddress;
    private String origUserName;

    public ImageWrapper(File file, String origUserName) {
        this.origUserName = origUserName;
        BufferedImage image;
        try {
            image = ImageIO.read(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            bytes = baos.toByteArray();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error while converting image to bytes" + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
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
     * Returns a BufferedImage of this object
     * 
     * @return the image file
     * @throws IOException
     */
    public BufferedImage getImage() throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        return image;
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
