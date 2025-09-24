package com.soutenance.assiduite1;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Utilise Optional pour gérer l'absence de résultat, prévenant les NullPointerException
    Optional<User> findByEmail(String email);

    // Nom de méthode plus général pour la recherche par rôle
    List<User> findByRole(String role);
}