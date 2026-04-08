import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

public class Server {
    static Connection conn;
    static Statement stmt;
  static Map<String, Map<String, String>> users = new HashMap<>();
    
    public static void main(String[] args) throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:./data/studentapp", "sa", "");
        stmt = conn.createStatement();
        
        // Tables
        stmt.execute("CREATE TABLE IF NOT EXISTS students (id VARCHAR(36) PRIMARY KEY, name VARCHAR(50), age INT, teacher_id VARCHAR(36))");
        stmt.execute("CREATE TABLE IF NOT EXISTS health_records (id VARCHAR(36) PRIMARY KEY, student_id VARCHAR(36), height DOUBLE, weight DOUBLE, vision DOUBLE, health_notes VARCHAR(500))");
        stmt.execute("CREATE TABLE IF NOT EXISTS learning_records (id VARCHAR(36) PRIMARY KEY, student_id VARCHAR(36), subject VARCHAR(50), score INT, notes VARCHAR(500))");
        stmt.execute("CREATE TABLE IF NOT EXISTS ai_reports (id VARCHAR(36) PRIMARY KEY, student_id VARCHAR(36), type VARCHAR(20), content VARCHAR(2000))");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id VARCHAR(36) PRIMARY KEY, username VARCHAR(50), password VARCHAR(100), name VARCHAR(50), role VARCHAR(20))");
        
        // Demo students
        stmt.execute("INSERT INTO students VALUES ('s1', '张三', 10, 't1')");
        stmt.execute("INSERT INTO students VALUES ('s2', '李四', 9, 't1')");
        stmt.execute("INSERT INTO students VALUES ('s3', '王五', 11, 't1')");
        
        // Demo users
        users.put("teacher1", Map.of("id", "t1", "username", "teacher1", "password", "pass123", "name", "张老师", "role", "teacher"));
        users.put("admin", Map.of("id", "a1", "username", "admin", "password", "admin123", "name", "管理员", "role", "admin"));
        
        HttpServer srv = HttpServer.create(new InetSocketAddress("0.0.0.0", 3000), 0);
        srv.createContext("/api/health", Server::onHealth);
        srv.createContext("/api/auth/login", Server::onLogin);
        srv.createContext("/api/students", Server::onStudents);
        srv.createContext("/api/students/", Server::onStudentDetail);
        srv.createContext("/api/ai/", Server::onAi);
        srv.setExecutor(null);
        srv.start();
        System.out.println("StudentApp ready on port 3000");
    }
    
    static void onHealth(HttpExchange ex) throws IOException { send(ex, "{\"status\":\"ok\"}"); }
    
    static void onLogin(HttpExchange ex) throws IOException {
        // 处理 CORS 预检
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            sendCors(ex);
            return;
        }
        var body = readBody(ex);
        var username = parse(body, "username", "");
        var password = parse(body, "password", "");
        
        var user = users.get(username);
        if (user != null && user.get("password").equals(password)) {
            send(ex, "{\"success\":true,\"token\":\"" + user.get("id") + "\",\"user\":{\"name\":\"" + user.get("name") + "\",\"role\":\"" + user.get("role") + "\"}}");
        } else {
            send(ex, "{\"success\":false,\"error\":\"登录失败\"}");
        }
    }
    
    static void sendCors(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.sendResponseHeaders(204, -1);
    }
    
    static void onStudents(HttpExchange ex) throws IOException {
        // 处理 CORS 预检
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            sendCors(ex);
            return;
        }
        try {
            if ("GET".equals(ex.getRequestMethod())) {
                var rs = stmt.executeQuery("SELECT * FROM students");
                var sb = new StringBuilder("[");
                while (rs.next()) {
                    if (sb.length() > 1) sb.append(",");
                    sb.append("{\"id\":\"").append(rs.getString("id")).append("\",\"name\":\"").append(rs.getString("name")).append("\",\"age\":").append(rs.getInt("age")).append("}");
                }
                sb.append("]");
                send(ex, sb.toString());
            } else if ("POST".equals(ex.getRequestMethod())) {
                var body = readBody(ex);
                var name = parse(body, "name", "Unknown");
                var age = Integer.parseInt(parse(body, "age", "10"));
                var id = "s" + System.currentTimeMillis();
                stmt.execute("INSERT INTO students VALUES ('" + id + "', '" + name + "', " + age + ", 't1')");
                send(ex, "{\"success\":true,\"id\":\"" + id + "\"}");
            }
        } catch (Exception e) { send(ex, "{\"error\":\"" + e.getMessage() + "\"}"); }
    }
    
    static void onStudentDetail(HttpExchange ex) throws IOException {
        // 处理 CORS 预检
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            sendCors(ex);
            return;
        }
        try {
            var path = ex.getRequestURI().getPath();
            var id = path.substring(path.lastIndexOf("/") + 1);
            if (id.isEmpty()) { send(ex, "[]"); return; }
            
            var rs = stmt.executeQuery("SELECT * FROM students WHERE id = '" + id + "'");
            if (!rs.next()) { send(ex, "{\"error\":\"不存在\"}"); return; }
            
            var sb = new StringBuilder("{\"id\":\"" + rs.getString("id") + "\",\"name\":\"" + rs.getString("name") + "\",\"age\":" + rs.getInt("age") + ",\"healthRecords\":[");
            var rs2 = stmt.executeQuery("SELECT * FROM health_records WHERE student_id = '" + id + "'");
            var first = true;
            while (rs2.next()) {
                if (!first) sb.append(",");
                sb.append("{\"height\":").append(rs2.getDouble("height")).append(",\"weight\":").append(rs2.getDouble("weight")).append(",\"vision\":").append(rs2.getDouble("vision")).append(",\"notes\":\"").append(nullable(rs2.getString("health_notes"))).append("\"}");
                first = false;
            }
            sb.append("],\"learningRecords\":[");
            
            var rs3 = stmt.executeQuery("SELECT * FROM learning_records WHERE student_id = '" + id + "'");
            first = true;
            while (rs3.next()) {
                if (!first) sb.append(",");
                sb.append("{\"subject\":\"").append(rs3.getString("subject")).append("\",\"score\":").append(rs3.getInt("score")).append(",\"notes\":\"").append(nullable(rs3.getString("notes"))).append("\"}");
                first = false;
            }
            sb.append("]}");
            send(ex, sb.toString());
        } catch (Exception e) { send(ex, "{\"error\":\"" + e.getMessage() + "\"}"); }
    }
    
    static void onAi(HttpExchange ex) throws IOException {
        // 处理 CORS 预检
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            sendCors(ex);
            return;
        }
        try {
            var body = readBody(ex);
            var studentId = parse(body, "studentId", "");
            var type = parse(body, "type", "health");
            
            if (studentId.isEmpty()) {
                send(ex, "{\"error\":\"需要studentId\"}");
                return;
            }
            
            // Get student
            var rs = stmt.executeQuery("SELECT name FROM students WHERE id = '" + studentId + "'");
            if (!rs.next()) { send(ex, "{\"error\":\"学生不存在\"}"); return; }
            var name = rs.getString("name");
            String content;
            
            if ("health".equals(type)) {
                content = "【健康分析报告】\\n学生: " + name + "\\n\\n根据体测数据分析，该生整体健康状况良好。建议继续保持规律作息和适量运动。";
            } else {
                content = "【学习分析报告】\\n学生: " + name + "\\n\\n该生学习态度端正，建议继续加强薄弱科目的练习。";
            }
            
            var reportId = UUID.randomUUID().toString();
            stmt.execute("INSERT INTO ai_reports (id, student_id, type, content) VALUES ('" + reportId + "', '" + studentId + "', '" + type + "', '" + content.replace("'", "") + "')");
            send(ex, "{\"success\":true,\"reportId\":\"" + reportId + "\",\"content\":\"" + content + "\"}");
        } catch (Exception e) { send(ex, "{\"error\":\"" + e.getMessage() + "\"}"); }
    }
    
    static String readBody(HttpExchange ex) throws IOException {
        var is = ex.getRequestBody();
        var baos = new ByteArrayOutputStream();
        var buf = new byte[1024];
        int r;
        while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
        return baos.toString();
    }
    
    static String parse(String s, String k, String def) {
        try {
            // Handle both "key": "value" and "key": value formats
            var pattern = "\"" + k + "\"\\s*:\\s*";
            var regex = pattern + "(?:\"([^\"]*)\"|(\\d+))";
            var m = java.util.regex.Pattern.compile(regex).matcher(s);
            if (m.find()) {
                if (m.group(1) != null) return m.group(1);
                if (m.group(2) != null) return m.group(2);
            }
            return def;
        } catch (Exception e) { return def; }
    }
    
    static String nullable(String s) { return s == null ? "" : s.replace("\"", "'"); }
    
    static void send(HttpExchange ex, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        var b = json.getBytes();
        ex.sendResponseHeaders(200, b.length);
        ex.getResponseBody().write(b);
    }
}
