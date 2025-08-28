package com.wipro.studentgrade.util;

public class InvalidMarkException extends Exception {
    
    public InvalidMarkException() {
        super("Invalid Marks: Marks must be between 0 and 100");
    }
    
    public InvalidMarkException(String message) {
        super(message);
    }
    
    @Override
    public String toString() {
        return "InvalidMarkException: " + getMessage();
    }
}
