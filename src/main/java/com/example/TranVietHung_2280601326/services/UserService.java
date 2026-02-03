package com.example.TranVietHung_2280601326.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.TranVietHung_2280601326.models.User;
import com.example.TranVietHung_2280601326.repositories.IUserRepository;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    @Autowired
    private IUserRepository userRepository;
    
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
    public void save(@NotNull User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) throws UsernameNotFoundException {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return user;
    }
    
    // Luu user tu OAuth2 (Google) - return username da luu
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
    public String saveOauthUser(String email, @NotNull String name) {
        // Check if user already exists by email (primary identifier for OAuth)
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            log.info("OAuth user already exists: {}", email);
            return existingUser.getUsername();
        }
        
        // Extract username from email (part before @)
        String username = email.substring(0, email.indexOf('@'));
        
        // Check if username already exists, if yes, use email as username
        User userWithSameUsername = userRepository.findByUsername(username);
        if (userWithSameUsername != null) {
            log.info("Username {} already exists, using email as username", username);
            username = email; // Fallback to full email
        }
        
        var user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(new BCryptPasswordEncoder().encode(username));
        user.setProvider(com.example.TranVietHung_2280601326.constants.Provider.GOOGLE.value);
        user.setPhone(null); // OAuth users don't have phone
        userRepository.save(user);
        log.info("Created new OAuth user: {} with username: {}", email, username);
        return username;
    }
}
