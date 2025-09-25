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

    @Autowired
    private ArduinoService arduinoService;

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

        // Récupérer la liste des étudiants de cette classe
        List<Etudiant> etudiantsDeLaClasse = etudiantRepo.findByClasse(classe);
        model.addAttribute("etudiantsDeLaClasse", etudiantsDeLaClasse);

        // Récupérer la liste des étudiants qui n'ont pas encore de classe
        List<Etudiant> etudiantsDisponibles = etudiantRepo.findByClasseIsNull();
        model.addAttribute("etudiantsDisponibles", etudiantsDisponibles);
        return "enseignant_classe";
    }


    @PostMapping("/enseignant/classe/{classeId}/add-etudiant")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public String addEtudiantToClasse(@PathVariable("classeId") Long classeId,
                                      @ModelAttribute("etudiantId") Long etudiantId,
                                      RedirectAttributes ra) {
        // Récupérer la classe et l'étudiant
        Classe classe = classeRepo.findById(classeId).orElseThrow(() -> new IllegalArgumentException("Classe invalide"));
        Etudiant etudiant = etudiantRepo.findById(etudiantId).orElseThrow(() -> new IllegalArgumentException("Étudiant invalide"));

        // Vérifier si l'étudiant est déjà affecté à une classe
        if (etudiant.getClasse() != null) {
            ra.addFlashAttribute("error", "Cet étudiant est déjà affecté à une autre classe.");
            return "redirect:/enseignant/classe/" + classeId;
        }

        // Affecter l'étudiant à la classe et sauvegarder (l'étudiant est maintenant lié)
        etudiant.setClasse(classe);
        etudiantRepo.save(etudiant);

        // Démarrer la capture de l'empreinte
        boolean captureStarted = arduinoService.startFingerprintCapture(etudiantId);

        if (captureStarted) {
            // --- MODIFICATION CRUCIALE ---
            // On ne redirige PAS vers la page de classe.
            // On redirige vers la page de statut d'attente, en passant l'ID.
            ra.addFlashAttribute("etudiantIdToEnroll", etudiantId);
            ra.addFlashAttribute("classeId", classeId); // Pour réutiliser dans la page de statut
            ra.addFlashAttribute("info", "Capture en cours. Veuillez suivre les instructions du capteur.");

            // Redirection vers le nouvel endpoint de statut
            return "redirect:/enseignant/classe/" + classeId + "/capture-en-cours";

        } else {
            // S'il y a une erreur technique (port occupé, etc.), on affiche l'erreur
            ra.addFlashAttribute("error", "Erreur: Une capture d'empreinte est déjà en cours ou le service Arduino est indisponible.");
            return "redirect:/enseignant/classe/" + classeId;
        }
    }


    // --- NOUVELLE MÉTHODE ---
// Cette méthode gère l'affichage de la page d'attente/statut de la capture
    @GetMapping("/enseignant/classe/{classeId}/capture-en-cours")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public String showCaptureStatus(@PathVariable("classeId") Long classeId,
                                    @ModelAttribute("etudiantIdToEnroll") Long etudiantId,
                                    @ModelAttribute("info") String infoMessage,
                                    Model model) {

        // Si l'attribut 'etudiantIdToEnroll' n'existe pas (accès direct à l'URL), on redirige
        if (etudiantId == null) {
            return "redirect:/enseignant/classe/" + classeId;
        }

        // On passe les infos au template
        model.addAttribute("classeId", classeId);
        model.addAttribute("etudiantId", etudiantId);
        model.addAttribute("info", infoMessage);

        // Le template sera capture_status.html
        return "capture_status";
    }

    // Dans AppController.java

    @PostMapping("/enseignant/classe/{classeId}/retirer-etudiant/{etudiantId}")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public String retirerEtudiantDeLaClasse(@PathVariable("classeId") Long classeId,
                                            @PathVariable("etudiantId") Long etudiantId,
                                            RedirectAttributes ra) {

        // 1. Récupérer l'étudiant à partir de son ID
        Etudiant etudiant = etudiantRepo.findById(etudiantId)
                .orElseThrow(() -> new IllegalArgumentException("ID d'étudiant invalide:" + etudiantId));

        // 2. Vérifier si l'étudiant est bien dans une classe
        if (etudiant.getClasse() != null) {
            // 3. Retirer l'affectation à la classe en mettant le champ à 'null'
            etudiant.setClasse(null);
            etudiantRepo.save(etudiant);

            // 4. Ajouter un message de succès
            ra.addFlashAttribute("success", "L'étudiant a été retiré de la classe avec succès.");
        } else {
            // 5. Ajouter un message d'erreur si l'étudiant n'est pas dans une classe
            ra.addFlashAttribute("error", "Cet étudiant n'est pas affecté à une classe.");
        }

        // 6. Rediriger vers la page de la classe
        return "redirect:/enseignant/classe/" + classeId;
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
    public String saveUser(@ModelAttribute("user") User user) {
        // 1. Encoder le mot de passe avant de sauvegarder l'utilisateur
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2. Sauvegarder l'utilisateur et récupérer l'objet sauvegardé
        // C'est crucial car l'objet retourné contient l'ID généré
        User savedUser = repo.save(user);

        // 3. Vérifier le rôle de l'utilisateur
        if ("ROLE_ENSEIGNANT".equals(user.getRole())) {
            // Si le rôle est "ENSEIGNANT", créer une nouvelle entité Enseignant
            Enseignant enseignant = new Enseignant();
            enseignant.setUser(savedUser); // Lier l'enseignant à l'utilisateur
            enseignantRepo.save(enseignant); // Sauvegarder l'entité Enseignant

        } else if ("ROLE_ETUDIANT".equals(user.getRole())) {
            // Si le rôle est "ETUDIANT", créer une nouvelle entité Etudiant
            Etudiant etudiant = new Etudiant();
            etudiant.setUser(savedUser); // Lier l'étudiant à l'utilisateur
            etudiantRepo.save(etudiant); // Sauvegarder l'entité Etudiant
        }

        // 4. Rediriger l'utilisateur
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
