package client;

import javax.swing.*;

public class BankGUI {

    public BankGUI(String phone) {
        JFrame frame = new JFrame("Bank");

        JButton deposit = new JButton("Deposit");
        JButton withdraw = new JButton("Withdraw");
        JButton transfer = new JButton("Transfer");
        JButton balance = new JButton("Check Balance");

        frame.setLayout(new java.awt.GridLayout(4,1));
        frame.add(deposit);
        frame.add(withdraw);
        frame.add(transfer);
        frame.add(balance);

        deposit.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog("Amount:");
            try {
                if (Client.service == null) throw new Exception("Not connected");
                double b = Client.service.deposit(phone, Double.parseDouble(amt));
                JOptionPane.showMessageDialog(frame, "Balance: " + b);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage()); }
        });

        withdraw.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog("Amount:");
            try {
                if (Client.service == null) throw new Exception("Not connected");
                double b = Client.service.withdraw(phone, Double.parseDouble(amt));
                JOptionPane.showMessageDialog(frame, "Balance: " + b);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage()); }
        });

        transfer.addActionListener(e -> {
            String acc = JOptionPane.showInputDialog("Target Account:");
            String amt = JOptionPane.showInputDialog("Amount:");
            try {
                if (Client.service == null) throw new Exception("Not connected");
                double b = Client.service.transfer(phone, acc, Double.parseDouble(amt));
                JOptionPane.showMessageDialog(frame, "Balance: " + b);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage()); }
        });

        balance.addActionListener(e -> {
            try {
                if (Client.service == null) throw new Exception("Not connected");
                double b = Client.service.getBalance(phone);
                JOptionPane.showMessageDialog(frame, "Balance: " + b);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage()); }
        });

        frame.setSize(300,300);
        frame.setVisible(true);
    }
}