package com.soutenance.assiduite1;

import com.soutenance.assiduite1.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClasseRepository extends JpaRepository<Classe, Long> {
    // Nouvelle méthode pour trouver une classe par son enseignant
    Optional<Classe> findByEnseignant(User enseignant);

    Optional<Classe> findById(Long id);
}