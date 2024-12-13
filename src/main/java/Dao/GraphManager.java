package Dao;


import javafx.scene.image.Image;

import java.io.*;
import java.sql.*;

public class GraphManager {
	
	 private Connection connection;

	    // Constructor to initialize the DB connection
	    public GraphManager(DBConnexion connection) throws SQLException, ClassNotFoundException {
	        this.connection = DBConnexion.getCon();
	    }

	    public boolean upsertGraph(int userId, String city, String graphType, byte[] image) throws SQLException {
	        String query = """
	        INSERT INTO graph (Id_user, Ville, Type_graph, Image, Date_dernier_update)
	        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
	        ON DUPLICATE KEY UPDATE
	        Image = VALUES(Image), Date_dernier_update = CURRENT_TIMESTAMP
	    """;

	        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
	            statement.setInt(1, userId);
	            statement.setString(2, city);
	            statement.setString(3, graphType);
	            statement.setBytes(4, image);

	            return statement.executeUpdate() != 0;
	        }
	    }


	    // Method to retrieve a graph for a user, city, and graph type
	    public byte[] getGraphImage(int userId, String city, String graphType) throws SQLException {
	        String query = "SELECT Image FROM graph WHERE Id_user = ? AND Ville = ? AND Type_graph = ?";
	        try (PreparedStatement statement = connection.prepareStatement(query)) {
	            statement.setInt(1, userId);
	            statement.setString(2, city);
	            statement.setString(3, graphType);

	            try (ResultSet resultSet = statement.executeQuery()) {
	                if (resultSet.next()) {
	                    return resultSet.getBytes("Image");
	                }
	            }
	        }
	        return null; // Return null if no graph found
	    }

	    // Method to check if an update is needed
	    public boolean isUpdateNeeded(int userId, String city, String graphType) throws SQLException {
	        String query = "SELECT Date_dernier_update FROM graph WHERE Id_user = ? AND Ville = ? AND Type_graph = ?";
	        try (PreparedStatement statement = connection.prepareStatement(query)) {
	            statement.setInt(1, userId);
	            statement.setString(2, city);
	            statement.setString(3, graphType);

	            try (ResultSet resultSet = statement.executeQuery()) {
	                if (resultSet.next()) {
	                    Timestamp lastUpdate = resultSet.getTimestamp("Date_dernier_update");
	                    // Check if the last update is before today
	                    return lastUpdate.before(new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
	                }
	            }
	        }
	        return true; // If no graph found, update is needed
	    }

	    // Method to delete all graphs for a user
	    public void deleteUserGraphs(int userId) throws SQLException {
	        String query = "DELETE FROM graph WHERE Id_user = ?";
	        try (PreparedStatement statement = connection.prepareStatement(query)) {
	            statement.setInt(1, userId);
	            statement.executeUpdate();
	        }
	    }

	    public Image loadImageFromDatabase(int userId) throws IOException {
	        String query = "SELECT graph_image FROM weather_graphs WHERE user_id = ?";
	        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/your_database", "username", "password");
	             PreparedStatement stmt = conn.prepareStatement(query)) {

	            stmt.setInt(1, userId);  // Set user ID parameter
	            ResultSet rs = stmt.executeQuery();

	            if (rs.next()) {
	                Blob blob = rs.getBlob("graph_image");
	                byte[] imageBytes = blob.getBytes(1, (int) blob.length());
	                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
	                return new Image(bis);  // Return the Image object
	            } else {
	                System.out.println("No image found for user ID: " + userId);
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;  // Return null if no image is found or an error occurs
	    }

	    // Close connection (optional for cleanup)
	    public void close() throws SQLException {
	        if (connection != null && !connection.isClosed()) {
	            connection.close();
	        }
	    }

}
