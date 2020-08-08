package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
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
public class Client implements Serializable{
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
    private JList<Client> clientList;

    public Client(GUI gui, String name) {
        this.gui = gui;
        this.textArea = gui.textArea;
        this.textField = gui.textField;
        this.name = name;
        this.clientList = Server.getClientList();
    }

    /**
     * Returns this client's socket
     * @return Socket
     */
    public Socket getSocket() {
        return socket;
    }

    public void send() {
        Client receivingClient = clientList.getSelectedValue();
        if (!textField.getText().equals("") && receivingClient != null) {
            try {
                pw = new PrintWriter(receivingClient.getSocket().getOutputStream());
                pw.println(name + ": " + textField.getText());
                textField.setText("");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(gui, "An error occured when sending message: " + e, "Error", JOptionPane.ERROR_MESSAGE);;
            }
        }
    }

    public void startClient() {
        try {
            // Try connect to server
            socket = new Socket(HOST_NAME, HOST_PORT);
            JOptionPane.showMessageDialog(gui, "Successfully connected", "Connected", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            // Show dialog if failed
            JOptionPane.showMessageDialog(gui, "Client cannot connect to server", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        try {
            // Create an autoflush output stream for the socket
            //pw = new PrintWriter(socket.getOutputStream(), true);
            // Create a buffered input stream for this socket
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Set open client to true
            isOpen = true;
            // Send this Client object to server
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(this);
            // Close object stream
            oos.close();
            // Replace the JList with the Server one
            gui.list = Server.getClientList();
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

    private class InnerReceive implements Runnable {

        @Override
        public void run() {
            String serverResponse;
            try {
                do {
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