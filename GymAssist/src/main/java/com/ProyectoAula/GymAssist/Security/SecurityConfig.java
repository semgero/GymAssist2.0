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

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/Api/Auth/login", "/Api/Auth/logout", "/Api/Auth/register").permitAll()
                        .requestMatchers("/Css/**", "/Img/**", "/Js/**").permitAll()
                        .requestMatchers("/Error/**", "/Error").permitAll()
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
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .invalidSessionUrl("/Api/Auth/Login")
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession))
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
        return (_, response, authentication) -> {
            System.out.println("SUCCES HANDLER: " + authentication.getName()); // Log de depuración
            // Obtiene el rol del usuario autenticado
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