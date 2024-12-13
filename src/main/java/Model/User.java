package Model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;



@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    // Champ optionnel si la base ne le contient pas
    private String nom;
    private String prenom;
    private String email;
    private String password; // Utilisation de 'password' au lieu de 'mdp'
    private int alerte = 0; // Par défaut désactivé
    private String villePreference; // Nouvelle propriété pour la ville préférée

    public User(int id, String nom, String prenom, String email, String password, int alerte) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.alerte = alerte;	// 0 = aucune, 1 = email, 2 = desktop, 3 = email + desktop

    }

    // Constructeur sans id (pour les nouveaux utilisateurs)
    public User(String username, String nom, String prenom, String email, String password) {

        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
    }

    public User(int id, String nom, String prenom, String email, String password) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;

    }

    public User(int id) {
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVillePreference() {
        return villePreference;
    }
    public void setVillePreference(String villePreference) {
        this.villePreference = villePreference;
    }
    public int isAlerte() {
        return alerte;
    }

    public void setAlerte(int alerte) {
        this.alerte = alerte;
    }
    public int getAlerte() {
        return alerte;
    }



}


