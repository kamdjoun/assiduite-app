package com.soutenance.assiduite1;

import java.time.LocalDateTime;

public class EtudiantDTO {

    private String nomComplet;
    private String email;
    private String role;
    private LocalDateTime creationDate; // Ajout du champ pour la date

    // Constructeur mis à jour
    public EtudiantDTO(String nomComplet, String email, String role, LocalDateTime creationDate) {
        this.nomComplet = nomComplet;
        this.email = email;
        this.role = role;
        this.creationDate = creationDate;
    }

    // Getters
    public String getNomComplet() {
        return nomComplet;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }
}