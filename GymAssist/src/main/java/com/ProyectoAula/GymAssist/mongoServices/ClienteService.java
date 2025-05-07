package com.ProyectoAula.GymAssist.mongoServices;

import org.bson.types.ObjectId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity.Asistencia;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClienteService {
    
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClientRepository clientRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Guardar un cliente nuevo (cuando el admin crea uno)
    public void crearCliente(String nombre, String correo, Integer telefono, String mensualidad, String username, String password, ObjectId adminId, ObjectId gymId, String idDocumento) {
        // Verificar si el correo electrónico ya está registrado
        if (userRepository.existsByEmail(correo)) {
            throw new RuntimeException("El correo electrónico ya está en uso.");
        }
    
        // Verificar si el nombre de usuario ya está registrado
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }
    
        String encodedPassword = passwordEncoder.encode(password);
        // Crear cliente
        ClientEntity cliente = new ClientEntity();             
        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        cliente.setIdDocumento(idDocumento); // Usamos el correo como ID de documento
        cliente.setTelefono(Integer.valueOf(telefono)); 
        cliente.setMensualidad(mensualidad);
        cliente.setUsername(username); // Usamos el username proporcionado
        cliente.setPassword(encodedPassword);
        cliente.setActivo(false); // Por defecto, el cliente está activo
        cliente.setAdminId(adminId);
        cliente.setGymId(gymId); // Encriptamos la contraseña
        clientRepository.save(cliente);
    
        // Crear su usuario con el rol CLIENTE y encriptar su contraseña
        UserEntity user = new UserEntity(); 
        user.setUsername(username); // Usamos el username proporcionado
        user.setEmail(correo);
        user.setPassword(encodedPassword); // setemas la contraseña que encriptamos en cliente
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
    public void actualizarCliente(ObjectId id, String nombre, String correo, String telefono, String mensualidad, String username, String password) {
        ClientEntity cliente = buscarClientePorId(id);
    
        // Buscar el usuario asociado usando el username original del cliente
        UserEntity existingUser = userRepository.findByUsername(cliente.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el nombre de usuario: " + cliente.getUsername()));
    
        // Actualizamos los atributos del cliente
        cliente.setNombre(nombre);
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

    public void eliminarCliente(ObjectId id) {
        ClientEntity cliente = buscarClientePorId(id);
    
        // Eliminar el usuario asociado si existe
        userRepository.findByUsername(cliente.getUsername())
            .ifPresent(userRepository::delete);
    
        // Eliminar el cliente
        clientRepository.deleteById(id);
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

}
