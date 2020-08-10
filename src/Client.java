package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * Represents the client
 * 
 * @author fbb3628
 */
public class Client {
    private static final String HOST_NAME = "192.168.1.78"; // Change host name when server starts
    private static final int HOST_PORT = 7777;
    private transient PrintWriter pw; // input stream to chatbox
    private transient BufferedReader br; // output stream from chatbox
    private Socket socket;
    private GUI gui;
    private JTextArea textArea;
    private JTextField textField;
    private boolean isOpen;
    private String name;
    private JList<String> clientList;

    public Client(GUI gui) {
        this.gui = gui;
        this.textArea = gui.textArea;
        this.textField = gui.textField;
        this.clientList = Server.getClientList();
    }

    /**
     * Returns this client's socket
     * @return Socket
     */
    public Socket getSocket() {
        return socket;
    }
    /**
     * Returns the client name
     * @return String
     */
    public String getName() {
        return name;
    }
    /**
     * Sets the client name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void send(String message) {
        Socket receivingSocket = Server.getClientSocket(clientList.getSelectedValue());
        try {
            // Send the name with message to the selected client;
            pw = new PrintWriter(receivingSocket.getOutputStream());
            pw.println(name + ": " + message);
            // Clear the text field
            textField.setText("");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(gui, "An error occured when sending message: " + e, "Error", JOptionPane.ERROR_MESSAGE);;
        }
    }
    /**
     * Starts the client with a socket
     */
    public void startClient() {
        try {
            // Try connect to server
            socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(HOST_NAME, HOST_PORT);
            socket.connect(address, 5000);
            JOptionPane.showMessageDialog(gui, "Successfully connected", "Connected", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            // Show dialog if failed
            JOptionPane.showMessageDialog(gui, "Client cannot connect to server: "+e, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        try {
            // Create an autoflush output stream for the socket
            pw = new PrintWriter(socket.getOutputStream(), true);
            // Create a buffered input stream for this socket
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Set open client to true
            isOpen = true;
            // Send this client's name to server to add to clientList
            pw.println(name + "@" + socket.getInetAddress());
            InnerReceive receive = new InnerReceive();
            Thread thread = new Thread(receive);
            thread.start();
            
        } catch (IOException e) {
            // Handle exceptions
            JOptionPane.showMessageDialog(gui, "Client error with chat: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Disconnects the client from the server
     */
    public void stopClient() {
        // Close all streams and socket
        pw.close();
        isOpen = false;
        try {
            br.close();
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(gui, "Client error with chat: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    /**
     * Inner class for receiving messages
     */
    private class InnerReceive implements Runnable {

        @Override
        public void run() {
            String serverResponse;
            try {
                do {
                    // Read response from sender
                    serverResponse = br.readLine();
                    textArea.setText(textArea.getText() + "\n" + serverResponse);
                } while (isOpen);
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(gui, "Client error with chat: " + e, "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        }
    }

    private class InnerSend implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub

        }
    
        
    }
}