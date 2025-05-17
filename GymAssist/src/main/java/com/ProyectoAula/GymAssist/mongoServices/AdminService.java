package com.ProyectoAula.GymAssist.mongoServices;

import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoRepository.AdminRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminEntity save(AdminEntity admin) {
        return adminRepository.save(admin);
    }

    public AdminEntity findById(ObjectId id) {
        return adminRepository.findById(id).orElse(null);
    }

    public void actualizarCorreoYPasswordAdmin(String nuevoCorreo, String nuevaPassword, String usernameActual) {
        UserEntity user = userRepository.findByUsername(usernameActual)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!nuevoCorreo.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(nuevoCorreo)) {
            throw new RuntimeException("El correo ya está registrado.");
        }

        user.setEmail(nuevoCorreo);

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(nuevaPassword)); // ✅ Ahora `passwordEncoder` está disponible
        }

        userRepository.save(user);
    }

}
