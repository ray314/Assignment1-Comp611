package src;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import java.awt.GridLayout;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JLabel;

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
                oos.flush();
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
            oos.reset();
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
        btnSend.setEnabled(true);
        btnPost.setEnabled(true);
        btnConnect.setEnabled(false); // Disable connect after client connected
        try {
            // Create socket and client
            this.socket = new Socket(HOST_NAME, PORT);
            client = new Client(userName);
            oos = new ObjectOutputStream(socket.getOutputStream()); // Output stream for this client
            Thread thread = new InnerReceive();
            thread.start();
            
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
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
                    }
                    
                } while (!closing); // End loop when client closes
            } catch (SocketException e) { // Connection closed, disable post, send buttons and re-enable connect
                System.err.println("Error with connection. Please connect again");
                btnSend.setEnabled(false);
                btnPost.setEnabled(false);
                btnConnect.setEnabled(true);
            } catch (IOException e) {
                System.err.println("Error receiving messages: " + e);
            } catch (ClassNotFoundException e) {
                System.err.println("Wrong object type: " + e);
            }
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