package com.services.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class UserService {

    @EJB
    private DatabaseInit db;


    //Register
    public String register(String username, String password,
            String role, String profession,
            double initialBalance) {
        try (Connection conn = db.getConnection()) {

            //Check if username already exists
            PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM users WHERE username = ?");
            check.setString(1, username);
            if (check.executeQuery().next()) {
                return error("Username already exists");
            }

            //Insert new user
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, role, profession, wallet) " +
                            "VALUES (?, ?, ?, ?, ?)");
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role.toUpperCase());
            stmt.setString(4, profession);
            stmt.setDouble(5, initialBalance);
            stmt.executeUpdate();

            return success("User registered successfully");

        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    //Login
    public String login(String username, String password) {
        try (Connection conn = db.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, role, profession, wallet FROM users " +
                            "WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("message", "Login successful");
                json.put("userId", rs.getInt("id"));
                json.put("role", rs.getString("role"));
                json.put("profession", rs.getString("profession"));
                json.put("wallet", rs.getDouble("wallet"));
                return json.toString();
            }

            return error("Invalid credentials");

        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    //Add funds
    public String addFunds(int userId, double amount) {
        try (Connection conn = db.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET wallet = wallet + ? WHERE id = ? AND role = 'CUSTOMER'");
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();

            if (rows == 0)
                return error("Customer not found");

            //return new balance
            PreparedStatement bal = conn.prepareStatement(
                    "SELECT wallet FROM users WHERE id = ?");
            bal.setInt(1, userId);
            ResultSet rs = bal.executeQuery();
            rs.next();

            JSONObject json = new JSONObject();
            json.put("message", "Funds added successfully");
            json.put("newBalance", rs.getDouble("wallet"));
            return json.toString();

        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    //get wallet balance
    public String getWallet(int userId) {
        try (Connection conn = db.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT wallet FROM users WHERE id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("balance", rs.getDouble("wallet"));
                return json.toString();
            }

            return error("User not found");

        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    //handle deduction
    public String deductWallet(int userId, double amount) {
        try (Connection conn = db.getConnection()) {

            //Check balance first
            PreparedStatement check = conn.prepareStatement(
                    "SELECT wallet FROM users WHERE id = ? AND role = 'CUSTOMER'");
            check.setInt(1, userId);
            ResultSet rs = check.executeQuery();

            if (!rs.next())
                return error("Customer not found");

            double balance = rs.getDouble("wallet");
            if (balance < amount)
                return error("Insufficient balance");

            //deduct
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET wallet = wallet - ? WHERE id = ?");
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            JSONObject json = new JSONObject();
            json.put("message", "Payment successful");
            json.put("remainingBalance", balance - amount);
            return json.toString();

        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    //refund to wallet
    public String refundWallet(int userId, double amount) {
        try (Connection conn = db.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET wallet = wallet + ? WHERE id = ?");
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            return success("Refund processed");

        } catch (Exception e) {
            return error(e.getMessage());
        }
    }


    public String getAllUsers() {
        try (Connection conn = db.getConnection()) {

            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT id, username, role, profession, wallet FROM users");

            JSONArray arr = new JSONArray();
            while (rs.next()) {
                JSONObject u = new JSONObject();
                u.put("id", rs.getInt("id"));
                u.put("username", rs.getString("username"));
                u.put("role", rs.getString("role"));
                u.put("profession", rs.getString("profession"));
                u.put("wallet", rs.getDouble("wallet"));
                arr.put(u);
            }
            return arr.toString();

        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    private String success(String msg) {
        return new JSONObject().put("message", msg).toString();
    }

    private String error(String msg) {
        return new JSONObject().put("error", msg).toString();
    }
}