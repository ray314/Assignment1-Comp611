package src;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A panel for display image received from the server
 */
public class ImagePanel extends JPanel implements Runnable{
    private BufferedImage image;
    private String origUserName;

    public ImagePanel(BufferedImage image, String origUserName) {
        this.image = image;
        this.origUserName = origUserName;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }

    @Override
    public void run() {
        // Create JFrame
        JFrame imageFrame = new JFrame();
        // Set up and add the image to the frame
        imageFrame.setTitle("You have received an image from " + origUserName);
        imageFrame.add(this);
        imageFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        imageFrame.setVisible(true);
    }
}