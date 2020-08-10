package src;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.GridLayout;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JLabel;

/**
 * The GUI for the client
 */
public class GUI extends JFrame implements ActionListener, WindowListener {

    private final String HOST_NAME = "";
    private final int PORT = 7777;
    private boolean closing;

    public String userName;

    protected JTextField textField;
    protected JTextArea textArea;
    protected JPanel southPanel;
    protected JPanel eastPanel;
    protected JPanel northPanel;
    protected JLabel lblTitle;
    protected JList<Client> list;
    protected JButton btnConnect;
    protected JButton btnSend;
    protected Client client;

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
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

        this.setBounds(100, 100, 450, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(new BorderLayout(0, 0));

        northPanel = new JPanel();
        this.getContentPane().add(northPanel, BorderLayout.NORTH);
        // Title
        lblTitle = new JLabel("Your name: ");
        northPanel.add(lblTitle);

        eastPanel = new JPanel();
        this.getContentPane().add(eastPanel, BorderLayout.EAST);
        eastPanel.setLayout(new GridLayout(0, 1, 0, 0));
        // List of connected clients
        list = new JList();

        btnConnect = new JButton("Connect");
        btnConnect.addActionListener(this);
        eastPanel.add(btnConnect);

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
        textArea.setEditable(false);
        this.getContentPane().add(textArea, BorderLayout.CENTER);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == btnConnect) {
            // Connect to server
            connect();
		} else if (source == btnSend) {
			Client destClient = list.getSelectedValue();
			if (!textField.getText().equals("") && destClient != null) {
                // Create message
                SendMessage msg = new SendMessage(userName, destClient.getIPAddress(), destClient.toString(), textField.getText());
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(destClient.getSocket().getOutputStream());
                    oos.writeObject(msg); // Write message to stream
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(this, "An error occurred when sending message: "+e1, "Error", JOptionPane.ERROR_MESSAGE);
                }
			}
		}
    }
    
    private void connect() {
        SetNameDialog dialog = new SetNameDialog(this);
        // Don't let the user close dialog until name is entered
        dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
        btnSend.setEnabled(true);
        btnConnect.setEnabled(false); // Disable connect after client connected
        try {
            Socket socket = new Socket(HOST_NAME, PORT);
            client = new Client(userName, socket);
            System.out.println(socket.getInetAddress().getHostAddress());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    // Update client list periodically
    private class UpdateClientList implements Runnable {

        @Override
        public void run() {
            Socket socket = client.getSocket();
            try {
                // Create streams
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                do {
                    // Write a JList into stream
                    oos.writeObject(list);
                    // Retrieve updated list from server
                    list = (JList<Client>) ois.readObject();
                    Thread.sleep(500); // Wait every half second
                } while (!closing); // End loop when client closes
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.err.println("Error updating client list: " + e);
            }
            
        }
        
    }

    @Override
    public void windowClosing(WindowEvent e) {

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