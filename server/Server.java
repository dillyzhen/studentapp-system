import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.sql.*;

public class Server {
    static final int PORT = 3000;
    static Connection conn;
    
    public static void main(String[] args) throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:./data/studentapp", "sa", "");
        
        HttpServer srv = HttpServer.create(new InetSocketAddress(PORT), 0);
        srv.createContext("/api/health", Server::health);
        srv.createContext("/api/students", Server::students);
        srv.setExecutor(null);
        srv.start();
        
        System.out.println("StudentApp running on " + PORT);
    }
    
    static void health(HttpExchange ex) {
        send(ex, "{\"status\":\"ok\"}");
    }
    
    static void students(HttpExchange ex) {
        try {
            if ("GET".equals(ex.getRequestMethod())) {
                var rs = conn.createStatement().executeQuery("SELECT * FROM students");
                var sb = new StringBuilder("[");
                while (rs.next()) {
                    if (sb.length() > 1) sb.append(",");
                    sb.append("{\"name\":\"").append(rs.getString("name")).append("\"}");
                }
                sb.append("]");
                send(ex, sb.toString());
            } else if ("POST".equals(ex.getRequestMethod())) {
                // Read body VERY carefully
                var is = ex.getRequestBody();
                var contentLength = ex.getRequestHeaders().getFirst("Content-Length");
                System.out.println("Content-Length: " + contentLength);
                
                var baos = new ByteArrayOutputStream();
                var buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                var body = baos.toString();
                System.out.println("Body: " + body);
                
                // Parse name
                var nameStart = body.indexOf("name") + 6;
                var nameEnd = body.indexOf("\"", nameStart);
                var name = body.substring(nameStart, nameEnd);
                
                var id = UUID.randomUUID().toString();
                conn.createStatement().execute("INSERT INTO students (id, name, age) VALUES ('" + id + "', '" + name + "', 10)");
                
                send(ex, "{\"success\":true,\"name\":\"" + name + "\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    static void send(HttpExchange ex, String json) {
        try {
            ex.getResponseHeaders().set("Content-Type", "application/json");
            var b = json.getBytes();
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
        } catch (Exception e) {}
    }
}
