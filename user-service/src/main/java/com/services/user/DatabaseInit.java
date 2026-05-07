package com.services.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
public class DatabaseInit {

    private static final String DB_URL = "jdbc:h2:file:./data/userdb;AUTO_SERVER=TRUE";

    @PostConstruct
    public void init() {
        try (Connection conn = DriverManager.getConnection(
                DB_URL, "sa", "")) {

            Statement stmt = conn.createStatement();

            // users table, stores custonmers and providers
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            username VARCHAR(100) UNIQUE NOT NULL,
                            password VARCHAR(100) NOT NULL,
                            role VARCHAR(20) NOT NULL,
                            profession VARCHAR(50),
                            wallet DOUBLE DEFAULT 0.0
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS categories (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) UNIQUE NOT NULL
                        )
                    """);

            // Default categories
            stmt.execute("MERGE INTO categories (name) KEY(name) VALUES ('Plumbing')");
            stmt.execute("MERGE INTO categories (name) KEY(name) VALUES ('Carpentry')");
            stmt.execute("MERGE INTO categories (name) KEY(name) VALUES ('Electrical')");
            stmt.execute("MERGE INTO categories (name) KEY(name) VALUES ('Cleaning')");
            stmt.execute("MERGE INTO categories (name) KEY(name) VALUES ('Painting')");

            // Admin user, created once at startup
            stmt.execute("""
                        MERGE INTO users (username, password, role, wallet)
                        KEY(username)
                        VALUES ('admin', 'admin123', 'ADMIN', 0.0)
                    """);

            System.out.println("UserDB initialized successfully");

        } catch (Exception e) {
            System.err.println("DB init failed: " + e.getMessage());
        }
    }

    // to call by userSerivce to get a connection
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, "sa", "");
    }
}