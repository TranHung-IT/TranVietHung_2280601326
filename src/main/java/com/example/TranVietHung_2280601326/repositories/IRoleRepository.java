package com.example.TranVietHung_2280601326.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRoleRepository extends JpaRepository<com.example.TranVietHung_2280601326.models.Role, Long> {
    com.example.TranVietHung_2280601326.models.Role findRoleById(Long id);
    com.example.TranVietHung_2280601326.models.Role findByName(String name);
}
