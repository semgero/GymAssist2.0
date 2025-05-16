package com.ProyectoAula.GymAssist.mongoServices;

import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity.Asistencia;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity.EstadoCliente;
import com.ProyectoAula.GymAssist.mongoModels.ClienteResumenDTO;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoRepository.AdminRepository;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import com.ProyectoAula.GymAssist.mongoRepository.PlanRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClienteService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlanRepository planRepository;
    private final AdminRepository adminRepository;

    public ClienteService(ClientRepository clientRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder, PlanRepository planRepository, AdminRepository adminRepository) {
        this.clientRepository = clientRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
    }

    // Guardar un cliente nuevo (cuando el admin crea uno)
    public void crearCliente(String nombre, String correo, String idDocumento, Integer telefono, String mensualidad,
            String username, String password, ObjectId gymId, ObjectId planId) {

        if (userRepository.existsByEmail(correo)) {
            throw new RuntimeException("El correo electrónico ya está en uso.");
        }

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }

        String encodedPassword = passwordEncoder.encode(password);

        // 🔍 Buscar el plan en base de datos
        PlanEntity plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("El plan no existe."));

        int duracionMeses = plan.getDuracion(); // o getDuracionMeses(), depende cómo lo hayas nombrado

        ClientEntity cliente = new ClientEntity();
        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        cliente.setIdDocumento(idDocumento);
        cliente.setTelefono(telefono);
        cliente.setMensualidad(mensualidad);
        cliente.setUsername(username);
        cliente.setPassword(encodedPassword);
        cliente.setPlanId(planId);
        cliente.setEstado(EstadoCliente.PENDIENTE);
        cliente.setGymId(gymId);

        // 📅 Generar fechas según duración del plan
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusMonths(duracionMeses);
        cliente.setFechaInicioMembresia(fechaInicio);
        cliente.setFechaFinMembresia(fechaFin);

        clientRepository.save(cliente);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(correo);
        user.setPassword(encodedPassword);
        user.setRole("CLIENTE");
        userRepository.save(user);
    }

    // Listar todos los clientes
    public List<ClientEntity> listarClientesPorGym(ObjectId gymId) {
        return clientRepository.findByGymId(gymId);
    }

    // Buscar cliente por ID
    public ClientEntity buscarClientePorId(ObjectId id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    // Actualizar cliente
    public void actualizarCliente(ObjectId id, String nombre, String correo, String telefono, String mensualidad,
            String username, String password) {
        ClientEntity cliente = buscarClientePorId(id);

        // Buscar el usuario asociado usando el username original del cliente
        UserEntity existingUser = userRepository.findByUsername(cliente.getUsername())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado con el nombre de usuario: " + cliente.getUsername()));

        // Actualizamos los atributos del cliente
        cliente.setCorreo(correo);
        cliente.setTelefono(Integer.valueOf(telefono));
        cliente.setMensualidad(mensualidad);
        cliente.setUsername(username); // Actualiza el username en el cliente
        clientRepository.save(cliente);

        // Actualizar el usuario con los nuevos datos
        existingUser.setEmail(correo);
        existingUser.setUsername(username); // Actualiza el username en el usuario
        if (password != null && !password.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(existingUser);
    }

    public void cambiarEstadoCliente(ObjectId id, EstadoCliente nuevoEstado) {
        ClientEntity cliente = buscarClientePorId(id);
        cliente.setEstado(nuevoEstado);
        clientRepository.save(cliente);
    }

    public void registrarAsistencia(ObjectId clienteId, LocalDate fecha, List<String> musculos) {
        ClientEntity cliente = buscarClientePorId(clienteId);

        Asistencia asistencia = new Asistencia();
        asistencia.setFecha(fecha);
        asistencia.setMusculos(musculos);

        cliente.getAsistencias().add(asistencia);
        clientRepository.save(cliente);
    }

    public void contarInasistencia(ObjectId clienteId) {
        ClientEntity cliente = buscarClientePorId(clienteId);
        cliente.setInasistencias(cliente.getInasistencias() + 1);
        clientRepository.save(cliente);
    }

    public ClientEntity buscarPorUsername(String username) {
        return clientRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con username: " + username));
    }

    public ClienteResumenDTO obtenerResumenPorGym(ObjectId gymId) {
        List<ClientEntity> clientes = clientRepository.findByGymId(gymId);

        long activos = clientes.stream()
                .filter(c -> c.getEstado() == ClientEntity.EstadoCliente.ACTIVO)
                .count();
        long suspendidos = clientes.stream()
                .filter(c -> c.getEstado() == ClientEntity.EstadoCliente.SUSPENDIDO)
                .count();

        return new ClienteResumenDTO(activos, suspendidos);
    }

    public void actualizarCliente(ClientEntity clienteActualizado) {
        clientRepository.save(clienteActualizado);
    }

    public void actualizarDatosPersonales(String nuevoCorreo, String nuevoUsername, String nuevaPassword,
            String usernameActual) {
        ClientEntity cliente = clientRepository.findByUsername(usernameActual)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        UserEntity user = userRepository.findByUsername(usernameActual)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si se cambia el correo o username (y evitar duplicados)
        if (!nuevoCorreo.equalsIgnoreCase(cliente.getCorreo()) && userRepository.existsByEmail(nuevoCorreo)) {
            throw new RuntimeException("El correo ya está registrado.");
        }

        if (!nuevoUsername.equalsIgnoreCase(cliente.getUsername()) && userRepository.existsByUsername(nuevoUsername)) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }

        // Actualizar en ClientEntity
        cliente.setCorreo(nuevoCorreo);
        cliente.setUsername(nuevoUsername);
        clientRepository.save(cliente);

        // Actualizar en UserEntity
        user.setEmail(nuevoCorreo);
        user.setUsername(nuevoUsername);

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(nuevaPassword));
            cliente.setPassword(passwordEncoder.encode(nuevaPassword)); // también en ClientEntity
        }

        userRepository.save(user);
        clientRepository.save(cliente); // importante: guardar también el cliente
    }

}
