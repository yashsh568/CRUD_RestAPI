package com.example.walletcrud.services;

import com.example.walletcrud.entities.Transaction;
import com.example.walletcrud.entities.Wallet;
import com.example.walletcrud.repositories.TransactionRepository;
import com.example.walletcrud.repositories.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private SMSService smsService;

    public Transaction createTransaction(Long walletId, Double amount, Transaction.TransactionType type) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCreated(LocalDateTime.now());
        transaction.setUpdated(LocalDateTime.now());
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Asynchronously send SMS after 5 seconds
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
                smsService.sendSMS(Long.valueOf(wallet.getMobile()), "Transaction of type " + type + " and amount " + amount + " was successful.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        return savedTransaction;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByWalletId(Long walletId) {
        return transactionRepository.findAllByWalletId(walletId);
    }
}
