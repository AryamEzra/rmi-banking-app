package bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BankServiceImpl extends UnicastRemoteObject implements BankService {

    private static final long serialVersionUID = 1L;

    // Sequential account number counter starting at 1000001
    private final AtomicInteger accountCounter = new AtomicInteger(1000001);

    // accountNo -> User
    private final Map<String, User> accounts = new HashMap<>();

    protected BankServiceImpl() throws RemoteException {
        super();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private User getUser(String accountNo) throws RemoteException {
        User u = accounts.get(accountNo);
        if (u == null) throw new RemoteException("Account not found: " + accountNo);
        return u;
    }

    private void validateEthiopianPhone(String phone) throws RemoteException {
            // Normalize: strip spaces/dashes
            String p = phone.replaceAll("[\\s\\-]", "");
            boolean valid = p.matches("^(\\+2519|\\+2517)\\d{8}$")   // +251 format (13 chars)
                        || p.matches("^(09|07)\\d{8}$");              // local 0X format (10 digits)
            if (!valid)
                throw new RemoteException(
                    "Enter a valid Ethiopian phone number (e.g. 0911234567 or +251911234567).");
        }

    // ── RMI operations ─────────────────────────────────────────────────────
    
    @Override
    public synchronized String registerUser(String name, String phone,
                                             String password, double initialDeposit)
            throws RemoteException {
        
        validateEthiopianPhone(phone);
                
        if (initialDeposit < 0)
            throw new RemoteException("Initial deposit cannot be negative.");

        // Check phone uniqueness
        for (User u : accounts.values()) {
            if (u.getPhone().equals(phone))
                throw new RemoteException("Phone number already registered.");
        }

        

        String accountNo = String.valueOf(accountCounter.getAndIncrement());
        String hash = sha256(password);
        User user = new User(accountNo, name, phone, hash, initialDeposit);
        accounts.put(accountNo, user);

        System.out.println("[Server] Registered: " + accountNo + " | " + name);
        return accountNo;
    }


    @Override
    public synchronized String[] login(String phone, String password) throws RemoteException {
        // Search by phone instead of account number
        User found = null;
        for (User u : accounts.values()) {
            if (u.getPhone().equals(phone)) { found = u; break; }
        }
        if (found == null || !found.getPasswordHash().equals(sha256(password)))
            throw new RemoteException("Invalid phone number or password.");
        return new String[]{ found.getAccountNo(), found.getName(),
                            found.getPhone(), String.valueOf(found.getBalance()) };
    }

    @Override
    public synchronized double deposit(String accountNo, double amount)
            throws RemoteException {
        if (amount <= 0) throw new RemoteException("Deposit amount must be positive.");
        User u = getUser(accountNo);
        u.setBalance(u.getBalance() + amount);
        System.out.printf("[Server] Deposit %.2f -> %s | Balance: %.2f%n",
                amount, accountNo, u.getBalance());
        return u.getBalance();
    }

    @Override
    public synchronized double withdraw(String accountNo, double amount)
            throws RemoteException {
        if (amount <= 0) throw new RemoteException("Withdrawal amount must be positive.");
        User u = getUser(accountNo);
        if (u.getBalance() < amount)
            throw new RemoteException("Insufficient funds. Balance: " + u.getBalance());
        u.setBalance(u.getBalance() - amount);
        System.out.printf("[Server] Withdraw %.2f <- %s | Balance: %.2f%n",
                amount, accountNo, u.getBalance());
        return u.getBalance();
    }

    @Override
    public synchronized double transfer(String fromAccount, String toAccount, double amount)
            throws RemoteException {
        if (amount <= 0) throw new RemoteException("Transfer amount must be positive.");
        if (fromAccount.equals(toAccount))
            throw new RemoteException("Cannot transfer to your own account.");

        User from = getUser(fromAccount);
        User to   = getUser(toAccount);   // throws if not found

        if (from.getBalance() < amount)
            throw new RemoteException("Insufficient funds. Balance: " + from.getBalance());

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        System.out.printf("[Server] Transfer %.2f  %s -> %s | New balance: %.2f%n",
                amount, fromAccount, toAccount, from.getBalance());
        return from.getBalance();
    }

    @Override
    public synchronized double getBalance(String accountNo) throws RemoteException {
        return getUser(accountNo).getBalance();
    }

    @Override
    public synchronized boolean accountExists(String accountNo) throws RemoteException {
        return accounts.containsKey(accountNo);
    }
}
