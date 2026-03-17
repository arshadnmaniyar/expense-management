package org.example.expensecommand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExpenseCommandApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseCommandApplication.class, args);
    }

}
