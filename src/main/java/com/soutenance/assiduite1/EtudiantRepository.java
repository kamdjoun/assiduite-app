package com.soutenance.assiduite1;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    // Trouve un étudiant par son User associé
    Optional<Etudiant> findByUser(User user);

    // Trouve tous les étudiants qui n'ont pas encore de classe
    List<Etudiant> findByClasseIsNull();

    // Trouve tous les étudiants d'une classe spécifique
    List<Etudiant> findByClasse(Classe classe);
}