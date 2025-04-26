package com.ProyectoAula.GymAssist.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ProyectoAula.GymAssist.models.UserEntity;
import com.ProyectoAula.GymAssist.repositories.UserRepositoryy;

@Service
public class UserDetailServices implements UserDetailsService{
    
    @Autowired
    private UserRepositoryy userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("ususario no encontrado"));

        List<GrantedAuthority> authorities = userEntity.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());

            return new org.springframework.security.core.userdetails.User(
            userEntity.getUsername(), userEntity.getPassword(), authorities);
    }
}
