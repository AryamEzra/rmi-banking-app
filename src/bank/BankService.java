package bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankService extends Remote {

    // Returns the new account number assigned
    String registerUser(String name, String phone, String password, double initialDeposit)
            throws RemoteException;

    // Returns the logged-in user's full info as a String array:
    // [accountNo, name, phone, balance]
    String[] login(String phone, String password) throws RemoteException;

    double deposit(String accountNo, double amount) throws RemoteException;

    double withdraw(String accountNo, double amount) throws RemoteException;

    // Returns new balance of source account after transfer
    double transfer(String fromAccount, String toAccount, double amount) throws RemoteException;

    double getBalance(String accountNo) throws RemoteException;

    boolean accountExists(String accountNo) throws RemoteException;
}
