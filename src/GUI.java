package src;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.GroupLayout;

/**
 * The GUI for the client
 */
public class GUI extends JFrame implements ActionListener, WindowListener {

    private final String HOST_NAME = "192.168.1.78";
    private final int PORT = 7777;
    private boolean closing; // Check if GUI closed
    private ObjectOutputStream oos;
    public String userName;

    protected JTextField textField;
    protected JTextArea textArea;
    protected JPanel southPanel;
    protected JPanel eastPanel;
    protected JPanel northPanel;
    protected JLabel lblTitle;
    protected DefaultListModel<Client> model;
    protected JList<Client> listView;
    protected JButton btnConnect;
    protected JButton btnSend;
    protected JButton btnSendImage;
    protected JButton btnPost;
    protected Client client;
    protected Socket socket;
    /**
     * Create the application.
     */
    public GUI() {
        initialize();
        closing = false;
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

        this.setBounds(100, 100, 450, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(new BorderLayout(0, 0));
        this.addWindowListener(this);

        northPanel = new JPanel();
        this.getContentPane().add(northPanel, BorderLayout.NORTH);
        // Title
        lblTitle = new JLabel("Your name: ");
        northPanel.add(lblTitle);

        eastPanel = new JPanel();
        this.getContentPane().add(eastPanel, BorderLayout.EAST);
        eastPanel.setLayout(new GridLayout(0, 1, 0, 0));
        // List of connected clients
        model = new DefaultListModel<>();
        listView = new JList<>(model);
        listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eastPanel.add(listView);

        btnConnect = new JButton("Connect");
        btnConnect.addActionListener(this);
        eastPanel.add(btnConnect);

        btnPost = new JButton("Post");
        btnPost.setEnabled(false);
        btnPost.addActionListener(this);
        eastPanel.add(btnPost);

        southPanel = new JPanel();
        this.getContentPane().add(southPanel, BorderLayout.SOUTH);
        southPanel.setLayout(new GridLayout(0, 2, 0, 0));

        textField = new JTextField();
        southPanel.add(textField);
        textField.setColumns(10);

        btnSend = new JButton("Send");
        btnSend.addActionListener(this); // Add action listener
        btnSend.setEnabled(false); // Don't enable until client has connected
        southPanel.add(btnSend);

        btnSendImage = new JButton("Send/Post Image");
        btnSendImage.addActionListener(this);
        btnSendImage.setEnabled(false);
        eastPanel.add(btnSendImage);

        textArea = new JTextArea();
        JScrollPane scroll = new JScrollPane(textArea);
        textArea.setEditable(false);
        this.getContentPane().add(scroll, BorderLayout.CENTER);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == btnConnect) {
            // Connect to server
            connect();
		} else if (source == btnSend) {
            sendMessage();
		} else if (source == btnPost) {
            sendPost();
        } else if (source == btnSendImage) {
            openImage();
        }
    }
    // Send message to specific client or post to everyone
    private void sendMessage() {
        Client destClient = listView.getSelectedValue();
        String text = textField.getText();
        if (!text.equals("") && destClient != null) {
            // Create message
            PrivateMessage msg = new PrivateMessage(userName, destClient.getIPAddress(), destClient.toString(), text);
            try {
                oos.writeObject(msg); // Write message to stream
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(this, "An error occurred when sending message: "+e1, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        textField.setText(""); // Clear text field
    }

    // Send post to all clients
    private void sendPost() {
        try {
            Post post = new Post(client.toString(), textField.getText());
            oos.writeObject(post);
            textField.setText(""); // Clear text field
            
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(this, "An error occurred when posting: "+e1, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void connect() {
        SetNameDialog dialog = new SetNameDialog(this);
        // Don't let the user close dialog until name is entered
        dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
        try {
            // Enable the posting/sending buttons
            btnSend.setEnabled(true);
            btnPost.setEnabled(true);
            btnSendImage.setEnabled(true);
            btnConnect.setEnabled(false); // Disable connect after client connected
            // Create socket and client
            this.socket = new Socket(HOST_NAME, PORT);
            client = new Client(userName);
            oos = new ObjectOutputStream(socket.getOutputStream()); // Output stream for this client
            Thread thread = new InnerReceive();
            thread.start();
            
        } catch (ConnectException e) {
            System.err.println("Error connecting to server. Please make sure the server is running: " + e);
        } catch (IOException e) {
            System.err.println("Error creating output stream: " +e);
        }
    }
    // Send an image to server by byte array
    private void sendImage(ImageWrapper imageWrapper) throws IOException {
        // Show options
        String[] options = {"Send to selected client", "Send to all clients"};
        // Ask the user whether to send or post
        String returnString = (String) JOptionPane.showInputDialog(this, "Do you want to send image to client or post to everyone?",
            "Send or Post?", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (returnString.equals(options[0])) {
            // Send to client
            // Check if a client has been selected from the list
            if (listView.getSelectedValue() != null) {
                imageWrapper.setPostBoolean(false);
                // Set the IP address destination
                imageWrapper.setIPAddress(listView.getSelectedValue().getIPAddress());
            } else {
                // Else pop a dialog informing the user that a client has not been selected
                JOptionPane.showMessageDialog(this, "Please select a client first", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
        } else {
            // Post to all clients
            imageWrapper.setPostBoolean(true);
        }
        // Write to stream
        oos.writeObject(imageWrapper);
    }
    // Open image
    private void openImage() {
        JFileChooser fChooser = new JFileChooser();
        fChooser.setDialogTitle("Select an image");
        fChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPEG/JPG and PNG files", "png", "jpeg", "jpg");
        // Add file extension filter to file chooser
        fChooser.addChoosableFileFilter(filter);
        int returnVal = fChooser.showOpenDialog(this);
        // When user selected a file
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fChooser.getSelectedFile();
            // Handle exceptions
            try {
                // Wrap file into custom image class
                ImageWrapper imageWrapper = new ImageWrapper(file, client.toString());
                // Send the image
                sendImage(imageWrapper);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error while opening image: "+e, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // Inner class to receive messages
    private class InnerReceive extends Thread {
        @Override
        public void run() {
            try {
                // Create streams
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                oos.writeObject(client); // Write client to stream
                do {
                    // Retrieve messages from server
                    Object serverResponse = ois.readObject();
                    if (serverResponse instanceof PrivateMessage ||
                        serverResponse instanceof Post) {
                        displayMessage(serverResponse);
                    } else if (serverResponse instanceof List) {
                        updateJList(serverResponse);
                    } else if (serverResponse instanceof ImageWrapper) {
                        displayImage(serverResponse);
                    }
                    
                } while (!closing); // End loop when client closes
            } catch (SocketException e) { // Connection closed, disable post, send buttons and re-enable connect
                System.err.println("Error with connection. Please connect again");
                btnSend.setEnabled(false);
                btnPost.setEnabled(false);
                btnSendImage.setEnabled(false);
                btnConnect.setEnabled(true);
            } catch (IOException e) {
                System.err.println("Error receiving messages: " + e);
            } catch (ClassNotFoundException e) {
                System.err.println("Wrong object type: " + e);
            }
        }

        private void displayImage(Object serverResponse) throws IOException {
            // Typecast into ImageWrapper
            ImageWrapper imageWrapper = (ImageWrapper) serverResponse;
            BufferedImage image = imageWrapper.getImage();
            System.out.println(imageWrapper.getOrigUserName());
            // Run the JFrame into another thread
            EventQueue.invokeLater(new ImagePanel(image, imageWrapper.getOrigUserName()));
        }

        private void updateJList(Object serverResponse) {
            // Update JList
            List<Client> list = (List) serverResponse;
            model.clear(); // Clear the model
            Iterator<Client> it = list.iterator();
            while(it.hasNext()) { // Iterate through the list and replace model elements
                model.addElement(it.next());
            }
        }

        private void displayMessage(Object serverResponse) {
            // Typecast into PrivateMessage or Post
            Message msg;
            if (serverResponse instanceof PrivateMessage) {
                msg = (PrivateMessage) serverResponse;
            } else {
                msg = (Post) serverResponse;
            }
            // Get text area and add new text in new line
            String message = textArea.getText() + "\n" + msg.getOrigUserName() + ": " + msg.getMessage();
            // Set text area
            textArea.setText(message);
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        closing = true;
    }

    // Rest useless methods
    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowClosed(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowActivated(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // TODO Auto-generated method stub

    }
}