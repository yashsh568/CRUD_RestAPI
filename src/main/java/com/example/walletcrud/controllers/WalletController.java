package com.example.walletcrud.controllers;

import com.example.walletcrud.entities.Transaction;
import com.example.walletcrud.entities.Wallet;
import com.example.walletcrud.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping
    public ResponseEntity<String> createWallet(@RequestBody Wallet wallet) {
        Wallet createdWallet = walletService.createWallet(wallet);
        return ResponseEntity.ok("Wallet created successfully with wallet Id : " + createdWallet.getId());
    }

    @GetMapping
    public ResponseEntity<List<Wallet>> getAllWallets() {
        List<Wallet> wallets = walletService.getAllWallets();
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Long id) {
        Optional<Wallet> wallet = walletService.getWalletById(id);
        return wallet.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Wallet> updateWallet(@PathVariable Long id, @RequestBody Wallet walletDetails) {
        Wallet updatedWallet = walletService.updateWallet(id, walletDetails);
        return ResponseEntity.ok(updatedWallet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/loadamount")
    public ResponseEntity<String> loadAmount(@RequestParam Long walletId, @RequestParam Double amount) {
        try {
            walletService.processTransaction(walletId, amount, Transaction.TransactionType.LOAD);
            return ResponseEntity.ok("Wallet Load Successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error in loading amount");
        }
    }

    @PostMapping("/spendamount")
    public ResponseEntity<String> spendAmount(@RequestParam Long walletId, @RequestParam Double amount) {
        try {
            walletService.processTransaction(walletId, amount, Transaction.TransactionType.SPEND);
            return ResponseEntity.ok("Wallet Spend Successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
