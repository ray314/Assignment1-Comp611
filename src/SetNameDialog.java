package src;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.event.*;
import java.awt.GridLayout;

public class SetNameDialog extends JDialog implements ActionListener {

    private JFrame owner;
    private boolean nameSet;
    private JLabel label;
    private JTextField nameField;

    public SetNameDialog(JFrame owner) {
        super(owner, "Enter name", true);
        this.owner = owner;
        this.nameSet = false;
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        label = new JLabel("Name: ");
        nameField = new JTextField();
        this.setLayout(new GridLayout(4, 2));
        this.add(label);
        this.add(nameField);
        setSize(400, 400);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }
}