import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackendServer {
    public static void main(String[] args) throws IOException {
        tryRegisterMySqlDriver();
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/products", new JsonHandler(() -> DB.productsJson()));
        server.createContext("/api/team", new JsonHandler(() -> DB.teamJson()));
        server.createContext("/api/testimonials", new JsonHandler(() -> DB.testimonialsJson()));
        server.createContext("/api/contact", new ContactHandler());
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        server.setExecutor(executor);
        server.start();
    }

    interface Supplier {
        String get();
    }

    static class JsonHandler implements HttpHandler {
        private final Supplier supplier;

        JsonHandler(Supplier supplier) {
            this.supplier = supplier;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                byte[] methodNotAllowed = "{\"error\":\"method not allowed\"}".getBytes(StandardCharsets.UTF_8);
                setJsonHeaders(exchange.getResponseHeaders());
                exchange.sendResponseHeaders(405, methodNotAllowed.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(methodNotAllowed);
                }
                return;
            }
            String json = supplier.get();
            byte[] payload = json.getBytes(StandardCharsets.UTF_8);
            setJsonHeaders(exchange.getResponseHeaders());
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(payload);
            }
        }
    }

    static class ContactHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                byte[] methodNotAllowed = "{\"error\":\"method not allowed\"}".getBytes(StandardCharsets.UTF_8);
                setJsonHeaders(exchange.getResponseHeaders());
                exchange.sendResponseHeaders(405, methodNotAllowed.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(methodNotAllowed);
                }
                return;
            }
            String body = readBody(exchange.getRequestBody());
            String response = DB.saveContact(body);
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            setJsonHeaders(exchange.getResponseHeaders());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static void addCors(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
    }

    static void setJsonHeaders(Headers headers) {
        headers.set("Content-Type", "application/json; charset=utf-8");
    }

    static String readBody(InputStream is) throws IOException {
        byte[] buf = is.readAllBytes();
        return new String(buf, StandardCharsets.UTF_8);
    }

    static void tryRegisterMySqlDriver() {
        try {
            Class<?> cls = Class.forName("com.mysql.cj.jdbc.Driver");
            Driver d = (Driver) cls.getDeclaredConstructor().newInstance();
            DriverManager.registerDriver(d);
        } catch (Exception ignored) {
        }
    }
}
