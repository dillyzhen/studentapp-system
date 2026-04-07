import java.sql.*;
public class VerifyDB {
    public static void main(String[] args) throws Exception {
        var conn = DriverManager.getConnection("jdbc:h2:./data/studentapp", "sa", "");
        
        // Check existing tables
        var rs = conn.getMetaData().getTables(null, null, "%", null);
        System.out.println("Existing tables:");
        while (rs.next()) {
            System.out.println("  - " + rs.getString("TABLE_NAME"));
        }
        
        // Now try creating
        try {
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS students (id VARCHAR(36) PRIMARY KEY, name VARCHAR(50), age INT)");
            System.out.println("Created students table");
        } catch (Exception e) {
            System.out.println("Error creating students: " + e.getMessage());
        }
        
        conn.close();
    }
}
