package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankService extends Remote {

    String register(String name, String phone, String password, double initialDeposit) throws RemoteException;

    boolean login(String phone, String password) throws RemoteException;

    double deposit(String phone, double amount) throws RemoteException;

    double withdraw(String phone, double amount) throws RemoteException;

    double transfer(String fromPhone, String toAccount, double amount) throws RemoteException;

    double getBalance(String phone) throws RemoteException;

    String getAccountNumber(String phone) throws RemoteException;
}