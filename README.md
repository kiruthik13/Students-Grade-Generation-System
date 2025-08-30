# Student Grade Generation System

A Java-based system that calculates student grades based on 5 subject marks with validation, grade assignment, and **SQLite database storage**.

## System Overview

The system validates marks (0-100), computes total and average, assigns grades (A-F), and stores results permanently in a SQLite database with generated student IDs.

## Package Structure

```
com.wipro.studentgrade/
├── bean/
│   └── StudentBean.java          # Bean class for student data
├── util/
│   ├── InvalidMarkException.java # Custom exception for invalid marks
│   └── DatabaseUtil.java         # Database connection and initialization
├── dao/
│   └── StudentDAO.java           # Data access object for database operations
├── service/
│   └── GradeProcessor.java       # Main service class for grade processing
└── test/
    └── TestGradeSystem.java      # Test cases demonstration
```

## New: Web UI (Attractive & Professional)

A modern, responsive web page is included for quickly calculating totals, averages, and grades without running Java.

- Location: `web/index.html`
- Assets: `web/styles.css`, `web/app.js`
- Features:
  - **Responsive** (mobile and desktop)
  - **Validation** for marks (0–100)
  - **Live result** with total, average, and grade (A–F)
  - **Professional design** using Inter font, soft gradients, and cards

### How to Use the Web UI

1. Open `web/index.html` in any modern browser (Chrome, Edge, Firefox).
2. Enter student name and five marks.
3. Click “Generate Grade”.
4. See total, average, and grade displayed in a styled result card.

> Note: The web page runs locally in the browser and does not connect to the SQLite DB. Use the Java CLI for DB storage.

## Web UI connected to Database

You can store and list students from the browser by running the lightweight HTTP API server.

### Start the API server
```powershell
# From project root
cd src
# With SQLite driver on classpath
java -cp ".;..\sqlite-jdbc.jar" com.wipro.studentgrade.service.HttpApiServer
# Server runs at http://localhost:8081
```

### Use from the browser
- Open `web/index.html`
- Fill the form and click Generate Grade
- The page will POST to `http://localhost:8081/api/students` and then GET the list to render the table
- Click “Refresh List” to reload

Note: CORS is enabled for convenience. Keep the server running while using the page.

## Grade Assignment

- **A**: Average ≥ 90
- **B**: Average ≥ 75
- **C**: Average ≥ 60
- **D**: Average ≥ 40
- **F**: Average < 40

## Features

- ✅ Mark validation (0-100 range)
- ✅ Total and average calculation
- ✅ Grade assignment based on average
- ✅ Student ID generation using name initials
- ✅ **SQLite database storage** (data persists permanently)
- ✅ **Database management** (add, view, delete, clear students)
- ✅ Custom exception handling
- ✅ Comprehensive test cases with generic student names
- ✅ Sleek, responsive web UI for quick calculations

## Database Features

### **SQLite Database**
- **File**: `student_grade_system.db` (created automatically)
- **Table**: `students` with all student details
- **Persistence**: Data survives program restarts
- **Operations**: INSERT, SELECT, DELETE, CLEAR

### **Database Schema**
```sql
CREATE TABLE students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    mark1, mark2, mark3, mark4, mark5 INTEGER NOT NULL,
    total, average INTEGER NOT NULL,
    grade VARCHAR(2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Test Cases

1. **High marks (95, 87, 93, 88, 91)** → Expected Grade: A
2. **Medium marks (60, 45, 55, 65, 59)** → Expected Grade: C
3. **Invalid marks (-5, any other)** → Expected: Invalid Marks
4. **Low marks (35, 42, 38, 30, 25)** → Expected Grade: F
5. **Borderline marks (75, 80, 78, 82, 76)** → Expected Grade: B

## Setup and Installation

### **Step 1: Download SQLite JDBC Driver**
```bash
# Run the download script
download_sqlite.bat

# Or download manually from:
# https://github.com/xerial/sqlite-jdbc/releases
```

### **Step 2: Compile and Run**
```bash
# Use the automated batch file
compile_and_run.bat

# Or compile manually with database support:
cd src
javac -cp ".;../sqlite-jdbc.jar" com/wipro/studentgrade/*/*.java

# Run with database support:
java -cp ".;../sqlite-jdbc.jar" com.wipro.studentgrade.service.GradeProcessor
```

## How to Use (CLI)

### **Interactive Mode with Database**
1. **Add New Student**: Enter student name and 5 subject marks
2. **View All Students**: See all stored students from database
3. **Delete Student**: Remove individual student by ID
4. **Clear All Students**: Remove all students (with confirmation)
5. **Data Persistence**: All data stored permanently in SQLite

### **Menu Options**
```
=== Student Grade Generation System ===
1. Add New Student
2. View All Students
3. Delete Student
4. Clear All Students
5. Exit
```

## System Requirements

- **Java 8 or higher**
- **SQLite JDBC Driver** (sqlite-jdbc.jar)
- **No external database server required** (SQLite is embedded)

## Architecture

- **Bean Layer**: Data transfer objects
- **Service Layer**: Business logic and grade processing
- **DAO Layer**: **Real database operations** (SQLite)
- **Util Layer**: Custom exceptions and **database utilities**
- **Test Layer**: Comprehensive testing with generic student names
- **Web UI**: Static HTML/CSS/JS for quick offline calculations

## File Structure

```
Student Grade Generation System/
├── src/
├── web/
│   ├── index.html
│   ├── styles.css
│   └── app.js
├── sqlite-jdbc.jar
├── student_grade_system.db
├── compile_and_run.bat
├── download_sqlite.bat
└── README.md
```

## Troubleshooting

- If DB features don’t work, ensure `sqlite-jdbc.jar` exists in the project root
- If styles don’t load, open `web/index.html` via local file path (no server required)

## Note

The Java CLI persists results in SQLite; the web UI is a lightweight companion for quick grade calculations with a polished UI.

## JDBC Configuration

The project now uses standard JDBC settings from `db.properties`.

- File: `db.properties`
- Keys:
  - `jdbc.driverClass` (optional for SQLite)
  - `jdbc.url`
  - `jdbc.username` (optional)
  - `jdbc.password` (optional)

### SQLite (default)
```
jdbc.url=jdbc:sqlite:student_grade_system.db
# jdbc.driverClass=org.sqlite.JDBC
```
Driver JAR: place `sqlite-jdbc.jar` in project root (use `download_sqlite.bat`).

### MySQL example
```
jdbc.driverClass=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/student_db?useSSL=false&serverTimezone=UTC
jdbc.username=root
jdbc.password=your_password_here
```
Driver JAR: download `mysql-connector-j-<version>.jar` and place in project root.

The launcher script `compile_and_run.bat` detects `sqlite-jdbc.jar` or `mysql-connector-j*.jar` and sets classpath accordingly.
