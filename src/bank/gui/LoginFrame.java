package bank.gui;

import bank.BankService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LoginFrame extends JFrame {

    private BankService bank;

    // Shared card panel
    private JPanel cards;
    private CardLayout cardLayout;

    // ── Login panel components
    private JTextField loginAccountField;
    private JPasswordField loginPasswordField;

    // ── Sign-up panel components
    private JTextField signupNameField;
    private JTextField signupPhoneField;
    private JTextField signupDepositField;
    private JPasswordField signupPasswordField;
    private JPasswordField signupConfirmField;

    // ── Color palette
    private static final Color BG        = new Color(0xF7F8FA);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color PRIMARY   = new Color(0x1A3C6E);
    private static final Color ACCENT    = new Color(0x2563EB);
    private static final Color MUTED     = new Color(0x6B7280);
    private static final Color BORDER_C  = new Color(0xE5E7EB);
    private static final Color ERROR_C   = new Color(0xDC2626);
    private static final Color SUCCESS_C = new Color(0x16A34A);

    public LoginFrame() {
        connectToServer();
        buildUI();
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            bank = (BankService) registry.lookup("BankService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to bank server.\nMake sure BankServer is running.",
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void buildUI() {
        setTitle("NexBank — Secure Banking");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(440, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        // Header
        add(buildHeader(), BorderLayout.NORTH);

        // Cards
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setOpaque(false);
        cards.add(buildLoginPanel(), "LOGIN");
        cards.add(buildSignupPanel(), "SIGNUP");
        add(cards, BorderLayout.CENTER);

        setVisible(true);
    }

    // ── Header ──────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel();
        h.setBackground(PRIMARY);
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setBorder(new EmptyBorder(28, 32, 24, 32));

        JLabel logo = new JLabel("NexBank");
        logo.setFont(new Font("Georgia", Font.BOLD, 26));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Modern Banking, Simply Done");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(0xBFD4F2));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        h.add(logo);
        h.add(Box.createVerticalStrut(4));
        h.add(sub);
        return h;
    }

    // ── Login Panel ─────────────────────────────────────────────────────────

    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(24, 32, 24, 32));

        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(28, 28, 28, 28)
        ));

        JLabel title = new JLabel("Sign In");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Enter your account credentials");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginAccountField  = styledField("Account Number");
        loginPasswordField = styledPassword("Password");

        JButton loginBtn = primaryButton("Sign In");
        loginBtn.addActionListener(e -> doLogin());
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        switchPanel.setOpaque(false);
        switchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel switchLbl = new JLabel("New customer? ");
        switchLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        switchLbl.setForeground(MUTED);
        JButton switchBtn = linkButton("Create Account");
        switchBtn.addActionListener(e -> cardLayout.show(cards, "SIGNUP"));
        switchPanel.add(switchLbl);
        switchPanel.add(switchBtn);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(22));
        card.add(label("Account Number"));
        card.add(Box.createVerticalStrut(6));
        card.add(loginAccountField);
        card.add(Box.createVerticalStrut(14));
        card.add(label("Password"));
        card.add(Box.createVerticalStrut(6));
        card.add(loginPasswordField);
        card.add(Box.createVerticalStrut(22));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(16));
        card.add(switchPanel);

        outer.add(card);
        return outer;
    }

    private void doLogin() {
        String acc  = loginAccountField.getText().trim();
        String pass = new String(loginPasswordField.getPassword());
        if (acc.isEmpty() || pass.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }
        try {
            String[] info = bank.login(acc, pass);
            new BankingFrame(bank, info[0], info[1], info[2], Double.parseDouble(info[3]));
            dispose();
        } catch (Exception ex) {
            showError(ex.getMessage().replace("java.rmi.RemoteException: ", ""));
        }
    }

    // ── Sign-up Panel ────────────────────────────────────────────────────────

    private JPanel buildSignupPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(16, 32, 16, 32));

        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(24, 28, 24, 28)
        ));

        JLabel title = new JLabel("Open an Account");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Fill in your details below");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        signupNameField    = styledField("Full Name");
        signupPhoneField   = styledField("Phone Number");
        signupDepositField = styledField("0.00");
        signupPasswordField = styledPassword("Password");
        signupConfirmField  = styledPassword("Confirm Password");

        JButton signupBtn = primaryButton("Create Account");
        signupBtn.addActionListener(e -> doSignup());
        signupBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        switchPanel.setOpaque(false);
        switchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel switchLbl = new JLabel("Already have an account? ");
        switchLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        switchLbl.setForeground(MUTED);
        JButton switchBtn = linkButton("Sign In");
        switchBtn.addActionListener(e -> cardLayout.show(cards, "LOGIN"));
        switchPanel.add(switchLbl);
        switchPanel.add(switchBtn);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(18));
        card.add(label("Full Name"));        card.add(Box.createVerticalStrut(5));
        card.add(signupNameField);
        card.add(Box.createVerticalStrut(12));
        card.add(label("Phone Number"));     card.add(Box.createVerticalStrut(5));
        card.add(signupPhoneField);
        card.add(Box.createVerticalStrut(12));
        card.add(label("Initial Deposit ($)")); card.add(Box.createVerticalStrut(5));
        card.add(signupDepositField);
        card.add(Box.createVerticalStrut(12));
        card.add(label("Password"));         card.add(Box.createVerticalStrut(5));
        card.add(signupPasswordField);
        card.add(Box.createVerticalStrut(12));
        card.add(label("Confirm Password")); card.add(Box.createVerticalStrut(5));
        card.add(signupConfirmField);
        card.add(Box.createVerticalStrut(20));
        card.add(signupBtn);
        card.add(Box.createVerticalStrut(14));
        card.add(switchPanel);

        outer.add(card);
        return outer;
    }

    private void doSignup() {
        String name    = signupNameField.getText().trim();
        String phone   = signupPhoneField.getText().trim();
        String depStr  = signupDepositField.getText().trim();
        String pass    = new String(signupPasswordField.getPassword());
        String confirm = new String(signupConfirmField.getPassword());

        if (name.isEmpty() || phone.isEmpty() || depStr.isEmpty()
                || pass.isEmpty() || confirm.isEmpty()) {
            showError("Please fill in all fields."); return;
        }
        if (!pass.equals(confirm)) {
            showError("Passwords do not match."); return;
        }
        if (pass.length() < 6) {
            showError("Password must be at least 6 characters."); return;
        }
        double deposit;
        try { deposit = Double.parseDouble(depStr); }
        catch (NumberFormatException ex) { showError("Enter a valid deposit amount."); return; }
        if (deposit < 0) { showError("Initial deposit cannot be negative."); return; }

        try {
            String accNo = bank.registerUser(name, phone, pass, deposit);
            JOptionPane.showMessageDialog(this,
                "<html><b>Account created successfully!</b><br><br>" +
                "Your Account Number: <b>" + accNo + "</b><br>" +
                "Please save this number — you will use it to log in.</html>",
                "Account Created", JOptionPane.INFORMATION_MESSAGE);
            // Clear fields and switch to login
            signupNameField.setText(""); signupPhoneField.setText("");
            signupDepositField.setText(""); signupPasswordField.setText("");
            signupConfirmField.setText("");
            loginAccountField.setText(accNo);
            cardLayout.show(cards, "LOGIN");
        } catch (Exception ex) {
            showError(ex.getMessage().replace("java.rmi.RemoteException: ", ""));
        }
    }

    // ── Component helpers ────────────────────────────────────────────────────

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(0x374151));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(new Color(0xADB5BD));
                    g.setFont(getFont().deriveFont(Font.ITALIC));
                    g.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setPreferredSize(new Dimension(300, 38));
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JPasswordField styledPassword(String placeholder) {
        JPasswordField f = new JPasswordField() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    g.setColor(new Color(0xADB5BD));
                    g.setFont(new Font("SansSerif", Font.ITALIC, 13));
                    g.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setPreferredSize(new Dimension(300, 38));
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(300, 40));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(ACCENT);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(0, 2, 0, 0));
        return b;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
