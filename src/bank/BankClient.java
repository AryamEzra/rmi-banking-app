package bank;
import bank.gui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class BankClient {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
