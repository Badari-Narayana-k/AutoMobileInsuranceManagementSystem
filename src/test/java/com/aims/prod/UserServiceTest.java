package com.aims.prod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.aims.prod.Entity.User;
import com.aims.prod.Repository.UserRepository;
import com.aims.prod.Service.UserService;

class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        User user = new User("John Doe", "john@example.com", "password123", "user");
        when(userRepo.findByEmail(user.getEmail())).thenReturn(null);
        boolean result = userService.registerUser(user);
        assertTrue(result);
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void testAuthenticate() {
        User user = new User("John Doe", "john@example.com", BCrypt.hashpw("password123", BCrypt.gensalt()), "user");
        when(userRepo.findByEmail(user.getEmail())).thenReturn(user);
        User authenticatedUser = userService.authenticate("john@example.com", "password123");
        assertNotNull(authenticatedUser);
        assertEquals(user.getEmail(), authenticatedUser.getEmail());
    }

    @Test
    void testCreateAgent() {
        User agent = new User("Agent Smith", "agent@example.com", "password123", "agent");
        userService.createAgent(agent);
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateRole() {
        User user = new User("John Doe", "john@example.com", "password123", "user");
        user.setId(1L);
        when(userRepo.getById(1L)).thenReturn(user);
        userService.updateRole(1L, "admin");
        assertEquals("admin", user.getRole());
        verify(userRepo, times(1)).save(user);
    }
}
