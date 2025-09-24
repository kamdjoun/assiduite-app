package com.soutenance.assiduite1;

import com.soutenance.assiduite1.User;
import jakarta.persistence.*;

@Entity
@Table(name = "enseignants")
public class Enseignant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liaison un-à-un avec la table User
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Getters and Setters
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