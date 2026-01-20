package com.simple.crud.demo.config;

import com.simple.crud.demo.model.entity.Product;
import com.simple.crud.demo.model.entity.User;
import com.simple.crud.demo.repository.ProductRepository;
import com.simple.crud.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize users if none exist
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);

            // Create regular user
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(User.Role.USER);
            userRepository.save(user);

            System.out.println("Default users created:");
            System.out.println("Admin - username: admin, password: admin123");
            System.out.println("User - username: user, password: user123");
        }

        // Initialize products if none exist
        if (productRepository.count() == 0) {
            Product laptop = new Product("Laptop", "High-performance laptop for work and gaming",
                                       new BigDecimal("999.99"), 10);
            productRepository.save(laptop);

            Product mouse = new Product("Wireless Mouse", "Ergonomic wireless mouse with long battery life",
                                      new BigDecimal("29.99"), 50);
            productRepository.save(mouse);

            Product keyboard = new Product("Mechanical Keyboard", "RGB mechanical keyboard for gaming",
                                         new BigDecimal("149.99"), 25);
            productRepository.save(keyboard);

            Product monitor = new Product("4K Monitor", "27-inch 4K Ultra HD monitor",
                                        new BigDecimal("299.99"), 15);
            productRepository.save(monitor);

            Product headphones = new Product("Bluetooth Headphones", "Noise-cancelling wireless headphones",
                                           new BigDecimal("199.99"), 30);
            productRepository.save(headphones);

            System.out.println("Sample products created successfully!");
        }
    }
}
