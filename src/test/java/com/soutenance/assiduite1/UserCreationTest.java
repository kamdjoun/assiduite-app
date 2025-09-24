package com.soutenance.assiduite1;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Importez cette classe
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppController.class)
public class UserCreationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean // Ajoutez cette ligne
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    public void testCreateNewUser() throws Exception {
        // Simule la soumission du formulaire
        mockMvc.perform(post("/save_user")
                        .param("email", "test@example.com")
                        .param("password", "password123")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("role", "ROLE_ETUDIANT"))
                .andExpect(status().isFound()) // 302 Found pour la redirection
                .andExpect(redirectedUrl("/list_users"));

        // Vérifie si la méthode save() du repository a été appelée une fois
        verify(userRepository, times(1)).save(any(User.class));
    }
}