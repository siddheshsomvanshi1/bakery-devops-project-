import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DB {
    static Connection connect() throws SQLException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER"); 
        String pass = System.getenv("DB_PASS");
        if (url == null || user == null || pass == null) throw new SQLException("missing env");
        return DriverManager.getConnection(url, user, pass);
    }

    static String productsJson() {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT id,name,price,image FROM products ORDER BY id ASC");
             ResultSet rs = ps.executeQuery()) {
            List<String> items = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String image = rs.getString("image");
                items.add("{\"id\":" + id + ",\"name\":\"" + esc(name) + "\",\"price\":" + price + ",\"image\":\"" + esc(image) + "\"}");
            }
            return "[" + String.join(",", items) + "]";
        } catch (Exception e) {
            return "[" +
                    "{\"id\":1,\"name\":\"Cake\",\"price\":49.99,\"image\":\"/img/product-1.jpg\"}," +
                    "{\"id\":2,\"name\":\"Bread\",\"price\":14.99,\"image\":\"/img/product-2.jpg\"}," +
                    "{\"id\":3,\"name\":\"Cookies\",\"price\":24.49,\"image\":\"/img/product-3.jpg\"}," +
                    "{\"id\":4,\"name\":\"Pastry\",\"price\":15.00,\"image\":\"/img/product-1.jpg\"}," +
                    "{\"id\":5,\"name\":\"Donuts\",\"price\":10.00,\"image\":\"/img/product-2.jpg\"}," +
                    "{\"id\":6,\"name\":\"Croissants\",\"price\":12.00,\"image\":\"/img/product-3.jpg\"}" +
                    "]";
        }
    }

    static String teamJson() {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT id,name,role,image FROM team ORDER BY id ASC");
             ResultSet rs = ps.executeQuery()) {
            List<String> items = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String role = rs.getString("role");
                String image = rs.getString("image");
                items.add("{\"id\":" + id + ",\"name\":\"" + esc(name) + "\",\"role\":\"" + esc(role) + "\",\"image\":\"" + esc(image) + "\"}");
            }
            return "[" + String.join(",", items) + "]";
        } catch (Exception e) {
            return "[" +
                    "{\"id\":1,\"name\":\"ganesh jadhav\",\"role\":\"Master Chef\",\"image\":\"/img/team-1.jpg\"}," +
                    "{\"id\":2,\"name\":\"akshay malviya\",\"role\":\"Bakery Specialist\",\"image\":\"/img/team-2.jpg\"}," +
                    "{\"id\":3,\"name\":\"krushna kharat\",\"role\":\"Cake Decorator\",\"image\":\"/img/team-3.jpg\"}," +
                    "{\"id\":4,\"name\":\"rushikesh yadhav\",\"role\":\"Pastry Expert\",\"image\":\"/img/team-4.jpg\"}" +
                    "]";
        }
    }

    static String testimonialsJson() {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT id,name,text FROM testimonials ORDER BY id ASC");
             ResultSet rs = ps.executeQuery()) {
            List<String> items = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String text = rs.getString("text");
                items.add("{\"id\":" + id + ",\"name\":\"" + esc(name) + "\",\"text\":\"" + esc(text) + "\"}");
            }
            return "[" + String.join(",", items) + "]";
        } catch (Exception e) {
            return "[" +
                    "{\"id\":1,\"name\":\"John\",\"text\":\"Best bakery in town\"}," +
                    "{\"id\":2,\"name\":\"Emma\",\"text\":\"Amazing croissants\"}," +
                    "{\"id\":3,\"name\":\"Liam\",\"text\":\"Great service and coffee\"}" +
                    "]";
        }
    }

    static String saveContact(String body) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("INSERT INTO contact_messages(payload) VALUES (?)")) {
            ps.setString(1, body);
            ps.executeUpdate();
            return "{\"status\":\"ok\",\"message\":\"saved to database\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + esc(e.getMessage()) + "\"}";
        }
    }

    static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
