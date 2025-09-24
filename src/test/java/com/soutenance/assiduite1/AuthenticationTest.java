package com.soutenance.assiduite1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@Rollback
public class AuthenticationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private static final String TEST_EMAIL = "test_auth@example.com";
    private static final String TEST_PASSWORD = "testpassword123";

    @BeforeEach
    public void setup() {
        // Crée un utilisateur de test avant chaque test
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setFirstname("Test");
        user.setLastname("User");
        user.setRole("ROLE_ETUDIANT");
        userRepository.save(user);
    }

    @Test
    public void testSuccessfulAuthentication() {
        // Crée une requête d'authentification avec les identifiants corrects
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(TEST_EMAIL, TEST_PASSWORD);

        // Exécute l'authentification
        Authentication authenticated = authenticationManager.authenticate(authenticationToken);

        // Vérifie si l'utilisateur est authentifié avec succès
        Assertions.assertNotNull(authenticated);
        assertTrue(authenticated.isAuthenticated());
    }

    @Test
    public void testFailedAuthentication() {
        // Crée une requête avec un mot de passe incorrect
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(TEST_EMAIL, "wrongpassword");

        // Vérifie que la tentative d'authentification échoue avec une exception
        assertThrows(BadCredentialsException.class, () -> {
            authenticationManager.authenticate(authenticationToken);
        });
    }
}