package com.example.TranVietHung_2280601326.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.TranVietHung_2280601326.models.User;
import com.example.TranVietHung_2280601326.repositories.IRoleRepository;
import com.example.TranVietHung_2280601326.repositories.IUserRepository;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    @Autowired
    private IUserRepository userRepository;
    
    @Autowired
    private IRoleRepository roleRepository;
    
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
    public void save(@NotNull User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) throws UsernameNotFoundException {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }
    
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
    public void setDefaultRole(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            com.example.TranVietHung_2280601326.models.Role userRole = roleRepository.findByName("USER");
            if (userRole != null) {
                user.getRoles().add(userRole);
                userRepository.save(user);
            }
        }
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        // Trả về chính User entity vì nó đã implements UserDetails
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
        setDefaultRole(username); // Set default USER role
        log.info("Created new OAuth user: {} with username: {}", email, username);
        return username;
    }
    
    // Admin user management methods
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByUsernameContainingOrEmailContaining(keyword, keyword, pageable);
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public void updateUser(User user) {
        userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public long countAllUsers() {
        return userRepository.count();
    }
    
    public long countActiveUsers() {
        // Assuming active users are those who have logged in recently or have orders
        return userRepository.count(); // Simplified - count all users
    }
    
    public List<Object[]> getUserRegistrationStats() {
        // Return user registration statistics grouped by date
        return List.of(); // Placeholder - implement query in repository if needed
    }
}
