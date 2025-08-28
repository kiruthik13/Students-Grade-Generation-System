<<<<<<< HEAD
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

## How to Use

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

## Database Operations

### **Insert Student**
- Validates marks (0-100)
- Calculates total, average, and grade
- Generates unique student ID
- Stores in SQLite database

### **View Students**
- Retrieves all students from database
- Displays in formatted table
- Shows total count

### **Delete Operations**
- Delete individual student by ID
- Clear all students with confirmation
- Maintains data integrity

## Architecture

- **Bean Layer**: Data transfer objects
- **Service Layer**: Business logic and grade processing
- **DAO Layer**: **Real database operations** (SQLite)
- **Util Layer**: Custom exceptions and **database utilities**
- **Test Layer**: Comprehensive testing with generic student names

## File Structure

```
Student Grade Generation System/
├── src/                          # Java source code
├── sqlite-jdbc.jar              # SQLite JDBC driver (download required)
├── student_grade_system.db      # SQLite database (created automatically)
├── compile_and_run.bat          # Main compilation and execution script
├── download_sqlite.bat          # SQLite driver download script
└── README.md                    # This documentation
```

## Troubleshooting

### **Database Connection Issues**
- Ensure `sqlite-jdbc.jar` is in the project root
- Check file permissions for database creation
- Verify Java version compatibility

### **Compilation Issues**
- Use `compile_and_run.bat` for automatic setup
- Ensure all dependencies are in classpath
- Check for syntax errors in Java files

## Note

This implementation now uses **real SQLite database storage** instead of simulation:
- ✅ **Permanent data storage** in SQLite database file
- ✅ **Full CRUD operations** (Create, Read, Update, Delete)
- ✅ **Data persistence** between program sessions
- ✅ **Professional database management** with proper error handling
- ✅ **Scalable architecture** ready for production use
=======
# Student-Grade-Generation-System
>>>>>>> d74eae55daeb94138059ca27b81d231729fa19dd
