package com.ProyectoAula.GymAssist.mongoServices;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;
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
    public void crearCliente(String nombre, String correo, String telefono, String mensualidad, String username, String password) {
        // Verificar si el correo electrónico ya está registrado
        if (userRepository.existsByEmail(correo)) {
            throw new RuntimeException("El correo electrónico ya está en uso.");
        }
    
        // Verificar si el nombre de usuario ya está registrado
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }
    
        // Crear cliente
        ClientEntity cliente = new ClientEntity();             
        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        cliente.setTelefono(Integer.valueOf(telefono)); 
        cliente.setMensualidad(mensualidad);
        clientRepository.save(cliente);
    
        // Crear su usuario con el rol CLIENTE y encriptar su contraseña
        UserEntity user = new UserEntity(); 
        user.setUsername(username); // Usamos el username proporcionado
        user.setEmail(correo);
        user.setPassword(passwordEncoder.encode(password)); // Encriptamos la contraseña
        user.setRole("CLIENTE");
        userRepository.save(user);

    }

    // Listar todos los clientes
    public List<ClientEntity> listarClientes() {
        return clientRepository.findAll();
    }

    // Buscar cliente por ID
    public ClientEntity buscarClientePorId(String id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    // Actualizar cliente
    public void actualizarCliente(String id, String nombre, String correo, String telefono, String mensualidad, String username, String password) {
        ClientEntity cliente = buscarClientePorId(id);
        
        // Actualizamos los atributos del cliente
        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        cliente.setTelefono(Integer.valueOf(telefono));  // Convertimos a Integer
        cliente.setMensualidad(mensualidad);
        clientRepository.save(cliente);
    
        // Buscar el usuario asociado
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el nombre de usuario: " + username));
    
        // Actualizar el usuario con los nuevos datos
        user.setEmail(correo);  // Actualizamos el correo electrónico
        user.setUsername(username);  // Verificamos el nombre de usuario
        user.setPassword(passwordEncoder.encode(password));  // Actualizamos la contraseña encriptada
        userRepository.save(user);  // Guardamos el usuario actualizado
    }

    // Eliminar cliente
    public void eliminarCliente(String id) {
        clientRepository.deleteById(id);
    }
}
