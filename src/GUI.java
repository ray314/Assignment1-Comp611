package src;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JLabel;

/**
 * The GUI for the client
 */
public class GUI extends JFrame implements ActionListener {

	protected JTextField textField;
	protected JTextArea textArea;
	protected JPanel southPanel;
	protected JPanel eastPanel;
	protected JPanel northPanel;
	protected JLabel lblTitle;
	protected JList<String> list;
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
                    window.client = new Client(window);
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
		list = Server.getClientList();
		
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
            if (client.getName() != null) {
				
				// Note that server must be started first to obtain list
            } else {
				SetNameDialog dialog = new SetNameDialog(this);
				// Don't let the user close dialog until name is entered
				dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
				dialog.setVisible(true);
				btnSend.setEnabled(true);
				btnConnect.setEnabled(false); // Disable connect after client connected
				client.startClient();
			}
		} else if (source == btnSend) {
			String receivingName = list.getSelectedValue();
			if (!textField.getText().equals("") && receivingName != null) {
				client.send(textField.getText());
			}
		}
	}
}