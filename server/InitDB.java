import java.sql.*;
public class InitDB {
    public static void main(String[] args) throws Exception {
        var conn = DriverManager.getConnection("jdbc:h2:./data/studentapp", "sa", "");
        var stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS students (id VARCHAR(36) PRIMARY KEY, name VARCHAR(50), age INT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS health_records (id VARCHAR(36) PRIMARY KEY, student_id VARCHAR(36), height DOUBLE, weight DOUBLE, vision DOUBLE, health_notes VARCHAR(500))");
        stmt.execute("CREATE TABLE IF NOT EXISTS learning_records (id VARCHAR(36) PRIMARY KEY, student_id VARCHAR(36), subject VARCHAR(50), score INT, notes VARCHAR(500))");
        System.out.println("Tables created in new DB");
        
        // Add test student
        stmt.execute("INSERT INTO students (id, name, age) VALUES ('test-001', '测试学生', 10)");
        System.out.println("Test student added");
        
        conn.close();
    }
}
