package com.tournamaths.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    /**
     * This controller is used to verify that the application is running and the database connection is healthy.
     */

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health_check")
    public ResponseEntity<String> healthCheck() {
        // Try-with-resources syntax here handles closing the resources automatically
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            if (rs.next()) {
                return ResponseEntity.ok("OK - Database connection is healthy");
            } else {
                return ResponseEntity.status(500).body("Error - Database query failed");
            }
        } catch (Exception e) {
            // Log the exception details for debugging purposes
            e.printStackTrace();
            // Respond with a server error status if the database connection fails
            return ResponseEntity.status(500).body("Error - Database connection failed");
        }
    }
}
