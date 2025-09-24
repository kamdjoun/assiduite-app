package com.soutenance.assiduite1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AppController {

    @Autowired
    private UserRepository repo;

    @Autowired
    private EtudiantRepository etudiantRepo;

    @Autowired
    private EnseignantRepository enseignantRepo;

    @Autowired
    private ClasseRepository classeRepo;

    @Autowired
    private ClasseRepository classeRepos;

    @GetMapping("/enseignant/classe/{classeId}")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public String showEnseignantClasse(@PathVariable("classeId") Long classeId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();
        User currentUser = repo.findByEmail(currentEmail).orElse(null);


        // Vérifie si la classe existe et si elle appartient à l'enseignant connecté
        Classe classe = classeRepos.findById(classeId).orElse(null);
        if (classe == null || !classe.getEnseignant().equals(currentUser)) {
            return "redirect:/access-denied"; // Gérer l'accès non autorisé
        }

        model.addAttribute("classe", classe);
        return "enseignant_classe";
    }

    @GetMapping("")
    public String viewHomePage(){
        return "index";
    }
    @GetMapping("/register")
    public String showSignUpForm(Model model){
        model.addAttribute("user",new User());
        return "signup_form";
    }
    @PostMapping("/process_register")
    public String processRegistration(User user){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoderPassword =encoder.encode(user.getPassword());
        user.setPassword(encoderPassword);
        user.setRole("ROLE_ADMIN");
        User savedUser = repo.save(user);

        if ("ROLE_ETUDIANT".equals(user.getRole())) {
            Etudiant etudiant = new Etudiant();
            etudiant.setUser(savedUser);
            etudiantRepo.save(etudiant);
        }// Nouvelle logique pour les enseignants
        else if ("ROLE_ENSEIGNANT".equals(user.getRole())) {
            Enseignant enseignant = new Enseignant();
            enseignant.setUser(savedUser);
            enseignantRepo.save(enseignant);
        }


        return "register_success";
    }

    // Nouveau endpoints pour les tableaux de bord
    @GetMapping("/etudiant_dashboard")
    public String etudiantDashboard() {
        return "etudiant_dashboard";
    }

    @GetMapping("/enseignant_dashboard")
    public String enseignantDashboard() {
        return "enseignant_dashboard";
    }

    @GetMapping("/admin_dashboard")
    public String adminDashboard() {
        return "admin_dashboard";
    }


    @GetMapping("/list_users")
    public String viewUserList(Model model){
        List<User> listUsers= repo.findAll();
        model.addAttribute("listUsers",listUsers);
        model.addAttribute("usersCount", listUsers.size());
        return "users";
    }


    @GetMapping("/add_user")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("pageTitle", "Ajouter un nouvel utilisateur");
        // Liste des rôles
        List<String> roles = Arrays.asList("ROLE_ADMIN", "ROLE_ENSEIGNANT", "ROLE_ETUDIANT");
        model.addAttribute("roles", roles);
        return "user_form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User user = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Modifier l'utilisateur (ID: " + id + ")");
        // Liste des rôles
        List<String> roles = Arrays.asList("ROLE_ADMIN", "ROLE_ENSEIGNANT", "ROLE_ETUDIANT");
        model.addAttribute("roles", roles);
        return "user_form";
    }

    @PostMapping("/save_user")
    public String saveUser(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repo.save(user);
        return "redirect:/list_users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            repo.deleteById(id);
            ra.addFlashAttribute("message", "L'utilisateur a été supprimé avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Une erreur est survenue lors de la suppression.");
        }
        return "redirect:/list_users";
    }

    @GetMapping("/list_etudiants")
    public String listEtudiants(Model model) {
        List<User> users = repo.findAll();
        List<User> etudiants = users.stream()
                .filter(user -> "ROLE_ETUDIANT".equals(user.getRole()))
                .collect(Collectors.toList());
        model.addAttribute("etudiantCount", etudiants.size());

        // Mise à jour de la logique de mapping pour inclure la date
        List<EtudiantDTO> etudiantDTOs = etudiants.stream()
                .map(user -> new EtudiantDTO(
                        user.getFirstname() + " " + user.getLastname(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCreationDate() // Récupère et passe la date
                ))
                .collect(Collectors.toList());

        model.addAttribute("etudiantList", etudiantDTOs);
        return "list_etudiants";
    }

    // ... dans AppController.java

    // Nouvelle méthode pour afficher la liste des enseignants
    @GetMapping("/list_enseignants")
    public String listEnseignants(Model model) {
        // Récupère tous les utilisateurs
        List<User> users = repo.findAll();

        // Filtre les utilisateurs pour ne garder que les enseignants
        List<User> enseignants = users.stream()
                .filter(user -> "ROLE_ENSEIGNANT".equals(user.getRole()))
                .collect(Collectors.toList());

        model.addAttribute("enseignantCount", enseignants.size());


        // Crée une liste de DTO (réutilisation d'EtudiantDTO, mais vous pouvez créer un EnseignantDTO si besoin)
        List<EtudiantDTO> enseignantDTOs = enseignants.stream()
                .map(user -> new EtudiantDTO(
                        user.getFirstname() + " " + user.getLastname(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCreationDate()
                ))
                .collect(Collectors.toList());

        // Ajoute la liste au modèle pour la vue
        model.addAttribute("enseignantList", enseignantDTOs);

        return "list_enseignants"; // Nom de la vue Thymeleaf
    }

    // Affiche le formulaire de création de classe
    @GetMapping("/admin/creer_classe")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateClasseForm(Model model) {
        // 1. Récupère tous les utilisateurs avec le rôle ENSEIGNANT
        List<User> enseignants = repo.findByRole("ROLE_ENSEIGNANT");

        // 2. Filtre les enseignants qui ne sont pas déjà affectés à une classe
        List<User> enseignantsDisponibles = enseignants.stream()
                .filter(enseignant -> !classeRepo.findByEnseignant(enseignant).isPresent())
                .collect(Collectors.toList());

        // 3. Ajoute la liste filtrée au modèle
        model.addAttribute("enseignants", enseignantsDisponibles);
        model.addAttribute("classe", new Classe());
        return "creer_classe";
    }

    // Traite la soumission du formulaire et enregistre la classe
    @PostMapping("/admin/creer_classe")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveClasse(@ModelAttribute Classe classe, Model model) {
        // Vérification de sécurité : L'enseignant est-il déjà affecté à une classe ?
        if (classeRepo.findByEnseignant(classe.getEnseignant()).isPresent()) {
            // Un enseignant ne peut pas être affecté deux fois.
            model.addAttribute("error", "Cet enseignant est déjà affecté à une classe.");
            // Recharger la liste des enseignants disponibles
            List<User> enseignants = repo.findByRole("ROLE_ENSEIGNANT");
            List<User> enseignantsDisponibles = enseignants.stream()
                    .filter(enseignant -> !classeRepo.findByEnseignant(enseignant).isPresent())
                    .collect(Collectors.toList());
            model.addAttribute("enseignants", enseignantsDisponibles);
            // Retourne au formulaire avec un message d'erreur
            return "creer_classe";
        }

        classeRepo.save(classe);
        return "redirect:/admin/liste_classes";
    }
    // Affiche la liste des classes
    @GetMapping("/admin/liste_classes")
    @PreAuthorize("hasRole('ADMIN')")
    public String listClasses(Model model) {
        List<Classe> classes = classeRepo.findAll();
        model.addAttribute("classes", classes);
        return "liste_classes";
    }

    // Affiche le formulaire de modification d'une classe
    @GetMapping("/admin/modifier_classe/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditClasseForm(@PathVariable Long id, Model model) {
        Classe classe = classeRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de classe invalide:" + id));
        List<User> enseignants = repo.findByRole("ROLE_ENSEIGNANT");
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("classe", classe);
        return "modifier_classe";
    }

    // Traite la mise à jour d'une classe
    @PostMapping("/admin/modifier_classe/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateClasse(@PathVariable Long id, @ModelAttribute Classe classe) {
        classe.setId(id); // S'assure que l'ID est mis à jour
        classeRepo.save(classe);
        return "redirect:/admin/liste_classes";
    }

    // Supprime une classe
    @GetMapping("/admin/supprimer_classe/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteClasse(@PathVariable Long id) {
        classeRepo.deleteById(id);
        return "redirect:/admin/liste_classes";
    }

}
