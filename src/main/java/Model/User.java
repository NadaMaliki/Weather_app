package Model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor

public class User {
	private int id;
   private String nom;
   private String prenom;
   private String email;
   private String password; 
   private int alerte;
   

   public User(int id, String nom, String prenom, String email, String password, int alerte, int preferencesId, int historiqueId) {
       this.setId(id);
       this.setNom(nom);
       this.setPrenom(prenom);
       this.setEmail(email);
       this.setPassword(password);
       this.setAlerte(alerte);	// 0 = aucune, 1 = email, 2 = desktop, 3 = email + desktop

   }
   
   public User(int i) {
       this.setId(i);
   }

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

	public int getAlerte() {
		return alerte;
	}

	public void setAlerte(int alerte) {
		this.alerte = alerte;
	}
   

}


