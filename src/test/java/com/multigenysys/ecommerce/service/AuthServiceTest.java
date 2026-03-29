package com.multigenysys.ecommerce.service;

import com.multigenysys.ecommerce.dto.auth.AuthResponse;
import com.multigenysys.ecommerce.dto.auth.RegisterRequest;
import com.multigenysys.ecommerce.entity.User;
import com.multigenysys.ecommerce.exception.BadRequestException;
import com.multigenysys.ecommerce.repository.UserRepository;
import com.multigenysys.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("user@mail.com")
                .password("encoded")
                .roles("USER")
                .build();
    }

    @Test
    void register_ShouldCreateUserWithoutToken() {
        RegisterRequest request = new RegisterRequest("Test", "user@mail.com", "password123");
        when(userRepository.existsByEmail("user@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(request);

        assertNull(response.token());
        assertEquals("user@mail.com", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("Test", "user@mail.com", "password123");
        when(userRepository.existsByEmail("user@mail.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }
}
