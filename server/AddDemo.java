import java.sql.*;
import java.util.*;

public class AddDemo {
    public static void main(String[] args) throws Exception {
        var conn = DriverManager.getConnection("jdbc:h2:./data/studentapp", "sa", "");
        
        var students = new String[][] {{"student-001", "张三", "10"}, {"student-002", "李四", "9"}, {"student-003", "王五", "11"}};
        for (var s : students) {
            var stmt = conn.prepareStatement("INSERT INTO students (id, name, age) VALUES (?, ?, ?)");
            stmt.setString(1, s[0]);
            stmt.setString(2, s[1]);
            stmt.setInt(3, Integer.parseInt(s[2]));
            stmt.execute();
        }
        
        var rs = conn.createStatement().executeQuery("SELECT * FROM students");
        System.out.println("Students in DB:");
        while (rs.next()) {
            System.out.println("  - " + rs.getString("name") + " (" + rs.getInt("age") + ")");
        }
        conn.close();
    }
}
