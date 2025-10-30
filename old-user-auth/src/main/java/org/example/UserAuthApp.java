package org.example;

import org.example.config.RsaKeyConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyConfigProperties.class)
public class UserAuthApp {

    public static void main(String[] args) {
        SpringApplication.run(UserAuthApp.class, args);
    }

//    @Bean
//    public CommandLineRunner initializeUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        return args -> {
//            AppUser appUser = new AppUser();
//            appUser.setUsername("exampleuser");
//            appUser.setEmail("example@gmail.com");
//            final String pass = passwordEncoder.encode("examplepassword");
//            System.out.println(pass);
//            appUser.setPassword(pass);
//
//            userRepository.save(appUser);
//        };
//    }
}
