package com.soutenance.assiduite1;

import jakarta.persistence.*;

@Entity
@Table(name = "etudiants")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Colonne pour stocker l'empreinte digitale
    @Lob // Indique que c'est un Large Object (BLOB)
    @Column(name = "empreinte_digitale")
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