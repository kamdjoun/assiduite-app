package com.soutenance.assiduite1;

import jakarta.persistence.*;

@Entity
@Table(name = "etudiants")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Colonne pour stocker l'empreinte digitale
    @Column(name = "empreinte_digitale", columnDefinition = "LONGBLOB")
    private byte[] empreinteDigitale;

    // Liaison avec la table User
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ... Getters et Setters ...


    public byte[] getEmpreinteDigitale() {
        return empreinteDigitale;
    }

    public void setEmpreinteDigitale(byte[] empreinteDigitale) {
        this.empreinteDigitale = empreinteDigitale;
    }

    @ManyToOne
    @JoinColumn(name = "classe_id") // Nom de la colonne dans la base de données
    private Classe classe;

    // ... Getters et Setters existants ...

    // Ajoutez les nouveaux Getters et Setters pour la classe
    public Classe getClasse() {
        return classe;
    }

    public void setClasse(Classe classe) {
        this.classe = classe;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}