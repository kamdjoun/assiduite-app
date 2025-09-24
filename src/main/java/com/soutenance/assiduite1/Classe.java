package com.soutenance.assiduite1;

import com.soutenance.assiduite1.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "classes")
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nomClasse;

    @Column(nullable = false)
    private int effectifs;

    // L'enseignant responsable de la classe
    @ManyToOne
    @JoinColumn(name = "enseignant_id", nullable = false)
    private User enseignant;

    @Column(name = "date_creation", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dateCreation;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomClasse() {
        return nomClasse;
    }

    public void setNomClasse(String nomClasse) {
        this.nomClasse = nomClasse;
    }

    public int getEffectifs() {
        return effectifs;
    }

    public void setEffectifs(int effectifs) {
        this.effectifs = effectifs;
    }

    public User getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(User enseignant) {
        this.enseignant = enseignant;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}