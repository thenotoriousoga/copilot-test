package com.example.api.service;

import com.example.api.entity.User;
import com.example.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testFindAll() {
        List<User> users = List.of(
            new User("田中太郎", "tanaka@example.com", 28),
            new User("鈴木花子", "suzuki@example.com", 34)
        );
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void testFindAllEmpty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = userService.findAll();

        assertEquals(0, result.size());
    }
}
