package com.example.walletcrud.services;

import com.example.walletcrud.entities.Transaction;
import com.example.walletcrud.entities.Wallet;
import com.example.walletcrud.repositories.TransactionRepository;
import com.example.walletcrud.repositories.WalletRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Wallet createWallet(Wallet wallet) {
        wallet.setCreated(LocalDateTime.now());
        wallet.setUpdated(LocalDateTime.now());
        wallet.setBalance(0.0); // Initialize balance
        return walletRepository.save(wallet);
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Optional<Wallet> getWalletById(Long id) {
        return walletRepository.findById(id);
    }

    public Wallet updateWallet(Long id, Wallet walletDetails) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (walletDetails.getName() != null) {
            wallet.setName(walletDetails.getName());
        }
        if (walletDetails.getMobile() != null) {
            wallet.setMobile(walletDetails.getMobile());
        }
        if (walletDetails.getEmail() != null) {
            wallet.setEmail(walletDetails.getEmail());
        }
        if (walletDetails.getAddress() != null) {
            wallet.setAddress(walletDetails.getAddress());
        }

        wallet.setUpdated(LocalDateTime.now());

        return walletRepository.save(wallet);
    }

    public void deleteWallet(Long id) {
        walletRepository.deleteById(id);
    }

    public void processTransaction(Long walletId, Double amount, Transaction.TransactionType type) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance() == null) {
            wallet.setBalance(0.0);
        }

        if (type == Transaction.TransactionType.SPEND && wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient Balance");
        }

        if (type == Transaction.TransactionType.LOAD) {
            wallet.setBalance(wallet.getBalance() + amount);
        } else if (type == Transaction.TransactionType.SPEND) {
            wallet.setBalance(wallet.getBalance() - amount);
        }

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCreated(LocalDateTime.now());
        transaction.setUpdated(LocalDateTime.now());

        transactionRepository.save(transaction);
        walletRepository.save(wallet);

        // Simulate delay and SMS sending
        sendSmsAsync(Long.valueOf(wallet.getMobile()), "Transaction of type " + type + " and amount " + amount + " was successful.");
    }

    @Async("taskExecutor")
    public void sendSmsAsync(Long mobile, String message) {
        try {
            Thread.sleep(5000); // Simulate 5 seconds delay
            sendSms(mobile, message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendSms(Long mobile, String message) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://api.kiwiplans.com:7002/send/sms"; // Use actual third-party API URL
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        SmsRequest smsRequest = new SmsRequest(mobile, message);

        try {
            HttpEntity<SmsRequest> requestEntity = new HttpEntity<>(smsRequest, headers);

            ResponseEntity<SmsResponse> responseEntity = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    SmsResponse.class
            );

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                SmsResponse smsResponse = responseEntity.getBody();
                if (smsResponse != null) {
                    System.out.println("SMS Sent Successfully: " + smsResponse.getMessage());
                }
            } else {
                System.err.println("Failed to send SMS. Status code: " + responseEntity.getStatusCodeValue());
                System.err.println("Response body: " + responseEntity.getBody());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("Error sending SMS: " + e.getMessage());
            System.err.println("Response body: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("General error sending SMS: " + e.getMessage());
        }
    }

    // Inner classes for SMS request and response
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class SmsRequest {
        private Long mobile;
        private String message;
    }

    @Data
    static class SmsResponse {
        private String message;
    }
}
