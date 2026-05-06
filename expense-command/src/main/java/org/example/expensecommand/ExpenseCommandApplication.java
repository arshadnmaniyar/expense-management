package org.example.expensecommand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(
        basePackages = "org.example.expensecommand",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.example\\.expensecommand\\.api\\.generated\\..*")
)
public class ExpenseCommandApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseCommandApplication.class, args);
    }

}