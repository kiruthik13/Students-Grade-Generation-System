package com.wipro.studentgrade.dao;

import com.wipro.studentgrade.bean.StudentBean;
import com.wipro.studentgrade.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDAO {
    private static Map<String, Integer> nameSequenceMap = new HashMap<>();
    
    /**
     * Generates student ID using name initials and sequence
     * @param name Student name
     * @return Generated student ID
     */
    public String generateId(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "STU001";
        }
        
        String[] nameParts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        
        for (String part : nameParts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        
        String baseId = initials.toString().toUpperCase();
        int sequence = nameSequenceMap.getOrDefault(baseId, 0) + 1;
        nameSequenceMap.put(baseId, sequence);
        
        return String.format("%s%03d", baseId, sequence);
    }
    
    /**
     * Inserts student record into database and returns result
     * @param bean StudentBean object containing student data
     * @return Result message
     */
    public String insertStudent(StudentBean bean) {
        String sql = """
            INSERT INTO students (student_id, name, mark1, mark2, mark3, mark4, mark5, total, average, grade)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, bean.getStudentId());
            pstmt.setString(2, bean.getName());
            pstmt.setInt(3, bean.getMark1());
            pstmt.setInt(4, bean.getMark2());
            pstmt.setInt(5, bean.getMark3());
            pstmt.setInt(6, bean.getMark4());
            pstmt.setInt(7, bean.getMark5());
            pstmt.setInt(8, bean.getTotal());
            pstmt.setInt(9, bean.getAverage());
            pstmt.setString(10, bean.getGrade());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return "Student record inserted successfully with ID: " + bean.getStudentId();
            } else {
                return "Failed to insert student record";
            }
            
        } catch (SQLException e) {
            return "Error inserting student record: " + e.getMessage();
        }
    }
    
    /**
     * Retrieves all stored student records from database
     * @return List of all student records
     */
    public List<StudentBean> getAllStudents() {
        List<StudentBean> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                StudentBean student = new StudentBean();
                student.setStudentId(rs.getString("student_id"));
                student.setName(rs.getString("name"));
                student.setMark1(rs.getInt("mark1"));
                student.setMark2(rs.getInt("mark2"));
                student.setMark3(rs.getInt("mark3"));
                student.setMark4(rs.getInt("mark4"));
                student.setMark5(rs.getInt("mark5"));
                student.setTotal(rs.getInt("total"));
                student.setAverage(rs.getInt("average"));
                student.setGrade(rs.getString("grade"));
                
                students.add(student);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving students: " + e.getMessage());
        }
        
        return students;
    }
    
    /**
     * Retrieves student by ID from database
     * @param studentId Student ID to search for
     * @return StudentBean if found, null otherwise
     */
    public StudentBean getStudentById(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    StudentBean student = new StudentBean();
                    student.setStudentId(rs.getString("student_id"));
                    student.setName(rs.getString("name"));
                    student.setMark1(rs.getInt("mark1"));
                    student.setMark2(rs.getInt("mark2"));
                    student.setMark3(rs.getInt("mark3"));
                    student.setMark4(rs.getInt("mark4"));
                    student.setMark5(rs.getInt("mark5"));
                    student.setTotal(rs.getInt("total"));
                    student.setAverage(rs.getInt("average"));
                    student.setGrade(rs.getString("grade"));
                    
                    return student;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving student: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Gets the total count of stored students from database
     * @return Number of students stored
     */
    public int getStudentCount() {
        String sql = "SELECT COUNT(*) FROM students";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting students: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Deletes a student record from database
     * @param studentId Student ID to delete
     * @return Success message or error message
     */
    public String deleteStudent(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return "Student with ID " + studentId + " deleted successfully";
            } else {
                return "Student with ID " + studentId + " not found";
            }
            
        } catch (SQLException e) {
            return "Error deleting student: " + e.getMessage();
        }
    }
    
    /**
     * Clears all stored student records from database
     * @return Success message or error message
     */
    public String clearAllStudents() {
        String sql = "DELETE FROM students";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int rowsAffected = pstmt.executeUpdate();
            nameSequenceMap.clear();
            
            return rowsAffected + " student records deleted successfully";
            
        } catch (SQLException e) {
            return "Error clearing students: " + e.getMessage();
        }
    }
}
