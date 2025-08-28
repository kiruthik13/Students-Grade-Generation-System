package com.wipro.studentgrade.service;

import com.wipro.studentgrade.bean.StudentBean;
import com.wipro.studentgrade.dao.StudentDAO;
import com.wipro.studentgrade.util.DatabaseUtil;
import com.wipro.studentgrade.util.InvalidMarkException;
import java.util.Scanner;
import java.util.List;

public class GradeProcessor {
    
    /**
     * Validates marks, calculates total, average, assigns grade
     * @param bean StudentBean object
     * @return Result message
     */
    public String generateGrade(StudentBean bean) {
        try {
            // Validate marks
            validateMarks(bean);
            
            // Calculate total and average
            int total = bean.getMark1() + bean.getMark2() + bean.getMark3() + bean.getMark4() + bean.getMark5();
            int average = total / 5;
            
            // Assign grade
            String grade = assignGrade(average);
            
            // Set calculated values
            bean.setTotal(total);
            bean.setAverage(average);
            bean.setGrade(grade);
            
            // Generate student ID
            StudentDAO dao = new StudentDAO();
            String studentId = dao.generateId(bean.getName());
            bean.setStudentId(studentId);
            
            // Insert into database
            String insertResult = dao.insertStudent(bean);
            
            return String.format("Student ID: %s\nName: %s\nTotal: %d\nAverage: %d\nGrade: %s\n%s", 
                               studentId, bean.getName(), total, average, grade, insertResult);
            
        } catch (InvalidMarkException e) {
            return "Error: " + e.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Validates that all marks are between 0 and 100
     * @param bean StudentBean object
     * @throws InvalidMarkException if marks are invalid
     */
    private void validateMarks(StudentBean bean) throws InvalidMarkException {
        if (bean.getMark1() < 0 || bean.getMark1() > 100 ||
            bean.getMark2() < 0 || bean.getMark2() > 100 ||
            bean.getMark3() < 0 || bean.getMark3() > 100 ||
            bean.getMark4() < 0 || bean.getMark4() > 100 ||
            bean.getMark5() < 0 || bean.getMark5() > 100) {
            throw new InvalidMarkException("Marks must be between 0 and 100");
        }
    }
    
    /**
     * Assigns grade based on average marks
     * @param average Average marks
     * @return Grade (A, B, C, D, or F)
     */
    private String assignGrade(int average) {
        if (average >= 90) {
            return "A";
        } else if (average >= 75) {
            return "B";
        } else if (average >= 60) {
            return "C";
        } else if (average >= 40) {
            return "D";
        } else {
            return "F";
        }
    }
    
    /**
     * Displays all stored student records from database
     */
    private void displayAllStudents() {
        StudentDAO dao = new StudentDAO();
        List<StudentBean> students = dao.getAllStudents();
        
        if (students.isEmpty()) {
            System.out.println("No students found in the database.");
            return;
        }
        
        System.out.println("\n=== All Stored Students ===");
        System.out.printf("%-12s %-20s %-8s %-8s %-6s\n", "Student ID", "Name", "Total", "Average", "Grade");
        System.out.println("------------------------------------------------------------");
        
        for (StudentBean student : students) {
            System.out.printf("%-12s %-20s %-8d %-8d %-6s\n", 
                            student.getStudentId(), 
                            student.getName(), 
                            student.getTotal(), 
                            student.getAverage(), 
                            student.getGrade());
        }
        System.out.println("------------------------------------------------------------");
        System.out.println("Total Students in Database: " + students.size());
    }
    
    /**
     * Adds a new student with input validation
     */
    private void addNewStudent() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n=== Add New Student ===");
        System.out.print("Enter student name: ");
        String name = scanner.nextLine();
        
        System.out.println("Enter marks for 5 subjects (0-100):");
        System.out.print("Subject 1: ");
        int mark1 = scanner.nextInt();
        System.out.print("Subject 2: ");
        int mark2 = scanner.nextInt();
        System.out.print("Subject 3: ");
        int mark3 = scanner.nextInt();
        System.out.print("Subject 4: ");
        int mark4 = scanner.nextInt();
        System.out.print("Subject 5: ");
        int mark5 = scanner.nextInt();
        
        // Create StudentBean object
        StudentBean student = new StudentBean(name, mark1, mark2, mark3, mark4, mark5);
        
        // Process grades
        String result = generateGrade(student);
        System.out.println("\n=== Result ===");
        System.out.println(result);
    }
    
    /**
     * Deletes a student by ID
     */
    private void deleteStudent() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== Delete Student ===");
        System.out.print("Enter Student ID to delete: ");
        String studentId = scanner.nextLine();
        
        StudentDAO dao = new StudentDAO();
        String result = dao.deleteStudent(studentId);
        System.out.println(result);
    }
    
    /**
     * Clears all students from database
     */
    private void clearAllStudents() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== Clear All Students ===");
        System.out.print("Are you sure you want to delete ALL students? (yes/no): ");
        String confirmation = scanner.nextLine().toLowerCase();
        
        if (confirmation.equals("yes")) {
            StudentDAO dao = new StudentDAO();
            String result = dao.clearAllStudents();
            System.out.println(result);
        } else {
            System.out.println("Operation cancelled.");
        }
    }
    
    /**
     * Main method to test the Student Grade Generation System
     */
    public static void main(String[] args) {
        // Initialize database on startup
        System.out.println("Initializing Student Grade Generation System...");
        DatabaseUtil.initializeDatabase();
        
        GradeProcessor processor = new GradeProcessor();
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n=== Student Grade Generation System ===");
            System.out.println("1. Add New Student");
            System.out.println("2. View All Students");
            System.out.println("3. Delete Student");
            System.out.println("4. Clear All Students");
            System.out.println("5. Exit");
            System.out.print("Enter your choice (1-5): ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (choice) {
                case 1:
                    processor.addNewStudent();
                    break;
                case 2:
                    processor.displayAllStudents();
                    break;
                case 3:
                    processor.deleteStudent();
                    break;
                case 4:
                    processor.clearAllStudents();
                    break;
                case 5:
                    System.out.println("Thank you for using the system!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice! Please enter 1-5.");
            }
        }
    }
}
