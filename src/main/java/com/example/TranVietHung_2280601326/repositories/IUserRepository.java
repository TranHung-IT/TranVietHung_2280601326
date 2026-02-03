package com.example.TranVietHung_2280601326.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.TranVietHung_2280601326.models.User;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
}
