package bank.gui;

import bank.BankService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;

public class BankingFrame extends JFrame {

    private final BankService bank;
    private final String accountNo;
    private double balance;

    // UI refs
    private JLabel balanceLabel;
    private JLabel welcomeLabel;

    // Tab buttons
    private JButton[] tabBtns;
    private JPanel contentArea;
    private CardLayout contentCard;

    // ── Palette
    private static final Color BG       = new Color(0xF0F4F8);
    private static final Color SIDEBAR  = new Color(0x0F2447);
    private static final Color PANEL_BG = Color.WHITE;
    private static final Color PRIMARY  = new Color(0x1A3C6E);
    private static final Color ACCENT   = new Color(0x2563EB);
    private static final Color GREEN    = new Color(0x16A34A);
    private static final Color RED      = new Color(0xDC2626);
    private static final Color MUTED    = new Color(0x6B7280);
    private static final Color BORDER_C = new Color(0xE5E7EB);
    private static final Color GOLD     = new Color(0xF59E0B);

    private static final NumberFormat CURRENCY =
        NumberFormat.getCurrencyInstance(Locale.US);

    public BankingFrame(BankService bank, String accountNo, String name,
                        String phone, double balance) {
        this.bank      = bank;
        this.accountNo = accountNo;
        this.balance   = balance;

        setTitle("DsBank — Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 560);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout(0, 0));

        add(buildSidebar(name, phone), BorderLayout.WEST);
        add(buildMain(), BorderLayout.CENTER);

        refreshBalance();
        setVisible(true);
    }

    // ── Sidebar ─────────────────────────────────────────────────────────────

    private JPanel buildSidebar(String name, String phone) {
        JPanel s = new JPanel();
        s.setPreferredSize(new Dimension(210, 0));
        s.setBackground(SIDEBAR);
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBorder(new EmptyBorder(28, 0, 28, 0));

        // Logo
        JLabel logo = new JLabel("DsBank");
        logo.setFont(new Font("Georgia", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // JLabel logoSub = new JLabel("Secure Banking");
        // logoSub.setFont(new Font("SansSerif", Font.PLAIN, 10));
        // logoSub.setForeground(new Color(0x7FB3D3));
        // logoSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Avatar / user block
        JPanel userBlock = new JPanel();
        userBlock.setOpaque(false);
        userBlock.setLayout(new BoxLayout(userBlock, BoxLayout.Y_AXIS));
        userBlock.setAlignmentX(Component.CENTER_ALIGNMENT);
        userBlock.setBorder(new EmptyBorder(20, 16, 20, 16));

        JLabel avatar = new JLabel(String.valueOf(name.charAt(0)).toUpperCase());
        avatar.setFont(new Font("Georgia", Font.BOLD, 22));
        avatar.setForeground(SIDEBAR);
        avatar.setOpaque(true);
        avatar.setBackground(GOLD);
        avatar.setPreferredSize(new Dimension(48, 48));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomeLabel = new JLabel(name);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel accLabel = new JLabel("Acct: " + accountNo);
        accLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        accLabel.setForeground(new Color(0x7FB3D3));
        accLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userBlock.add(avatar);
        userBlock.add(Box.createVerticalStrut(10));
        userBlock.add(welcomeLabel);
        userBlock.add(Box.createVerticalStrut(3));
        userBlock.add(accLabel);

        // Nav tabs
        String[] tabs = {"Deposit", "Withdraw", "Transfer", "Balance"};
        String[] icons = {"↑", "↓", "⇄", "◈"};
        tabBtns = new JButton[tabs.length];
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(0, 12, 0, 12));

        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            JButton btn = navButton(icons[i] + "  " + tabs[i]);
            btn.addActionListener(e -> switchTab(idx));
            tabBtns[i] = btn;
            nav.add(btn);
            nav.add(Box.createVerticalStrut(4));
        }

        // Logout
        JButton logout = new JButton("⎋  Log Out");
        logout.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logout.setForeground(new Color(0x7FB3D3));
        logout.setContentAreaFilled(false);
        logout.setBorderPainted(false);
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        s.add(logo);
        s.add(Box.createVerticalStrut(2));
        // s.add(logoSub);
        s.add(userBlock);
        s.add(new JSeparator() {{
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            setForeground(new Color(0x1E3A5F));
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }});
        s.add(Box.createVerticalStrut(16));
        s.add(nav);
        s.add(Box.createVerticalGlue());
        s.add(logout);
        return s;
    }

    // ── Main area ────────────────────────────────────────────────────────────

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(BG);

        // Top balance bar
        main.add(buildBalanceBar(), BorderLayout.NORTH);

        // Content cards
        contentCard = new CardLayout();
        contentArea = new JPanel(contentCard);
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(20, 24, 24, 24));

        contentArea.add(buildDepositPanel(),   "Deposit");
        contentArea.add(buildWithdrawPanel(),  "Withdraw");
        contentArea.add(buildTransferPanel(),  "Transfer");
        contentArea.add(buildBalancePanel(),   "Balance");

        main.add(contentArea, BorderLayout.CENTER);

        // Select first tab by default
        switchTab(0);
        return main;
    }

    private JPanel buildBalanceBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY);
        bar.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel lbl = new JLabel("Available Balance");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(new Color(0xBFD4F2));

        balanceLabel = new JLabel();
        balanceLabel.setFont(new Font("Georgia", Font.BOLD, 28));
        balanceLabel.setForeground(Color.WHITE);

        bar.add(lbl, BorderLayout.NORTH);
        bar.add(balanceLabel, BorderLayout.CENTER);
        return bar;
    }

    // ── Operation panels ─────────────────────────────────────────────────────

    private JPanel buildDepositPanel() {
        JPanel p = operationCard("Deposit Funds",
            "Add money to your account");

        JTextField amtField = opField("0.00");
        JLabel result = resultLabel();

        JButton btn = actionButton("Deposit", GREEN);
        btn.addActionListener(e -> {
            double amt = parseAmount(amtField, result);
            if (amt <= 0) return;
            try {
                double newBal = bank.deposit(accountNo, amt);
                balance = newBal;
                refreshBalance();
                result.setForeground(GREEN);
                result.setText("✓  Deposited " + CURRENCY.format(amt) +
                    "  |  New balance: " + CURRENCY.format(newBal));
                amtField.setText("");
            } catch (Exception ex) {
                showResult(result, "✗  " + clean(ex.getMessage()), RED);
            }
        });

        p.add(fieldLabel("Amount ($)"));
        p.add(Box.createVerticalStrut(6));
        p.add(amtField);
        p.add(Box.createVerticalStrut(18));
        p.add(btn);
        p.add(Box.createVerticalStrut(14));
        p.add(result);
        return wrapCard(p);
    }

    private JPanel buildWithdrawPanel() {
        JPanel p = operationCard("Withdraw Funds",
            "Take money out of your account");

        JTextField amtField = opField("0.00");
        JLabel result = resultLabel();

        JButton btn = actionButton("Withdraw", RED);
        btn.addActionListener(e -> {
            double amt = parseAmount(amtField, result);
            if (amt <= 0) return;
            try {
                double newBal = bank.withdraw(accountNo, amt);
                balance = newBal;
                refreshBalance();
                result.setForeground(GREEN);
                result.setText("✓  Withdrew " + CURRENCY.format(amt) +
                    "  |  Remaining: " + CURRENCY.format(newBal));
                amtField.setText("");
            } catch (Exception ex) {
                showResult(result, "✗  " + clean(ex.getMessage()), RED);
            }
        });

        p.add(fieldLabel("Amount ($)"));
        p.add(Box.createVerticalStrut(6));
        p.add(amtField);
        p.add(Box.createVerticalStrut(18));
        p.add(btn);
        p.add(Box.createVerticalStrut(14));
        p.add(result);
        return wrapCard(p);
    }

    private JPanel buildTransferPanel() {
        JPanel p = operationCard("Transfer Funds",
            "Send money to another DsBank account");

        JTextField toField  = opField("Recipient Account Number");
        JTextField amtField = opField("0.00");
        JLabel result = resultLabel();

        JButton btn = actionButton("Transfer", ACCENT);
        btn.addActionListener(e -> {
            String to = toField.getText().trim();
            double amt = parseAmount(amtField, result);
            if (to.isEmpty()) {
                showResult(result, "✗  Enter a recipient account number.", RED); return;
            }
            if (to.equals(accountNo)) {
                showResult(result, "✗  Cannot transfer to your own account.", RED); return;
            }
            if (amt <= 0) return;
            try {
                // Validate destination first
                if (!bank.accountExists(to)) {
                    showResult(result, "✗  Account " + to + " does not exist.", RED); return;
                }
                double newBal = bank.transfer(accountNo, to, amt);
                balance = newBal;
                refreshBalance();
                result.setForeground(GREEN);
                result.setText("<html>✓  Transferred " + CURRENCY.format(amt) +
                    " to account " + to +
                    "<br>Remaining balance: " + CURRENCY.format(newBal) + "</html>");
                toField.setText(""); amtField.setText("");
            } catch (Exception ex) {
                showResult(result, "✗  " + clean(ex.getMessage()), RED);
            }
        });

        p.add(fieldLabel("Recipient Account Number"));
        p.add(Box.createVerticalStrut(6));
        p.add(toField);
        p.add(Box.createVerticalStrut(14));
        p.add(fieldLabel("Amount ($)"));
        p.add(Box.createVerticalStrut(6));
        p.add(amtField);
        p.add(Box.createVerticalStrut(18));
        p.add(btn);
        p.add(Box.createVerticalStrut(14));
        p.add(result);
        return wrapCard(p);
    }

    private JPanel buildBalancePanel() {
        JPanel p = operationCard("Account Balance",
            "View your current available balance");

        JLabel bigBalance = new JLabel(CURRENCY.format(balance));
        bigBalance.setFont(new Font("Georgia", Font.BOLD, 40));
        bigBalance.setForeground(PRIMARY);
        bigBalance.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel asOf = new JLabel("as of last transaction");
        asOf.setFont(new Font("SansSerif", Font.ITALIC, 12));
        asOf.setForeground(MUTED);
        asOf.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel result = resultLabel();

        JButton refresh = actionButton("Refresh Balance", ACCENT);
        refresh.addActionListener(e -> {
            try {
                balance = bank.getBalance(accountNo);
                refreshBalance();
                bigBalance.setText(CURRENCY.format(balance));
                showResult(result, "✓  Balance updated.", GREEN);
            } catch (Exception ex) {
                showResult(result, "✗  " + clean(ex.getMessage()), RED);
            }
        });

        p.add(bigBalance);
        p.add(Box.createVerticalStrut(4));
        p.add(asOf);
        p.add(Box.createVerticalStrut(24));
        p.add(refresh);
        p.add(Box.createVerticalStrut(14));
        p.add(result);
        return wrapCard(p);
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    private void switchTab(int idx) {
        String[] names = {"Deposit", "Withdraw", "Transfer", "Balance"};
        contentCard.show(contentArea, names[idx]);
        for (int i = 0; i < tabBtns.length; i++) {
            tabBtns[i].setBackground(i == idx ? new Color(0x1E3A5F) : SIDEBAR);
            tabBtns[i].setForeground(i == idx ? Color.WHITE : new Color(0x7FB3D3));
        }
    }

    private void refreshBalance() {
        if (balanceLabel != null)
            balanceLabel.setText(CURRENCY.format(balance));
    }

    // ── Component helpers ─────────────────────────────────────────────────────

    private JPanel operationCard(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Georgia", Font.BOLD, 20));
        t.setForeground(PRIMARY);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("SansSerif", Font.PLAIN, 12));
        s.setForeground(MUTED);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(s);
        p.add(Box.createVerticalStrut(24));
        return p;
    }

    private JPanel wrapCard(JPanel inner) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANEL_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(28, 28, 28, 28)
        ));
        card.add(inner, BorderLayout.NORTH);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        outer.add(card, gbc);
        return outer;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(0x374151));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField opField(String placeholder) {
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
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setPreferredSize(new Dimension(340, 40));
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JButton actionButton(String text, Color color) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? color.darker() : color);
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
        b.setPreferredSize(new Dimension(200, 40));
        b.setMaximumSize(new Dimension(200, 40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    private JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setForeground(new Color(0x7FB3D3));
        b.setBackground(SIDEBAR);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    private JLabel resultLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private double parseAmount(JTextField f, JLabel result) {
        try {
            double v = Double.parseDouble(f.getText().trim());
            if (v <= 0) { showResult(result, "✗  Amount must be greater than zero.", RED); return -1; }
            return v;
        } catch (NumberFormatException ex) {
            showResult(result, "✗  Enter a valid numeric amount.", RED);
            return -1;
        }
    }

    private void showResult(JLabel l, String msg, Color color) {
        l.setForeground(color);
        l.setText("<html>" + msg + "</html>");
    }

    private String clean(String msg) {
        return msg == null ? "Unknown error"
            : msg.replace("java.rmi.RemoteException: ", "")
                 .replace("RemoteException: ", "");
    }
}
