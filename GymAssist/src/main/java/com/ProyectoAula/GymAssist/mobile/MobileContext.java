package com.ProyectoAula.GymAssist.mobile;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoRepository.AdminRepository;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import com.ProyectoAula.GymAssist.mongoRepository.GimnasiosRepository;
import com.ProyectoAula.GymAssist.mongoRepository.PlanRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;

/** Quién está haciendo la petición (a partir del token) y a qué gimnasio pertenece. */
@Component
public class MobileContext {

    private final UserRepository users;
    private final AdminRepository admins;
    private final GimnasiosRepository gyms;
    private final ClientRepository clients;
    private final PlanRepository plans;

    public MobileContext(UserRepository users, AdminRepository admins, GimnasiosRepository gyms,
            ClientRepository clients, PlanRepository plans) {
        this.users = users;
        this.admins = admins;
        this.gyms = gyms;
        this.clients = clients;
        this.plans = plans;
    }

    public String username() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión");
        return auth.getName();
    }

    public UserEntity usuario() {
        return users.findByUsername(username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    public Optional<AdminEntity> admin(UserEntity u) {
        return admins.findByUserId(u.getId());
    }

    public Optional<GimnasiosEntity> gymDe(UserEntity u) {
        return admin(u).flatMap(a -> gyms.findByAdminId(a.getId()));
    }

    /** Gimnasio del admin logueado; 409 si todavía no lo ha registrado. */
    public GimnasiosEntity gymDelAdmin() {
        return gymDe(usuario()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                "Primero registra tu gimnasio"));
    }

    public ClientEntity cliente() {
        return clients.findByUsername(username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    public Optional<PlanEntity> planDe(ClientEntity c) {
        return c.getPlanId() == null ? Optional.empty() : plans.findById(c.getPlanId());
    }

    public ObjectId gymIdDe(ClientEntity c) {
        return planDe(c).map(PlanEntity::getGymId).orElse(null);
    }

    /** Evita que un admin toque clientes, planes o rutinas de otro gimnasio. */
    public void verificarMismoGym(ObjectId gymIdRecurso, GimnasiosEntity gym) {
        if (gymIdRecurso == null || !gymIdRecurso.equals(gym.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No pertenece a tu gimnasio");
        }
    }

    public static ObjectId oid(String id) {
        try {
            return new ObjectId(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id inválido: " + id);
        }
    }
}
