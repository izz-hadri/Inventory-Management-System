package com.example.origin_inventorymanagementsystem;

import java.io.Serializable;

public class Student implements Serializable {
    private String student_id = null;
    private String student_name = null;
    private String student_email = null;
    private String student_program = null;
    private String student_semester = null;
    private String student_class = null;

    public Student() {

    }
    public Student(String student_id, String student_name, String student_email, String student_program, String student_semester, String student_class) {
        this.student_id = student_id;
        this.student_name = student_name;
        this.student_email = student_email;
        this.student_program = student_program;
        this.student_semester = student_semester;
        this.student_class = student_class;

    }

    public String getStudent_id() {
        return student_id;
    }

    public String getStudent_name() {
        return student_name;
    }

    public String getStudent_email() {
        return student_email;
    }

    public String getStudent_program() {
        return student_program;
    }

    public String getStudent_semester() {
        return student_semester;
    }

    public String getStudent_class() {
        return student_class;
    }

    @Override
    public String toString() {
        return "Student{" +
                "student_id='" + student_id + '\'' +
                ", student_name='" + student_name + '\'' +
                ", student_email='" + student_email + '\'' +
                ", student_program='" + student_program + '\'' +
                '}';
    }
}
