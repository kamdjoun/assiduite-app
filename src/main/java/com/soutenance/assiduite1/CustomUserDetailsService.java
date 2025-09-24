package com.soutenance.assiduite1;

import com.soutenance.assiduite1.User;
import com.soutenance.assiduite1.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Recherche l'utilisateur par email. La méthode retourne un Optional<User>.
        Optional<User> userOptional = repo.findByEmail(email);

        // Si l'Optional est vide, lance une exception, sinon extrait l'utilisateur.
        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // Crée l'autorité (le rôle) et retourne l'objet UserDetails de Spring Security.
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(authority)
        );
    }
}