package com.assetmarket.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class AssetMarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssetMarketApplication.class, args);
    }
}
