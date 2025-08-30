package com.wipro.studentgrade.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseUtil {
	private static final String DEFAULT_SQLITE_URL = "jdbc:sqlite:student_grade_system.db";
	private static final Properties PROPS = new Properties();
	private static String driverClass;
	private static String jdbcUrl;
	private static String username;
	private static String password;

	static {
		loadConfiguration();
		loadDriverClass();
	}

	private static void loadConfiguration() {
		// Defaults (SQLite embedded)
		jdbcUrl = DEFAULT_SQLITE_URL;
		username = "";
		password = "";

		try (FileInputStream fis = new FileInputStream("db.properties")) {
			PROPS.load(fis);
			driverClass = PROPS.getProperty("jdbc.driverClass");
			jdbcUrl = PROPS.getProperty("jdbc.url", jdbcUrl);
			username = PROPS.getProperty("jdbc.username", username);
			password = PROPS.getProperty("jdbc.password", password);
		} catch (IOException ignored) {
			// Use defaults if properties file not found
		}
	}

	private static void loadDriverClass() {
		try {
			if (driverClass != null && !driverClass.isEmpty()) {
				Class.forName(driverClass);
			}
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver class not found: " + driverClass + ". Ensure the driver JAR is on the classpath.");
		}
	}

	public static Connection getConnection() throws SQLException {
		if (username == null || username.isEmpty()) {
			return DriverManager.getConnection(jdbcUrl);
		}
		return DriverManager.getConnection(jdbcUrl, username, password);
	}

	public static void initializeDatabase() {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			String ddl = resolveCreateTableDDL(jdbcUrl);
			stmt.execute(ddl);
			System.out.println("Database initialized successfully!");
		} catch (SQLException e) {
			System.err.println("Error initializing database: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static String resolveCreateTableDDL(String url) {
		boolean isSqlite = url != null && url.toLowerCase().contains("sqlite");
		if (isSqlite) {
			return """
				CREATE TABLE IF NOT EXISTS students (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					student_id VARCHAR(20) UNIQUE NOT NULL,
					name VARCHAR(100) NOT NULL,
					mark1 INTEGER NOT NULL,
					mark2 INTEGER NOT NULL,
					mark3 INTEGER NOT NULL,
					mark4 INTEGER NOT NULL,
					mark5 INTEGER NOT NULL,
					total INTEGER NOT NULL,
					average INTEGER NOT NULL,
					grade VARCHAR(2) NOT NULL,
					created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
				)
				""";
		}
		// MySQL / MariaDB friendly DDL
		return """
			CREATE TABLE IF NOT EXISTS students (
				id INT AUTO_INCREMENT PRIMARY KEY,
				student_id VARCHAR(20) UNIQUE NOT NULL,
				name VARCHAR(100) NOT NULL,
				mark1 INT NOT NULL,
				mark2 INT NOT NULL,
				mark3 INT NOT NULL,
				mark4 INT NOT NULL,
				mark5 INT NOT NULL,
				total INT NOT NULL,
				average INT NOT NULL,
				grade VARCHAR(2) NOT NULL,
				created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
			) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
			""";
	}
}
