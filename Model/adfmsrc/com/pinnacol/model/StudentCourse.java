package com.pinnacol.model;

import java.math.BigDecimal;

import java.util.Date;

public class StudentCourse {
    public StudentCourse() {
        super();
    }

    public StudentCourse(BigDecimal id) {
        super();
        this.id = id;
   }

    private BigDecimal courseId;
    private BigDecimal id;
    private BigDecimal studentId;


    public void setCourseId(BigDecimal courseId) {
        this.courseId = courseId;
    }

    public BigDecimal getCourseId() {
        return courseId;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public BigDecimal getId() {
        return id;
    }

    public void setStudentId(BigDecimal studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getStudentId() {
        return studentId;
    }
}
