package src;

import java.awt.EventQueue;

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

    private final String HOST_NAME = "172.28.44.120";
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
    protected JList<Client> list;
    protected JButton btnConnect;
    protected JButton btnSend;
    protected JButton btnPost;
    protected Client client;
    protected Socket socket;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUI window = new GUI();
                    window.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

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
        list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eastPanel.add(list);

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
            list.clearSelection();
        }
    }
    // Send message to specific client or post to everyone
    private void sendMessage() {
        Client destClient = list.getSelectedValue();
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
            // Send to all
        } else if (!text.equals("")){
            try {
                Post post = new Post(client.toString(), text);
                oos.writeObject(post);
                oos.flush();
                
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        textField.setText(""); // Clear text field
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
            // Create stream
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.oos.writeObject(client); // Write client to stream
            this.oos.flush();
            Thread thread2 = new InnerReceive();
            //thread.start();
            thread2.start();
            
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
                do {
                    // Retrieve messages from server
                    Object serverResponse = ois.readObject();
                    if (serverResponse instanceof PrivateMessage ||
                        serverResponse instanceof Post) {
                        displayMessage(serverResponse);
                    } else if (serverResponse instanceof DefaultListModel) {
                        // Update JList
                        System.out.println(model.getSize());
                        model = (DefaultListModel<Client>) serverResponse;
                    }
                    
                } while (!closing); // End loop when client closes
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error receiving messages: " + e);
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