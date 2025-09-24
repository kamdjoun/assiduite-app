package com.soutenance.assiduite1;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AppController.class, webSecurityConfig.class})
@Import({CustomLoginSuccessHandle.class, CustomUserDetailsService.class, BCryptPasswordEncoder.class})
public class WebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // Simule les dépendances non chargées par @WebMvcTest
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DataSource dataSource;

    // Teste l'accès public
    @Test
    public void testPublicAccess() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    // Teste l'accès à une page restreinte sans authentification
    @Test
    public void testRestrictedAccessWithoutLogin() throws Exception {
        mockMvc.perform(get("/admin_dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    // Teste une connexion réussie pour un administrateur
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testAdminLoginSuccess() throws Exception {
        mockMvc.perform(get("/admin_dashboard"))
                .andExpect(status().isOk());
    }

    // Teste une connexion réussie pour un enseignant
    @Test
    @WithMockUser(username = "enseignant", roles = "ENSEIGNANT")
    public void testEnseignantLoginSuccess() throws Exception {
        mockMvc.perform(get("/enseignant_dashboard"))
                .andExpect(status().isOk());
    }

    // Teste une connexion réussie pour un étudiant
    @Test
    @WithMockUser(username = "etudiant", roles = "ETUDIANT")
    public void testEtudiantLoginSuccess() throws Exception {
        mockMvc.perform(get("/etudiant_dashboard"))
                .andExpect(status().isOk());
    }

    // Teste un accès non autorisé à une page d'un autre rôle
    @Test
    @WithMockUser(username = "etudiant", roles = "ETUDIANT")
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/admin_dashboard"))
                .andExpect(status().isForbidden()); 
    }
}