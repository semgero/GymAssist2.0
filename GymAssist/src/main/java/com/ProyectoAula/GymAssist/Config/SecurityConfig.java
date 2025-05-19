package com.ProyectoAula.GymAssist.Config;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.config.Customizer;

import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoRepository.AdminRepository;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import com.ProyectoAula.GymAssist.mongoRepository.GimnasiosRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;
import com.ProyectoAula.GymAssist.mongoServices.CustomUserDetailsService;

import jakarta.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final GimnasiosRepository gimnasiosRepository;
    private final ClientRepository clientRepository;

    public SecurityConfig(UserRepository userRepository, AdminRepository adminRepository,
            GimnasiosRepository gimnasiosRepository, ClientRepository clientRepository) {
        this.gimnasiosRepository = gimnasiosRepository;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.clientRepository = clientRepository;
    }

    /**
     * Configura las reglas de seguridad de la aplicación.
     * 
     * @param http La configuración de seguridad HTTP.
     * @return El filtro de seguridad configurado.
     * @throws Exception Si ocurre un error al configurar la seguridad.
     */

    @Bean
    public SecurityFilterChain securedFilterChain(final HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/Api/Auth/index", "/Api/Auth/login", "/Api/Auth/logout",
                                "/Api/Auth/register")
                        .permitAll()
                        .requestMatchers("/Styles/**", "/Imagenes/**", "/Js/**", "/uploads/**", "/content/**")
                        .permitAll()
                        .requestMatchers("/Error/**", "/Error").permitAll()
                        .requestMatchers("/gimnasios/register").authenticated()
                        .requestMatchers("/rutinas/**").authenticated()
                        .requestMatchers("/Api/Admin/**").hasRole("ADMIN")
                        .requestMatchers("/Api/Cliente/**").hasRole("CLIENTE")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/Api/Auth/login")
                        .loginProcessingUrl("/Api/Auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/Api/Auth/Logout")
                        .logoutSuccessUrl("/Api/Auth/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/Api/Auth/Login")
                        .sessionFixation().newSession())
                .authenticationProvider(authenticationProvider(userDetailsService(userRepository), passwordEncoder())) // SOLO
                // ESTA
                // LÍNEA
                // AQUÍ
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Configura el servicio de usuarios personalizados.
     * 
     * @param userRepository El repositorio de usuarios.
     * @return El servicio de usuarios para autenticación.
     */
    @Bean
    public CustomUserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    /**
     * Configura el codificador de contraseñas.
     * 
     * @return Codificador BCrypt para contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación.
     * 
     * @param userDetailsService El servicio de usuarios.
     * @param passwordEncoder    El codificador de contraseñas.
     * @return El proveedor de autenticación.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Configura el manejador de autenticación.
     * 
     * @param authenticationConfiguration La configuración de autenticación.
     * @return El manejador de autenticación.
     * @throws Exception Si ocurre un error al configurar.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configura el manejador de éxito al autenticarse.
     * Redirige al usuario según su rol.
     * 
     * @return El manejador de éxito personalizado.
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("");

            HttpSession session = request.getSession(); 
            String redirectUrl = "/Api/Auth/login";

            if ("ROLE_ADMIN".equals(role)) {
                UserEntity user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    AdminEntity admin = adminRepository.findByUserId(user.getId()).orElse(null);
                    if (admin != null) {
                        boolean tieneGym = gimnasiosRepository.findByAdminId(admin.getId()).isPresent();
                        if (!tieneGym) {
                            redirectUrl = "/gimnasios/register";
                        } else {
                            redirectUrl = "/Api/Admin/AdminHome";
                            ObjectId gymId = gimnasiosRepository.findByAdminId(admin.getId())
                                    .map(GimnasiosEntity::getId)
                                    .orElse(null);
                            session.setAttribute("gymId", gymId);
                        }
                    }
                }
            } else if ("ROLE_CLIENTE".equals(role)) {
                UserEntity user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    Optional<ClientEntity> clienteOpt = clientRepository.findByUsername(username);
                    if (clienteOpt.isPresent()) {
                        ClientEntity cliente = clienteOpt.get();
                        ObjectId gymId = cliente.getGymId();
                        session.setAttribute("gymId", gymId);

                        switch (cliente.getEstado()) {
                            case SUSPENDIDO:
                                redirectUrl = "/Api/Auth/login?error=accesoDenegado";
                                break;
                            case PENDIENTE:
                                redirectUrl = "/Api/Cliente/ClientePago";
                                break;
                            case ACTIVO:
                                redirectUrl = "/Api/Cliente/ClienteHome";
                                break;
                        }
                    }
                }
            }

            response.sendRedirect(redirectUrl);
        };
    }
}