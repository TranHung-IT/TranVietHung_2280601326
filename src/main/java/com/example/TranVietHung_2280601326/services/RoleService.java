package com.example.TranVietHung_2280601326.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.TranVietHung_2280601326.models.Role;
import com.example.TranVietHung_2280601326.repositories.IRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final IRoleRepository roleRepository;
    
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public Role getRoleById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }
    
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name);
    }
}
