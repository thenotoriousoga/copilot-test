package com.example.api.config;

import com.example.api.entity.User;
import com.example.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            List<User> users = List.of(
                new User("田中太郎", "tanaka@example.com", 28),
                new User("鈴木花子", "suzuki@example.com", 34),
                new User("佐藤健一", "sato@example.com", 25),
                new User("高橋美咲", "takahashi@example.com", 31),
                new User("伊藤大輔", "ito@example.com", 42),
                new User("渡辺由美", "watanabe@example.com", 29),
                new User("山本翔太", "yamamoto@example.com", 36),
                new User("中村あかり", "nakamura@example.com", 23),
                new User("小林誠", "kobayashi@example.com", 38),
                new User("加藤裕子", "kato@example.com", 27)
            );
            userRepository.saveAll(users);
        };
    }
}
