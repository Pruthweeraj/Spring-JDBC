package com.spring.jdbctraining.service;

import com.spring.jdbctraining.model.Student;
import rx.Observable;

public interface StudentService {
    Observable<Student> getAllStudents();

    Observable<Student> getStudent(int studentId);

    Observable<Void> saveStudent(Student student);

    Observable<Void> updateStudent(Student student);

    Observable<Void> deleteStudent(int studentId);

    Observable<Student> createStudentsAndGetAllStudents();
}
