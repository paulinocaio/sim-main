package io.sim.Project;

import java.util.ArrayList;
import java.util.List;

public class Account extends Thread {

    private String login;
    private String password;
    private double balance;
    private List<Transaction> transactionHistory;

    // Construtor
    public Account(String login, String password) {
        this.login = login;
        this.password = password;
        this.balance = 1000.0;
        this.transactionHistory = new ArrayList<>();
    }

    public synchronized String getLogin() {
        return login;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public synchronized double getBalance() {
        return balance;
    }

    public synchronized void updateBalance(double amount, TransactionType type) {
        if (type == TransactionType.DEPOSIT) {
            balance += amount;
        } else if (type == TransactionType.WITHDRAWAL) {
            if (balance >= amount) {
                balance -= amount;
            }
        }

        // Registrar a transação no histórico
        transactionHistory.add(new Transaction(amount, type));
    }
}

class Transaction {

    private double amount;
    private TransactionType type;

    // Construtor
    public Transaction(double amount, TransactionType type) {
        this.amount = amount;
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }
}
