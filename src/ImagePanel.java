package src;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.awt.Graphics;
import java.awt.Toolkit;

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