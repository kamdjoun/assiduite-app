package com.soutenance.assiduite1; // Correct package name


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomLoginSuccessHandle implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String userEmail = authentication.getName();

        // If the user is an ADMIN
        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin_dashboard");
            return;
        }

        // If the user is a TEACHER
        if (roles.contains("ROLE_ENSEIGNANT")) {
            User enseignant = userRepo.findByEmail(userEmail).orElse(null);
            if (enseignant != null && enseignant.getClasse() != null) {
                // Redirects the teacher to their assigned class page
                response.sendRedirect("/enseignant/classe/" + enseignant.getClasse().getId());
                return;
            } else {
                // Redirects to a general dashboard for teachers without an assigned class
                response.sendRedirect("/enseignant_dashboard");
                return;
            }
        }

        // If the user is a STUDENT
        if (roles.contains("ROLE_ETUDIANT")) {
            response.sendRedirect("/etudiant_dashboard");
            return;
        }

        // For all other cases (undefined roles)
        response.sendRedirect("/");
    }
}