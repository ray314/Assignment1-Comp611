package src;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.GridLayout;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JLabel;

/**
 * The GUI for the client
 */
public class GUI extends JFrame implements ActionListener {

    private final String HOST_NAME = "";
    private final int PORT = 7777;
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
    protected Server server;

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
        server = Server.server;
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
        list = server.getClientList();

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

				server.sendToOneClient(destClient.toString(), destClient.getIPAddress(), textField.getText());
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
            Client client = new Client(userName, socket.getInetAddress().getHostAddress(), socket);
            System.out.println(socket.getInetAddress().getHostAddress());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}