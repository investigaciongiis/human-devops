package com.suken27.humanfactorsjava.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabaseSeeder {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void seed() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Comprobamos si ya hay datos
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM action_type");
            rs.next();
            int count = rs.getInt(1);

            if (count == 0) {
                // Ejecutamos el script SQL solo si no hay datos
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("data.sql"));
                System.out.println("Database initialized with data.sql");
            } else {
                System.out.println("Database already initialized, data.sql is not executed.");
            }

        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
