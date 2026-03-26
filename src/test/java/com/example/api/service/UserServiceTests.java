package com.example.api.service;

import com.example.api.entity.User;
import com.example.api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("findAll")
    class findAllTests {

        @Test
        @DisplayName("正常系：ユーザー一覧を全件取得できること")
        void 正常系_全件取得できる() {
            List<User> users = List.of(
                new User("田中太郎", "tanaka@example.com", 28),
                new User("鈴木花子", "suzuki@example.com", 34)
            );
            when(userRepository.findAll()).thenReturn(users);

            List<User> result = userService.findAll();

            assertIterableEquals(users, result);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("正常系：ユーザーが存在しない場合に空リストを返すこと")
        void 正常系_空リストを返す() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<User> result = userService.findAll();

            assertTrue(result.isEmpty());
            verify(userRepository).findAll();
        }
    }
}
