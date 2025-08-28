package com.wipro.studentgrade.test;

import com.wipro.studentgrade.bean.StudentBean;
import com.wipro.studentgrade.service.GradeProcessor;

public class TestGradeSystem {
    
    public static void main(String[] args) {
        GradeProcessor processor = new GradeProcessor();
        
        System.out.println("=== Testing Student Grade Generation System ===\n");
        
        // Test Case 1: Marks: 95, 87, 93, 88, 91 - Expected Grade: A
        System.out.println("Test Case 1: High marks (Expected Grade: A)");
        StudentBean student1 = new StudentBean("Student 1", 95, 87, 93, 88, 91);
        String result1 = processor.generateGrade(student1);
        System.out.println(result1);
        System.out.println();
        
        // Test Case 2: Marks: 60, 45, 55, 65, 59 - Expected Grade: C
        System.out.println("Test Case 2: Medium marks (Expected Grade: C)");
        StudentBean student2 = new StudentBean("Student 2", 60, 45, 55, 65, 59);
        String result2 = processor.generateGrade(student2);
        System.out.println(result2);
        System.out.println();
        
        // Test Case 3: Invalid marks - Expected: Invalid Marks
        System.out.println("Test Case 3: Invalid marks (Expected: Invalid Marks)");
        StudentBean student3 = new StudentBean("Student 3", -5, 85, 90, 88, 92);
        String result3 = processor.generateGrade(student3);
        System.out.println(result3);
        System.out.println();
        
        // Test Case 4: Low marks - Expected Grade: F
        System.out.println("Test Case 4: Low marks (Expected Grade: F)");
        StudentBean student4 = new StudentBean("Student 4", 35, 42, 38, 30, 25);
        String result4 = processor.generateGrade(student4);
        System.out.println(result4);
        System.out.println();
        
        // Test Case 5: Borderline marks - Expected Grade: B
        System.out.println("Test Case 5: Borderline marks (Expected Grade: B)");
        StudentBean student5 = new StudentBean("Student 5", 75, 80, 78, 82, 76);
        String result5 = processor.generateGrade(student5);
        System.out.println(result5);
    }
}
