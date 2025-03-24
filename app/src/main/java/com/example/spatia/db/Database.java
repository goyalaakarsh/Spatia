package com.example.spatia.db;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private Connection connect() {
        String url = "jdbc:sqlite:ecommerce.db"; // Creates ecommerce.db in project folder
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        return conn;
    }

    public void init() {
        String[] tables = {
                "CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY, name TEXT, price REAL)",
                "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT)",
                "CREATE TABLE IF NOT EXISTS cart (userId INTEGER, productId INTEGER, quantity INTEGER)"
        };
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
            // Seed sample data
            stmt.execute("INSERT OR IGNORE INTO products (id, name, price) VALUES (1, 'Shirt', 19.99)");
            stmt.execute("INSERT OR IGNORE INTO products (id, name, price) VALUES (2, 'Pants', 29.99)");
        } catch (SQLException e) {
            System.out.println("Init error: " + e.getMessage());
        }
    }

    public List<Product> getAllProducts() {
        String sql = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(new Product(rs.getLong("id"), rs.getString("name"), rs.getDouble("price")));
            }
        } catch (SQLException e) {
            System.out.println("Get products error: " + e.getMessage());
        }
        return products;
    }

    public User addUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return new User(rs.getLong(1), username, password);
            }
        } catch (SQLException e) {
            System.out.println("Add user error: " + e.getMessage());
        }
        return null;
    }

    public User getUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getLong("id"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            System.out.println("Get user error: " + e.getMessage());
        }
        return null;
    }

    public void addToCart(long userId, long productId, int quantity) {
        String sql = "INSERT INTO cart (userId, productId, quantity) VALUES (?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Add to cart error: " + e.getMessage());
        }
    }
}