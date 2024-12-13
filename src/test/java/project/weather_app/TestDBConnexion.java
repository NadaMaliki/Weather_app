package project.weather_app;



import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;

import Dao.DBConnexion;
import org.junit.Before;
import org.junit.Test;

public class TestDBConnexion {

    private DBConnexion dbConnexion;

    @Before
    public void setUp() {
        // Initialiser la connexion avant chaque test
        dbConnexion = new DBConnexion();
    }

    @Test
    public void testConnection() {
        try (Connection conn = dbConnexion.getCon()) {
            // Vérifier si la connexion est valide
            assertTrue("La connexion à la base de données a échoué", conn != null && !conn.isClosed());
        } catch (SQLException e) {
            fail("Erreur lors de la connexion à la base de données : " + e.getMessage());
        }
    }
}
