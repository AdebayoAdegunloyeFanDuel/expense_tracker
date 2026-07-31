package com.accounting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot Application
 * 
 * Expense Tracker with Bonus Points System
 * Multi-user accounting application with category-based rewards
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.accounting.controller",
    "com.accounting.service",
    "com.accounting.repository",
    "com.accounting.security",
    "com.accounting.config"
})
public class ExpenseTrackerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }
}
