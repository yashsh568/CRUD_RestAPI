package com.example.walletcrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WalletCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletCrudApplication.class, args);
    }

}
