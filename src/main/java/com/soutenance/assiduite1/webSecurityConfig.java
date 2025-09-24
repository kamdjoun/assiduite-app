package com.soutenance.assiduite1;

import com.soutenance.assiduite1.CustomLoginSuccessHandle;
import com.soutenance.assiduite1.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import javax.sql.DataSource;

@EnableWebSecurity
@Configuration
public class webSecurityConfig {

    @Autowired
    private DataSource dataSource;

    // Injecter le CustomUserDetailsService qui est déjà un bean
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // Injecter le CustomLoginSuccessHandle qui est déjà un bean
    @Autowired
    private CustomLoginSuccessHandle customLoginSuccessHandle;

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService); // Utiliser le bean injecté
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/","/register", "/process_register").permitAll()
                        .requestMatchers("/admin_dashboard", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/enseignant_dashboard", "/enseignant/**").hasRole("ENSEIGNANT")
                        .requestMatchers("/etudiant_dashboard", "/etudiant/**").hasRole("ETUDIANT")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .successHandler(customLoginSuccessHandle) // Utiliser le bean injecté
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        return http.build();
    }
}