package Model;
//llllllllllllllllllllllllllllllllll
import Model.User;

public class Session {
    private static User currentUser;

    // Définit l'utilisateur connecté
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Récupère l'utilisateur connecté
    public static User getCurrentUser() {
        return currentUser;
    }

    // Vérifie si un utilisateur est connecté
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // Déconnecte l'utilisateur
    public static void logout() {
        currentUser = null;
    }

    // Récupère l'email de l'utilisateur connecté
    public static String getCurrentUserEmail() {
        return isLoggedIn() ? currentUser.getEmail() : null;
    }

    // Récupère le nom complet de l'utilisateur connecté
    public static String getCurrentUserFullName() {
        return isLoggedIn() ? currentUser.getNom() + " " + currentUser.getPrenom() : null;
    }

    
}
