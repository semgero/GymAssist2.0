package com.ProyectoAula.GymAssist.mongoServices;

import org.springframework.stereotype.Service;
import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoRepository.AdminRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminEntity save(AdminEntity admin) {
        return adminRepository.save(admin);
    }
}
