package io.sim.Project;

import java.util.HashMap;
import java.util.Map;

public class AlphaBank extends Thread {

    private Map<String, Account> accounts; // Mapa de contas por login

    // Construtor
    public AlphaBank() {
        this.accounts = new HashMap<>();
    }

    public synchronized void createAccount(String login, String password) {
        Account account = new Account(login, password);
        accounts.put(login, account);
    }

    public synchronized Account getAccount(String login) {
        return accounts.get(login);
    }

    // Lógica para realizar transações com contas
    public synchronized void performTransaction(String login, double amount, TransactionType type) {
        Account account = accounts.get(login);
        if (account != null) {
            account.updateBalance(amount, type);
        }
    }
}

enum TransactionType {
    DEPOSIT,
    WITHDRAWAL
}