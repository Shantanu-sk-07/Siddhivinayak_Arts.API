package com.suraj.MurtiSystem;

import com.suraj.MurtiSystem.entity.User;
import com.suraj.MurtiSystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MurtiSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MurtiSystemApplication.class, args);
	}

	@Bean
	public CommandLineRunner createAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByEmail("admin@siddhivinayak.com")) {
				User admin = new User();
				admin.setName("Super Admin");
				admin.setEmail("Admin@gmail.com");
				admin.setPhone("9876543210");
				admin.setPassword(passwordEncoder.encode("Admin@123"));
				admin.setRole(User.UserRole.SUPER_ADMIN);
				admin.setIsActive(true);
				userRepository.save(admin);
				System.out.println("Admin user created successfully!");
			}
		};
	}
}