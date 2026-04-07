import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Server {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:h2:./data/studentapp", "sa", "");
        Statement stmt = conn.createStatement();
        
        stmt.execute("CREATE TABLE IF NOT EXISTS students (id VARCHAR(36) PRIMARY KEY, name VARCHAR(50), age INT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS health_records (id VARCHAR(36) PRIMARY KEY, student_id VARCHAR(36), height DOUBLE, weight DOUBLE, vision DOUBLE, health_notes VARCHAR(500))");
        stmt.execute("CREATE TABLE IF NOT EXISTS learning_records (id VARCHAR(36) PRIMARY KEY, student_id VARCHAR(36), subject VARCHAR(50), score INT, notes VARCHAR(500))");
        
        stmt.execute("INSERT INTO students VALUES ('s1', '张三', 10)");
        stmt.execute("INSERT INTO students VALUES ('s2', '李四', 9)");
        stmt.execute("INSERT INTO students VALUES ('s3', '王五', 11)");
        
        HttpServer srv = HttpServer.create(new InetSocketAddress(3000), 0);
        srv.createContext("/api/health", Server::onHealth);
        srv.createContext("/api/students", new StudentsHandler(stmt));
        srv.setExecutor(null);
        srv.start();
        System.out.println("Server ready on port 3000");
    }
    
    public static void onHealth(HttpExchange ex) throws IOException {
        String json = "{\"status\":\"ok\"}";
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(200, json.getBytes().length);
        ex.getResponseBody().write(json.getBytes());
    }
}

class StudentsHandler implements HttpHandler {
    Statement stmt;
    StudentsHandler(Statement stmt) { this.stmt = stmt; }
    
    public void handle(HttpExchange ex) throws IOException {
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
                var name = parse(body, "name", "NoName");
                var age = Integer.parseInt(parse(body, "age", "10"));
                var id = UUID.randomUUID().toString();
                stmt.execute("INSERT INTO students VALUES ('" + id + "', '" + name + "', " + age + ")");
                send(ex, "{\"success\":true,\"id\":\"" + id + "\"}");
            }
        } catch (Exception e) {
            send(ex, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    String readBody(HttpExchange ex) throws IOException {
        var is = ex.getRequestBody();
        var baos = new ByteArrayOutputStream();
        var buf = new byte[1024];
        int r;
        while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
        return baos.toString();
    }
    
    String parse(String s, String k, String def) {
        try {
            int i = s.indexOf("\"" + k + "\"");
            if (i < 0) return def;
            int colon = s.indexOf(":", i);
            int q1 = s.indexOf("\"", colon);
            int q2 = s.indexOf("\"", q1 + 1);
            return s.substring(q1 + 1, q2);
        } catch (Exception e) { return def; }
    }
    
    void send(HttpExchange ex, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        var b = json.getBytes();
        ex.sendResponseHeaders(200, b.length);
        ex.getResponseBody().write(b);
    }
}
