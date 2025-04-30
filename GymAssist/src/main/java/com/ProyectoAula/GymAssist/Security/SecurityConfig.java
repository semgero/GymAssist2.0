package com.ProyectoAula.GymAssist.Security;

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
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;
import com.ProyectoAula.GymAssist.mongoServices.CustomUserDetailsService;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * Configura las reglas de seguridad de la aplicación.
     * @param http La configuración de seguridad HTTP.
     * @return El filtro de seguridad configurado.
     * @throws Exception Si ocurre un error al configurar la seguridad.
     */
    @Bean
    public SecurityFilterChain securedFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/Api/Auth/login", "/Api/Auth/logout", "/Api/Auth/register").permitAll() // Permite acceso público
                        .requestMatchers("/Css/**", "/Img/**", "/Js/**").permitAll() // Permite acceso público
                        .requestMatchers("/Error/**", "/Error").permitAll()
                        .requestMatchers("/Api/Admin/**").hasRole("ADMIN") // Requiere rol de administrador
                        .requestMatchers("/Api/Cliente/**").hasRole("CLIENTE") // Requiere rol de usuario
                        .anyRequest().authenticated() // Autenticación para otras rutas
                )
                .formLogin(form -> form
                        .loginPage("/Api/Auth/login") // Página de inicio de sesión personalizada
                        .loginProcessingUrl("/Api/Auth/login") // URL de procesamiento de inicio de sesión
                        .usernameParameter("username") // Parámetro de nombre de usuario
                        .passwordParameter("password") // Parámetro de contraseña
                        .successHandler(authenticationSuccessHandler()) // Manejador de éxito personalizado
                        .permitAll() // Permitir acceso a la página de login
                )
                .logout(logout -> logout
                        .logoutUrl("/Api/Auth/Logout") // URL de cierre de sesión
                        .logoutSuccessUrl("/Api/Auth/login?logout") // Redirige tras cerrar sesión
                        .invalidateHttpSession(true) // Invalida completamente la sesión
                        .clearAuthentication(true) // Borra la autenticación actual
                        .deleteCookies("JSESSIONID") // Borra la cookie de sesión
                        .permitAll() // Permitir acceso a la página de logout
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS) // Crea nueva sesión al autenticarse
                        .invalidSessionUrl("/Api/Auth/Login") // Redirige si la sesión es inválida
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession) // Nueva sesión tras autenticarse
                )
                .httpBasic(Customizer.withDefaults()); // Habilita autenticación básica
        return http.build();
    }

    /**
     * Configura el servicio de usuarios personalizados.
     * @param userRepository El repositorio de usuarios.
     * @return El servicio de usuarios para autenticación.
     */
    @Bean
    public CustomUserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    /**
     * Configura el codificador de contraseñas.
     * @return Codificador BCrypt para contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación.
     * @param userDetailsService El servicio de usuarios.
     * @param passwordEncoder El codificador de contraseñas.
     * @return El proveedor de autenticación.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Configura el manejador de autenticación.
     * @param authenticationConfiguration La configuración de autenticación.
     * @return El manejador de autenticación.
     * @throws Exception Si ocurre un error al configurar.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configura el manejador de éxito al autenticarse.
     * Redirige al usuario según su rol.
     * @return El manejador de éxito personalizado.
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (_, response, authentication) -> {
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("");

            // Log de depuración
            System.out.println("Autenticado con el rol: " + role);

            String redirectUrl;
            switch (role) {
                case "ROLE_ADMIN":
                    redirectUrl = "/Api/Admin/AdminHome";
                    break;
                case "ROLE_CLIENTE":
                    redirectUrl = "/Api/Cliente/ClienteHome";
                    break;
                default:
                    redirectUrl = "/Api/Auth/login"; // En caso de que el rol no sea válido
                    break;
            }

            System.out.println("Redirigiendo a: " + redirectUrl);
            response.sendRedirect(redirectUrl); // Redirige al URL correspondiente
        };
    }
}