package StartUpSupport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class SuccessStory {
    private String startupName;
    private String description;

    public SuccessStory(String startupName, String description) {
        this.startupName = startupName;
        this.description = description;
    }

    public String getStartupName() {
        return startupName;
    }

    public void setStartupName(String startupName) {
        this.startupName = startupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

public class SuccessStoryManager {
    private Connection conn;

    public SuccessStoryManager(Connection conn) {
        this.conn = conn;
    }

    public List<SuccessStory> getAllSuccessStories() throws SQLException {
        List<SuccessStory> successStories = new ArrayList<>();
        String query = "SELECT startup_name, description FROM SuccessStories";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String startupName = rs.getString("startup_name");
                String description = rs.getString("description");
                successStories.add(new SuccessStory(startupName, description));
            }
        }
        return successStories;
    }
    
    public void displayAllSuccessStories() throws SQLException {
        List<SuccessStory> successStories = getAllSuccessStories();
        System.out.println("Success Stories:");
        System.out.println("+---------------------+-----------------------------------------------------------+");
        System.out.println("| Startup Name        | Description                                               |");
        System.out.println("+---------------------+-----------------------------------------------------------+");
        for (SuccessStory story : successStories) {
            System.out.printf("| %-20s | %-65s |\n", story.getStartupName(), formatDescription(story.getDescription()));
        }
        System.out.println("+---------------------+-----------------------------------------------------------+");
    }

    private String formatDescription(String description) {
        // Split the description into multiple lines with maximum length of 65 characters
        StringBuilder formattedDescription = new StringBuilder();
        int maxLineLength = 65;
        for (int i = 0; i < description.length(); i += maxLineLength) {
            int endIndex = Math.min(i + maxLineLength, description.length());
            formattedDescription.append(String.format("%-65s", description.substring(i, endIndex))).append("\n");
        }
        return formattedDescription.toString();
    }
}
