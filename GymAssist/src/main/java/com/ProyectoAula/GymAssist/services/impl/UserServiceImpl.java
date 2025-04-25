package com.ProyectoAula.GymAssist.services.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ProyectoAula.GymAssist.Dto.RegisterDto;
import com.ProyectoAula.GymAssist.models.AdminEntity;
import com.ProyectoAula.GymAssist.models.ClientEntity;
import com.ProyectoAula.GymAssist.models.ERole;
import com.ProyectoAula.GymAssist.models.RoleEntity;
import com.ProyectoAula.GymAssist.models.UserEntity;
import com.ProyectoAula.GymAssist.repositories.AdminRepositoryy;
import com.ProyectoAula.GymAssist.repositories.ClientRepositoryy;
import com.ProyectoAula.GymAssist.repositories.RoleRepository;
import com.ProyectoAula.GymAssist.repositories.UserRepositoryy;
import com.ProyectoAula.GymAssist.services.UserService;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService{
    
    private final UserRepositoryy userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepositoryy adminRepository;
    private final ClientRepositoryy clientRepository;

    public UserServiceImpl(UserRepositoryy userRepository, RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        AdminRepositoryy adminRepository,
        ClientRepositoryy clientRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.adminRepository = adminRepository;
    this.clientRepository = clientRepository;
    }

    @Override
    public void registerUser(RegisterDto registerDto) {
        ERole roleEnum = ERole.valueOf(registerDto.getRole().toUpperCase());
        RoleEntity role = roleRepository.findByName(roleEnum).orElseGet(() -> {
            RoleEntity newRole = new RoleEntity();
            newRole.setName(roleEnum);
            return roleRepository.save(newRole);
        });

        if (roleEnum.equals(ERole.ADMIN)) {
            AdminEntity admin = new AdminEntity();
            admin.setUsername(registerDto.getUsername());
            admin.setEmail(registerDto.getEmail());
            admin.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            admin.setRole(roleEnum);
            admin.setRoles(Set.of(role));
            adminRepository.save(admin); // esto guarda en la tabla admin Y en user
        } else if (roleEnum.equals(ERole.CLIENTE)) {
            ClientEntity client = new ClientEntity();
            client.setUsername(registerDto.getUsername());
            client.setEmail(registerDto.getEmail());
            client.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            client.setRole(roleEnum);
            client.setRoles(Set.of(role));
            clientRepository.save(client);
        } else {
            UserEntity user = new UserEntity();
            user.setUsername(registerDto.getUsername());
            user.setEmail(registerDto.getEmail());
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            user.setRole(roleEnum);
            user.setRoles(Set.of(role));
            userRepository.save(user);
        }
    }
}
