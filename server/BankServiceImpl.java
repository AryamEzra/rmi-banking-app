package server;

import common.BankService;
import common.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import util.HashUtil;

public class BankServiceImpl extends UnicastRemoteObject implements BankService {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger accountCounter = new AtomicInteger(1000);

    protected BankServiceImpl() throws RemoteException {
        super();
    }

    private String generateAccountNumber() {
        return String.valueOf(accountCounter.getAndIncrement());
    }

    @Override
    public synchronized String register(String name, String phone, String password, double deposit) throws RemoteException {
        if (users.containsKey(phone)) return "EXISTS";

        String acc = generateAccountNumber();
        String hash = HashUtil.hash(password);

        users.put(phone, new User(name, phone, acc, hash, deposit));

        return acc;
    }

    @Override
    public boolean login(String phone, String password) throws RemoteException {
        User u = users.get(phone);
        if (u == null) return false;

        return u.passwordHash.equals(HashUtil.hash(password));
    }

    @Override
    public double deposit(String phone, double amount) throws RemoteException {
        User u = users.get(phone);
        if (u == null) throw new RemoteException("User not found");
        if (amount <= 0) throw new RemoteException("Invalid amount");

        synchronized (u) {
            u.balance += amount;
            return u.balance;
        }
    }

    @Override
    public double withdraw(String phone, double amount) throws RemoteException {
        User u = users.get(phone);
        if (u == null) throw new RemoteException("User not found");
        if (amount <= 0) throw new RemoteException("Invalid amount");

        synchronized (u) {
            if (u.balance >= amount) {
                u.balance -= amount;
            } else {
                throw new RemoteException("Insufficient funds");
            }
            return u.balance;
        }
    }

    @Override
    public double transfer(String fromPhone, String toAccount, double amount) throws RemoteException {
        User from = users.get(fromPhone);
        if (from == null) throw new RemoteException("Sender not found");

        User to = null;
        for (User u : users.values()) {
            if (u.accountNumber.equals(toAccount)) {
                to = u;
                break;
            }
        }

        if (to == null) throw new RemoteException("Recipient not found");
        if (amount <= 0) throw new RemoteException("Invalid amount");

        // lock ordering to avoid deadlock: use System.identityHashCode
        Object first = from;
        Object second = to;
        if (System.identityHashCode(first) > System.identityHashCode(second)) {
            Object tmp = first; first = second; second = tmp;
        }

        synchronized (first) {
            synchronized (second) {
                if (from.balance < amount) throw new RemoteException("Insufficient funds");
                from.balance -= amount;
                to.balance += amount;
                return from.balance;
            }
        }
    }

    @Override
    public double getBalance(String phone) throws RemoteException {
        User u = users.get(phone);
        if (u == null) throw new RemoteException("User not found");
        return u.balance;
    }

    @Override
    public String getAccountNumber(String phone) throws RemoteException {
        User u = users.get(phone);
        if (u == null) throw new RemoteException("User not found");
        return u.accountNumber;
    }
}