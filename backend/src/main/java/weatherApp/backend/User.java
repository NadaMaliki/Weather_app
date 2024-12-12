package weatherApp.backend;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
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
       this.id = id;
       this.nom = nom;
       this.prenom = prenom;
       this.email = email;
       this.password = password;
       this.alerte = alerte;	// 0 = aucune, 1 = email, 2 = desktop, 3 = email + desktop

   }
   
   public User(int i) {
       this.id = i;
   }
   

}

